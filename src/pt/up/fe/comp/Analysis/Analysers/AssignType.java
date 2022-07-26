package pt.up.fe.comp.Analysis.Analysers;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.AST.AstUtils;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.ReportsProvider;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class AssignType extends PreorderJmmVisitor<Integer, Integer> implements ReportsProvider {

    private final SymbolTable symbolTable;
    private final List<Report>  reports;

    public AssignType(SymbolTable symbolTable, JmmNode rootNode) {
        this.symbolTable = symbolTable;
        this.reports = new ArrayList<>();
        addVisit(AstNode.ASSIGNMENT, this::assignVisit);
        visit(rootNode);
    }
    public Integer assignVisit(JmmNode node, Integer dummy) {
        JmmNode leftChild = node.getJmmChild(0);
        JmmNode rightChild = node.getJmmChild(1);

        //try{
        //        //    leftIdType = getIdType(leftChild).getName();
        //        //}catch(Exception e){
        //        //    if (leftChild.getKind().equals(AstNode.ARRAY_ACCESS_EXPRESSION.toString())){
        //        //        leftIdType = getIdType(leftChild.getJmmChild(0)).getName();
        //        //    }
        //        //}
        String leftIdType = typeCheck(leftChild);

        String rightIdType = typeCheck(rightChild);
        //System.out.println("RIGHT = " + rightIdType);

        if (leftIdType.equals("import")) {
            return 0;
        }
        if (rightIdType.equals("import")) {
            return 0;
        }

        if (rightIdType.equals("new")){

                JmmNode grandchild = rightChild.getJmmChild(0);
                //System.out.println("---> "+ grandchild);

                //só pode ser ArrayDeclaration ou Id

                if(grandchild.getKind().equals(AstNode.ARRAY_DECLARATION.toString())){
                    rightIdType = grandchild.getJmmChild(0).get("type");

                }else{ // ID
                    //System.out.println("-------->" + grandchild.get("name"));
                    //System.out.println("tou aqui dentro");
                    //rightIdType = leftIdType;
                    //TODO: caso rightChild = 'new qqcoisa()'
                    var c = symbolTable.getClassName();
                    if (grandchild.get("name").equals(c)) {
                        rightIdType = c;
                    } else {
                        rightIdType = "import";
                    }

                }


        }

        if(!leftIdType.equals(rightIdType)){
            this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC,
                    Integer.valueOf(node.get("line")) , Integer.valueOf(node.get("col")),
                    "Assignment with wrong types"));
        }

        return 0;
    }

    public Type getIdType(JmmNode node){
        var father = AstUtils.getPreviousNode(node, AstNode.METHOD_DECLARATION);
        //LocalVars
        for (var localVariable :symbolTable.getLocalVariables( father.get("name") )) {
            if(node.get("name").equals(localVariable.getName()))
                return localVariable.getType();
        }
        //Params
        for (var param :symbolTable.getParameters( father.get("name") )) {
            if(node.get("name").equals(param.getName()))
                return param.getType();
        }

        //Fields
        for (var field :symbolTable.getFields() ) {
            if(node.get("name").equals(field.getName()))
                return field.getType();
        }
        return null;
    }

    public boolean isTypeExternal(Type type) {
        if (type == null) return false;
        var type_name = type.getName();
        // check extend
        var extend = symbolTable.getSuper();
        // check imports
        var imports = symbolTable.getImports();
        // add extend to loop if not null
//        if (extend != null)           // TODO: MR.GOLOSO PLZ FIX THIS, THIS IS CAUSING TROUBLE IN OLLIR
//            imports.add(0, extend);
        for (var t: imports) {
            if (type_name.equals(t))
                return true;
        }

        return false;
    }

    private String typeCheck(JmmNode node) {
        var myKind = node.getKind();

        if (myKind.equals(AstNode.BIN_OP.toString())) {

            if (node.get("value").equals("&&") || node.get("value").equals("<")) {
                return "boolean";
            }
            return "int";
        }

        if (myKind.equals(AstNode.LITERAL.toString())) {
            return node.get("type");
        }
        if (myKind.equals(AstNode.OBJECT_CREATION_EXPRESSION.toString())) {
            return "new";
        }
        if (myKind.equals(AstNode.ID.toString())) {
            var type = getIdType(node);
            if (type == null) return "null";
            if (isTypeExternal(type)) {
                return "import";
            }

            return type.getName();
        }
        if (myKind.equals(AstNode.ARRAY_ACCESS_EXPRESSION.toString())) {
            // if it's an array[] it produces an id as child 0 , just return its type
            return typeCheck(node.getJmmChild(0));
        }
        if (myKind.equals(AstNode.METHOD_DECLARATION.toString())) {
            return "null"; // TODO: implement method
        }

        return "null";
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }
}
