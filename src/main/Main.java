
package main;

import java.io.File;
import java.util.*;

import identification.Solver;
import automata.Automaton;

// for tests
import automata.APTA;
import automata.LatexSaver;


public class Main {
	public static void main(String args[]) {

		// Testing compareOnTraces()
		// JUST TESTS BELOW

		// Build a tree
		APTA<Character> a1 = new APTA<Character>();
		APTA<Character> a2 = new APTA<Character>();
		APTA<Character> a3 = new APTA<Character>();

		// Sequences to add
		String[] stringsToAdd = { "ciao", "ciar", "ci", "ca", ""};
		boolean[] ok = { true, false, true, true, true };

		// Convert the sequences
		List<List<Character>> sequencesToAdd = new ArrayList<>();
		for (int i = 0; i < stringsToAdd.length; ++i) {
			List<Character> seq = new ArrayList<>();
			sequencesToAdd.add(seq);
			for (Character c: stringsToAdd[i].toCharArray()) {
				seq.add(c);
			}
		}

		// Strings to parse
		String[] stringsToParse = {"ciao", "c", "ca", "ciar", "cia", "cc", "d", ""};

		// Convert the sequences
		List<List<Character>> sequencesToParse = new ArrayList<>();
		for (int i = 0; i < stringsToParse.length; ++i) {
			List<Character> seq = new ArrayList<>();
			sequencesToParse.add(seq);
			for (Character c: stringsToParse[i].toCharArray()) {
				seq.add(c);
			}
		}

		// Test tree expansion
		for (int i = 0; i < sequencesToAdd.size(); ++i) {
			if (ok[i]) {
				a1.acceptSequence(sequencesToAdd.get(i));
				a2.acceptSequence(sequencesToAdd.get(i));
				a3.acceptSequence(sequencesToAdd.get(i));
			} else {
				a1.rejectSequence(sequencesToAdd.get(i));
				a2.rejectSequence(sequencesToAdd.get(i));
				a3.rejectSequence(sequencesToAdd.get(i));
			}
		}

		a3.rejectSequence(new ArrayList<Character>());

		System.out.println("Are equal: " +
				compareOnTraces(sequencesToParse, a1, a2, a3));

		// Show 
		LatexSaver.saveLatexFile(a1, new File("latex/a1.tex"), 1);
		LatexSaver.saveLatexFile(a2, new File("latex/a2.tex"), 1);
		LatexSaver.saveLatexFile(a3, new File("latex/a3.tex"), 1);

		System.out.println();
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

