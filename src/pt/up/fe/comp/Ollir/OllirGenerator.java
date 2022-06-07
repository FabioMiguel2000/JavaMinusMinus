package pt.up.fe.comp.Ollir;

import pt.up.fe.comp.AST.AstNode;
import pt.up.fe.comp.AST.AstUtils;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Integer, Integer> {
    private final StringBuilder code;
    private final SymbolTable symbolTable;

    private String temporaryStorage;
    private int ifCounter;
    private int loopCounter;

    public OllirGenerator(SymbolTable symbolTable){
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;
        this.temporaryStorage = "";
        ifCounter = 0;
        loopCounter = 0;

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
        addVisit(AstNode.WHILE_STATEMENT, this::whileVisit);
        addVisit(AstNode.RETURN_DECLARATION, this::returnVisit);

//        addVisit(AstNode.);

    }

    public String getCode() {
        return code.toString();
    }

    private Integer programVisit(JmmNode program, Integer dummy){
        System.out.println(symbolTable.getImports());
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

        code.append(methodSignature).append("(");

//        code.append("main(");

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
        if(methodSignature.equals("main")){
            code.append("ret.V;\n");

        }

        code.append("}\n");

        return 0;
    }

    private Integer returnVisit(JmmNode returnNode, Integer dummy){

        OllirThreeAddressCoder coder = new OllirThreeAddressCoder(symbolTable);

        var returnStmtCode = coder.visit(returnNode.getJmmChild(0));

        code.append(returnStmtCode.get(0));

        code.append("ret.");


        String methodType = OllirUtils.getOllirType(symbolTable.getReturnType(
                AstUtils.getPreviousNode(returnNode,AstNode.METHOD_DECLARATION).get("name")
        ).getName());

        code.append(methodType).append(" ");

        code.append(returnStmtCode.get(1));

        code.append(";\n");

        return 0;
    }

    public Integer stmtVisit(JmmNode stmt, Integer dummy){
        for(var stmtChild : stmt.getChildren()){
            visit(stmtChild);
            if(stmtChild.getKind().equals(AstNode.IF_ELSE_STATEMENT.toString())){
                continue;
            }
            if(stmtChild.getKind().equals(AstNode.ASSIGNMENT.toString())){
                continue;
            }
            if(stmtChild.getKind().equals(AstNode.WHILE_STATEMENT.toString())){
                continue;
            }
            code.append(";\n");
        }

        return 0;
    }
    public Integer whileVisit(JmmNode whileNode, Integer dummy){
        int localLoopCounter = loopCounter++;

        code.append("Loop_" + localLoopCounter).append(":\n");

        var whileConditionNode = whileNode.getJmmChild(0);

        OllirThreeAddressCoder coder = new OllirThreeAddressCoder(symbolTable);

        var coditionCode = coder.visit(whileConditionNode);

        code.append(coditionCode.get(0));

        code.append("if(");

        code.append(coditionCode.get(1));

        code.append(") goto Body_" + localLoopCounter).append(";\n");
        code.append("goto EndLoop_" + localLoopCounter).append(";\n");
        code.append("Body_"+ localLoopCounter +":\n");

        visit(whileNode.getJmmChild(1));

        code.append("goto Loop_"+ localLoopCounter +";\n" );

        code.append("EndLoop_" + localLoopCounter + ":\n");


        return 0;
    }


    public Integer ifElseStmtDeclaration(JmmNode ifElseStmt, Integer dummy){
        int localIfCounter = ifCounter++;

        var ifStmt = ifElseStmt.getJmmChild(0);
        var elseStmt = ifElseStmt.getJmmChild(1);

        var ifConditionNode = ifStmt.getJmmChild(0); // if ( THIS PART )

        OllirThreeAddressCoder coder = new OllirThreeAddressCoder(symbolTable);

        var coditionCode = coder.visit(ifConditionNode);

        code.append(coditionCode.get(0));

        code.append("if(");

        code.append("!.bool ").append(coditionCode.get(1));

        var ifScope = ifStmt.getJmmChild(1); // if(){ THIS PART }

        code.append(") goto else_" + localIfCounter +";\n");
        visit(ifScope);
        code.append("goto endif_" +localIfCounter + ";\n");
        code.append("else_"+localIfCounter+":\n");
        visit(elseStmt.getJmmChild(0));

        code.append("endif_"+localIfCounter +":\n");


        return 0;
    }


    public Integer literalVisit(JmmNode literalNode, Integer dummy){
        if(literalNode.get("value").equals("true")){
            code.append("1");
        }
        else if(literalNode.get("value").equals("false")){
            code.append("0");
        }
        else{
            code.append(literalNode.get("value"));
        }
        code.append(".");
        code.append(OllirUtils.getOllirType(literalNode.get("type")));
        return 0;
    }

    public Integer varDeclaration(JmmNode varDeclNode, Integer dummy){
        var parent = varDeclNode.getJmmParent();
        if(parent.getKind().equals(AstNode.CLASS_DECLARATION.toString())){  // Only field declaration is converted to ollir
            code.append(".field private ").append(varDeclNode.getJmmChild(1).get("name")).append(".");

            String type = "";
            if(varDeclNode.getJmmChild(0).get("isArray").equals("true")){
                type += "array.";
            }
            type += OllirUtils.getOllirType(varDeclNode.getJmmChild(0).get("name")) + ";\n";
//            var isArray = varDeclNode.getJmmChild(0).get("")
            code.append(type);
        }

        return 0;
    }



    public String getInvokeCode(JmmNode callExpr){
        var parentMethod = AstUtils.getPreviousNode(callExpr, AstNode.METHOD_DECLARATION);

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

        var params = symbolTable.getParameters(parentMethod.get("name"));

        for(var param: params){
            if(param.equals(callExpr.getJmmChild(0).get("name"))){
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

        visit(callExpr.getJmmChild(2));

        code.append(invokeType).append("(");

        visit(callExpr.getJmmChild(0));
        if(invokeType.equals("invokevirtual")){
            var type = getVariableStringByName(callExpr.getJmmChild(0).get("name"), AstUtils.getPreviousNode(callExpr.getJmmChild(0), AstNode.METHOD_DECLARATION)).get(1);
            code.append(type);
        }

        code.append(", \"");
        visit(callExpr.getJmmChild(1));
        code.append("\"");
//        visit(callExpr.getJmmChild(2));
        code.append(this.temporaryStorage);
        code.append(")");

        var parentNode = callExpr.getJmmParent();

        if(parentNode.getKind().equals(AstNode.ASSIGNMENT.toString())){
            var type = getVariableStringByName(parentNode.getJmmChild(0).get("name"), parentNode).get(1);
            code.append(type);

        }
        else{
            code.append(".V");
        }

        return 0;
    }

    public Integer objectCreationVisit(JmmNode objectCreationNode, Integer dummy){
        code.append("new(");
        var objectName =objectCreationNode.getJmmChild(0).get("name");
        code.append(objectName).append(").");
        code.append(objectName).append("\n");


        return 0;
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


    public Integer assignmentVisit(JmmNode assignmentNode, Integer dummy){

        var ollirOp = new OllirThreeAddressCoder(symbolTable);

        var threeAdressCode = ollirOp.visit(assignmentNode).get(0);
        code.append(threeAdressCode);

        return 0;

    }

    public Integer argumentsVisit(JmmNode arguments, Integer dummy) {
        OllirThreeAddressCoder coder = new OllirThreeAddressCoder(symbolTable);
        StringBuilder tempCode = new StringBuilder();
        this.temporaryStorage = "";
        for(var child: arguments.getChildren()){
//            code.append(", ");
//            if(child.getKind().equals(AstNode.ID.toString())){
//                var childInfo = getVariableStringByName(child.get("name"), child);
//                code.append(childInfo.get(0)).append(childInfo.get(1));
//            }
//            else{
//                visit(child);
//            }


            var argCode = coder.visit(child);

            code.append(argCode.get(0));
            this.temporaryStorage += ", " +argCode.get(1);
//            tempCode.append(", ");
//            tempCode.append(argCode.get(1));

        }

        code.append(tempCode);
        return 0;
    }



    public Integer idVisit(JmmNode id, Integer dummy) {

        code.append(id.get("name"));

        return 0;
    }

}
