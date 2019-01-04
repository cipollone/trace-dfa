
package identification;

import java.util.*;
import java.io.File;

import automata.*;
import cnf.*;
import identification.ConstraintsGraph.CNode;
import util.*;

/**
 * Problem encoding: represent the coloring problem (DFA identification) in
 * a CNF clause.
 * See the reference paper for more informations.
 */
public class ProblemEncoding {


	// >>> Fields

	// Unique boolean variables
	private EncodingVariable[][] x;

	private Map<String, EncodingVariable>[][] y;

	private EncodingVariable[] z;

	// Dimension of the problem
	private int vertices;

	private Set<String> labels;

	private int colors;

	// Inputs
	private APTA<String> apta;

	private ConstraintsGraph cg;

	private Set<CNode> clique;

	// The result
	private Formula encoding = new Formula();


	// Private functions 

	/**
	 * Each vertex in the clique has a different color
	 */
	private void initCliqueVar() {

		// Each node in clique gets its color
		Iterator<CNode> cliqueIt = clique.iterator();
		for (int color = 0; color < colors && cliqueIt.hasNext(); ++color) {

			CNode n = cliqueIt.next();

			// n uses color
			Clause c = new Clause();
			c.addPositiveVariable(x[n.id][color]);
			encoding.addClause(c);

			// is color final?
			c = new Clause();
			if(n.response == APTA.Response.ACCEPT) {
				c.addPositiveVariable(z[color]);
			} else {
				c.addNegatedVariable(z[color]);
			}
			encoding.addClause(c);
		}
	}

	/**
	 * Each vertex has at least one color
	 */
	private void atLeastOneColor() {
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
	private void accRejNotSameColor() {

		Set<CNode> acceptingNodes = cg.getAcceptingNodes();
		Set<CNode> rejectingNodes = cg.getRejectingNodes();

		for (int i = 0; i < colors; i++) {
			for (CNode v : acceptingNodes) {
				Clause c = new Clause();
				c.addNegatedVariable(x[v.id][i]);
				c.addPositiveVariable(z[i]);
				encoding.addClause(c);
			}
			for (CNode w : rejectingNodes) {
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
	private void parentRelationWhenColor() {
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
	private void parentAtMostOneColor() {
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
	private void atMostOneColor() {
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
	private void parentAtLeastOneColor() {
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
	private void parentForceVertex() {
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
	private void determinConflicts() {

		// Add all edges of the consistency graph
		for (Pair<CNode,CNode> p :
				cg.constraints()) {

			CNode v = p.left;
			CNode w = p.right;
			if (w.id > v.id) { // once per pair
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

	// >>> Public functions

	/**
	 * Constructor.
	 * Sets the apta, the corresponding cg and the number of colors; initializes
	 * the variables x, y and z.
	 * @param apta The apta
	 * @param cg The constraints graph derived from apta
	 * @param clique A fully connected region computed from cg
	 * @param colors The total number of colors to use
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	public ProblemEncoding(APTA<String> apta, ConstraintsGraph cg,
			Set<CNode> clique, int colors) {

		// Check
		if(!cg.isBuiltOnAPTA(apta)) {
			throw new IllegalArgumentException(
					"The ConstraintsGraph is not associated with the given APTA");
		}
		
		// Set the input
		this.apta = apta;
		this.cg = cg;
		this.clique = clique;

		this.vertices = cg.numberOfInputNodes();
		this.labels = cg.allLabels();
		this.colors = colors;

		// Create all Boolean variables for this problem
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
	 * Returns the formula encoding the problem
	 * @return the formula encoding the problem
	 */
	public Formula getEncoding() {
		return encoding;
	}


	/**
	 * Generate the basic clauses for the encoding.
	 * Minimal clauses + clause for complete DFA (total transition function).
	 */
	public void generateClauses() {
		initCliqueVar();
		atLeastOneColor();
		accRejNotSameColor();
		parentRelationWhenColor();
		parentAtMostOneColor();
		parentAtLeastOneColor();
	}

	/**
	 * Generate redundant clauses.
	 */
	public void generateRedundantClauses() {
		atMostOneColor();
		parentForceVertex();
		determinConflicts();
	}
}
