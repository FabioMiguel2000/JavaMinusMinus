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

public class ArrayIndexIsTypeInteger extends PreorderJmmVisitor<Integer, Integer> implements ReportsProvider{
    /**
     * This class verify that array_var [ int ] -- checks if index is int.
     * Has a preorder to lookup for ARRAY_ACCESS_EXPRESSION, then it calls SearchChild on that node
     * It will verify that all top level is int and below.
     */


    // TODO: myAnalysis/ArrayIndexIsInteger.jmm must pass

    private final SymbolTable symbolTable;
    private final List<Report>  reports;



    public ArrayIndexIsTypeInteger(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.ARRAY_ACCESS_EXPRESSION, this::arrayAccessVisit);
        visit(rootNode);
    }
    public Integer arrayAccessVisit(JmmNode node, Integer dummy) {
        //node = node.getJmmChild(1);

        if(!(new SearchChild(symbolTable, node.getJmmChild(1)).getIsValid()))
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                    "Array index is not Integer"));

        return 0;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}

class SearchChild extends PreorderJmmVisitor<Integer, Integer>{
    /**
     * This needs to change from preorder to manual recursion.
     * All because you can have boolean or other types since it's inside a function.
     * Preorder will search inside function too... which is not ideal. TODO
     */
    private final SymbolTable symbolTable;
    private boolean isValid = true;
    private final JmmNode rootNode;


    public SearchChild(SymbolTable s, JmmNode rootNode){
        this.symbolTable = s;
        this.rootNode = rootNode;

        addVisit(AstNode.BIN_OP, this::visitBinOp);
        addVisit(AstNode.LITERAL, this::visitLiteral);
        addVisit(AstNode.ID, this::visitId);
        // TODO : function type
        //visit para fn call

        visit(rootNode);
    }

    private Integer visitBinOp(JmmNode node, Integer dummy) {
        // do nothing if is inside fn call
        //warning, fn calls , they accept it...
        if(!isValid)
            return 0;
        var type = node.get("value");

        if (type.equals("<") || type.equals("&&"))
            isValid = false;
        return 0;
    }

    private Integer visitLiteral(JmmNode node, Integer dummy) {
        // do nothing if is inside fn call
        //warning, fn calls , they accept it...
        if(!isValid)
            return 0;
        if(!node.get("type").equals("int"))
            isValid = false;

        return 0;
    }
    private Integer visitId(JmmNode node, Integer dummy) {
        // do nothing if is inside fn call
        //warning, fn calls , they accept it...
        if(!isValid)
            return 0;

        JmmNode parent = node.getJmmParent();
        if(node.get("name").equals(parent.getJmmChild(0).get("name")) )
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