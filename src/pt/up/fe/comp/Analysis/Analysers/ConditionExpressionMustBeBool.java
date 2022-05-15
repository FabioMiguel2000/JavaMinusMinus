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

public class ConditionExpressionMustBeBool  extends PreorderJmmVisitor<Integer, Integer> implements ReportsProvider {

    private final SymbolTable symbolTable;
    private final List<Report> reports;
    private final JmmNode rootNode;

    public ConditionExpressionMustBeBool(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.rootNode = rootNode;
        this.reports = new ArrayList<>();
        addVisit(AstNode.WHILE_STATEMENT, this::boolCheck);
        addVisit(AstNode.IF_STATEMENT, this::boolCheck);
        visit(rootNode);
    }

    public Integer boolCheck(JmmNode parentNode, Integer dummy) {
        JmmNode node = parentNode.getJmmChild(0);
        var res = _boolCheck(node);
        System.out.println(res);

        var expectedType = "boolean"; //(node.get("value").equals("<") || node.get("value").equals("&&")) ? "boolean" : "int";
        if (!expectedType.equals(res))
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                    "Statement without condition"));

        return 0;
    }

    private String _boolCheck(JmmNode node) {
        var myKind = node.getKind();

        if (myKind.equals(AstNode.BIN_OP.toString())) {
            boolean isAnd = node.get("value").equals("&&");
            var left = _boolCheck(node.getJmmChild(0));
            var right= _boolCheck(node.getJmmChild(1));
            if (isAnd && !(left.equals("boolean")) && right.equals("boolean")) { return "null"; }
            if (!(left.equals("int")) && right.equals("int")) { return "null"; }

            if (node.get("value").equals("&&") || node.get("value").equals("<")) {
                return "boolean";
            }
            return "int";
        }

        if (myKind.equals(AstNode.LITERAL.toString())) {
            return node.get("type");
        }
        if (myKind.equals(AstNode.ID.toString())) {
            return getVarType(node);
        }
        if (myKind.equals(AstNode.METHOD_DECLARATION.toString())) {
            return "null"; // TODO: implement method
        }

        return "null";
    }

    private String getVarType(JmmNode node) {
        var idName = node.get("name");
        var parentMethodName = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION).get("name");
        for (var localVar: symbolTable.getLocalVariables(parentMethodName)) {
            // search in localVariables
            if (localVar.getName().equals(idName))
                return localVar.getType().getName();
        }

        for (var param: symbolTable.getParameters(parentMethodName)) {
            // search in parameters
            if (param.getName().equals(idName))
                return param.getType().getName();
        }

        for (var field: symbolTable.getFields()) {
            // search in parameters
            if (field.getName().equals(idName))
                return field.getType().getName();
        }

        return "null";
    }

    public List<Report> getReports() {
        return reports;
    }
}

//class RecSearch extends AJmmVisitor<Integer, Integer> {