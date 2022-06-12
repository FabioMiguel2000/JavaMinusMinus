package pt.up.fe.comp.Analysis;


import pt.up.fe.comp.Analysis.Analysers.*;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportsProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis { 
    @Override 
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        List<Report> reports = new ArrayList<>();

        var symbolTable = new JmmSymbolTableBuilder();

        var symbolTableFiller = new JmmSymbolTableFiller();

        symbolTableFiller.visit(parserResult.getRootNode(), symbolTable); // Fills the information for symbolTable

        reports.addAll(symbolTableFiller.getReports());

        List<ReportsProvider> analysers = Arrays.asList(
                new ArrayAccessIsDoneOverArray(symbolTable, parserResult.getRootNode()),
                new ArrayIndexIsTypeInteger(symbolTable, parserResult.getRootNode()),
                new ArrayInArithmeticOperation(symbolTable, parserResult.getRootNode()),
                new VarIsNotDeclared(symbolTable, parserResult.getRootNode()),
                new OperationType(symbolTable, parserResult.getRootNode()),
                new ConditionExpressionMustBeBool(symbolTable, parserResult.getRootNode()),
                new AssignType(symbolTable, parserResult.getRootNode()),
                new MethodCallEqualsMethodDeclaration(symbolTable, parserResult.getRootNode()),
                new ReturnMatchesType(symbolTable, parserResult.getRootNode())
                );

//        for(var analyser : analysers){
//            reports.addAll(analyser.getReports());
//        }



        return new JmmSemanticsResult(parserResult, symbolTable, reports);

    }
}