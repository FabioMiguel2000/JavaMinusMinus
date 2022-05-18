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
    /**
     * This class checks for *array_var* + 10
     * Since array_var is type int[] it should throw an error because array is not int.
     * This class stops at every BinOp and checks if each child is like the example.
     */

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

            // just checks for variables, since well-defined arrays have ArrayAccessExpression first
            if(!childNode.getKind().equals(AstNode.ID.toString()))
                continue;

            if (isNodeIdArray(childNode, tempMethod, symbolTable))
                this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                    "Cannot use Arithmetic Operations without Array Index"));
        }
        return 0;
    }

    boolean isNodeIdArray(JmmNode childNode, String tempMethod, SymbolTable symbolTable) {
        /**
         * @Param JmmNode childNode  -  this node to search;
         * @Param String tempMethod  -  parent method name;
         * @Param SymbolTable symbolTable  -  classic;
         */

        String childName = childNode.get("name");

        for (var localVariable :symbolTable.getLocalVariables(tempMethod)) {

            if(localVariable.getName().equals(childName))
                return localVariable.getType().isArray();

        }
        for(var localParam : symbolTable.getParameters(tempMethod)){
            if(localParam.getName().equals(childName))
                return localParam.getType().isArray();
        }


        var fields = symbolTable.getFields();

        for (var f:fields )
            if(childName.equals(f.getName())){
                return f.getType().isArray();
            }

        // default throws error since it didn't found the var name.
        return true;
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
