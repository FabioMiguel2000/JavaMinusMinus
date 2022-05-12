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

public class ArrayIndexIsTypeInteger extends PreorderJmmVisitor<Integer, Integer> implements SemanticAnalyser {

    private final SymbolTable symbolTable;
    private final List<Report>  reports;



    public ArrayIndexIsTypeInteger(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.ARRAY_ACCESS_EXPRESSION, this::arrayAccessVisit);
        visit(rootNode);
    }
    public Integer arrayAccessVisit(JmmNode node, Integer dummy) {

        if(!(new SearchChild(symbolTable, node).getIsValid()))
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                    "Array index is not Integer"));

        return 0;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}

class SearchChild extends PreorderJmmVisitor<Integer, Integer>{
    private final SymbolTable symbolTable;
    private boolean isValid = true;
    private final JmmNode rootNode;


    public SearchChild(SymbolTable s, JmmNode rootNode){
        this.symbolTable = s;
        this.rootNode = rootNode;
        addVisit(AstNode.BIN_OP, this::visitBinOp);
        addVisit(AstNode.LITERAL, this::visitLiteral);
        addVisit(AstNode.ID, this::visitId);

        visit(rootNode);
    }

    private Integer visitBinOp(JmmNode node, Integer dummy) {
        if(!isValid)
            return 0;

        try {
            node.get("op");
        }catch (Exception e){
            isValid = false;
        }
        return 0;

    }
    private Integer visitLiteral(JmmNode node, Integer dummy) {
        if(!isValid)
            return 0;
        if(!node.get("type").equals("int"))
            isValid = false;

        return 0;
    }
    private Integer visitId(JmmNode node, Integer dummy) {
        if(!isValid)
            return 0;


        if(node.get("name").equals(rootNode.getJmmChild(0).get("name")) )
            return 0;

        var tempMethod = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION).get("name");

        for (var localVariable :symbolTable.getLocalVariables(tempMethod)) {
            if(localVariable.getName().equals(node.get("name")))
                if(!localVariable.getType().isArray()){
                    isValid = false;
                }else{
                    return 0;
                }

        }
        for(var localParam : symbolTable.getParameters(tempMethod)){
            if(localParam.getName().equals(node.get("name")))
                if(!localParam.getType().isArray()){
                    isValid = false;
                }else{
                    return 0;
                }
        }


        var fields = symbolTable.getFields();

        for (var f:fields )
            if(node.get("name").equals(f.getName())){
                if(!f.getType().isArray()){
                    isValid = false;
                }else{
                    return 0;
                }
            }

        return 0;
    }

    public boolean getIsValid(){
        return isValid;
    }
}