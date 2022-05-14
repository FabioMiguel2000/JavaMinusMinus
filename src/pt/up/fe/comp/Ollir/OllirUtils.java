package pt.up.fe.comp.Ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            case "boolean":
                return "bool";
            default:
                return jmmType;
        }

    }

    public static String getOllirType(Type type){

        switch (type.getName()){
            case "void":
                return "V";
            case "int":
                return type.isArray()? "array.i32" : "i32";
            case "boolean":
                return "bool";
            default:
                return type.getName();
        }
    }


    public static int getMaxDepth(JmmNode currentNode){

        if (currentNode == null){
            return 0;
        }
        List<Integer> depths = new ArrayList<Integer>();
        depths.add(0);
        for(var child: currentNode.getChildren()){
            depths.add(getMaxDepth(child));
        }
        return Collections.max(depths, null)+1;
    }

}
