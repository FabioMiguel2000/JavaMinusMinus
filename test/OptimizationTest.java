import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class OptimizationTest {
    @Test
    public void test(){
        var result = TestUtils.parse(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        System.out.println(result.getRootNode().toTree());
        var result2 = TestUtils.analyse(SpecsIo.getResource("fixtures/public/HelloWorld.jmm")); // Calls JmmAnalyser semanticAnalysis()
        System.out.println("SymbolTable: \n" + result2.getSymbolTable().print());
        TestUtils.noErrors(result2);

        var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        TestUtils.noErrors(ollirResult);
    }
}
