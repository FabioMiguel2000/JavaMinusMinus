package pt.up.fe.comp.Ollir;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Objects;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, Integer> {
    private final StringBuilder code;
    private final SymbolTable symbolTable;
    private JmmNode PreviousNode;


    public OllirGenerator(SymbolTable symbolTable){
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;

        addVisit(AstNode.PROGRAM, this::programVisit);
        addVisit(AstNode.CLASS_DECLARATION, this::classDeclVisit);
        addVisit(AstNode.METHOD_DECLARATION, this::methodDeclVisit);
        addVisit(AstNode.STATEMENT, this::stmtVisit);
        addVisit(AstNode.CALL_EXPRESSION, this::callExprVisit);
        addVisit(AstNode.ARGUMENTS, this::argumentsVisit);
        addVisit(AstNode.ID, this::idVisit);


    }

    public String getCode() {
        return code.toString();
    }

    private Integer programVisit(JmmNode program, Integer dummy){
        for(var importString: symbolTable.getImports()){
            code.append("import ").append(importString).append(";\n");
        }

        for (var child: program.getChildren()){
            visit(child);
        }


        return 0;
    }

    private Integer classDeclVisit(JmmNode classDecl, Integer dummy){
        code.append("public ").append(symbolTable.getClassName());
        var superClass = symbolTable.getSuper();
        if(superClass != null){
            code.append(" extends ").append(superClass);
        }

        code.append(" {\n");

        for (var child: classDecl.getChildren()){
            visit(child);
        }

        code.append(" }\n");

        return 0;
    }

    private Integer methodDeclVisit(JmmNode methodDecl, Integer dummy){

        var methodSignature = methodDecl.get("name");
        var isStatic = Objects.equals(methodDecl.get("isStatic"), "true");

        code.append(".method public ");
        if(isStatic){
            code.append("static ");
        }

        code.append("main(");

        var params = symbolTable.getParameters(methodSignature);

        var paramCode = params.stream()
                .map(symbol -> OllirUtils.getCode(symbol))
                .collect(Collectors.joining(", "));

        code.append(paramCode);

        code.append(").");

        code.append(OllirUtils.getCode(symbolTable.getReturnType(methodSignature)));

        code.append(" {\n");

        int lastParamIndex = -1;
        for(int i = 0; i < methodDecl.getNumChildren(); i++){
            if(methodDecl.getJmmChild(i).getKind().equals("Parameter")){
                lastParamIndex = i;
            }
        }

        var stmts = methodDecl.getChildren().subList(lastParamIndex +1, methodDecl.getNumChildren());
        System.out.println("STMTS: " + stmts);
        for(var stmt: stmts){
            visit(stmt);
        }

        code.append("}\n");

        return 0;
    }

    public Integer stmtVisit(JmmNode stmt, Integer dummy){
        visit(stmt.getJmmChild(0));
        code.append(";\n");

        return 0;
    }

    public Integer callExprVisit(JmmNode callExpr, Integer dummy){
        // todo see details on video 02:43:00
        code.append("invokestatic(");
        // todo : ExpressionToOllir -> codeBefore, value
        visit(callExpr.getJmmChild(0));
        // TODO: RESOLVER PRIMEIRO A DUVIDA DE CALLEXPRESSION
        code.append(", \"");
        visit(callExpr.getJmmChild(1));
        code.append("\"");
        visit(callExpr.getJmmChild(2));
        code.append(").V");



        return 0;
    }

    public Integer argumentsVisit(JmmNode arguments, Integer dummy) {
        for(var child: arguments.getChildren()){
            code.append(", ");
            visit(child);
        }
        return 0;
    }

    public Integer idVisit(JmmNode id, Integer dummy) {

        code.append(id.get("name"));
        return 0;
    }
}
