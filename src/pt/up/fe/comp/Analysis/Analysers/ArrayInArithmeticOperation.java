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

public class ArrayInArithmeticOperation extends PreorderJmmVisitor<Integer, Integer> implements ReportsProvider {

    private final SymbolTable symbolTable;
    private final List<Report>  reports;

    public ArrayInArithmeticOperation(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.BIN_OP, this::binOpVisit);
        visit(rootNode);
    }
    public Integer binOpVisit(JmmNode node, Integer dummy) {

        List<JmmNode> children = new ArrayList<>();
        children.add(node.getJmmChild(0));
        children.add(node.getJmmChild(1));

        var tempMethod = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION).get("name");
        for (JmmNode childNode:children ) {


            if(!childNode.getKind().equals(AstNode.ID.toString()))
                continue;

            String childName = childNode.get("name");

            for (var localVariable :symbolTable.getLocalVariables(tempMethod)) {

                if(localVariable.getName().equals(childName))
                    if(localVariable.getType().isArray()){
                        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                                "Cannot use Arithmetic Operations without Array Index"));
                    }else{
                        break;
                    }

            }
            for(var localParam : symbolTable.getParameters(tempMethod)){
                if(localParam.getName().equals(childName))
                    if(localParam.getType().isArray()){
                        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                                "Cannot use Arithmetic Operations without Array Index"));
                    }else{
                        break;
                    }
            }


            var fields = symbolTable.getFields();

            for (var f:fields )
                if(childName.equals(f.getName())){
                    if(f.getType().isArray()){
                        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                                "Cannot use Arithmetic Operations without Array Index"));
                    }else{
                        break;
                    }
                }


            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(node.get("line")), Integer.valueOf(node.get("col")),
                    "Cannot use Arithmetic Operations without Array Index"));



        }
        return 0;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
