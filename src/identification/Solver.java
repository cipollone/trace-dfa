
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
	 * @return A list of variables, with boolean member assigned.
	 */
	public static List<EncodingVariable> extractSolution(Formula f) {

		// Translate formula in Dimacs format
		DimacsSaver saver = new DimacsSaver(f);
		try {
			boolean ret = saver.saveToDimacsFile(new File("test.cnf"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Initialize solution and mapping between dimacs format and our Variable format
		List<EncodingVariable> solution = null;
        Map<Integer,Variable> mapToVar = saver.idToVarsMap();

        // Find the solution using sat4j
		ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(3600); // 1 hour timeout
        Reader reader = new DimacsReader(solver);
        // PrintWriter out = new PrintWriter(System.out,true);
        // CNF filename is given on the command line 
        try {
            IProblem problem = reader.parseInstance("test.cnf");
            if (problem.isSatisfiable()) {
                // reader.decode(problem.model(),out);
                System.out.println("Satisfiable!");
                solution = new ArrayList<>(problem.model().length);
                for (Integer i : problem.model()) {
                	if (i > 0) {
                		EncodingVariable ev = (EncodingVariable)mapToVar.get(i);
                		ev.assign(true);
                		solution.add(ev);
                	}
                }
            } else {
                System.out.println("Unsatisfiable!");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        } catch (ParseFormatException e) {
            System.out.println("Problem during parsing!");
        } catch (IOException e) {
            System.out.println("Problem during I/O operations");
        } catch (ContradictionException e) {
            System.out.println("Unsatisfiable (trivial)!");
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry!");      
        }
        return solution;
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


	/**
	 * Debugging
	 */
	// public static void test() {

	// 	DFA<String> dfa = extractNewDFA();
	// 	LatexSaver.saveLatexFile(dfa, new File("latex/extractedDFA.tex"), 1);

	// }
}
