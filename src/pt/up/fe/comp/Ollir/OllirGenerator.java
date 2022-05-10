package pt.up.fe.comp.Ollir;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.Objects;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, Integer> {
    private final StringBuilder code;
    private final SymbolTable symbolTable;
    private boolean ifLoop;

    public OllirGenerator(SymbolTable symbolTable){
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;
        ifLoop = false;

        addVisit(AstNode.PROGRAM, this::programVisit);
        addVisit(AstNode.CLASS_DECLARATION, this::classDeclVisit);
        addVisit(AstNode.METHOD_DECLARATION, this::methodDeclVisit);
        addVisit(AstNode.STATEMENT, this::stmtVisit);
        addVisit(AstNode.CALL_EXPRESSION, this::callExprVisit);
        addVisit(AstNode.ARGUMENTS, this::argumentsVisit);
        addVisit(AstNode.ID, this::idVisit);
        addVisit(AstNode.ASSIGNMENT, this::assignmentVisit);
        addVisit(AstNode.OBJECT_CREATION_EXPRESSION, this::objectCreationVisit);
        addVisit(AstNode.VAR_DECLARATION, this::varDeclaration);
        addVisit(AstNode.IF_ELSE_STATEMENT, this::ifElseStmtDeclaration);
        addVisit(AstNode.LITERAL, this::literalVisit);

//        addVisit(AstNode.);

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
        for(var stmt: stmts){
            visit(stmt);
        }

        code.append("}\n");

        return 0;
    }

    public Integer stmtVisit(JmmNode stmt, Integer dummy){
        visit(stmt.getJmmChild(0));
        if(stmt.getJmmChild(0).getKind().equals(AstNode.IF_ELSE_STATEMENT.toString())){
            return 0;
        }

        code.append(";\n");

        return 0;
    }
    public Integer ifElseStmtDeclaration(JmmNode ifElseStmt, Integer dummy){
        var ifStmt = ifElseStmt.getJmmChild(0);
        var elseStmt = ifElseStmt.getJmmChild(1);
        code.append("if(");

        var ifConditionNode = ifStmt.getJmmChild(0); // if ( THIS PART )
        var ifScope = ifStmt.getJmmChild(1); // if(){ THIS PART }
        if(ifConditionNode.getKind().equals(AstNode.BIN_OP.toString()) && ifConditionNode.get("value").equals("<")) {
            System.out.println("entrou");
            visit(ifConditionNode.getJmmChild(0));
            code.append(">=");
            visit(ifConditionNode.getJmmChild(1));
        }
        else{
            System.out.println("nao entrou");
            visit(ifConditionNode);
        }


        code.append(") goto else;\n");
        visit(ifScope);
        code.append("goto endif;\n");
        code.append("else:\n");
        visit(elseStmt.getJmmChild(0));

        code.append("endif:\n");

        return 0;
    }
    public Integer literalVisit(JmmNode literalNode, Integer dummy){
        code.append(literalNode.get("value")).append(".");
        code.append(OllirUtils.getOllirType(literalNode.get("type")));
        return 0;
    }

    public Integer varDeclaration(JmmNode varDeclNode, Integer dummy){
        var parent = varDeclNode.getJmmParent();
        if(parent.getKind().equals(AstNode.CLASS_DECLARATION.toString())){
            code.append(".field private ").append(varDeclNode.getJmmChild(1).get("name")).append(".");
            var type = varDeclNode.getJmmChild(0).get("name");
            code.append(OllirUtils.getOllirType(type)).append(";\n");
        }

        return 0;
    }



    public String getInvokeCode(JmmNode callExpr){
        var parentMethod = OllirUtils.getPreviousNode(callExpr, AstNode.METHOD_DECLARATION);

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

        var imports = symbolTable.getImports();
        for(int i = 0; i < imports.size(); i++){
            if(imports.get(i).equals(callExpr.getJmmChild(0).get("name"))){
                return "invokestatic";
            }
        }

        throw new NotImplementedException(this);
    }



    public Integer callExprVisit(JmmNode callExpr, Integer dummy){
        // todo see details on video 02:43:00
        // ver o tipo da expressao = objeto -> chamada de instancia
        // tipo = classe -> chamada estatica

        var invokeType = getInvokeCode(callExpr);

        code.append(invokeType).append("(");

        visit(callExpr.getJmmChild(0));
        if(invokeType.equals("invokevirtual")){
            code.append(".")
                    .append(getFieldOrLocalVarType(callExpr.getJmmChild(0).get("name"), OllirUtils.getPreviousNode(callExpr.getJmmChild(0), AstNode.METHOD_DECLARATION)));
        }
        // TODO: RESOLVER PRIMEIRO A DUVIDA DE CALLEXPRESSION
        code.append(", \"");
        visit(callExpr.getJmmChild(1));
        code.append("\"");
        visit(callExpr.getJmmChild(2));
        code.append(").");


        var parentNode = callExpr.getJmmParent();

        if(parentNode.getKind().equals(AstNode.ASSIGNMENT.toString())){
            var parentMethod = OllirUtils.getPreviousNode(callExpr, AstNode.METHOD_DECLARATION);
            var localVars = symbolTable.getLocalVariables(parentMethod.get("name"));
            String type;
            for (var localVar: localVars) {
                if(localVar.getName().equals(parentNode.getJmmChild(0).get("name"))){

                    type = OllirUtils.getOllirType(localVar.getType().getName());
                    code.append(OllirUtils.getOllirType(type));
                    return 0;
                }
            }

            var fields = symbolTable.getFields();

            for(var field : fields){
                if(field.getName().equals(parentNode.getJmmChild(0).get("name"))){
                    type = OllirUtils.getOllirType(field.getType().getName());
                    code.append(OllirUtils.getOllirType(type));
                    return 0;
                }
            }
        }
        else{
            code.append("V");
        }

        return 0;
    }

    public Integer objectCreationVisit(JmmNode objectCreationNode, Integer dummy){
        code.append("new(");
        var objectName =objectCreationNode.getJmmChild(0).get("name");
        code.append(objectName).append(").");
        code.append(objectName);

        return 0;
    }

    public String getFieldOrLocalVarType(String name, JmmNode methodNode){
        var localVars = symbolTable.getLocalVariables(methodNode.get("name"));
        String type;
        for (var localVar: localVars) {
            if(localVar.getName().equals(name)){
                return localVar.getType().getName();
            }
        }

        var fields = symbolTable.getFields();

        for(var field : fields){
            if(field.getName().equals(name)){
                return field.getType().getName();
            }
        }
        return "";
    }

    public Integer assignmentVisit(JmmNode arguments, Integer dummy){

        var name = arguments.getJmmChild(0).get("name");

        var ParentMethod = OllirUtils.getPreviousNode(arguments, AstNode.METHOD_DECLARATION);
        var typeString = getFieldOrLocalVarType(name, ParentMethod);
        var type = OllirUtils.getOllirType(typeString);

        code.append(name).append(".").append(type);
        code.append(" :=.").append(type).append(" ");

        var rightHandNode = arguments.getJmmChild(1);

        visit(rightHandNode);

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
//        var type = getFieldOrLocalVarType(id.get("name"), OllirUtils.getPreviousNode(id, AstNode.METHOD_DECLARATION));

        code.append(id.get("name"));
//        if(type!=null){
//            code.append(".").append(type);
//        }
        return 0;
    }

}
