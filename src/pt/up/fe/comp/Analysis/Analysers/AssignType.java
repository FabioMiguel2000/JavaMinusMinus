package pt.up.fe.comp.Analysis.Analysers;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.AST.AstUtils;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.ReportsProvider;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class AssignType extends PreorderJmmVisitor<Integer, Integer> implements ReportsProvider {

    private final SymbolTable symbolTable;
    private final List<Report>  reports;

    public AssignType(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.ASSIGNMENT, this::assignVisit);
        visit(rootNode);
    }
    public Integer assignVisit(JmmNode node, Integer dummy) {

        JmmNode leftChild = node.getJmmChild(0);
        JmmNode rightChild = node.getJmmChild(1);


        String leftIdType = getIdType(leftChild).getName();
        //System.out.println("LEFT = " + leftIdType);

        String rightIdType = _typeCheck(rightChild);
        //System.out.println("RIGHT = " + rightIdType);

        //TODO: checkar se rightChild = 'new qqcoisa()'

        if(!leftIdType.equals(rightIdType))
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                    "Assignment with wrong types"));

        return 0;
    }

    public Type getIdType(JmmNode node){
        var father = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION);
        //localvars
        for (var localVariable :symbolTable.getLocalVariables( father.get("name") )) {
            if(node.get("name").equals(localVariable.getName()))
                return localVariable.getType();
        }
        //params
        for (var param :symbolTable.getParameters( father.get("name") )) {
            if(node.get("name").equals(param.getName()))
                return param.getType();
        }
        //fields
        for (var field :symbolTable.getFields() ) {
            if(node.get("name").equals(field.getName()))
                return field.getType();
        }
        return null;
    }

    private String _typeCheck(JmmNode node) {
        var myKind = node.getKind();

        if (myKind.equals(AstNode.BIN_OP.toString())) {
            boolean isAnd = node.get("value").equals("&&");
            var left = _typeCheck(node.getJmmChild(0));
            var right= _typeCheck(node.getJmmChild(1));
            if (isAnd && !(left.equals("boolean")) && right.equals("boolean")) { return "null"; }
            if (!(left.equals("int")) && right.equals("int")) { return "null"; }

            if (node.get("value").equals("&&") || node.get("value").equals("<")) {
                return "boolean";
            }
            if( left.equals("int") && right.equals("int") )
                return "int";
        }

        if (myKind.equals(AstNode.LITERAL.toString())) {
            return node.get("type");
        }
        if (myKind.equals(AstNode.ID.toString())) {
            return getIdType(node).getName();
        }
        if (myKind.equals(AstNode.METHOD_DECLARATION.toString())) {
            return "null"; // TODO: implement method
        }

        return "null";
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
