
package main;

import automata.*;
import identification.*;
import cnf.*;
import tracemanager.*;

import java.io.*;
import java.util.*;


public class Main {

	public static void main(String args[]) {

        // Build a tree and fill it with traces
        File fileDir = new File(args[0]);
        APTA<String> apta = TraceManager.parseTracesFiles(fileDir);

        // OPTIONAL: draw APTA
        LatexSaver.saveLatexFile(apta, new File("latex/apta.tex"), 1);

        // Build graph

        // Find initial number of colors
        int numberOfColors = 1;

        // Try with increasing number of colors
        for (int i = numberOfColors; i < 100; i++) {

            // Encode tree in a formula
            ProblemEncoding pe = new ProblemEncoding(apta, i);
            pe.generateClauses();
            pe.generateRedundantClauses();
            Formula encoding = pe.getEncoding();
    
            // Extract solution with SAT
            List<EncodingVariable> solution = Solver.extractSolution(encoding);
    
            if (solution != null) {
                // Create the final DFA
                DFA<String> dfa = Solver.extractNewDFA(solution);
        
                // OPTIONAL: draw final DFA
                LatexSaver.saveLatexFile(dfa, new File("latex/extractedDFA.tex"), 1);

                break;
            }
        }
	}
}