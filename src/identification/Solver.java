
package identification;

import cnf.Variable;
import automata.*;
import java.util.*;
import java.io.File;


/**
 * This class is used to call the SAT solver and to extract a solution.
 * See the reference paper to understand the role of each variable.
 */
public class Solver {

	
	// >>> Private functions
	
	/**
	 * Extract the true variables from a solution
	 * @return A list of variables, with boolean member assigned.
	 */
	private static List<EncodingVariable> extractSolution() {

		// TODO: change this with the result of the solver. This was just for
		// testing
		EncodingVariable[] vars = {
				new ParentVariable("ciao", 0, 1, true),
				new ParentVariable("ciao", 1, 1),
				new ParentVariable("ooo", 1, 2, true),
				new ParentVariable("o", 2, 0, true),
				new ParentVariable("e", 2, 0, true),
				new ParentVariable("ancora", 2, 0, true),
				new ParentVariable("p", 2, 2, true),
				new FinalVariable(2, true),
				new FinalVariable(0, true),
				new FinalVariable(1),
				new FinalVariable(5),
				new ParentVariable("v", 5, 2, true),
				new InitialColorVariable(55, 5, true)
		};
		List<EncodingVariable> varsL = new ArrayList<EncodingVariable>();
		for (EncodingVariable v: vars) {
			varsL.add(v);
		}

		return varsL;
	}
	

	// >>> Public functions
	
	/**
	 * Create a DFA from the given solution.
	 * @return The DFA extracted
	 */
	public static DFA<String> extractNewDFA() {

		DFABuilder<String> dfaBuilder = new DFABuilder<String>();

		// Extending the DFA
		List<EncodingVariable> solution = extractSolution();
		for (EncodingVariable var: solution) {
			var.extendDFA(dfaBuilder);
		}

		return dfaBuilder.getDFA();
	}


	/**
	 * Debugging
	 */
	public static void test() {

		DFA<String> dfa = extractNewDFA();
		LatexSaver.saveLatexFile(dfa, new File("latex/extractedDFA.tex"), 2);

	}
}
