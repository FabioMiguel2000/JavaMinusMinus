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
    @Test
    public void test1(){
        var result = TestUtils.parse(SpecsIo.getResource("myOllirJmm/test1.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test1.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test2(){
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test2.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test3(){
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test3.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test4(){
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test4.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test5(){
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test5.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test6(){
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test6.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test7(){
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test7.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test8(){
        var result = TestUtils.parse(SpecsIo.getResource("myOllirJmm/test8.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test8.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test9(){
        var result = TestUtils.parse(SpecsIo.getResource("myOllirJmm/test9.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test9.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test10(){
        var result = TestUtils.parse(SpecsIo.getResource("myOllirJmm/test10.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test10.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test11(){
        var result = TestUtils.parse(SpecsIo.getResource("myOllirJmm/test11.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test11.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test12(){
        var result = TestUtils.parse(SpecsIo.getResource("myOllirJmm/test12.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test12.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test13(){
        var result = TestUtils.parse(SpecsIo.getResource("myOllirJmm/test13.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test13.jmm"));
        TestUtils.noErrors(ollirResult);
    }
}
