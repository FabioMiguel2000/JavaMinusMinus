package pt.up.fe.comp.Ollir;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class OllirUtils {
    public OllirUtils(){

    }

    public static String getCode(Symbol symbol){
        return symbol.getName() + "." + getCode(symbol.getType());
    }

    public static String getCode(Type type){
        StringBuilder code = new StringBuilder();

        if(type.isArray()){
            code.append("array.");
        }

        code.append(getOllirType(type.getName()));

        return code.toString();
    }

    public static String getOllirType(String jmmType){
        switch(jmmType){
            case "void":
                return "V";
            case "int":
                return "i32";
            default:
                return jmmType;
        }

    }


    public static JmmNode getPreviousNode(JmmNode node, AstNode type){
        var currentNode = node;

        while(!currentNode.getKind().equals(type.toString())){
            currentNode = currentNode.getJmmParent();
        }

        return currentNode;
    }


}
