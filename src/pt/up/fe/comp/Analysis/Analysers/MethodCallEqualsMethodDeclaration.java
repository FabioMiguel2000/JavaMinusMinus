package pt.up.fe.comp.Analysis.Analysers;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportsProvider;

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
        // ver se


        return 0;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
