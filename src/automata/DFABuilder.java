
package automata;

import java.util.*;
import java.io.File;


/**
 * This class can be used to build DFA objects.
 * It allows to specify the DFA in terms of the nodes and arcs that contains.
 * States are identified with integers. Every integer used implicitly generates
 * a new node. All referenced states are kept in a DFABuilder, but unconnected
 * nodes will be deleted from the returned DFA.
 * @see DFABuilder#newArc
 * @see DFA
 */
public class DFABuilder<LabelT> {

	// >>> Fields
	
	/* From the outside one can use IDs to refer to nodes of the graph.
	 * These are mapped to new states internally. It's also useful to remember
	 * disconnected nodes */
	private Map<Integer, DFA.DNode<LabelT>> states = new HashMap<>();

	/* The internal DFA */
	private DFA<LabelT> dfa = new DFA<>();


	// >>> Private functions
	
	/**
	 * Returns the given state object.
	 * If this is a new state, add it to the graph (still unconnected).
	 * @param state The id of a node
	 * @return The state corresponding to the ID
	 */
	private DFA.DNode<LabelT> aState(int state) {
		DFA.DNode<LabelT> node = states.get(state);
		if (node == null) { // This is new
			node = dfa.newNode();
			states.put(state, node);
		}
		return node;
	}


	// >>> Public functions

	/**
	 * Constructor.
	 */
	public DFABuilder() {
		states.put(0, dfa.firstNode); // Inital state is already there
	}


	/**
	 * Touch a state.
	 * This function creates a new state which is named with the given id.  If id
	 * has been already used, nothing is done. Nodes need not to be defined in
	 * advance. However this function is useful if one needs to assing a fixed
	 * order to the nodes.
	 * @param state An id
	 */
	public void touchState(int state) {
		aState(state);
	}


	/**
	 * Set this state as final.
	 * If necessary, it is added to the graph as a new state.
	 * @param state The id of a final state
	 */
	public void setFinalState(int state) {
		DFA.DNode<LabelT> node = aState(state);
		node.setFinalFlag(true);
	}


	/**
	 * Set this state as the initial state in the DFA.
	 * @param state The id of the initial state
	 */
	public void setInitialState(int state) {
		DFA.DNode<LabelT> node = aState(state);
		dfa.firstNode = node;
	}


	/**
	 * Add an arc between two nodes.
	 * If necessary, any of these states is added to the graph.
	 * @param parent The id of the parent state
	 * @param label The label of the arc
	 * @param child The id of the child state
	 */
	public void newArc(int parent, LabelT label, int child) {
		DFA.DNode<LabelT> parentNode = aState(parent);
		DFA.DNode<LabelT> childNode = aState(child);
		parentNode.addArc(label, childNode);
	}


	/**
	 * Returns the DFA built.
	 * @return The DFA under construction
	 */
	public DFA<LabelT> getDFA() {
		return dfa;
	}
}
