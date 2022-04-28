package pt.up.fe.comp; 


import java.util.*;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

public class JmmJasminBackend implements JasminBackend{

    // todo
    // Converts the OLLIR to Jasmin Bytecodes with optimizations performed at the AST level and at the OLLIR
    // level.<br>
    // Note that this step also for Checkpoint 2 (CP2), but only for code structures defined in the project description
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        List<Report> reports = new ArrayList<Report>();
        Map<String, String> config = new HashMap<>();

        return new JasminResult("className", "jasminCode", reports, config);
    }
}
