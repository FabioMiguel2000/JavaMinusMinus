package pt.up.fe.comp.AST;

import pt.up.fe.specs.util.SpecsStrings;

public enum AstNode {
    PROGRAM,
    IMPORT_DECLARATION,
    CLASS_DECLARATION,
    METHOD_DECLARATION,
    STATEMENT,
    CALL_EXPRESSION,
    ARGUMENTS,
    ID,
    ASSIGNMENT,
    VAR_DECLARATION,
    ARRAY_ACCESS_EXPRESSION;

    private final String name;

    private AstNode(){
        this.name = SpecsStrings.toCamelCase(name(), "_", true);
    }

    @Override
    public String toString(){
        return name;
    }
}
