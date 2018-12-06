
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
        List<List<String>> traces = TraceManager.getTracesFiles(fileDir);

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
            // pe.generateRedundantClauses();
            Formula encoding = pe.getEncoding();
    
            // Extract solution with SAT
            List<EncodingVariable> solution = Solver.extractSolution(encoding);
    
            if (solution != null) {
                // Create the final DFA
                DFA<String> dfa = Solver.extractNewDFA(solution);

                System.out.println("Consistent: " + compareOnTraces(traces,apta,dfa));
        
                // OPTIONAL: draw final DFA
                LatexSaver.saveLatexFile(dfa, new File("latex/extractedDFA.tex"), 2);

                break;
            }
        }
	}

	/**
	 * Test all models on a set of traces.
	 * All automata in models must parse with the same result all traces.
	 * @param traces A list of test sequences
	 * @param models The models to compare
	 * @return true if all results are consistent, false otherwise.
	 */
	@SafeVarargs
	public static <LabelT> boolean compareOnTraces(List<List<LabelT>> traces,
			Automaton<LabelT>... models) { 

		// For each sequence
		for (List<LabelT> trace: traces) {

			// For each model
			Boolean result = null;
			for (Automaton<LabelT> model: models) {
				if (result == null) { // Save first
					result = model.parseSequence(trace);
				} else {              // Compare others
					if (result != model.parseSequence(trace)) {
						return false;
					}
				}
			}
		}

		return true;
	}
}