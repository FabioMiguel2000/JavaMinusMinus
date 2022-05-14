package pt.up.fe.comp.Analysis.Analysers;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.AST.AstUtils;
import pt.up.fe.comp.Analysis.SemanticAnalyser;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class OperationType extends PreorderJmmVisitor<Integer, Integer> implements SemanticAnalyser {

    private final SymbolTable symbolTable;
    private final List<Report>  reports;

    public OperationType(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.BIN_OP, this::operationVisit);
        visit(rootNode);
    }
    public Integer operationVisit(JmmNode node, Integer dummy) {

        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");


        try {
            String nodeValue = node.get("value");

            if(nodeValue.equals("add") || nodeValue.equals("sub")
            || nodeValue.equals("*") || nodeValue.equals("/") || nodeValue.equals("<") ){

                List<JmmNode> children = node.getChildren();

                for (var c:children) {
                    try {
                        System.out.println("-->" + c.getKind());
                        if(!(c.getKind().equals(AstNode.LITERAL.toString()) && c.get("type").equals("int") )){
                            if( !(c.get("value").equals("add") || c.get("value").equals("sub")
                                    || c.get("value").equals("*") || c.get("value").equals("/") || nodeValue.equals("<") ) ){
                                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                                        "Operation with wrong types"));
                            }
                        }


                    } catch (Exception e) {
                        if(!getIdType(c).equals("int"))
                            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                                    "Operation with wrong types"));
                    }
                }

            }
            else if(nodeValue.equals("&&") ){
                List<JmmNode> children = node.getChildren();

                for (var c: children ) {
                    if(!(c.getKind().equals(AstNode.LITERAL.toString()) && c.get("type").equals("boolean") )){
                        if( !(c.get("value").equals("&") ))
                            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                                "Operation with wrong types"));

                    }
                }
            }


        }catch (Exception e){

            //TODO se for função ou assim :(
                System.out.println(node.get("op"));

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

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
