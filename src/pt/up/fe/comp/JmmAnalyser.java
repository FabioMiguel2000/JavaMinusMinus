package pt.up.fe.comp; 


import java.util.Collections; 
import pt.up.fe.comp.jmm.analysis.JmmAnalysis; 
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult; 
import pt.up.fe.comp.jmm.analysis.table.SymbolTable; 
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.JmmSymbolTable;

public class JmmAnalyser implements JmmAnalysis { 
    @Override 
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) { 
        JmmSymbolTable symbolTable = null; 

        // todo -> symbolTable
        return new JmmSemanticsResult(parserResult, symbolTable, Collections.emptyList()); 

    }
}