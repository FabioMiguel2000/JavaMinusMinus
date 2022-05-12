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

    }



    public ArrayList assignmentVisit(JmmNode assignmentNode, ArrayList children){

        var result = new ArrayList<>();

        var leftChild = visit(assignmentNode.getJmmChild(0));
        var rightChild = visit(assignmentNode.getJmmChild(1));


        var address = leftChild.get(1) ;
        String assignType = leftChild.get(1).toString().split("\\.")[1];
        String code;
//        if(complete){
//            code = leftChild.get(1) + " :=."+ assignType + " " + rightChild.get(0).toString();
//
//        }
//        else{
            code = rightChild.get(0).toString() + address.toString() + " :=." + assignType + " " + rightChild.get(1) + ";\n";
//        }

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
//        if(countVisit == 0){
//            address = "";
//            complete = true;
//            code = leftChild.get(1) +" "+ opString +" "+ rightChild.get(1) + ";\n";
//        }
//        else{
            address = "temp_" + tempVarCounter++ + opType;
            code = leftChild.get(0).toString() + rightChild.get(0).toString() +
                    address + " :=" + opType + " " +leftChild.get(1) +" "+ opString +" "+ rightChild.get(1) + ";\n";
//        }
//        address = "temp_" + tempVarCounter++ + ".i32";





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
                result.add("." + OllirUtils.getOllirType(localVar.getType().getName()));
                return result;
            }
        }

        var fields = symbolTable.getFields();

        for(var field : fields){
            if(field.getName().equals(name)){
                result.add(name);
                result.add("." + OllirUtils.getOllirType(field.getType().getName()));
                return result;
            }
        }

        int counter = 1;
        for(var methodParam: methodParameters){
            if(methodParam.getName().equals(name)){
                result.add("$" +counter+"."+ name);
                result.add("." + OllirUtils.getOllirType(methodParam.getType().getName()));
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


        var address = variable.get(0) + variable.get(1);

        result.add(code);
        result.add(address);


        return result;
    }

}
