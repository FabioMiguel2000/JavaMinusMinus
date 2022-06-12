package pt.up.fe.comp.Jasmin;

import org.specs.comp.ollir.*;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OllirToJasmin {
    private final ClassUnit classUnit;
    HashMap<String, Descriptor> varTable;

    private int labelCounter;
    public OllirToJasmin(ClassUnit classUnit){
        this.classUnit = classUnit;
        this.labelCounter = 0;
        classUnit.buildVarTables();
    }



    public String getCode(){
        var code = new StringBuilder();

        code.append(".class public ").append(classUnit.getClassName()).append("\n");

//        System.out.println("current code = " + code);
        String superClassQualifiedName;
        code.append(".super ");
        if(classUnit.getSuperClass() == null){
            superClassQualifiedName = "java/lang/Object";
            code.append(superClassQualifiedName).append("\n");
        }
        else{
            superClassQualifiedName = getFullyQualifiedName(classUnit.getSuperClass());
            code.append(classUnit.getSuperClass()).append("\n");
        }
        for (var field : classUnit.getFields()){
            code.append(".field ").append(field.getFieldName()).append(" ").append(getJasminType(field.getFieldType())).append("\n");
        }


        code.append(SpecsIo.getResource("templates/JasminConstructor.template").replace("${SUPER_NAME}", superClassQualifiedName)).append("\n");


        for (var method: classUnit.getMethods()){
            code.append(getCode(method));
        }


        return code.toString();
    }
    public String getCode(Field field){

        return "";
    }

    public String getCode(Method method){
        var code = new StringBuilder();

        this.varTable = method.getVarTable();

//        System.out.println("VarTable:" + method.getVarTable());

        if(method.isConstructMethod()){
            return "";
        }

        code.append(".method ");

        switch (method.getMethodAccessModifier()){
            case PROTECTED:
                code.append("protected ");
                break;
            case PRIVATE:
                code.append("private ");
                break;
            case PUBLIC:
                code.append("public ");
                break;
            case DEFAULT:
                break;
            default:
                throw new NotImplementedException(method.getMethodAccessModifier());
        }

        if(method.isStaticMethod()){
            code.append("static ");
        }

        code.append(method.getMethodName()).append("(");

        var methodParamTypes = method.getParams().stream()
                .map(element -> getJasminType(element.getType()))
                .collect(Collectors.joining());

//        System.out.println("METHOD PARAMS: " + methodParamTypes);

        code.append(methodParamTypes).append(")").append(getJasminType(method.getReturnType())).append("\n");
        code.append(".limit stack 99\n");
        code.append(".limit locals 99\n");


        for(var instruction: method.getInstructions()){
            code.append(getInstructionCode(instruction, method));
        }
//        if(method.getMethodName().equals("main")){
//            code.append("return\n");
//
//        }
        code.append(".end method\n\n");
        return code.toString();

    }
    public String getInstructionCode(Instruction instruction, Method method){
        instruction.show();
        StringBuilder code = new StringBuilder();
        for (Map.Entry<String, Instruction> entry : method.getLabels().entrySet()) {
            if (entry.getValue().equals(instruction)){
                code.append(entry.getKey()).append(":\n");
            }
        }
        switch (instruction.getInstType()){
            case NOPER:
                return code.append(getCode((SingleOpInstruction) instruction)).toString();
            case CALL:
                return code.append(getCode((CallInstruction) instruction)).toString();
            case ASSIGN:
                return code.append(getAssignInstructionCode((AssignInstruction)instruction, method)).toString();
            case RETURN:
                return code.append(getCode((ReturnInstruction)instruction)).toString();

            case GETFIELD:
                return code.append(getCode((GetFieldInstruction) instruction)).toString();
            case PUTFIELD:
                return code.append(getCode((PutFieldInstruction) instruction)).toString();
            case BINARYOPER:
                return code.append(getCode((BinaryOpInstruction)instruction)).toString();
            case BRANCH:
                return code.append(getCode((CondBranchInstruction)instruction)).toString();
            case GOTO:
                return code.append(getCode((GotoInstruction)instruction)).toString();
            case UNARYOPER:
                return code.append(getCode((UnaryOpInstruction)instruction)).toString();
            default:
                throw new NotImplementedException(instruction.getInstType());
        }

//        return "";
    }

    private String getCode(UnaryOpInstruction unaryOpInstruction) {
        StringBuilder code = new StringBuilder();

//        unaryOpInstruction.show();

        code.append(loadElement(unaryOpInstruction.getOperands().get(0)));
        int counter = this.labelCounter ++;
        code.append("ifeq NOT_BRANCH_" + counter + "\n"); // ifeq -> pop the value on stack and checks equals 0, jump to branch if so

        code.append("iconst_0\n").append("goto ").append("END_IF_").append(counter).append("\n").
                append("NOT_BRANCH_" + counter).append(":\n").
                append("iconst_1\n").append("END_IF_").append(counter).append(":\n");
        return code.toString();
    }

    private String getCode(GotoInstruction gotoInstruction) {
        StringBuilder code = new StringBuilder();
        code.append("goto " + gotoInstruction.getLabel()).append("\n");
        return code.toString();

    }

    private String getCode(CondBranchInstruction condBranchInstruction) {
//        condBranchInstruction.show();
        StringBuilder code = new StringBuilder();
        code.append(this.loadElement(condBranchInstruction.getOperands().get(0)))
                .append("ifeq " + condBranchInstruction.getLabel()).append("\n");
        return code.toString();

    }

    public String getCode(PutFieldInstruction putFieldInstruction){
        StringBuilder code = new StringBuilder();

        Operand firstOperand = (Operand) putFieldInstruction.getFirstOperand();
        code.append(loadElement(firstOperand));

        Element value = putFieldInstruction.getThirdOperand();
        code.append(loadElement(value));

        code.append("putfield ");

        Operand secondOperand = (Operand) putFieldInstruction.getSecondOperand();

        String fieldSpec = classUnit.getClassName()+"/" + secondOperand.getName();
        code.append(fieldSpec).append(" ");

        code.append(getJasminType(secondOperand.getType()) + "\n");

        return code.toString();
    }
    public String getCode(GetFieldInstruction getFieldInstruction){
        StringBuilder code = new StringBuilder();

        Operand firstOperand = (Operand) getFieldInstruction.getFirstOperand();
        code.append(loadElement(firstOperand));

        code.append("getfield ");

        Operand secondOperand = (Operand) getFieldInstruction.getSecondOperand();

        String fieldSpec = classUnit.getClassName()+"/" + secondOperand.getName();
        code.append(fieldSpec).append(" ");

        code.append(getJasminType(secondOperand.getType()) + "\n");

        return code.toString();
    }


    public String getCode(ReturnInstruction returnInstruction){
        if(!returnInstruction.hasReturnValue()){
            return "return\n";
        }

        switch (returnInstruction.getOperand().getType().getTypeOfElement()){
            case VOID:
                return "return\n";
            case INT32, BOOLEAN:
                return loadElement(returnInstruction.getOperand()) + "ireturn\n";
            case ARRAYREF, OBJECTREF:
                return loadElement(returnInstruction.getOperand()) + "areturn\n";
            default:
                throw new NotImplementedException(returnInstruction.getOperand().getType().getTypeOfElement());
        }
    }
    public String getCode(SingleOpInstruction instruction){
        return loadElement(instruction.getSingleOperand());
    }
    public String getCode(BinaryOpInstruction binaryOpInstruction){
        StringBuilder code = new StringBuilder();
        if(binaryOpInstruction.getOperation().getOpType() == OperationType.ANDB){
            int counter = this.labelCounter ++;
            String ifqeCondition = "ifeq AND_BRANCH_" + counter + "\n"; // ifeq -> pop the value on stack and checks equals 0, jump to branch if so
            code.append(loadElement(binaryOpInstruction.getLeftOperand())).append(ifqeCondition);
            code.append(loadElement(binaryOpInstruction.getRightOperand())).append(ifqeCondition);

            code.append("iconst_1\n")
                    .append("goto ").append("END_IF_").append(counter).append("\n").
                    append("AND_BRANCH_" + counter).append(":\n").
                    append("iconst_0\n").append("END_IF_").append(counter).append(":\n");
            return code.toString();
        } else if (binaryOpInstruction.getOperation().getOpType() == OperationType.LTH) {

            int counter = this.labelCounter ++;
            code.append(loadElement(binaryOpInstruction.getLeftOperand()));

            code.append(loadElement(binaryOpInstruction.getRightOperand()));

            code.append("if_icmplt LESS_THAN_JUMP_"+counter).append("\n")  //if_icmpge -> pop the 2 values on stack and checks if value1 >= value2, jump to branch if so
                    .append("iconst_0\n")
                    .append("goto END_IF_").append(counter).append("\n")
                    .append("LESS_THAN_JUMP_"+counter).append(":\n")
                    .append("iconst_1\n")
                    .append("END_IF_").append(counter).append(":\n");
            return code.toString();
        }
        code.append(loadElement(binaryOpInstruction.getLeftOperand()));
        code.append(loadElement(binaryOpInstruction.getRightOperand()));

        switch (binaryOpInstruction.getOperation().getOpType()){
            case ADD:
                code.append("iadd\n");
                break;
            case SUB:
                code.append("isub\n");
                break;

            case MUL:
                code.append("imul\n");
                break;
            case DIV:
                code.append("idiv\n");
                break;
            default:
                throw new NotImplementedException(binaryOpInstruction.getOperation().getOpType());
        }

        return code.toString();
    }





    public String getAssignInstructionCode(AssignInstruction assignInstruction, Method parentMethod){

        parentMethod.buildVarTable();
        this.varTable = parentMethod.getVarTable();

        StringBuilder code = new StringBuilder();

        Operand operand = (Operand) assignInstruction.getDest(); // left hand side of the expression ' THIS_PART = 1+2*2 '

        if(operand instanceof ArrayOperand){
            // left hand side is an array type
            ArrayOperand arrayOperand = (ArrayOperand) operand;

            code.append("aload").append(this.getVirtualRegister(arrayOperand.getName())).append("\n");
            code.append(loadElement(arrayOperand.getIndexOperands().get(0)));
        }

//        assignInstruction.getRhs().show();

        code.append(getInstructionCode(assignInstruction.getRhs(), parentMethod)); // append the right hand side instruction code first


        if(!(operand.getType().getTypeOfElement().equals(ElementType.OBJECTREF)
                && assignInstruction.getRhs() instanceof CallInstruction)) {
            code.append(storeValueIntoVariable(operand));
        }

        return code.toString();
    }


//

    public String getCode(CallInstruction callInstruction){

        var code = new StringBuilder();

        switch (callInstruction.getInvocationType()){
            case invokestatic:
                return getCodeInvokeStatic(callInstruction);
            case invokevirtual:
                return getCodeInvokeVirtual(callInstruction);
            case invokespecial:
                return getCodeInvokeSpecial(callInstruction);
            case NEW:
                return getCodeNewObject(callInstruction);
            case arraylength:
                code.append(this.loadElement(callInstruction.getFirstArg()));
                code.append("arraylength\n");
                return code.toString();
            default:
                throw new NotImplementedException(callInstruction.getInvocationType());
        }

    }



    private String getCodeNewObject(CallInstruction callInstruction){
        var code = new StringBuilder();
//        callInstruction.show();
        Element element = callInstruction.getFirstArg();

//        element.show();
        switch (element.getType().getTypeOfElement()){
            case OBJECTREF:
                String objName = getFullyQualifiedName( ((Operand)element).getName());
                code.append("new ").append(objName).append("\n");
                code.append("dup\n");
//                System.out.println(code);
                break;
            case ARRAYREF:
                code.append(loadElement(callInstruction.getListOfOperands().get(0)));
                code.append("newarray int\n");
                break;
            default:
                throw new NotImplementedException(element.getType().getTypeOfElement());
        }

        return code.toString();
    }

    private String getCodeInvokeVirtual(CallInstruction callInstruction){
        var code = new StringBuilder();

        code.append(loadElement(callInstruction.getFirstArg()));

        for(var operand: callInstruction.getListOfOperands()){
            code.append(loadElement(operand));
        }

        code.append("invokevirtual ");

        var methodClass = ((ClassType)callInstruction.getFirstArg().getType()).getName();

        code.append(getFullyQualifiedName(methodClass));

        code.append("/");
        var calledMethod = ((LiteralElement) callInstruction.getSecondArg()).getLiteral();
        code.append(calledMethod.substring(1, calledMethod.length()-1));
        code.append("(");

        for(var operand: callInstruction.getListOfOperands()){
            code.append(getArgumentCode(operand));
        }

        code.append(")");

        code.append(getJasminType(callInstruction.getReturnType()));

        code.append("\n");


        return code.toString();

    }


    private String getCodeInvokeStatic(CallInstruction callInstruction){
        var code = new StringBuilder();

        code.append(loadElement(callInstruction.getFirstArg()));

        for(var operand: callInstruction.getListOfOperands()){
            code.append(loadElement(operand));
        }

        code.append("invokestatic ");

        var methodClass = ((Operand) callInstruction.getFirstArg()).getName();

        code.append(getFullyQualifiedName(methodClass));

        code.append("/");
        var calledMethod = ((LiteralElement) callInstruction.getSecondArg()).getLiteral();
        code.append(calledMethod.substring(1, calledMethod.length()-1));
        code.append("(");

        for(var operand: callInstruction.getListOfOperands()){
            code.append(getArgumentCode(operand));
        }

        code.append(")");

        code.append(getJasminType(callInstruction.getReturnType()));

        code.append("\n");


        return code.toString();
    }

    private String getCodeInvokeSpecial(CallInstruction callInstruction){

        var code = new StringBuilder();


        for(var operand: callInstruction.getListOfOperands()){
            code.append(loadElement(operand));
        }

        code.append("invokespecial ");

        var methodClass = ((ClassType)callInstruction.getFirstArg().getType()).getName();

        code.append(getFullyQualifiedName(methodClass));

        code.append("/");
        var calledMethod = ((LiteralElement) callInstruction.getSecondArg()).getLiteral();
        code.append(calledMethod.substring(1, calledMethod.length()-1));
        code.append("(");

        for(var operand: callInstruction.getListOfOperands()){
            code.append(getArgumentCode(operand));
        }

        code.append(")");

        code.append(getJasminType(callInstruction.getReturnType()));

        code.append("\n");

        if(!((ClassType)callInstruction.getFirstArg().getType()).getName().equals("this")){
            code.append(this.storeValueIntoVariable((Operand) callInstruction.getFirstArg()));
        }
        return code.toString();

    }


    private String getArgumentCode(Element operand){
        return getJasminType(operand.getType());
    }


    // ------------------------- UTILS --------------------------------------

    public String getJasminType(Type type){

        if(type instanceof ArrayType){
            return "[" + getJasminType(((ArrayType) type).getTypeOfElements());
        }
        if(type.getTypeOfElement().equals(ElementType.OBJECTREF)){
            String objectName = ((ClassType)type).getName();
            return "L" + this.getFullyQualifiedName(objectName) + ";";
        }
        return getJasminType(type.getTypeOfElement());
    }



    public String getJasminType(ElementType type) {
        //TODO: adicionando os outros casos que faltam a medida que for testando

        switch (type){
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            default:
                throw new NotImplementedException(type);
        }
    }


    public String getFullyQualifiedName(String className){
        for (var importString: classUnit.getImports()){
            var splittedImport  = importString.split("\\.");
            String lastName;

            if(splittedImport.length == 0){
                lastName = importString;
            }
            else{
                lastName = splittedImport[splittedImport.length-1];
            }

            if(lastName.equals(className)){
                return importString.replace('.', '/');
            }
        }
        if(classUnit.getClassName().equals(className)){ // Can import own class file???
            return className;
        }
        throw new RuntimeException("Could not find import for class " + className);
    }

    private String getVirtualRegister(String variableName){
        int virtualRegister = this.varTable.get(variableName).getVirtualReg();
        if(virtualRegister > 3){
            return " " + virtualRegister;
        }
        return "_" + virtualRegister;
    }

    private String loadIntToStack(String literal){
        if(Integer.parseInt(literal) <= 5 && Integer.parseInt(literal) >= 0){ // 0 -> 5
            return "iconst_" + literal + "\n";
        }
        if(Integer.parseInt(literal) == -1 ){
            return "iconst_m1"+ "\n";
        }
        if(Integer.parseInt(literal) <= 127 && Integer.parseInt(literal) >= -128){
            return "bipush " + literal+ "\n";
        }
        if(Integer.parseInt(literal) <= 32767 && Integer.parseInt(literal) >= -327678){
            return "sipush " + literal+ "\n";
        }
        return "ldc " + literal+ "\n";
    }

    public String loadElement(Element element){


//        System.out.println("Inside NOPER: ");
//        instruction.show();


//        Element singleOperandElement = instruction.getSingleOperand();
        if(element instanceof ArrayOperand){
            StringBuilder code = new StringBuilder();
            ArrayOperand operand = (ArrayOperand) element;

            code.append("aload").append(getVirtualRegister(operand.getName())).append("\n");

            code.append(loadElement(operand.getIndexOperands().get(0)));

            return code + "iaload\n";
        }
        if(element.isLiteral()){
            String literalString = ((LiteralElement) element).getLiteral();
            return loadIntToStack(literalString);
        }
        if(element instanceof Operand){
            Operand operand = (Operand) element;
            switch(operand.getType().getTypeOfElement()){
                case THIS:
                    return "aload_0\n";
                case INT32, BOOLEAN:
                    return "iload"+ getVirtualRegister(operand.getName()) + "\n";
                case OBJECTREF, ARRAYREF:
                    return "aload" + getVirtualRegister(operand.getName()) + "\n";
                case CLASS:
                    return "";
                default:
                    throw new NotImplementedException(operand.getType().getTypeOfElement());
            }
        }

        throw new NotImplementedException(element);
    }

    private String storeValueIntoVariable(Operand operand){
        if(operand instanceof ArrayOperand){
            return "iastore\n";
        }

        switch(operand.getType().getTypeOfElement()){
            case INT32, BOOLEAN:
                return "istore" + getVirtualRegister(operand.getName()) + "\n";
            case OBJECTREF, ARRAYREF:
                return "astore" + getVirtualRegister(operand.getName())+ "\n";
            default:
                throw new NotImplementedException(operand.getType().getTypeOfElement());
        }
    }
}
