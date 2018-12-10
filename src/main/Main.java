
package main;

import automata.*;
import identification.*;
import cnf.*;
import tracemanager.*;

import java.io.*;
import java.util.*;


public class Main {

	public static void main(String args[]) {

		// Check
		if (args.length != 2) {
			throw new IllegalArgumentException(
					"Need two directories: train .xes files and test .xes files");
		}

		// Arguments
		File trainTracesDir = new File(args[0]);
		File testTracesDir = new File(args[1]);

		// Learning
		DFA<String> dfa = learnDFA(trainTracesDir);

		// OPTIONAL: draw final DFA
		LatexSaver.saveLatexFile(dfa, new File("latex/dfa.tex"), 2);

		// Testing
		float result = testDFA(dfa, testTracesDir);
		System.out.println("Consistent in " + (result*100) + "% of traces.");
}

		
	/**
	 * Launch the algorithm on traces and return the DFA.
	 * @param trainTracesDir Directory of .xes files to use for learning.
	 * @return The DFA extracted.
	 */
	public static DFA<String> learnDFA(File trainTracesDir) {

		// Check
		if (!trainTracesDir.isDirectory()) {
			throw new IllegalArgumentException(trainTracesDir + "is not a directory");
		}

		// Build a tree and fill it with traces
		APTA<String> apta = TraceManager.parseTracesFiles(trainTracesDir);

		// OPTIONAL: draw APTA
		LatexSaver.saveLatexFile(apta, new File("latex/apta.tex"), 1);

        // Build constraints graph
        ConstraintsGraph cg = new ConstraintsGraph(apta);

		// Build graph
		DFA<String> dfa = null;

		// Find initial number of colors (clique)
		int numberOfColors = cg.getClique().size();
		int maxColors = 100; // made up

		// Try with increasing number of colors
		for (int i = numberOfColors; i < maxColors; i++) {

				// Encode tree in a formula
				ProblemEncoding pe = new ProblemEncoding(apta, cg, i);
				pe.generateClauses();
				//pe.generateRedundantClauses();
				Formula encoding = pe.getEncoding();

				// Extract solution with SAT
				List<EncodingVariable> solution = Solver.extractSolution(encoding);

				// Solution found
				if (solution != null) {

						dfa = Solver.extractNewDFA(solution);
						return dfa;
				}
		}

		throw new RuntimeException("Reached limit of states: unsatisfiable with " +
				maxColors);
	}


	/**
	 * Test the DFA. A single test succed if dfa is consistent with an APTA build
	 * on the test trace.
	 * @param dfa A dfa to test with traces
	 * @param testTracesDir Directory of .xes files to use for testing.
	 * @return The fraction of tests passed.
	 */
	public static float testDFA(DFA<String> dfa, File testTracesDir) {

		// Check
		if (!testTracesDir.isDirectory()) {
			throw new IllegalArgumentException(testTracesDir + "is not a directory");
		}

		// Build a tree and fill it with traces
		APTA<String> apta = TraceManager.parseTracesFiles(testTracesDir);
		List<List<String>> testTraces = TraceManager.getTracesFiles(testTracesDir);

		return compareOnTraces(testTraces, apta, dfa);
	}

	/**
	 * Test all models on a set of traces.
	 * All automata in models must parse with the same result all traces.
	 * @param traces A list of test sequences
	 * @param models The models to compare
	 * @return true if all results are consistent, false otherwise.
	 */
	@SafeVarargs
	public static <LabelT> boolean testOnTraces(List<List<LabelT>> traces,
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
				//System.out.print(" " + result);
			}
			//System.out.print(" " + trace + "\n");
		}

		return true;
	}


	/**
	 * Test all models on a set of traces.
	 * Returns the fraction of traces for which the automata return the same
	 * result.
	 * @param traces A list of test sequences
	 * @param models The models to compare
	 * @return The fraction of equal results
	 */
	@SafeVarargs
	public static <LabelT> float compareOnTraces(List<List<LabelT>> traces,
			Automaton<LabelT>... models) { 

		// Counters
		int numberOfTests = traces.size();
		int numberOfPositiveTests = 0;

		// For each sequence
		for (List<LabelT> trace: traces) {

			// For each model
			Boolean result = null;
			boolean testPassed = true;
			for (Automaton<LabelT> model: models) {
				if (result == null) { // Save first
					result = model.parseSequence(trace);
				} else {              // Compare others
					if (result != model.parseSequence(trace)) {
						testPassed = false;
						break; // Test failed
					}
				}
			}
			if (testPassed) {
				++numberOfPositiveTests;
			}
		}

		return numberOfPositiveTests/(float)numberOfTests;
	}
}
