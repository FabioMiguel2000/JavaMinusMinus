package pt.up.fe.comp.Analysis;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
        METHOD_DECL = "MethodDeclaration";
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
        // For methodDeclaration 'name' and 'isStatic' is a node attribute, and 'Type' is a
        // todo
        String methodName = methodDecl.get("name");

        if(symbolTable.hasMethod(methodName)){  // Check whether already exists
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.valueOf(methodDecl.get("line")), Integer.valueOf(methodDecl.get("col")), "Found duplicated method with signature '" + methodName + "'"));
        }

        var returnType = methodDecl.getJmmChild(0);
        var typeName = returnType.get("name");
        boolean isArray = Objects.equals(returnType.get("isArray"), "true");

        //var params = methodDecl.getChildren().subList(2, methodDecl.getNumChildren()-1);

        var params =  methodDecl.getChildren().subList(1, methodDecl.getNumChildren()).stream()
                        .filter(node->node.getKind().equals("Parameter"))
                        .collect(Collectors.toList());


        System.out.println("PARAMS: "+ params);
        symbolTable.addMethod(methodName, new Type(typeName, isArray), Collections.emptyList());



        return 0;
    }
}
