import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class AnalysisTest {
    @Test
    public void test(){
        var result = TestUtils.parse(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
        System.out.println(result.getRootNode().toTree());
        var result2 = TestUtils.analyse(SpecsIo.getResource("fixtures/public/FindMaximum.jmm")); // Calls JmmAnalyser semanticAnalysis()
        System.out.println("SymbolTable: \n" + result2.getSymbolTable().print());
        TestUtils.noErrors(result2);
    }

    public void test_noErrors(String testLocationFile) {
        var testLocation = "myAnalysis/" + testLocationFile;
        var result = TestUtils.parse(SpecsIo.getResource(testLocation));
        System.out.println(result.getRootNode().toTree());
        var result2 = TestUtils.analyse(SpecsIo.getResource(testLocation)); // Calls JmmAnalyser semanticAnalysis()
        System.out.println("SymbolTable: \n" + result2.getSymbolTable().print());
        TestUtils.noErrors(result2);
    }

    public void test_mustFail(String testLocationFile) {
        var testLocation = "myAnalysis/" + testLocationFile;
        var result = TestUtils.parse(SpecsIo.getResource(testLocation));
        System.out.println(result.getRootNode().toTree());
        var result2 = TestUtils.analyse(SpecsIo.getResource(testLocation)); // Calls JmmAnalyser semanticAnalysis()
        System.out.println("SymbolTable: \n" + result2.getSymbolTable().print());
        TestUtils.mustFail(result2);
    }

    // Testes located inside myAnalysis folder
    @Test
    public void fail_test1() { test_mustFail("FindMaximum2.jmm"); }

    @Test
    public void pass_test1() { test_noErrors("FindMaximum.jmm"); }
}
