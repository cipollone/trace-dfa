
package identification;

import automata.DFABuilder;
import cnf.Variable;


/**
 * This abstract class is the common interface of all variables in this encoding.
 * Every subclass must define ad action method, {@link #actionOnDFA}, which
 * can be used to make some additions to the DFA every time a variable is
 * processed. From the outside, the action should be invoked calling
 * {@link #extendDFA}.
 */
public abstract class EncodingVariable
		extends Variable {

	// >>> Protected functions

	/**
	 * Modifies the DFA. Internal call.
	 * @param dfa The partially reconstructed automaton to be modified
	 */
	protected abstract void actionOnDFA(DFABuilder<String> dfa);


	// >>> Public functions

	/**
	 * Constructor. Forwarding.
	 * @param name The unique name of this variable
	 */
	public EncodingVariable(String name) {
		super(name);
	}


	/**
	 * Constructor. Forwarding.
	 * @param name The unique name of this variable
	 * @param value The boolean value
	 */
	public EncodingVariable(String name, boolean value) {
		super(name, value);
	}


	/**
	 * Modifies the DFA.
	 * @param dfa The partially reconstructed automaton to be modified
	 */
	public void extendDFA(DFABuilder<String> dfa) {
		if (isTrue()) {
			actionOnDFA(dfa);
		}
	}
}
