package pt.up.fe.comp.AST;

import pt.up.fe.specs.util.SpecsStrings;

public enum AstNode {
    ARGUMENTS,
    ARRAY_ACCESS_EXPRESSION,
    ARRAY_DECLARATION,
    ASSIGNMENT,
    BIN_OP,
    CALL_EXPRESSION,
    CLASS_DECLARATION,
    ELSE_STATEMENT,
    ID,
    ID_PROGRAM,
    IF_ELSE_STATEMENT,
    IF_STATEMENT,
    IMPORT_DECLARATION,
    LENGTH_PROPERTY,
    LITERAL,
    LITERAL_PROGRAM,
    METHOD_DECLARATION,
    NOT_EXPRESSION,
    OBJECT_CREATION_EXPRESSION,
    PARAMETER,
    PROGRAM,
    RETURN_DECLARATION,
    STATEMENT,
    TYPE,
    VAR_DECLARATION,
    WHILE_STATEMENT;

    private final String name;

    private AstNode(){
        this.name = SpecsStrings.toCamelCase(name(), "_", true);
    }

    @Override
    public String toString(){
        return name;
    }
}
