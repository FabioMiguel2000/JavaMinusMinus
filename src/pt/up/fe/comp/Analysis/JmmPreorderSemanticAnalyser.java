package pt.up.fe.comp.Analysis;

import java.util.List;
import java.util.ArrayList;

import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportsProvider;


public abstract class JmmPreorderSemanticAnalyser extends PreorderJmmVisitor<Integer, Integer>
        implements ReportsProvider {

    private final List<Report> reports;

    public JmmPreorderSemanticAnalyser() {
        reports = new ArrayList<>();
    }

    @Override
    public List<Report> getReports() {
        return reports;
    }

    protected void addReport(Report report) {
        reports.add(report);
    }
}
