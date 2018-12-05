
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

	
	// >>> Private functions
	
	/**
	 * Extract the true variables from a solution
	 * @return A list of variables, with boolean member assigned.
	 */
	private static List<EncodingVariable> extractSolution() {

		Formula f = ProblemEncoding.test();

		DimacsSaver saver = new DimacsSaver(f);
		try {
			boolean ret = saver.saveToDimacsFile(new File("test.cnf"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<EncodingVariable> varList = null;

		ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(3600); // 1 hour timeout
        Reader reader = new DimacsReader(solver);
        PrintWriter out = new PrintWriter(System.out,true);
        // CNF filename is given on the command line 
        try {
            IProblem problem = reader.parseInstance("test.cnf");
            if (problem.isSatisfiable()) {
                reader.decode(problem.model(),out);
                System.out.println("Satisfiable !");
                Map<Integer,Variable> mapToVar = saver.idToVarsMap();
                for (int i = 0; i < problem.model().length; i++) {
                	if (problem.model()[i] < 0) {
                		System.out.println("-" + mapToVar.get(Math.abs(problem.model()[i])));
                	} else {
                		System.out.println(mapToVar.get(Math.abs(problem.model()[i])));
                	}
                }
                System.out.println("\n\n");
                for (int i = 0; i < problem.model().length; i++) {
                	if (problem.model()[i] < 0) {
                	} else if (problem.model()[i] >= 0) {
                		if (mapToVar.get(Math.abs(problem.model()[i])).getIndex().substring(0,1).equals("z")) {
                			System.out.println(mapToVar.get(problem.model()[i]));
                		}
                	}
                }
                System.out.println("\n\n");
                for (int i = 0; i < problem.model().length; i++) {
                	if (problem.model()[i] < 0) {
                	} else if (problem.model()[i] >= 0) {
                		if (mapToVar.get(Math.abs(problem.model()[i])).getIndex().substring(0,1).equals("x")) {
                			System.out.println(mapToVar.get(problem.model()[i]));
                		}
                	}
                }
                System.out.println("\n\n");
                for (int i = 0; i < problem.model().length; i++) {
                	if (problem.model()[i] < 0) {
                	} else if (problem.model()[i] >= 0) {
                		if (mapToVar.get(Math.abs(problem.model()[i])).getIndex().substring(0,1).equals("y")) {
                			System.out.println(mapToVar.get(problem.model()[i]));
                		}
                	}
                }
                varList = new ArrayList<>(problem.model().length);
                for (Integer i : problem.model()) {
                	if (i > 0) {
                		EncodingVariable ev = (EncodingVariable)mapToVar.get(i);
                		ev.assign(true);
                		varList.add(ev);
                	}
                }
            } else {
                System.out.println("Unsatisfiable !");
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
        } catch (ParseFormatException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        } catch (ContradictionException e) {
            System.out.println("Unsatisfiable (trivial)!");
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry!");      
        }
        return varList;
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
		LatexSaver.saveLatexFile(dfa, new File("latex/extractedDFA.tex"), 1);

	}
}
