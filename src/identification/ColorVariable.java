
package identification;

import cnf.Variable;
import automata.DFABuilder;


/**
 * This boolean variable is true if a vertex v has color i.
 * Variable x with two subscrips (v,i)
 */
public class ColorVariable
		extends EncodingVariable {


	// >>> Fields

	protected final int vertexId; // v
	protected final int colorId;  // i


	// >>> Public functions
	
	/**
	 * Constructor
	 * @param vertexId The numeric id of a vertex
	 * @param colorId The numeric id of a color
	 * @param value The boolean value
	 */
	public ColorVariable(int vertexId, int colorId, boolean value) {
		super("x_" + Integer.toString(vertexId) + "," + Integer.toString(colorId), value);
		this.vertexId = vertexId;
		this.colorId = colorId;
	}


	/**
	 * Constructor
	 * @param vertexId The numeric id of a vertex
	 * @param colorId The numeric id of a color
	 */
	public ColorVariable(int vertexId, int colorId) {
		this(vertexId, colorId, false);
	}


	/**
	 * Nothing to do with these variables: nodes are created automatically.
	 */
	@Override
	public void actionOnDFA(DFABuilder<String> dfa) { }
}
