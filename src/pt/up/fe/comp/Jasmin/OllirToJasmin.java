package pt.up.fe.comp.Jasmin;

import org.specs.comp.ollir.*;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.HashMap;
import java.util.stream.Collectors;

public class OllirToJasmin {
    private final ClassUnit classUnit;
    HashMap<String, Descriptor> varTable;
    public OllirToJasmin(ClassUnit classUnit){
        this.classUnit = classUnit;
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

//        for(var field: classUnit.getFields()){
//            // TODO: Fields  using classUnit.getFields()
//
//            code.append(getCode(field));
//        }



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
//        instruction.show();

//         return instructionMap.apply(instruction);
//        if(instruction instanceof CallInstruction){
//            return getCode((CallInstruction) instruction);
//        }
//        if(instruction instanceof AssignInstruction){
//            return getAssignInstructionCode((AssignInstruction)instruction, method);
//        }

//        if(instruction instanceof OpInstruction){
//
//        }

        switch (instruction.getInstType()){
            case NOPER:
                return getNOPERCode((SingleOpInstruction) instruction, method);

            case CALL:
                return getCode((CallInstruction) instruction);
//            case GOTO:

            case ASSIGN:
                return getAssignInstructionCode((AssignInstruction)instruction, method);
            case RETURN:
                return getReturnInstructionCode((ReturnInstruction)instruction, method);
//            case BRANCH:
//                case RETURN:
            case GETFIELD:
                return getGetFieldInstructionCode((GetFieldInstruction) instruction, method);
            case PUTFIELD:
                return getPutFieldInstructionCode((PutFieldInstruction) instruction, method);
//            case UNARYOPER:
//                return getCode((OpInstruction) instruction);
            case BINARYOPER:
                return getBinaryOpInstructionCode((BinaryOpInstruction)instruction, method);
//                return getCode((OpInstruction) instruction);
            default:
                throw new NotImplementedException(instruction.getInstType());
        }

//        return "";
    }
    public String getPutFieldInstructionCode(PutFieldInstruction putFieldInstruction, Method parentMethod){
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
    public String getGetFieldInstructionCode(GetFieldInstruction getFieldInstruction, Method parentMethod){
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


    public String getReturnInstructionCode(ReturnInstruction returnInstruction, Method parentMethod){
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
    public String getNOPERCode(SingleOpInstruction instruction, Method parentMethod){
        return loadElement(instruction.getSingleOperand());
    }
    public String getBinaryOpInstructionCode(BinaryOpInstruction binaryOpInstruction, Method method){
        StringBuilder code = new StringBuilder();
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
            case AND:
                code.append("TODO:AND_NOT_IMPLEMENTED\n");
                break;
            case LTH:
                code.append("TODO:LTH_NOT_IMPLEMENTED\n");
                break;
            default:
                throw new NotImplementedException(binaryOpInstruction.getOperation().getOpType());
        }

        return code.toString();
    }

    public String loadElement(Element element){


//        System.out.println("Inside NOPER: ");
//        instruction.show();


//        Element singleOperandElement = instruction.getSingleOperand();
        if(element instanceof ArrayOperand){
            StringBuilder code = new StringBuilder();
            ArrayOperand operand = (ArrayOperand) element;

            code.append("aload ").append(getVirtualRegister(operand.getName())).append("\n");

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



    public String getAssignInstructionCode(AssignInstruction assignInstruction, Method parentMethod){

        parentMethod.buildVarTable();
        this.varTable = parentMethod.getVarTable();

        StringBuilder code = new StringBuilder();

        Operand operand = (Operand) assignInstruction.getDest(); // left hand side of the expression ' THIS_PART = 1+2*2 '

        if(operand instanceof ArrayOperand){
            // left hand side is an array type
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

        code.append("invokespecial <init>()V\n"); // invokeSpecial will be only used new objet creation, so only need to consider this condition

        if(!((ClassType)callInstruction.getFirstArg().getType()).getName().equals("this")){
            code.append(this.storeValueIntoVariable((Operand) callInstruction.getFirstArg()));
        }
        return code.toString();
    }


    private String getArgumentCode(Element operand){
        return getJasminType(operand.getType());
//        throw new NotImplementedException(this);
    }



    public String getJasminType(Type type){

        if(type instanceof ArrayType){
            return "[" + getJasminType(((ArrayType) type).getTypeOfElements());
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

    // ------------------------- UTILS --------------------------------------

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
