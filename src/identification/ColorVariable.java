
package identification;

import cnf.Variable;
import automata.DFA;


/**
 * This boolean variable is true if a vertex v has color i.
 * Variable x with two subscrips (v,i)
 */
public class ColorVariable
		extends Variable
		implements DfaAction {


	// >>> Fields

	private final int vertexId; // v
	private final int colorId;  // i


	// >>> Public functions
	
	/**
	 * Constructor
	 * @param vertexId The numeric id of a vertex
	 * @param colorId The numeric id of a color
	 */
	public ColorVariable(int vertexId, int colorId) {
		super("x_" + Integer.toString(vertexId) + "," + Integer.toString(colorId));
		this.vertexId = vertexId;
		this.colorId = colorId;
	}


	/**
	 * Nothing to do with these variables: nodes are created automatically.
	 */
	@Override
	public void extendDFA(DFA<String> dfa) { }
}
