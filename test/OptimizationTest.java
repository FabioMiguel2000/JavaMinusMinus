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
    @Test
    public void test14(){
        var result = TestUtils.parse(SpecsIo.getResource("myOllirJmm/test14.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test14.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void OllirArithmetic(){
        var result = TestUtils.parse(SpecsIo.getResource("fixtures/public/cp2/OllirArithmetic.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/cp2/OllirArithmetic.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void OllirAssignment(){
        var result = TestUtils.parse(SpecsIo.getResource("fixtures/public/cp2/OllirAssignment.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/cp2/OllirAssignment.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void OllirBasic(){
        var result = TestUtils.parse(SpecsIo.getResource("fixtures/public/cp2/OllirBasic.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/cp2/OllirBasic.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void OllirMethodInvocation(){
        var result = TestUtils.parse(SpecsIo.getResource("fixtures/public/cp2/OllirMethodInvocation.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/cp2/OllirMethodInvocation.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test15(){
        var result = TestUtils.parse(SpecsIo.getResource("myOllirJmm/test15.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test15.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void test16(){
        var result = TestUtils.parse(SpecsIo.getResource("myOllirJmm/test16.jmm"));
        System.out.println(result.getRootNode().toTree());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("myOllirJmm/test16.jmm"));
        TestUtils.noErrors(ollirResult);
    }
    @Test
    public void cpfTempTest(){
        var result = TestUtils.parse(SpecsIo.getResource("fixtures/public/cpf/4_jasmin/calls/UsesPop.jmm"));
        System.out.println(result.getRootNode().toTree());
        var result2 = TestUtils.analyse(SpecsIo.getResource("fixtures/public/cpf/4_jasmin/calls/UsesPop.jmm")); // Calls JmmAnalyser semanticAnalysis()
        System.out.println("SymbolTable: \n" + result2.getSymbolTable().print());
        var ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/public/cpf/4_jasmin/calls/UsesPop.jmm"));
        TestUtils.noErrors(ollirResult);
    }
}
