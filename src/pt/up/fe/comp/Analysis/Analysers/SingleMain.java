package pt.up.fe.comp.Analysis.Analysers;

import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.Analysis.JmmPreorderSemanticAnalyser;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class SingleMain extends JmmPreorderSemanticAnalyser {

    @Override
    public List<Report> getReports() {

        //return Arrays.asList(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "random error"));
        //return Collections.emptyList();
        return Collections.emptyList();
    }
}
