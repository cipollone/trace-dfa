
package identification;

import cnf.Variable;
import automata.DFABuilder;


/**
 * This boolean variable is true if a color i is used for final states.
 * Variable z with one subscript (i)
 */
public class FinalVariable
		extends EncodingVariable {


	// >>> Fields

	private final int colorId;  // i


	// >>> Public functions
	
	/**
	 * Constructor
	 * @param colorId The numeric id of a color
	 * @param value The boolean value
	 */
	public FinalVariable(int colorId, boolean value) {
		super("z_" + Integer.toString(colorId), value);
		this.colorId = colorId;
	}


	/**
	 * Constructor
	 * @param colorId The numeric id of a color
	 */
	public FinalVariable(int colorId) {
		this(colorId, false);
	}


	/**
	 * This variable sets a new state (i) as final.
	 * @param dfa The partial DFA
	 */ 
	@Override
	public void actionOnDFA(DFABuilder<String> dfa) {
		dfa.setFinalState(colorId);
	}
}
