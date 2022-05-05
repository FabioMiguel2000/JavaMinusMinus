package pt.up.fe.comp.AST;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.Objects;

public class AstUtils {
    public static Type buildType(JmmNode type){
        SpecsCheck.checkArgument(type.getKind().equals("Type"),
                ()-> "Expected mode o type 'Type', got '" + type.getKind() + "'");

        var typeName = type.get("name");
        boolean isArray = Objects.equals(type.get("isArray"), "true");

        return new Type(typeName, isArray);
    }
}
