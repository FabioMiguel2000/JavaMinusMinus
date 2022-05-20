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

public class MethodCallEqualsMethodDeclaration extends PreorderJmmVisitor<Integer, Integer> implements ReportsProvider {

    private final SymbolTable symbolTable;
    private final List<Report> reports;

    public MethodCallEqualsMethodDeclaration(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.CALL_EXPRESSION, this::callExpressionVisit);
        visit(rootNode);
    }

    Integer callExpressionVisit(JmmNode node, Integer dummy) {
        boolean exists = exists(node);
        if (!exists) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                    "Method doesn't exist."));
        }

        return 0;
    }

    private boolean exists(JmmNode node) {
        var base = node.getJmmChild(0);
        var method = node.getJmmChild(1);
        var type = getIdType(base);

        // verificar class / extend / imports
        if (type.equals("null")) {
            if (base.get("name").equals(symbolTable.getClassName())) {
                if (symbolTable.getSuper() == null) {
                    return symbolTable.getMethods().contains(method.get("name"));
                } else {
                    return true;
                }
            }
            if (base.get("name").equals(symbolTable.getSuper())) {
                return true;
            }
            if (symbolTable.getImports().contains(base.get("name"))) return true;

        }
        if (type.equals(symbolTable.getClassName())) {
            if (symbolTable.getSuper() == null) {
                return symbolTable.getMethods().contains(method.get("name"));
            } else {
                return true;
            }
        }
        if (type.equals(symbolTable.getSuper())) {
            return true;
        }
        if (symbolTable.getImports().contains(type)) return true;

        return false;
    }

    public String getIdType(JmmNode node){
        var father = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION);
        //localVars
        for (var localVariable :symbolTable.getLocalVariables( father.get("name") )) {
            if(node.get("name").equals(localVariable.getName()))
                return localVariable.getType().getName();
        }
        //params
        for (var param :symbolTable.getParameters( father.get("name") )) {
            if(node.get("name").equals(param.getName()))
                return param.getType().getName();
        }
        //fields
        for (var field :symbolTable.getFields() ) {
            if(node.get("name").equals(field.getName()))
                return field.getType().getName();
        }
        return "null";
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
