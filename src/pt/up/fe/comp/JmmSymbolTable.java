package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.List;


public class JmmSymbolTable implements SymbolTable{
    
    /**
     * @return a list of fully qualified names of imports
     */
    @Override
    public List<String> getImports(){
        List<String> list = new ArrayList<String>();

        //todo

        return list;
    }

    /**
     * @return the name of the main class
     */
    @Override
    public String getClassName(){
        String className = "";

        //todo

        return className;
    }

    /**
     * 
     * @return the name that the classes extends, or null if the class does not extend another class
     */
    @Override
    public String getSuper(){
        String name = "";

        //todo


        return name;
    }

    /**
     * 
     * @return a list of Symbols that represent the fields of the class
     */
    @Override
    public List<Symbol> getFields(){
        List<Symbol> symbolList = new ArrayList<Symbol>();

        //todo


        return symbolList;
    }

    /**
     * 
     * @return a list with the methods signatures of the class
     */
    @Override
    public List<String> getMethods(){
        List<String> methodList = new ArrayList<String>();

        //todo
        return methodList;
    }

    /**
     * 
     * @return the return type of the given method
     */
    @Override
    public Type getReturnType(String methodSignature){

        //todo


        return new Type("name", true);
    }

    /**
     * 
     * @param methodSignature
     * @return a list of parameters of the given method
     */
    @Override
    public List<Symbol> getParameters(String methodSignature){
        List<Symbol> parametersList = new ArrayList<Symbol>();

        // todo

        return parametersList;
    }

    /**
     * 
     * @param methodSignature
     * @return a list of local variables declared in the given method
     */
    @Override
    public List<Symbol> getLocalVariables(String methodSignature){
        List<Symbol> localVariablesList = new ArrayList<Symbol>();

        return localVariablesList;
    }


    
}
