package pt.up.fe.comp.Analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;

public class JmmSymbolTableBuilder implements SymbolTable {
    private final List<String> imports;
    private String className;
    private String superClass;
    private final List<String> methods;
    private final Map<String,Type> methodReturnTypes;
    private final List<Symbol> fields;
    private final Map<String, List<Symbol>> methodParams;

    public JmmSymbolTableBuilder() {
        this.imports = new ArrayList<>();
        this.className = null;
        this.superClass = null;
        this.methods = new ArrayList<>();
        this.methodReturnTypes = new HashMap<>();
        this.methodParams = new HashMap<>();
        this.fields = new ArrayList<>();
    }


    @Override
    public List<String> getImports() {
        return this.imports;
    }

    public void addImport(String importString){
        imports.add(importString);
    }
    @Override
    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className){
        this.className = className;
    }

    @Override
    public String getSuper() {
        return this.superClass;
    }

    public void setSuperClass(String superClass){
        this.superClass = superClass;
    }

    @Override
    public List<Symbol> getFields() {

        return this.fields;
    }

//    public void addField(){
//
//    }

    @Override
    public List<String> getMethods() {
        return this.methods;
    }

    public boolean hasMethod(String methodSignature){
        return methods.contains(methodSignature);
    }

    public void addMethod(String methodSignature, Type returnType, List<Symbol> params){
        this.methods.add(methodSignature);
        this.methodReturnTypes.put(methodSignature, returnType);
        this.methodParams.put(methodSignature, params);
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return this.methodReturnTypes.get(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return this.methodParams.get(methodSignature);
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return Collections.emptyList();
    }
}
