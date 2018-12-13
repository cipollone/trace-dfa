
package automata;

import java.util.List;


/**
 * Interface for generic models of parsers.
 * A generic automaton is able to parse sequences.
 * LabelT is the label type.
 */
public interface Automaton<LabelT> {

	/**
	 * Parse the sequence with this mode.
	 * @param sequence A list of labels
	 * @param strict If true, for any sequence leading to impossible transitions,
	 * a RuntimeException is thrown; if false, the sequence is just rejected.
	 * @return true if the sequence is accepted, false otherwise
	 */
	public boolean parseSequence(List<LabelT> sequence, boolean strict);
}
