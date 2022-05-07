package pt.up.fe.comp.Jasmin;

import org.specs.comp.ollir.ClassUnit;

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

        code.append(".class public ").append(classUnit.getClass()).append("\n");
        code.append(".super ").append(classUnit.getSuperClass()).append("\n");

        System.out.println(getFullyQualifiedName(classUnit.getSuperClass()));

        return code.toString();
    }



}
