
package identification;

import automata.DFA;


/**
 * This interface defines a single action method.
 * That method is used to execute different actions for Boolean variables
 * with different meaning, every time they are set to true.
 * Every action can modify, or extend the DFA.
 */
public interface DfaAction {

	/**
	 * Action method
	 * @param dfa The partially reconstructed automaton to be modified
	 */
	public void extendDFA(DFA<String> dfa);
}
