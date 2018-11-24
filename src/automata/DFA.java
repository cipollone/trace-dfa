
package automata;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;


/**
 * Deterministic Finite-States Automaton class.
 * Each transition is corresponds to a label of type LabelT.
 */
public class DFA<LabelT> {

	// >>> Fields
	
	/* The inital state */


	// >> Public functions
	
	/**
	 * Debugging
	 */
	public static void test() {

		// Testing ANode
		ANode.test();
	}


	// >>> Nested classes
	
	/**
	 * Class for each node of the DFA.
	 * Each node can be final (i.e. accepting), or not.
	 * Labels in outgoing edges are exclusive (determinism).
	 */
	protected static class ANode<LabelT> {

		// >>> Fields
		
		/* Identifier */
		private final int id;

		/* Each state can be final or not */
		private boolean finalState = false;

		/* Graph structure: labelled arcs */
		private Map<LabelT, ANode<LabelT>> outArcs = new HashMap<>();

		/* Default arc */
		private ANode<LabelT> defaultOutArc = null;


		// >>> Public functions
		
		/**
		 * Constructor: just set the id
		 * @param id Any identifier
		 */
		public ANode(int id) {
			this.id = id;
		}


		/**
		 * Constructor: id and final
		 * @param id Any identifier
		 * @param isFinal Whether this is a final state
		 */
		public ANode(int id, boolean isFinal) {
			this(id);
			this.finalState = isFinal;
		}


		/**
		 * Get the id
		 * @return The numeric id
		 */
		public int getId() {
			return this.id;
		}


		/**
		 * Set if this state is accepting or not.
		 * @param isFinal
		 */
		public void setFinalFlag(boolean isFinal) {
			this.finalState = isFinal;
		}


		/**
		 * Returns whether this is a final state
		 */
		public boolean getFinalFlag() {
			return this.finalState;
		}


		/**
		 * Sets the default arc.
		 * This is the default outward connection for labels with no arc
		 * associated. Without a default arc, following an inexistent edge raises
		 * an exception.
		 * @param defaultOutArc A node, or null to unset.
		 */
		public void setDefault(ANode<LabelT> defaultOutArc) {
			this.defaultOutArc = defaultOutArc;
		}


		/**
		 * Returns the node connected to the current node through the arc
		 * labelled as label.
		 * Following an non existend edge leads to the defaultOutArc (if it exists)
		 * or raise an exception.
		 * @param label The label of the arc
		 * @return Connected node, or null if there was no such arc.
		 */
		public ANode<LabelT> followArc(LabelT label) {

			// Normal case
			ANode<LabelT> node = outArcs.get(label);
			if (node != null) { return node; }

			// Inexistent edge
			if (defaultOutArc != null) { return defaultOutArc; }
			throw new IllegalArgumentException(
					"Arc " + label + " do not exists from node " + toString());
		}


		/**
		 * Returns the set of labels available from this node
		 * @return A set of labels
		 */
		public Set<LabelT> getLabels() {
			return outArcs.keySet();
		}


		/**
		 * Remove an arc from the current node.
		 * @param label The label of the arc to remove.
		 * @return The connected node or null if there was no edge.
		 */
		public ANode<LabelT> removeArc(LabelT label) {
			return outArcs.remove(label);
		}


		/**
		 * Add an arc from the current node.
		 * If a connection with the same label already exists, that connection is
		 * substituted with the new one.
		 * @param label Label of the new connection
		 * @param node The target node
		 */
		public void addArc(LabelT label, ANode<LabelT> node) {

			// Checks
			if (label == null || node == null) {
				throw new IllegalArgumentException("Null argument");
			}

			outArcs.put(label, node);
		} 


		/**
		 * String representation
		 * @return A string with the id and '_final' if that is the case
		 */
		@Override
		public String toString() {
			if (finalState) {
				return Integer.toString(id) + "_Final";
			} else {
				return Integer.toString(id);
			}
		}


		/**
		 * Debugging
		 */
		public static void test() {

			// Testing basic methods
			System.out.println("ANode");
			ANode<Character> n1 = new ANode<>(1, true);
			ANode<Character> n2 = new ANode<>(2);
			ANode<Character> n3 = new ANode<>(3, true);

			n1.addArc('a', n2);
			n2.addArc('b', n3);
			n2.addArc('c', n2);
			n2.addArc('d', n1);

			System.out.println(n2.getLabels()); // ['b', 'c', 'd']
			System.out.println(n3.getLabels()); // []

			System.out.println(n1.followArc('a').followArc('c')); // 2

			n1.setDefault(n3);
			System.out.println(n1.followArc('g')); // 3

			n2.removeArc('h');
			n2.removeArc('c');
			try {
				n1.followArc('a').followArc('c'); // exception
			} catch (IllegalArgumentException e) {
				System.out.println("Caught: " + e.getMessage());
			}
		}
	}
}
