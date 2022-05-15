import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;

import java.util.Collections;

public class JasminTest {
    @Test
    public void test(){
        var jasminResult = TestUtils.backend(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        TestUtils.noErrors(jasminResult);

//        result.compile();
        System.out.println("Jasmin Result:\n");
        String result = jasminResult.run();
    }
    @Test
    public void OllirToJasminArithmetics(){
        var ollirResult = new OllirResult(
                SpecsIo.getResource("fixtures/public/cp2/OllirToJasminArithmetics.ollir"),
                Collections.emptyMap());
        var jasminResult = TestUtils
                .backend(ollirResult);
        TestUtils.noErrors(jasminResult);

        jasminResult.compile();
//        System.out.println("Jasmin Result:\n");
//        String result = jasminResult.run();
    }

}
