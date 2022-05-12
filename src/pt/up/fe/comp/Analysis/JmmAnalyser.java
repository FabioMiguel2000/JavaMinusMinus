package pt.up.fe.comp.Analysis;


import pt.up.fe.comp.Analysis.Analysers.ArrayAccessIsDoneOverArray;
import pt.up.fe.comp.Analysis.Analysers.ArrayIndexIsTypeInteger;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.Analysis.Analysers.SingleMain;

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

        List<SemanticAnalyser> analysers = Arrays.asList(
                new SingleMain(symbolTable),
                new ArrayAccessIsDoneOverArray(symbolTable, parserResult.getRootNode()),
                new ArrayIndexIsTypeInteger(symbolTable, parserResult.getRootNode())
        );

        for(var analyser : analysers){
            reports.addAll(analyser.getReports());
        }



        return new JmmSemanticsResult(parserResult, symbolTable, reports);

    }
}