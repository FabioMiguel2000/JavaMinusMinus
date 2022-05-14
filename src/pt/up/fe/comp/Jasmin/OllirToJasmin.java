package pt.up.fe.comp.Jasmin;

import org.specs.comp.ollir.*;

import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.stream.Collectors;

public class OllirToJasmin {
    private final ClassUnit classUnit;
    public OllirToJasmin(ClassUnit classUnit){
        this.classUnit = classUnit;

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
        throw new RuntimeException("Could not find import for class " + className);
    }

    public String getCode(){
        var code = new StringBuilder();

        code.append(".class public ").append(classUnit.getClassName()).append("\n");

        var superClassQualifiedName = getFullyQualifiedName(classUnit.getSuperClass());

        code.append(".super ").append(classUnit.getSuperClass()).append("\n");

        code.append(SpecsIo.getResource("templates/JasminConstructor.template").replace("${SUPER_NAME}", superClassQualifiedName)).append("\n");
//        System.out.println(getFullyQualifiedName(classUnit.getSuperClass()));

        // TODO: Fields  using classUnit.getFields()

        for (var method: classUnit.getMethods()){
            code.append(getCode(method));
        }


        return code.toString();
    }

    public String getCode(Method method){
        var code = new StringBuilder();

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

//        System.out.println("method.getParams() = " + method.get);
//        System.out.println("Param size=" + method.getParams().size());

        var methodParamTypes = method.getParams().stream()
                .map(element -> getJasminType(element.getType()))
                .collect(Collectors.joining());

        System.out.println("METHOD PARAMS: " + methodParamTypes);

        code.append(methodParamTypes).append(")").append(getJasminType(method.getReturnType())).append("\n");
        code.append(".limit stack 99\n");
        code.append(".limit locals 99\n");


        for(var instruction: method.getInstructions()){
            code.append(getCode(instruction));
        }
        code.append("return\n");
        code.append(".end method\n\n");
        return code.toString();

    }
    public String getCode(Instruction instruction){

        // return instructionMap.apply(instruction);
        if(instruction instanceof CallInstruction){
            return getCode((CallInstruction) instruction);
        }
        return "";
    }

    public String getCode(CallInstruction callInstruction){

        var code = new StringBuilder();

        switch (callInstruction.getInvocationType()){
            case invokestatic:
                return getCodeInvokeStatic(callInstruction);
            case invokevirtual:
                return getCodeInvokeVirtual(callInstruction);
            default:
                throw new NotImplementedException(callInstruction.getInvocationType());
        }

    }

    private String getCodeInvokeVirtual(CallInstruction callInstruction){
        var code = new StringBuilder();
//        code.append("invokevirtual ");

        throw new NotImplementedException(callInstruction);

//        return code.toString();

    }


    private String getCodeInvokeStatic(CallInstruction callInstruction){
        var code = new StringBuilder();

        code.append("invokestatic ");

        var methodClass = ((Operand) callInstruction.getFirstArg()).getName();

        code.append(getFullyQualifiedName(methodClass));
        code.append("/");
        var calledMethod = ((LiteralElement) callInstruction.getSecondArg()).getLiteral();
        code.append(calledMethod.substring(1, calledMethod.length()-1));
        code.append("(");

        for(var operand: callInstruction.getListOfOperands()){
            getArgumentCode(operand);
        }

        code.append(")");

        code.append(getJasminType(callInstruction.getReturnType()));

        code.append("\n");

//        var firstType = callInstruction.getFirstArg();
//
//        System.out.println("TYPE of Element: " + firstType.getType().getTypeOfElement());
//
//        System.out.println("CLASS NAME: " +  ((ClassType)firstType.getType()).getName());
//
//        System.out.println("SECOND ARG: " + callInstruction.getSecondArg());


        return code.toString();
    }


    private void getArgumentCode(Element operand){
        throw new NotImplementedException(this);
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


}
