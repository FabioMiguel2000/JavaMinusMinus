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

public class VarIsNotDeclared extends PreorderJmmVisitor<Integer, Integer> implements SemanticAnalyser {

    private final SymbolTable symbolTable;
    private final List<Report>  reports;

    public VarIsNotDeclared(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.ID, this::idVisit);
        visit(rootNode);
    }
    public Integer idVisit(JmmNode node, Integer dummy) {

        var father = node.getJmmParent();

        if(father.getKind().equals(AstNode.IMPORT_DECLARATION.toString())
            || father.getKind().equals(AstNode.VAR_DECLARATION.toString())
            || father.getKind().equals(AstNode.PARAMETER.toString())  ){
            return 0;
        }
        if(father.getKind().equals(AstNode.CALL_EXPRESSION.toString())){
            var firstChild =father.getJmmChild(0);

            if(firstChild.getKind().equals(AstNode.LITERAL.toString())){
                if(symbolTable.getMethods().contains(father.getJmmChild(1).get("name")))
                    return 0;
                else{
                    this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                            "Var is not declared"));
                    return 0;
                }

            }

            System.out.println(firstChild);
            if(search(firstChild.get("name"), node)){
                return 0;
            }

        }else{
            if(search(node.get("name"), node)){
                return 0;
            }
        }
        if(symbolTable.getClassName().equals(node.get("name")))
            return 0;

        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                "Var is not declared"));

        return 0;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }

    public boolean search( String childName , JmmNode node){
        var tempMethod = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION).get("name");

        for (var localVar: symbolTable.getLocalVariables(tempMethod)) {
            if(localVar.getName().equals(childName))
                return true;
        }

        for (var param: symbolTable.getParameters(tempMethod)) {
            if(param.getName().equals(childName))
                return true;
        }

        for (var field : symbolTable.getFields()){
            if(field.getName().equals(childName))
                return true;
        }

        for (var imports : symbolTable.getImports()){
            if(childName.equals(imports))
                return true;
        }
        return false;
    }
}
