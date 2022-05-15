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

public class ConditionExpressionMustBeBool extends PreorderJmmVisitor<Integer, Integer> implements ReportsProvider {

    private final SymbolTable symbolTable;
    private final List<Report>  reports;

    public ConditionExpressionMustBeBool(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.STATEMENT, this::statementVisit);
        visit(rootNode);
    }
    public Integer statementVisit(JmmNode node, Integer dummy) {

        if(node.getChildren().size()==0)
            return 0;

        JmmNode firstChild = node.getJmmChild(0);

        if(firstChild.getKind().equals(AstNode.WHILE_STATEMENT.toString())){

            try {
                if(!(firstChild.getJmmChild(0).get("value").equals("&&") || firstChild.getJmmChild(0).get("value").equals("<") )){

                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                            "Statement without condition"));
                }
            }catch (Exception e){
                if(firstChild.getJmmChild(0).getKind().equals(AstNode.ID.toString())){
                    System.out.println("----> ID "+getIdType(firstChild.getJmmChild(0))  );
                    //if(!getIdType(firstChild.getJmmChild(0)))
                    //TODO check if is array or ID(true/false)
                    System.out.println("bingo");
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                            "Statement without condition"));
                }
            }



        }else if(firstChild.getJmmChild(0).getKind().equals(AstNode.IF_STATEMENT.toString())){
            JmmNode binOp = firstChild.getJmmChild(0).getJmmChild(0);

            if(!(binOp.get("value").equals("&&") || binOp.get("value").equals("<") )){
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                        "Statement without condition"));
            }


        }

        return 0;
    }


    @Override
    public List<Report> getReports() {
        return reports;
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
}
