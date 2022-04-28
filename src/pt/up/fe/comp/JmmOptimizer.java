package pt.up.fe.comp; 


import java.util.Collections;
import pt.up.fe.comp.jmm.ollir.*;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult; 



public class JmmOptimizer implements JmmOptimization {

    /**
     * Step 1 (for CP3): optimize code at the AST level
     *
     * @param semanticsResult
     * @return
     */
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return JmmOptimization.super.optimize(semanticsResult);
    }

    /**
     * Step 2 (for CP2): convert the AST to the OLLIR format
     *
     * @param semanticsResult
     * @return
     */
    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        return null;
    }

    /**
     * Step 3 (for CP3): optimize code at the OLLIR level
     *
     * @param ollirResult
     * @return
     */
    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return JmmOptimization.super.optimize(ollirResult);
    }
}
