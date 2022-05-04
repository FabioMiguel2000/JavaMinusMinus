package pt.up.fe.comp.Analysis;


import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis { 
    @Override 
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        List<Report> reports = new ArrayList<>();

        var symbolTable = new JmmSymbolTableBuilder();

        var symbolTableFiller = new JmmSymbolTableFiller();
        symbolTableFiller.visit(parserResult.getRootNode(), symbolTable);
        reports.addAll(symbolTableFiller.getReports());


        return new JmmSemanticsResult(parserResult, symbolTable, reports);

    }
}