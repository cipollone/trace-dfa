
package automata;

import java.util.*;
import java.io.File;

import util.Pair;


/**
 * Deterministic Finite-States Automaton class.
 * Each transition is corresponds to a label of type LabelT.
 * NOTE: destructive modifications are not allowed.
 *
 * TODO: modifications are not allowed yet; we need to be careful with
 * IDs and unique node references.
 */
public class DFA<LabelT>
		implements Iterable<DFA.ANode<LabelT>>, Automaton<LabelT>,
			LatexPrintableGraph {

	// >>> Fields
	
	/* The inital state */
	private final ANode<LabelT> initialState;

	/* A fail state for inexistent transitions */
	private final ANode<LabelT> failState = new ANode<>(0);

	/* Counter */
	private int statesNum = 1; // Just the failState


	// >> Private functions
	
	/**
	 * Returns a new state to be added to the tree.
	 * This should be the only function used to add new states.
	 * The returned node is not connected to the graph.
	 * @return A new ANode with a new id
	 */
	private ANode<LabelT> newNode() {
		ANode<LabelT> node = new ANode<>(statesNum++);
		node.setDefault(failState);
		return node;
	}


	/**
	 * Creates a new connection in the DFA.
	 * @param parent The parent
	 * @param label The label of the connection
	 * @param child The child 
	 */
	private void newArc(ANode<LabelT> parent, LabelT label, ANode<LabelT> child) {

		// Checks
		if (parent == null || child == null || label == null) {
			throw new IllegalArgumentException("Null argument");
		}
		if (parent.hasArc(label)) {
			throw new IllegalArgumentException(
					"Label " + label + " already exists from node " + parent);
		}

		// Connect
		parent.addArc(label, child);
	}


	/**
	 * Creates a new child of parent, connected with arc label.
	 * @param parent The parent of the new node
	 * @param label The label of the new arc
	 * @return The created and attached node
	 */
	private ANode<LabelT> newChild(ANode<LabelT> parent, LabelT label) {

		ANode<LabelT> child = newNode();
		newArc(parent, label, child);
		return child;
	}


	/**
	 * Build LaTex tree representation.
	 * Depth first visit of the graph. First part of the helper function:
	 * just a spanning tree is printed.
	 * NOTE: assuming the labels are not Latex special codes.
	 * @param stringB The string representation, modified in place,initally empty
	 * @param parent The parent node, initially null.
	 * @param outLabel The label to go through, initially null.
	 * @param visited Set of visited states, initially empty.
	 * @param loops Arcs not printed because they form loops, initially empty.
	 * @see DFA#getLatexGraphRepresentation
	 */
	private void buildLatexRepresentation1(StringBuilder stringB,
			ANode<LabelT> parent, LabelT outLabel, Set<ANode<LabelT>> visited,
			Set<Pair<ANode<LabelT>, LabelT>> loops) {

		stringB.append("\n\t\t");

		// Get the current node
		ANode<LabelT> node;
		if (parent != null) {
			node = parent.followArc(outLabel);
		} else {
			node = initialState;
		}

		// If this is already drawn, save for later
		if (visited.contains(node)) {
			Pair<ANode<LabelT>, LabelT> arc = new Pair<>(parent, outLabel);
			loops.add(arc);
			return;
		}

		// Read this node
		visited.add(node);
		Set<LabelT> labels = node.getLabels();

		// Add the node id
		stringB.append(node.getId()).append(' ');

		// Add final, if that is the case
		boolean openBracket = false;
		if (node.getFinalFlag()) {
			stringB.append("[accept");
			openBracket = true;
		}

		// Add the incoming label
		if (outLabel != null) {
			char c = (openBracket) ? ',' : '[';
			openBracket = true;
			stringB.append(c).append(">\"").append(outLabel.toString()).append('"');
		}

		if (openBracket) { stringB.append("] "); }

		// Base case: no children
		if (labels.isEmpty()) { return; }

		// Recursion
		stringB.append("-> {");

		int i = labels.size();
		for (LabelT l: labels) {
			buildLatexRepresentation1(stringB, node, l, visited, loops);
			--i;
			char sep = (i > 0) ? ',' : '}';
			stringB.append(sep);
		}
	}


	/**
	 * Build LaTex tree representation.
	 * Second part of the helper function: the loops are printed.
	 * NOTE: assuming the labels are not Latex special codes.
	 * @param stringB The string representation, result is appended.
	 * @param loops Arcs to print.
	 * @see DFA#getLatexGraphRepresentation
	 */
	private void buildLatexRepresentation2(StringBuilder stringB,
			Set<Pair<ANode<LabelT>, LabelT>> loops) {

		stringB.append(",\n");

		// Writing edges one by one.
		for (Pair<ANode<LabelT>, LabelT> arc: loops) {
			ANode<LabelT> node = arc.left;
			LabelT l = arc.right;

			// If this is a self loop
			if (node.followArc(l) == node) {
				stringB.append("\t\t").
						append(node.getId()).
						append(" -> [self loop] ").
						append(node.getId()).
						append(",\n");
			}
			// Else, we go to some previous node
			else {
				stringB.append("\t\t").
						append(node.getId()).
						append(" -> [backward] ").
						append(node.followArc(l).getId()).
						append(",\n");
			}
		}
		stringB.delete(stringB.length()-2, stringB.length());
	}


	// >> Public functions
	
	/**
	 * Constructor.
	 */
	public DFA() {

		// Initialize the fields
		initialState = newNode(); // Id: 1
		failState.setDefault(failState); // Looping
		failState.setFinalFlag(false); // Just to be sure
	}

	
	/**
	 * Return a new iterator.
	 * Iterates the nodes with DepthPreIterator, a depth first visit.
	 * @return A new DepthPreIterator
	 */
	@Override
	public Iterator<ANode<LabelT>> iterator() {
		return new DepthPreIterator();
	}


	/**
	 * Parse this sequence and return the result.
	 * If the sequence has some not recognized labels, false is returned.
	 * @param sequence A list of labels
	 * @return true if the sequence is accepted, false otherwise
	 */
	@Override
	public boolean parseSequence(List<LabelT> sequence) {

		// Check
		if (sequence == null) {
			throw new IllegalArgumentException("Null sequence");
		}

		// Traverse the automaton
		ANode<LabelT> node = initialState;
		for (LabelT label: sequence) {
			node = node.followArc(label);
		}

		return node.getFinalFlag();
	}


	/**
	 * Returns the body of a tikzpicture in Latex that represents this graph.
	 * NOTE: just the reachable states are printed.
	 * @return The string for this graph
	 */
	@Override
	public String getLatexGraphRepresentation() {

		// Data structures
		StringBuilder stringB = new StringBuilder();
		HashSet<ANode<LabelT>> visited = new HashSet<>();
		Set<Pair<ANode<LabelT>,LabelT>> loops = new HashSet<>();

		// Recursive call
		buildLatexRepresentation1(stringB, null, null, visited, loops);

		// Adding remaining edges
		buildLatexRepresentation2(stringB, loops);

		return stringB.toString();
	}


	/**
	 * Debugging
	 */
	public static void test() {

		// Define a graph
		DFA<Character> dfa = new DFA<>();
		ANode<Character> n2 = dfa.newChild(dfa.initialState, 'a');
		ANode<Character> n3 = dfa.newChild(n2, 'b');
		ANode<Character> n4 = dfa.newChild(n2, 'c');
		dfa.newArc(n4, 'a', n2);
		dfa.newArc(n4, 'g', n4);
		n3.setFinalFlag(true);
		dfa.initialState.setFinalFlag(true);
		ANode<Character> n5 = dfa.newChild(n3, 'c');
		n5.setFinalFlag(true);
		dfa.newArc(n5,'f',dfa.initialState);

		// Test Latex
		LatexPrintableGraph printableGraph = dfa;
		LatexSaver.saveLatexFile(printableGraph, new File("latex/dfa.tex"), 1);
	}


	// >>> Nested classes
	
	/**
	 * Iterator class for depth-first visit of the graph.
	 * Nodes are returned in pre-order.
	 */
	private class DepthPreIterator implements Iterator<ANode<LabelT>> {

		/* Stack of remaining nodes */
		private Stack<ANode<LabelT>> nodesLeft = new Stack<>();

		/* Set of visited states */
		private Set<ANode<LabelT>> visited = new HashSet<>();

		
		/* Constructor */
		public DepthPreIterator() {
			nodesLeft.add(initialState);
			nodesLeft.add(failState);
		}


		@Override
		public boolean hasNext() {
			return !(visited.size() == statesNum);
		}


		@Override
		public ANode<LabelT> next() {

			// Termination
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			// Find a new node
			ANode<LabelT> node;
			do {
				node = nodesLeft.pop();
			} while (visited.contains(node));
			visited.add(node);

			// Expand
			Set<LabelT> labels = node.getLabels();
			for (LabelT l: labels) {
				ANode<LabelT> n = node.followArc(l);
				if (!visited.contains(n)) {
					nodesLeft.push(n);
				}
			}

			// Return expanded
			return node;
		}
	}


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
		 * Returns whether label exists from the current node.
		 * @param label The label to check
		 */
		public boolean hasArc(LabelT label) {
			return outArcs.containsKey(label);
		}


		/**
		 * Returns the node connected to the current node through the arc
		 * labelled as label.
		 * Following an non existend edge leads to the defaultOutArc (if it exists)
		 * or raise an exception.
		 * @param label The label of the arc
		 * @return Connected node
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
