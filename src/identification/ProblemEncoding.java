
package identification;

import java.util.*;
import java.io.File;

import com.google.common.collect.*;

import automata.*;
import cnf.*;
import util.Pair;

/**
* Problem encoding, definition TBD
*/
public class ProblemEncoding {


	// >>> Fields

	private Variable[][] x;

	private Map<String, Variable>[][] y;

	private Variable[] z;

	private int vertices;

	private Set<String> labels;

	private int colors;

	private Formula encoding = new Formula();

	private DirectConstraintsGraph dcg;

	private APTA<String> apta;

	private BiMap<Variable,Integer> varToInt;


	// >>> Public functions

	/**
	 * Constructor: sets the apta, the corresponding dcg and the number of colors; initializes the variables x, y and z
	 * @param dcg The direct constraints graph associated to the apta
	 * @param apta The apta
	 * @param colors The total number of colors
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	public ProblemEncoding(DirectConstraintsGraph dcg, APTA<String> apta, int colors) {
		
		this.apta = apta;
		this.dcg = dcg;
		this.vertices = dcg.numberOfStates();
		this.labels = dcg.allLabels();
		this.colors = colors;

		this.x = new Variable[vertices][colors];
		this.y = new HashMap[colors][colors];
		this.z = new Variable[colors];

		this.varToInt = HashBiMap.create();

		int varCount = 1;

		for (int v = 0; v < vertices; v++) {
			for (int i = 0; i < colors; i++) {
				x[v][i] = new Variable("x_" + Integer.toString(v) + "," + Integer.toString(i));
				varToInt.put(x[v][i],varCount);
				varCount++;
			}
		}

		for (int i = 0; i < colors; i++) {
			for (int j = 0; j < colors; j++) {
				y[i][j] = new HashMap<>();
				for (String label : labels) {
					y[i][j].put(label, new Variable("y_" + label + "," + Integer.toString(i) + "," + Integer.toString(j)));
					varToInt.put(y[i][j].get(label),varCount);
					varCount++;
				}
			}
		}

		for (int i = 0; i < colors; i++) {
			z[i] = new Variable("z_" + Integer.toString(i));
			varToInt.put(z[i],varCount);
			varCount++;
		}
	}

	/**
	 * Returns all the x variables
	 * @return all the x variables
	 */
	public Variable[][] getX() {
		return x;
	}

	/**
	 * Returns all the y variables
	 * @return all the y variables
	 */
	public Map<String, Variable>[][] getY() {
		return y;
	}

	/**
	 * Returns all the z variables
	 * @return all the z variables
	 */
	public Variable[] getZ() {
		return z;
	}

	/**
	 * Returns the formula encoding the problem
	 * @return the formula encoding the problem
	 */
	public Formula getEncoding() {
		return encoding;
	}

	/**
	 * Returns the mapping from variable to integer
	 * @return the mapping from variable to integer
	 */
	public BiMap<Variable,Integer> getVarToIntMapping() {
		return varToInt;
	}

	/**
	 * Returns the mapping from integer to variable
	 * @return the mapping from integer to variable
	 */
	public BiMap<Integer,Variable> getIntToVarMapping() {
		return varToInt.inverse();
	}

	/**
	 * Each vertex has at least one color
	 */
	public void atLeastOneColor() {
		for (int v = 0; v < vertices; v++) {
			Clause c = new Clause();
			for (int i = 0; i < colors; i++) {
				c.addPositiveVariable(x[v][i]);
			}
			encoding.addClause(c);
		}
	}

	/**
	 * Accepting vertices cannot have the same color as rejecting vertices
	 */
	public void accRejNotSameColor() {
		for (int i = 0; i < colors; i++) {
			for (Integer v : dcg.acceptingStates()) {
				Clause c = new Clause();
				c.addNegatedVariable(x[v][i]);
				c.addPositiveVariable(z[i]);
				encoding.addClause(c);
			}
			for (Integer w : dcg.rejectingStates()) {
				Clause c = new Clause();
				c.addNegatedVariable(x[w][i]);
				c.addNegatedVariable(z[i]);
				encoding.addClause(c);
			}
		}
	}

	/**
	 * A parent relation is set when a vertex and its parent are colored
	 */
	public void parentRelationWhenColor() {
		for (APTA.ANode<String> v : apta) {
			for (int i = 0; i < colors; i++) {
				for (int j = 0; j < colors; j++) {
					if (v.getParent() != null) {
						Clause c = new Clause();
						c.addPositiveVariable(y[i][j].get(v.getParentLabel()));
						c.addNegatedVariable(x[v.getParent().id][i]);
						c.addNegatedVariable(x[v.id][j]);
						encoding.addClause(c);
					}
				}
			}
		}
	}

	/**
	 * Each parent relation can target at most one color
	 */
	public void parentAtMostOneColor() {
		List<Variable> yList = new ArrayList<>();
		for (String s : labels) {
			for (int i = 0; i < colors; i++) {
				yList.clear();
				for (int w = 0; w < colors; w++) {
					yList.add(y[i][w].get(s));
				}
				for (int j = 0; j < yList.size(); j++) {
					for (int h = j + 1; h < yList.size(); h++) {
						Clause c = new Clause();
						c.addNegatedVariable(y[i][j].get(s));
						c.addNegatedVariable(y[i][h].get(s));
						encoding.addClause(c);
					}
				}
			}
		}
	}

	/**
	 * Each vertex has at most one color
	 */
	public void atMostOneColor() {
		List<Variable> xList = new ArrayList<>();
		for (int v = 0; v < vertices; v++) {
			xList.clear();
			for (int w = 0; w < colors; w++) {
				xList.add(x[v][w]);
			}
			for (int i = 0; i < xList.size(); i++) {
				for (int j = i + 1; j < xList.size(); j++) {
					Clause c = new Clause();
					c.addNegatedVariable(x[v][i]);
					c.addNegatedVariable(x[v][j]);
					encoding.addClause(c);
				}
			}
		}
	}

	/**
	 * Each parent relation must target at least one color
	 */
	public void parentAtLeastOneColor() {
		for (String s : labels) {
			for (int i = 0; i < colors; i++) {
				Clause c = new Clause();
				for (int j = 0; j < colors; j++) {
					c.addPositiveVariable(y[i][j].get(s));
				}
				encoding.addClause(c);
			}
		}
	}

	/**
	 * A parent relation forces a vertex once the parent is colored
	 */
	public void parentForceVertex() {
		for (APTA.ANode<String> v : apta) {
			for (int i = 0; i < colors; i++) {
				for (int j = 0; j < colors; j++) {
					if (v.getParent() != null) {
						Clause c = new Clause();
						c.addNegatedVariable(y[i][j].get(v.getParentLabel()));
						c.addNegatedVariable(x[v.getParent().id][i]);
						c.addPositiveVariable(x[v.id][j]);
						encoding.addClause(c);
					}
				}
			}
		}
	}

	/**
	 * All determinization conflicts explicitly added as clauses
	 */
	public void determinConflicts() {
		for (Pair<Integer,Integer> p : dcg.directConstraints()) {
			int v = p.left;
			int w = p.right;
			if (w > v) {
				continue;
			}
			for (int i = 0; i < colors; i++) {
				Clause c = new Clause();
				c.addNegatedVariable(x[v][i]);
				c.addNegatedVariable(x[w][i]);
				encoding.addClause(c);
			}
		}
	}








	public static void test() {

		System.out.println("ProblemEncoding");

		// Build a tree
		APTA<String> apta = new APTA<>();

		// Sequences to add
		String[][] sa1 = {
			{"ciao", "come", "stai", "?"},
			{"ciao", "come", "stai", "?", "tutto", "bene", "?"},
			{"ciao", "tutto", "bene", "?"},
			{"salve", "come", "stai", "?"},
			{"salve", "come", "va", "?"},
			{"ciao"},
			{"salve"}
		};
		
		String[][] sr1 = {
			{"ciao", "come", "va"},
			{"salve", "come", "va"},
			{"salve", "come", "stai"},
			{"ciao", "come", "stai"},
			{"bella"},
			{"bella", "zi"}
		};

		for (String[] seq: sa1) {
			List<String> seqL = new ArrayList<>();
			for (String s: seq) {
				seqL.add(s);
			}
			apta.acceptSequence(seqL);
		} 
		for (String[] seq: sr1) {
			List<String> seqL = new ArrayList<>();
			for (String s: seq) {
				seqL.add(s);
			}
			apta.rejectSequence(seqL);
		}

		LatexSaver.saveLatexFile(apta, new File("latex/apta.tex"), 1);

		// DirectConstraintsGraph
		DirectConstraintsGraph graph = new DirectConstraintsGraph(apta);

		for (int colors = 4; colors < 5; colors++) {
			ProblemEncoding pe = new ProblemEncoding(graph,apta,colors);
			pe.atLeastOneColor();
			pe.accRejNotSameColor();
			pe.parentRelationWhenColor();
			pe.parentAtMostOneColor();
			pe.atMostOneColor();
			pe.parentAtLeastOneColor();
			pe.parentForceVertex();
			pe.determinConflicts();
			// System.out.println(pe.getEncoding().toString());
			// System.out.println(pe.getVarToIntMapping());
			// System.out.println(pe.getIntToVarMapping());
			// System.out.println(pe.getEncoding().getClauseList().iterator().next());
			// System.out.println(pe.getEncoding().getClauseList().iterator().next().getAllVariables().iterator().next());
			// System.out.println(pe.getVarToIntMapping().get(pe.getEncoding().getClauseList().iterator().next().getAllVariables().iterator().next()));
			System.out.println("Number of clauses: " + pe.getEncoding().getClauseNum() + "\n\n");
		}
	}
}