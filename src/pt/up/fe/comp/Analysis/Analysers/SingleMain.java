package pt.up.fe.comp.Analysis.Analysers;

import pt.up.fe.comp.Analysis.SemanticAnalyser;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class SingleMain implements SemanticAnalyser {

    private final SymbolTable symbolTable;

    public SingleMain(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public List<Report> getReports() {
        if(!symbolTable.getMethods().contains("main")){
            return Arrays.asList(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Class '" + symbolTable.getClassName() + "' does not contain main method"));
        }

        return Collections.emptyList();
    }
}
