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

    private int currstackLimit;
    private int localLimit;

    private int stackLimit;
    HashMap<String, Descriptor> varTable;

    private int labelCounter;
    public OllirToJasmin(ClassUnit classUnit){
        this.classUnit = classUnit;
        this.labelCounter = 0;
        this.localLimit = 0;
        this.stackLimit  = 0;
        this.currstackLimit = 0;
        classUnit.buildVarTables();
    }



    public String getCode(){
        var code = new StringBuilder();

        code.append(".class public ").append(classUnit.getClassName()).append("\n");

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
            this.stackLimit = 0;
            this.localLimit = 0;
            code.append(getCode(method));
        }


        return code.toString();
    }

    public String getCode(Method method){
        var code = new StringBuilder();

        this.varTable = method.getVarTable();


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
        else {
            this.localLimit ++;
        }

        code.append(method.getMethodName()).append("(");

        var methodParamTypes = method.getParams().stream()
                .map(element -> getJasminType(element.getType()))
                .collect(Collectors.joining());

        code.append(methodParamTypes).append(")").append(getJasminType(method.getReturnType())).append("\n");


        StringBuilder tempCode = new StringBuilder();
        for(var instruction: method.getInstructions()){
            tempCode.append(getInstructionCode(instruction, method));
            if(instruction instanceof CallInstruction && ((CallInstruction)instruction).getReturnType().getTypeOfElement() != ElementType.VOID){
                tempCode.append("pop\n");
                this.decrementCurrentStackLimit();
            }
        }

        code.append(".limit stack " + this.stackLimit +"\n");
        this.localLimit += method.getVarTable().size();
        code.append(".limit locals "+ this.localLimit+"\n");

        code.append(tempCode);

        code.append(".end method\n\n");
        return code.toString();

    }
    public String getInstructionCode(Instruction instruction, Method method){

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

    }

    private String getCode(UnaryOpInstruction unaryOpInstruction) {
        StringBuilder code = new StringBuilder();


        code.append(loadElement(unaryOpInstruction.getOperands().get(0)));
        int counter = this.labelCounter ++;
        code.append("ifeq NOT_BRANCH_" + counter + "\n"); // ifeq -> pop the value on stack and checks equals 0, jump to branch if so

        code.append("iconst_0\n").append("goto ").append("END_IF_").append(counter).append("\n").
                append("NOT_BRANCH_" + counter).append(":\n").
                append("iconst_1\n").append("END_IF_").append(counter).append(":\n");

        // ifeq consumes 1 from stack, NOT result is pushed to the stack, therefore no overall change
        return code.toString();
    }

    private String getCode(GotoInstruction gotoInstruction) {
        StringBuilder code = new StringBuilder();
        code.append("goto " + gotoInstruction.getLabel()).append("\n");
        return code.toString();

    }

    private String getCode(CondBranchInstruction condBranchInstruction) {
        StringBuilder code = new StringBuilder();
        code.append(this.loadElement(condBranchInstruction.getOperands().get(0)))
                .append("ifne " + condBranchInstruction.getLabel()).append("\n");
        this.decrementCurrentStackLimit(); // ifne consumes 1 from stack
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

        this.decrementCurrentStackLimit(2); //putfield will consume 2 values from stack
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
        String code = "";

        switch (returnInstruction.getOperand().getType().getTypeOfElement()){
            case VOID:
                return "return\n";
            case INT32, BOOLEAN:
                code = loadElement(returnInstruction.getOperand()) + "ireturn\n";
                this.decrementCurrentStackLimit();
                return code;
            case ARRAYREF, OBJECTREF:
                code = loadElement(returnInstruction.getOperand()) + "areturn\n";
                this.decrementCurrentStackLimit();
                return code;
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

            code.append(loadElement(binaryOpInstruction.getLeftOperand()));
            code.append(ifqeCondition);
            this.decrementCurrentStackLimit(); // ifeq consumes 1 value on stack

            code.append(loadElement(binaryOpInstruction.getRightOperand()));
            code.append(ifqeCondition);
            this.decrementCurrentStackLimit();

            code.append("iconst_1\n")
                    .append("goto ").append("END_IF_").append(counter).append("\n").
                    append("AND_BRANCH_" + counter).append(":\n").
                    append("iconst_0\n").append("END_IF_").append(counter).append(":\n");
            this.incrementCurrentStackLimit();  // The result of AND is pushed to the stack

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
            this.decrementCurrentStackLimit();  // if_icmplt pops 2, and true/false is loaded to the stack
                                                // note: loadElement is done separately
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
        this.decrementCurrentStackLimit(); // 2 were pop from stack due to binop, the result is pushed into the stack
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
            this.incrementCurrentStackLimit();
            code.append(loadElement(arrayOperand.getIndexOperands().get(0)));
        }

        code.append(getInstructionCode(assignInstruction.getRhs(), parentMethod)); // append the right hand side instruction code first


        if(!(operand.getType().getTypeOfElement().equals(ElementType.OBJECTREF)
                && assignInstruction.getRhs() instanceof CallInstruction)) {
            code.append(storeValueIntoVariable(operand));
        }

        return code.toString();
    }


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
        Element element = callInstruction.getFirstArg();

        switch (element.getType().getTypeOfElement()){
            case OBJECTREF:
                String objName = getFullyQualifiedName( ((Operand)element).getName());
                code.append("new ").append(objName).append("\n");
                code.append("dup\n");
                this.incrementCurrentStackLimit(2); // new will load 1 value to stack, and dup will duplicate (hence load 1 to stack)
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
            this.decrementCurrentStackLimit();
        }
        this.decrementCurrentStackLimit(); // Since invoke virtual is instanciated, objectRef is also consumed from stack during call
        code.append(")");

        code.append(getJasminType(callInstruction.getReturnType()));
        if(callInstruction.getReturnType().getTypeOfElement() != ElementType.VOID){
            this.incrementCurrentStackLimit();
        }
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
            this.decrementCurrentStackLimit();
        }

        code.append(")");

        code.append(getJasminType(callInstruction.getReturnType()));
        if(callInstruction.getReturnType().getTypeOfElement() != ElementType.VOID){
            this.incrementCurrentStackLimit();
        }
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
            this.decrementCurrentStackLimit();
        }
        this.decrementCurrentStackLimit();

        code.append(")");

        code.append(getJasminType(callInstruction.getReturnType()));
        if(callInstruction.getReturnType().getTypeOfElement() != ElementType.VOID){
            this.incrementCurrentStackLimit();
        }
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
        if(element instanceof ArrayOperand){
            StringBuilder code = new StringBuilder();
            ArrayOperand operand = (ArrayOperand) element;

            code.append("aload").append(getVirtualRegister(operand.getName())).append("\n");
            this.incrementCurrentStackLimit();

            code.append(loadElement(operand.getIndexOperands().get(0)));

            this.decrementCurrentStackLimit();
            return code + "iaload\n";
        }
        if(element.isLiteral()){
            String literalString = ((LiteralElement) element).getLiteral();
            this.incrementCurrentStackLimit();
            return loadIntToStack(literalString);
        }
        if(element instanceof Operand){
            Operand operand = (Operand) element;
            switch(operand.getType().getTypeOfElement()){
                case THIS:
                    this.incrementCurrentStackLimit();
                    return "aload_0\n";
                case INT32, BOOLEAN:
                    this.incrementCurrentStackLimit();
                    return "iload"+ getVirtualRegister(operand.getName()) + "\n";
                case OBJECTREF, ARRAYREF:
                    this.incrementCurrentStackLimit();
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
            this.decrementCurrentStackLimit(3);
            return "iastore\n";
        }

        switch(operand.getType().getTypeOfElement()){
            case INT32, BOOLEAN:
                this.decrementCurrentStackLimit();
                return "istore" + getVirtualRegister(operand.getName()) + "\n";
            case OBJECTREF, ARRAYREF:
                this.decrementCurrentStackLimit();
                return "astore" + getVirtualRegister(operand.getName())+ "\n";
            default:
                throw new NotImplementedException(operand.getType().getTypeOfElement());
        }
    }

    public void incrementCurrentStackLimit(int n) {
        this.currstackLimit += n;
        if(this.currstackLimit > this.stackLimit){
            this.stackLimit = this.currstackLimit;
        }
    }
    public void incrementCurrentStackLimit() {
        this.incrementCurrentStackLimit(1);
    }

    public void decrementCurrentStackLimit(int n) {
        this.currstackLimit -= n;
    }
    public void decrementCurrentStackLimit() {
        this.decrementCurrentStackLimit(1);
    }
}
