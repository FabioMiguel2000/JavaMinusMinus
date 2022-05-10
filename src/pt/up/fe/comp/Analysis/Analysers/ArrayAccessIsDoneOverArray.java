package pt.up.fe.comp.Analysis.Analysers;

import pt.up.fe.comp.Analysis.SemanticAnalyser;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArrayAccessIsDoneOverArray implements SemanticAnalyser {

    private final SymbolTable symbolTable;

    public ArrayAccessIsDoneOverArray(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public List<Report> getReports() {
        // WIP

        /*
        if(!symbolTable.getMethods().contains("find_maximum")){
            return Arrays.asList(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1,
                    "Var access must be done over array"));
        }
        */
        return Collections.emptyList();
    }
}
