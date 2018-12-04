
package identification;

import cnf.Variable;
import automata.DFA;


/**
 * This boolean variable is true if a color i is used for final states.
 * Variable z with one subscript (i)
 */
public class FinalVariable
		extends Variable
		implements DfaAction {


	// >>> Fields

	private final int colorId;  // i


	// >>> Public functions
	
	/**
	 * Constructor
	 * @param colorId The numeric id of a color
	 */
	public FinalVariable(int colorId) {
		super("z_" + Integer.toString(colorId));
		this.colorId = colorId;
	}


	/**
	 * This variable sets a new state (i) as final.
	 * @param dfa The partial DFA
	 */ 
	@Override
	public void extendDFA(DFA<String> dfa) {
		// TODO
	}
}
