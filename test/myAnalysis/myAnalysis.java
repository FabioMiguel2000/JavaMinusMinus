package myAnalysis;

import org.junit.Test;
import pt.up.fe.comp.Analysis.Analysers.*;
import pt.up.fe.comp.Analysis.JmmSymbolTableBuilder;
import pt.up.fe.comp.Analysis.JmmSymbolTableFiller;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportsProvider;
import pt.up.fe.specs.util.SpecsIo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class myAnalysis {
    enum test {
        ArrayAccessIsDoneOverArray,
        ArrayIndexIsTypeInteger,
        ArrayInArithmeticOperation,
        VarIsNotDeclared,
        OperationType,
        ConditionExpressionMustBeBool
    }

    class myJmmAnalyser {

        ReportsProvider reports;
        JmmSymbolTableBuilder symbolTable;
        JmmParserResult parserResult;

        myJmmAnalyser(String locationFile) {
            var testLocation = "myAnalysis/" + locationFile;
            parserResult = TestUtils.parse(SpecsIo.getResource(testLocation));
            reports = null;
            symbolTable = new JmmSymbolTableBuilder();
            var symbolTableFiller = new JmmSymbolTableFiller();
            symbolTableFiller.visit(this.parserResult.getRootNode(), symbolTable);
            var rep = symbolTableFiller.getReports();
        }

        ReportsProvider playTest(test type) {
            switch (type) {
                case ArrayAccessIsDoneOverArray -> {
                    return new ArrayAccessIsDoneOverArray(symbolTable, parserResult.getRootNode());
                }
                case ArrayIndexIsTypeInteger -> {
                    return new ArrayIndexIsTypeInteger(symbolTable, parserResult.getRootNode());
                }
                case ArrayInArithmeticOperation -> {
                    return new ArrayInArithmeticOperation(symbolTable, parserResult.getRootNode());
                }
                case VarIsNotDeclared -> {
                    return new VarIsNotDeclared(symbolTable, parserResult.getRootNode());
                }
                case OperationType -> {
                    return new OperationType(symbolTable, parserResult.getRootNode());
                }
                case ConditionExpressionMustBeBool -> {
                    return new ConditionExpressionMustBeBool(symbolTable, parserResult.getRootNode());
                }
                default -> { return null; }
            }
        }

        void resetReports() {
            reports = null;
        }

        void run_noErrors(test type) {
            System.out.println(parserResult.getRootNode().toTree());
            System.out.println("SymbolTable: \n" + symbolTable.print());
            TestUtils.noErrors(this.playTest(type));
        }

        void run_mustFail(test type) {
            System.out.println(parserResult.getRootNode().toTree());
            System.out.println("SymbolTable: \n" + symbolTable.print());
            TestUtils.mustFail(this.playTest(type));
        }
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
    //@Test
    //public void fail_test1() { test_mustFail("FindMaximum2.jmm"); }

    //@Test
    //public void pass_test1() { test_noErrors("FindMaximum.jmm"); }


    myJmmAnalyser FindMaximum = new myJmmAnalyser("FindMaximum.jmm");
    @Test
    public void test_01_01_FindMaximum() { FindMaximum.run_noErrors(test.ArrayIndexIsTypeInteger); }
    @Test
    public void test_01_02_FindMaximum() { FindMaximum.run_mustFail(test.ArrayAccessIsDoneOverArray); }
}
