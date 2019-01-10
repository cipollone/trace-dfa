
import automata.*;
import identification.*;
import cnf.*;
import util.*;

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

		// Draw the DFA in Latex
		dfa.useLegend(true);
		LatexSaver.saveLatexFile(dfa, new File("output/dfa.tex"), 2);

		// Save in .dot file
		dfa.saveDotFile(new File("output/dfa.dot"));

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

		// Draw the APTA in Latex
		LatexSaver.saveLatexFile(apta, new File("output/apta.tex"), 1);

		// Build constraints graph
		ConstraintsGraph cg = new ConstraintsGraph(apta);

		// Draw the constraints graph in Latex
		LatexSaver.saveLatexFile(cg, new File("output/constraints.tex"), 1);

		// Build graph
		DFA<String> dfa = null;

		// Find initial number of colors (clique)
		Set<ConstraintsGraph.CNode> clique = cg.getClique();
		int numberOfColors = clique.size();
		int maxColors = 100; // made up

		System.out.println("Initial number of states: " + numberOfColors);

		// Try with increasing number of colors
		for (; numberOfColors < maxColors; numberOfColors++) {

			// Encode the coloring problem
			//   NOTE: the blocked clauses are not used because they slow down this solver
			ProblemEncoding pe = new ProblemEncoding(apta, cg, clique, numberOfColors);
			pe.generateClauses();
			//pe.generateRedundantClauses();
			Formula encoding = pe.getEncoding();

			// Extract solution with SAT
			List<EncodingVariable> solution = Solver.extractSolution(encoding);

			// Solution found
			if (solution != null) {
				System.out.println("Satisfiable with " + numberOfColors + " states");
				dfa = Solver.extractNewDFA(solution);
				return dfa;
			} else {
				System.out.println("Unsatisfiable with " + numberOfColors + " states");
			}
		}

		throw new RuntimeException("Reached limit of states: unsatisfiable with " +
				maxColors);
	}


	/**
	 * Test the DFA.
	 * A single test succed if DFA is consistent with an APTA built on the test
	 * traces.
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
					result = model.parseSequence(trace, true);
				} else {              // Compare others
					if (result != model.parseSequence(trace, true)) {
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
