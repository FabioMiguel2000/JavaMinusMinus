package pt.up.fe.comp.Analysis.Analysers;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.AST.AstUtils;
import pt.up.fe.comp.Analysis.SemanticAnalyser;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArrayAccessIsDoneOverArray extends PreorderJmmVisitor<Integer, Integer> implements SemanticAnalyser {

    private final SymbolTable symbolTable;
    private final List<Report>  reports;

    public ArrayAccessIsDoneOverArray(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.ARRAY_ACCESS_EXPRESSION, this::arrayAccessVisit);
        visit(rootNode);
    }
    public Integer arrayAccessVisit(JmmNode node, Integer dummy) {

        String arrName = node.getJmmChild(0).get("name");

        // TODO Chamar metodo já criado de backtrack de AST para os metodos
        var tempMethod = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION).get("name");
        var methods = symbolTable.getMethods();
        JmmNode methodNode;

        for (var localVariable :symbolTable.getLocalVariables(tempMethod)) {
            if(localVariable.getName().equals(arrName))
                if(!localVariable.getType().isArray()){
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                            "Var access must be done over array"));
                }else{
                    return 0;
                }

        }
        for(var localParam : symbolTable.getParameters(tempMethod)){
            if(localParam.getName().equals(arrName))
                if(!localParam.getType().isArray()){
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                            "Var access must be done over array"));
                }else{
                    return 0;
                }
        }


        var fields = symbolTable.getFields();

        for (var f:fields )
            if(arrName.equals(f.getName())){
                if(!f.getType().isArray()){
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                            "Var access must be done over array"));
                }else{
                    return 0;
                }
            }


        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                "Var access must be done over array"));
        return 0;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
