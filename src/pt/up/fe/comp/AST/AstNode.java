package pt.up.fe.comp.AST;

import pt.up.fe.specs.util.SpecsStrings;

public enum AstNode {
    PROGRAM,
    IMPORT_DECLARATION,
    CLASS_DECLARATION,
    METHOD_DECLARATION,
    STATEMENT,
    CALL_EXPRESSION,
    ASSIGNMENT,
    OBJECT_CREATION_EXPRESSION,
    VAR_DECLARATION,
    IF_ELSE_STATEMENT,
    ARGUMENTS,
    BIN_OP,
    LITERAL,
    ID;

    private final String name;

    private AstNode(){
        this.name = SpecsStrings.toCamelCase(name(), "_", true);
    }

    @Override
    public String toString(){
        return name;
    }
}
