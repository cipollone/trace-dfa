
package identification;

import java.util.*;
import java.io.File;

import automata.*;
import cnf.*;
import util.*;

/**
* Problem encoding, definition TBD
*/
public class ProblemEncoding {


	// >>> Fields

	private EncodingVariable[][] x;

	private Map<String, EncodingVariable>[][] y;

	private EncodingVariable[] z;

	private int vertices;

	private Set<String> labels;

	private int colors;

	private Formula encoding = new Formula();

	private ConstraintsGraph cg;

	private APTA<String> apta;

	// >>> Public functions

	/**
	 * Constructor: sets the apta, the corresponding cg and the number of colors; initializes the variables x, y and z
	 * @param apta The apta
	 * @param cg The constraints graph derived from the apta
	 * @param colors The total number of colors
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	public ProblemEncoding(APTA<String> apta, ConstraintsGraph cg, int colors) {
		
		this.cg = cg;

		this.apta = apta;
		this.vertices = cg.numberOfInputNodes();
		this.labels = cg.allLabels();
		this.colors = colors;

		this.x = new EncodingVariable[vertices][colors];
		this.y = new HashMap[colors][colors];
		this.z = new EncodingVariable[colors];

		int firstNodeId = apta.getFirstNode().id;

		for (int v = 0; v < vertices; v++) {
			for (int i = 0; i < colors; i++) {
				if (firstNodeId == v) {
					x[v][i] = new InitialColorVariable(v,i);
				} else {
					x[v][i] = new ColorVariable(v,i);
				}
			}
		}

		for (int i = 0; i < colors; i++) {
			for (int j = 0; j < colors; j++) {
				y[i][j] = new HashMap<>();
				for (String label : labels) {
					y[i][j].put(label, new ParentVariable(label, i, j));
				}
			}
		}

		for (int i = 0; i < colors; i++) {
			z[i] = new FinalVariable(i);
		}
	}

	/**
	 * Returns all the x variables
	 * @return all the x variables
	 */
	public EncodingVariable[][] getX() {
		return x;
	}

	/**
	 * Returns all the y variables
	 * @return all the y variables
	 */
	public Map<String, EncodingVariable>[][] getY() {
		return y;
	}

	/**
	 * Returns all the z variables
	 * @return all the z variables
	 */
	public EncodingVariable[] getZ() {
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
			for (ConstraintsGraph.CNode v : cg.getAcceptingNodes()) {
				Clause c = new Clause();
				c.addNegatedVariable(x[v.id][i]);
				c.addPositiveVariable(z[i]);
				encoding.addClause(c);
			}
			for (ConstraintsGraph.CNode w : cg.getRejectingNodes()) {
				Clause c = new Clause();
				c.addNegatedVariable(x[w.id][i]);
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
		for (String s : labels) {
			for (int i = 0; i < colors; i++) {
				for (int h = 0; h < colors; h++) {
					for (int j = h + 1; j < colors; j++) {
						Clause c = new Clause();
						c.addNegatedVariable(y[i][h].get(s));
						c.addNegatedVariable(y[i][j].get(s));
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
		for (int v = 0; v < vertices; v++) {
			for (int i = 0; i < colors; i++) {
				for (int j = i + 1; j < colors; j++) {
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
		for (Pair<ConstraintsGraph.CNode,ConstraintsGraph.CNode> p : cg.constraints()) {
			ConstraintsGraph.CNode v = p.left;
			ConstraintsGraph.CNode w = p.right;
			if (w.id > v.id) {
				continue;
			}
			for (int i = 0; i < colors; i++) {
				Clause c = new Clause();
				c.addNegatedVariable(x[v.id][i]);
				c.addNegatedVariable(x[w.id][i]);
				encoding.addClause(c);
			}
		}
	}

	/**
	 * Generate the basic clauses for the encoding
	 */
	public void generateClauses() {
		atLeastOneColor();
		accRejNotSameColor();
		parentRelationWhenColor();
		parentAtMostOneColor();
	}

	/**
	 * Generate the redundant clauses
	 */
	public void generateRedundantClauses() {
		atMostOneColor();
		parentAtLeastOneColor();
		parentForceVertex();
		determinConflicts();
	}
}

