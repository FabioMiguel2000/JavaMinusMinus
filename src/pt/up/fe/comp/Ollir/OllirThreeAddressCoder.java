package pt.up.fe.comp.Ollir;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.AST.AstUtils;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.specs.util.exceptions.NotImplementedException;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class OllirThreeAddressCoder extends AJmmVisitor<ArrayList, ArrayList> {

    private int tempVarCounter;
    private int countVisit;
    private boolean complete;
    private Stack<String> operatorStack;
    private final SymbolTable symbolTable;

    public OllirThreeAddressCoder(SymbolTable symbolTable){
        this.symbolTable = symbolTable;
        countVisit = 0;
        tempVarCounter = 0;
        complete = false;
        operatorStack = new Stack<String>();


        addVisit(AstNode.ASSIGNMENT, this::assignmentVisit);
        addVisit(AstNode.BIN_OP, this::binOPVisit);
        addVisit(AstNode.LITERAL, this::literalVisit);
        addVisit(AstNode.ID, this::idVisit);
        addVisit(AstNode.OBJECT_CREATION_EXPRESSION, this::objectCreationExpression);
        addVisit(AstNode.CALL_EXPRESSION, this::callExprVisitor);
        addVisit(AstNode.ARGUMENTS, this::argumentsVisit);
        addVisit(AstNode.ARRAY_ACCESS_EXPRESSION, this::arrayAccessVisit);

    }

    public String getInvokeCode(JmmNode callExpr){
        var parentMethod = AstUtils.getPreviousNode(callExpr, AstNode.METHOD_DECLARATION);

        var localVars = symbolTable.getLocalVariables(parentMethod.get("name"));

        for (var localVar: localVars) {
            if(localVar.getName().equals(callExpr.getJmmChild(0).get("name"))){
                return "invokevirtual";

            }
        }

        var fields = symbolTable.getFields();

        for(var field : fields){
            if(field.equals(callExpr.getJmmChild(0).get("name"))){
                return "invokevirtual";
            }
        }

        var params = symbolTable.getParameters(parentMethod.get("name"));

        for(var param: params){
            if(param.equals(callExpr.getJmmChild(0).get("name"))){
                return "invokevirtual";
            }
        }

        var imports = symbolTable.getImports();
        for(int i = 0; i < imports.size(); i++){
            if(imports.get(i).equals(callExpr.getJmmChild(0).get("name"))){
                return "invokestatic";
            }
        }

        throw new NotImplementedException(this);
    }

    public ArrayList arrayAccessVisit(JmmNode arrayAccessNode, ArrayList children){
        var result = new ArrayList<>();


        String address = "";
        String code = "";

        var leftChild = visit(arrayAccessNode.getJmmChild(0));
        var rightChild = visit(arrayAccessNode.getJmmChild(1));


        String arrayType = ".i32";

        String onlyLeftChildName = getVariableStringByName(arrayAccessNode.getJmmChild(0).get("name"),arrayAccessNode.getJmmChild(0)).get(0);

        address = "temp_" + tempVarCounter++ + arrayType;
        code = leftChild.get(0).toString() + rightChild.get(0).toString() +
                address + " :=" + arrayType + " " + onlyLeftChildName + "[" + rightChild.get(1) + "].i32" + ";\n";


        result.add(code);
        result.add(address);

        return result;
    }

    public ArrayList callExprVisitor(JmmNode callExprNode, ArrayList children){
        var result = new ArrayList<>();

        String invokeCode = "";

        var invokeType = getInvokeCode(callExprNode);

        invokeCode += invokeType + "(";

        var leftChild = visit(callExprNode.getJmmChild(0));
        var rightChild = visit(callExprNode.getJmmChild(1));
        var argumentsNode = visit(callExprNode.getJmmChild(2));


        invokeCode += parseName(leftChild.get(1).toString());

        if(invokeType.equals("invokevirtual")){
            var type = getVariableStringByName(callExprNode.getJmmChild(0).get("name"), callExprNode).get(1);
            invokeCode += type;
        }

        invokeCode += ", \"";
        invokeCode += rightChild.get(1);
        invokeCode+= "\"";

        invokeCode += argumentsNode.get(1);

        invokeCode += ")";


        var assignNode =AstUtils.getPreviousNode(callExprNode, AstNode.ASSIGNMENT);
        String type;
        if(assignNode == null){
            type = ".V";
        }
        else{
            type = getVariableStringByName(assignNode.getJmmChild(0).get("name"), assignNode).get(1);
        }



        invokeCode += type;
        String address = "temp_" + tempVarCounter++ + type;


        String code = leftChild.get(0).toString() + rightChild.get(0).toString() + argumentsNode.get(0).toString() +
                address + " :=" + type + " "+ invokeCode + ";\n";

        result.add(code);
        result.add(address);

        return result;

    }

    public String parseName(String fullVarName){
        var tokens = fullVarName.split("\\.");
        if(tokens.length == 2 || tokens.length == 1){
            return tokens[0];
        }
        else{
            return tokens[0] + "." + tokens[1];
        }
    }

    public ArrayList argumentsVisit(JmmNode arguments, ArrayList children){
        var result = new ArrayList<>();
        String address = "";
        String code = "";
        for(var child: arguments.getChildren()){
            var arg = visit(child);
            address += ", " + arg.get(1);
            code += arg.get(0);

        }

        result.add(code);
        result.add(address);

        return result;
    }

    public ArrayList assignmentVisit(JmmNode assignmentNode, ArrayList children){

        var result = new ArrayList<>();

        var leftChild = visit(assignmentNode.getJmmChild(0));
        var rightChild = visit(assignmentNode.getJmmChild(1));


        var address = leftChild.get(1) ;
//        String assignType = leftChild.get(1).toString().split("\\.")[1];
        String assignType;
        if(assignmentNode.getJmmChild(0).getKind().equals(AstNode.ID.toString())){
            assignType = getVariableStringByName(assignmentNode.getJmmChild(0).get("name"), assignmentNode.getJmmChild(0)).get(1).toString();

        }
        else{ // array -> .i32
            assignType = ".i32";
        }
        String code;
//        if(complete){
//            code = leftChild.get(1) + " :=."+ assignType + " " + rightChild.get(0).toString();
//
//        }
//        else{
        System.out.println("AssignementNode  = " + assignmentNode);
        code = rightChild.get(0).toString() + address.toString() + " :=" + assignType + " " + rightChild.get(1) + ";\n";
//        }

        result.add(code);
        result.add(address);


        return result;
    }

    public ArrayList objectCreationExpression(JmmNode objectCreationNode, ArrayList children){
        var result = new ArrayList<>();

        String address = "new(";


        if(objectCreationNode.getJmmChild(0).getKind().equals(AstNode.ID.toString())){
            var objectName = objectCreationNode.getJmmChild(0).get("name");
            address += objectName + ")." + objectName;
        }
        else{ // Array Declaration
            address+= "array,";
            var arrayDeclChild = visit(objectCreationNode.getJmmChild(0).getJmmChild(0));
            address += arrayDeclChild.get(1).toString() + ").array.i32";

        }

//        var child = visit(objectCreationNode.getJmmChild(0));
//
//
//        var objectName = child.get(1).toString();
//        address += objectName + ")." + objectName;

        String code = "";

        result.add(code);
        result.add(address);

        return result;
    }


    public ArrayList binOPVisit(JmmNode assignmentNode, ArrayList children){
        var result = new ArrayList<>();

        var leftChild = visit(assignmentNode.getJmmChild(0));
        var rightChild = visit(assignmentNode.getJmmChild(1));

        String opString;
        String opType;
        switch (assignmentNode.get("value")){
            case "*":
                opString = "*.i32";
                opType = ".i32";
                break;
            case "add":
                opString = "+.i32";
                opType = ".i32";
                break;
            case "sub":
                opString = "-.i32";
                opType = ".i32";
                break;
            case "/":
                opString = "/.i32";
                opType = ".i32";
                break;
            case "&&":
                opString = "&&.bool";
                opType = ".bool";
                break;
            case "<":
                opString = "<.bool";
                opType = ".bool";
                break;
            default:
                throw new NotImplementedException(this);
        }



        String address;
        String code;

        address = "temp_" + tempVarCounter++ + opType;
        code = leftChild.get(0).toString() + rightChild.get(0).toString() +
                    address + " :=" + opType + " " +leftChild.get(1) +" "+ opString +" "+ rightChild.get(1) + ";\n";


        result.add(code);
        result.add(address);
        countVisit ++;


        return result;
    }
    public ArrayList literalVisit(JmmNode literalNode, ArrayList children){
        var result = new ArrayList<>();

        var code = "";

        var address = literalNode.get("value");
        switch (literalNode.get("type")){
            case "int":
                address += ".i32";
                operatorStack.push(".i32");
                break;
            case "boolean":
                address += ".bool";
                operatorStack.push(".bool");
                break;

        }
        result.add(code);
        result.add(address);


        return result;
    }

    // returns with array with [name, type]
    // example: ["varName", ".i32"], ["$1.varName", ".i32"]
    public ArrayList<String> getVariableStringByName(String name, JmmNode currentNode){
        var parentMethodNode = AstUtils.getPreviousNode(currentNode, AstNode.METHOD_DECLARATION);
        var localVars = symbolTable.getLocalVariables(parentMethodNode.get("name"));
        var methodParameters = symbolTable.getParameters(parentMethodNode.get("name"));
        var result = new ArrayList<String>();
        for (var localVar: localVars) {
            if(localVar.getName().equals(name)){
                result.add(name);
                result.add("." + OllirUtils.getOllirType(localVar.getType()));
                return result;
            }
        }

        var fields = symbolTable.getFields();

        for(var field : fields){
            if(field.getName().equals(name)){
                result.add(name);
                result.add("." + OllirUtils.getOllirType(field.getType()));
                return result;
            }
        }

        int counter = 1;
        for(var methodParam: methodParameters){
            if(methodParam.getName().equals(name)){
                result.add("$" +counter+"."+ name);
                result.add("." + OllirUtils.getOllirType(methodParam.getType()));
                return result;
            }
            counter ++;
        }

        return result;
    }


    public ArrayList idVisit(JmmNode idNode, ArrayList children){
        var result = new ArrayList<>();

        var code = "";


        var variable = getVariableStringByName(idNode.get("name"), idNode);

        String address;
        if(variable.size() == 0){
            address = idNode.get("name");
        }else{
            address = variable.get(0) + variable.get(1);

        }


        result.add(code);
        result.add(address);

        return result;
    }



}
