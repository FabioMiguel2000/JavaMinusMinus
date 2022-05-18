package pt.up.fe.comp.Analysis.Analysers;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.AST.AstUtils;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.ReportsProvider;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class OperationType extends PreorderJmmVisitor<Integer, Integer> implements ReportsProvider {

    private final SymbolTable symbolTable;
    private final List<Report>  reports;

    public OperationType(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.BIN_OP, this::operationVisit);
        visit(rootNode);
    }
    public Integer operationVisit(JmmNode node, Integer dummy) {
        String nodeValue = node.get("value");

        //System.out.println(nodeValue);

        String res = _typeCheck(node);
        //System.out.println(res);

        if(res.equals("null")){
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                    "Operation with wrong types"));

        }else{

            //pelo que parece não é preciso fazer mais nada :)
            /*if(res.equals("int")){
                if(!(nodeValue.equals("add") || nodeValue.equals("sub")
                        || nodeValue.equals("*") || nodeValue.equals("/") || nodeValue.equals("<") ))
                        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                                "Operation with wrong types"));

            }*/
        }

        return 0;
    }

    public String getIdType(JmmNode node){
        var father = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION);
        //localvars
        for (var localVariable :symbolTable.getLocalVariables( father.get("name") )) {
            if(node.get("name").equals(localVariable.getName()))
                return localVariable.getType().toString();
        }
        //params
        for (var param :symbolTable.getParameters( father.get("name") )) {
            if(node.get("name").equals(param.getName()))
                return param.getType().toString();
        }
        //fields
        for (var field :symbolTable.getFields() ) {
            if(node.get("name").equals(field.getName()))
                return field.getType().toString();
        }
        return "";
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
            return getIdType(node);
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
