
package identification;

import util.Pair;


/**
 * This class represents the graph of direct constraints in a DFA
 * identification problem.
 *
 * An algorithm which merges states of an input graph (in this case the APTA)
 * needs to know which pair of states can't be merged. The direct constraints
 * are a subset of those and connect states with a different labellings:
 * rejecting and accepting.
 * Every query on the constraints graph, is processed with queries on the
 * APTA.
 * Nodes are returned with their unique ID, not full objects.
 * @see automata.APTA
 */
public interface DirectConstraintsGraph {

	// >>> Public functions

	// > Problem dimension
	
	/**
	 * Size of the input domain: number of nodes.
	 * Remember: positive states + negative states != states.
	 * @return The number of states
	 */
	public int numberOfStates();

	
	/**
	 * Number of distinct labels among all transitions.
	 * @return Number of labels
	 */
	public int numberOfLabels();


	/**
	 * Number of accepting states
	 * @return Number of states
	 */
	public int numberOfAcceptingStates();

	
	/**
	 * Number of rejecting states
	 * @return Number of states
	 */
	public int numberOfRejectingStates();


	/**
	 * Returns an Iterable object containing all states.
	 * @return An Iterable with all states.
	 */
	public Iterable<Integer> states();


	/**
	 * Returns an Iterable object containing all positive states.
	 * @return An Iterable
	 */
	public Iterable<Integer> acceptingStates();


	/**
	 * Returns an Iterable object containing all negative states.
	 * @return An Iterable
	 */
	public Iterable<Integer> rejectingStates();


	/**
	 * Returns an Iterable with all direct constraints (arcs).
	 * @return Pairs of states
	 */
	public Iterable<Pair<Integer,Integer>> directConstraints();
}
