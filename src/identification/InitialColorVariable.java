
package identification;

import automata.DFABuilder;


/**
 * This boolean variable is true if a vertex v has color i and v is the
 * initial state.
 * Variable x with two subscrips (v,i)
 */
public class InitialColorVariable
		extends ColorVariable {


	// >>> Public functions
	
	/**
	 * Constructor. Forwarding.
	 * @param vertexId The numeric id of the initial state
	 * @param colorId The numeric id of a color
	 */
	public InitialColorVariable(int vertexId, int colorId) {
		super(vertexId, colorId);
	}


	/**
	 * Constructor. Forwarding.
	 * @param vertexId The numeric id of the initial state
	 * @param colorId The numeric id of a color
	 * @param value The boolean value
	 */
	public InitialColorVariable(int vertexId, int colorId, boolean value) {
		super(vertexId, colorId, value);
	}


	/**
	 * This variable sets the color of this vertex as initial state in DFA.
	 * @param dfa The partial DFA
	 */
	@Override
	public void actionOnDFA(DFABuilder<String> dfa) {
		dfa.setInitialState(colorId);
	}
}
