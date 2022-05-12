package pt.up.fe.comp.Ollir;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.specs.util.exceptions.NotImplementedException;


import java.util.ArrayList;

public class OllirThreeAddressCoder extends AJmmVisitor<ArrayList, ArrayList> {

    private String code;
    private int tempVarCounter;
    public OllirThreeAddressCoder(){
        tempVarCounter = 0;
        code = "";
        addVisit(AstNode.ASSIGNMENT, this::assignmentVisit);
        addVisit(AstNode.BIN_OP, this::binOPVisit);
        addVisit(AstNode.LITERAL, this::literalVisit);
        addVisit(AstNode.ID, this:: idVisit);
    }

    public ArrayList assignmentVisit(JmmNode assignmentNode, ArrayList children){
        var result = new ArrayList<>();

        var leftChild = visit(assignmentNode.getJmmChild(0));
        var rightChild = visit(assignmentNode.getJmmChild(1));

        var address = leftChild.get(1) ;
        var code = rightChild.get(0).toString() + address.toString() + " :=.i32 " + rightChild.get(1) + ";\n";

        result.add(code);
        result.add(address);


        return result;
    }
    public ArrayList binOPVisit(JmmNode assignmentNode, ArrayList children){
        var result = new ArrayList<>();

        var leftChild = visit(assignmentNode.getJmmChild(0));
        var rightChild = visit(assignmentNode.getJmmChild(1));

        String opString;

        switch (assignmentNode.get("op")){
            case "*":
                opString = "*.i32";
                break;
            case "add":
                opString = "+.i32";
                break;
            case "sub":
                opString = "-.i32";
                break;

            default:
                throw new NotImplementedException(this);
        }



        var address = "temp_" + tempVarCounter++ + ".i32";;

        var code = leftChild.get(0).toString() + rightChild.get(0).toString() +
                address + " :=.i32 " +leftChild.get(1) +" "+ opString +" "+ rightChild.get(1) + ";\n";


        result.add(code);
        result.add(address);

        return result;
    }
    public ArrayList literalVisit(JmmNode literalNode, ArrayList children){
        var result = new ArrayList<>();

        var code = "";
        var address = literalNode.get("value") + ".i32";

        result.add(code);
        result.add(address);


        return result;
    }
    public ArrayList idVisit(JmmNode idNode, ArrayList children){
        var result = new ArrayList<>();

        var code = "";
        var address = idNode.get("name") + ".i32";

        result.add(code);
        result.add(address);


        return result;
    }

}
