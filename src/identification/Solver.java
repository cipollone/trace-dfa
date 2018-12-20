
package identification;

import cnf.*;
import automata.*;
import java.util.*;
import java.io.*;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;


/**
 * This class is used to call the SAT solver and to extract a solution.
 * See the reference paper to understand the role of each variable.
 */
public class Solver {

	// >>> Public functions
	/**
	 * Extract the true variables from a solution
	 * @return A list of variables, with boolean values assigned, or null, if the
	 * problem is unsatisfiable.
	 * @throws RuntimeException To describe any IO or logic error.
	 */
	public static List<EncodingVariable> extractSolution(Formula f) {

		try {
			// Translate formula in Dimacs format
			DimacsSaver saver = new DimacsSaver(f);
			boolean ret = saver.saveToDimacsFile(new File("output/dimacsFormula.cnf"));

			// Initialize solution and mapping between dimacs format and our Variable format
			List<EncodingVariable> solution = null;
			Map<Integer,Variable> mapToVar = saver.idToVarsMap();

			// Find the solution using sat4j
			ISolver solver = SolverFactory.newDefault();
			solver.setTimeout(3600); // 1 hour timeout
			Reader reader = new DimacsReader(solver);
			// PrintWriter out = new PrintWriter(System.out,true);
			// CNF filename is given on the command line 
			IProblem problem = reader.parseInstance("output/dimacsFormula.cnf");
			if (problem.isSatisfiable()) {
				// reader.decode(problem.model(),out);
				solution = new ArrayList<>(problem.model().length);
				for (Integer i : problem.model()) {
					if (i > 0) {
						EncodingVariable ev = (EncodingVariable)mapToVar.get(i);
						ev.assign(true);
						solution.add(ev);
					}
				}
				return solution;
			}
		} catch (ContradictionException e) { // Unsatisfiable, trivial
			return null;
		} catch (TimeoutException e) {
			throw new RuntimeException("Can't solve: timeout", e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Can't solve: dimacs file not found.", e);
		} catch (ParseFormatException e) {
			throw new RuntimeException("Can't solve: wrong format in dimacs file.", e);
		} catch (IOException e) {
			throw new RuntimeException("Can't solve: IO error with dimacs file.", e);
		}
		return null;
	}
	
	/**
	 * Create a DFA from the given solution.
	 * @return The DFA extracted
	 */
	public static DFA<String> extractNewDFA(List<EncodingVariable> solution) {

		DFABuilder<String> dfaBuilder = new DFABuilder<String>();

		// Extending the DFA
		if (solution == null) {return null;}
		for (EncodingVariable var : solution) {
			var.extendDFA(dfaBuilder);
		}

		return dfaBuilder.getDFA();
	}
}
