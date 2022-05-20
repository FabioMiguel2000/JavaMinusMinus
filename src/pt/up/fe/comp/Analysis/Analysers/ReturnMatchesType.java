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

public class ReturnMatchesType extends PreorderJmmVisitor<Integer, Integer> implements ReportsProvider{

    private final SymbolTable symbolTable;
    private final List<Report> reports;

    public ReturnMatchesType(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.RETURN_DECLARATION, this::returnVisit);
        visit(rootNode);
    }

    Integer returnVisit(JmmNode node, Integer dummy) {
        var returnType = typeCheck(node.getJmmChild(0));
        var methodType = getMethodType(node.getJmmParent().get("name"));
        if (!typesMatch(methodType, returnType)) {
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                    "Return type doesn't match with method."));
        }

        return 0;
    }

    private boolean typesMatch(String methodType, String returnType) {
        // fazer cuidado com os import types
        // ver se o tipo retornado e o de um import / extends
        // ver tb se e do tipo da class
        if (returnType.equals("import")) return true;
        return methodType.equals(returnType);
    }

    private String getMethodType(String method) {
        var _ret = symbolTable.getReturnType(method);
        var ret = _ret.getName();
        return ret;
    }

   private String typeCheck(JmmNode node) {
       // TODO : rever os int que sao array
       var myKind = node.getKind();

       if (myKind.equals(AstNode.BIN_OP.toString())) {
           if (node.get("value").equals("<") || node.get("value").equals("&&")) {
               return "boolean";
           }
           return "int";
       }

       if (myKind.equals(AstNode.LITERAL.toString())) {
           return node.get("type");
       }
       if (myKind.equals(AstNode.ID.toString())) {
           return getIdType(node);
       }
       if (myKind.equals(AstNode.CALL_EXPRESSION.toString())) {
           var base = node.getJmmChild(0);
           var base2 = getIdType(base);
           if (base2.equals("null")) {
               var base3 = base.get("name");
               // procurar na minha class
               if (base3.equals(symbolTable.getClassName())) {
                   return symbolTable.getReturnType(node.getJmmChild(1).get("name")).getName();
               }
               // fora da class
               return "import";
           }
           // caso o call exp seja uma var , ver se da match com o class name
           if (base2.equals(symbolTable.getClassName())) {
               return symbolTable.getReturnType(node.getJmmChild(1).get("name")).getName();
           }

           return "import";
       }

       return "null";
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
