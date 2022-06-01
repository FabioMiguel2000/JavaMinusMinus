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

public class ArrayAccessIsDoneOverArray extends PreorderJmmVisitor<Integer, Integer> implements ReportsProvider{
    /**
     * This class just checks for : array_var[ don't matter ], and array_var must be of type array.
     * It stops at "ARRAY_ACCESS_EXPRESSION" then checks at symbolTable for array_var;
     * first it gets the method name... with it, it checks if array_var is inside localVariables of method name + if
     * is array type for every step.
     * second checks if in parameters of method.
     * third checks fields.
     * last if it didn't find then is not declared.
     */

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

        var tempMethod = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION).get("name");

        // for each local variable
        for (var localVariable :symbolTable.getLocalVariables(tempMethod)) {
            if(localVariable.getName().equals(arrName))
                if(!localVariable.getType().isArray()){
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                            Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                            "Var access must be done over array"));
                }else{
                    return 0;
                }

        }

        // for each method parameter
        for(var localParam : symbolTable.getParameters(tempMethod)){
            if(localParam.getName().equals(arrName))
                if(!localParam.getType().isArray()){
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                            Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                            "Var access must be done over array"));
                }else{
                    return 0;
                }
        }


        var fields = symbolTable.getFields();

        // for each class field
        for (var f:fields )
            if(arrName.equals(f.getName())){
                if(!f.getType().isArray()){
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                            Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                            "Var access must be done over array"));
                }else{
                    return 0;
                }
            }


        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                "Var access must be done over array"));
        return 0;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
