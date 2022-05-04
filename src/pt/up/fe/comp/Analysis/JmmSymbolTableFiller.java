package pt.up.fe.comp.Analysis;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JmmSymbolTableFiller extends PreorderJmmVisitor<JmmSymbolTableBuilder, Integer> {
    private List<Report> reports;

    private final String IMPORT_DECL;
    private final String CLASS_DECL;
    private final String PROGRAM;
    private final String METHOD_DECL;

    public JmmSymbolTableFiller(){
        this.reports = new ArrayList<>();
        IMPORT_DECL = "ImportDeclaration";
        CLASS_DECL = "ClassDeclaration";
        METHOD_DECL = "MethodTypes";
        PROGRAM = "Program";

        addVisit(IMPORT_DECL, this::importDeclVisit); //Every time IMPORT_DECL is seen it will call the `this::importDeclVisit` method
        addVisit(CLASS_DECL, this::classDeclVisit); //Every time CLASS_DECL is seen it will call the `this::ClassDeclVisit` method
        addVisit(METHOD_DECL, this::methodDeclVisit); //Every time METHOD_DECL is seen it will call the `this::methodDeclVisit` method


    }

    public List<Report> getReports() {
        return reports;
    }

    private Integer importDeclVisit(JmmNode importDecl, JmmSymbolTableBuilder symbolTable){
        var importString = importDecl.getChildren().stream()
                .map(id->id.get("name")).collect(Collectors.joining("."));    // for imports like import ioPlus.somthin.s;

//        System.out.println("IMPORT: " + importString);

        symbolTable.addImport(importString);

        return 0;
    }

    private Integer classDeclVisit(JmmNode classDecl, JmmSymbolTableBuilder symbolTable){
        String className = classDecl.get("name");

        classDecl.getOptional("extends")    //Checks whether this node has a super class
                .ifPresent(symbolTable::setSuperClass); //if yes ("extends" is not a null), the setsuperclass

        symbolTable.setClassName(className);


        return 0;
    }

    private Integer methodDeclVisit(JmmNode methodDecl, JmmSymbolTableBuilder symbolTable){
        // todo
        var Method = methodDecl;


        return 0;
    }
}
