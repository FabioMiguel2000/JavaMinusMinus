package pt.up.fe.comp.Ollir;


import java.util.Collections;
import pt.up.fe.comp.jmm.ollir.*;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult; 



public class JmmOptimizer implements JmmOptimization {
    /**
     * Step 2 (for CP2): convert the AST to the OLLIR format
     *
     * @param semanticsResult
     * @return
     */
    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        var ollirGenerator = new OllirGenerator(semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());

        var ollirCode = ollirGenerator.getCode();



//        ollirCode = "import ioPlus;\n" +
//                "import BoardBase;\n" +
//                "import java.io.File;\n" +
//                "public HelloWorld extends BoardBase {\n" +
//                ".method public static main(args.array.String).V {\n" +
//                "invokestatic(ioPlus, \"print\").V;\n" +
//                "b.Method :=.Method new(Method).Method;\n" +
//                "b.Method :=.Method invokestatic(ioPlus, \"print\").Method;\n" +
//                "temp_0.i32 :=.i32 invokevirtual(b.Method, \"add\", 1.i32, 2.i32).i32;\n"+
//                "temp_1.i32 :=.i32 temp_0.i32 +.i32 1.i32;\n" +
//                "a.i32 :=.i32 temp_0.i32;\n" +
//                "ret.V;\n" +
//                "}\n" +
//                " }";

        System.out.println("OLLIR CODE: \n" + ollirCode);



        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }

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
