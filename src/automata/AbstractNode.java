
package automata;

import java.util.*;


/**
 * This is the base class for all node classes in graphs.
 * The generic arguments are:
 *	LabelT - The type of each label
 *	NodeT - The type of each node
 * In particular NodeT is the type of all linked nodes. This has been
 * introduced to completely avoid downcasts in subclasses. In fact, a class
 * SubNode can extend this class like:
 * <pre>{@code
 *	class SubNode<LabelT> extends AbstractNode<LabelT,SubNode<LabelT>>
 * }</pre>
 */
public abstract class AbstractNode<LabelT,
		NodeT extends AbstractNode<LabelT,NodeT>> {

	// >>> Fields
	
	/* Identifier */
	public final int id;

	/* Graph structure: output labelled arcs */
	private final Map<LabelT,NodeT> outArcs = new HashMap<>();
	

	// >>> Public functions
	
	/**
	 * Constructor: just set an id
	 * @param id Any numeric identifier
	 */
	public AbstractNode(int id) {
		this.id = id;
	}


	/**
	 * Returns whether label exists from the current node.
	 * @param label The label to check
	 * @return Does this arc exist?
	 */
	public boolean hasArc(LabelT label) {
		return outArcs.containsKey(label);
	}


	/**
	 * Returns the set of labels available from this node
	 * @return An unmodifiable set of labels
	 */
	public Set<LabelT> getLabels() {
		return Collections.unmodifiableSet(outArcs.keySet());
	}


	/**
	 * Returns the node connected through the arc label.
	 * @param label The label of the arc
	 * @return Connected node, or null if there was no such arc.
	 */
	public NodeT followArc(LabelT label) {
		return outArcs.get(label);
	}


	/**
	 * Remove an arc from the current node.
	 * @param label The label of the arc to remove.
	 * @return The connected node or null if there was no edge.
	 */
	public NodeT removeArc(LabelT label) {
		return outArcs.remove(label);
	}


	/**
	 * Add an arc from the current node.
	 * If a connection with the same label already exists, that connection is
	 * substituted with the new one.
	 * @param label Label of the new connection
	 * @param node The target node
	 */
	public void addArc(LabelT label, NodeT node) {

		// Checks
		if (label == null || node == null) {
			throw new IllegalArgumentException("Null argument");
		}

		outArcs.put(label, node);
	}


	/**
	 * String representation
	 * @return The id
	 */
	@Override
	public String toString() {
		return Integer.toString(id);
	}
}
