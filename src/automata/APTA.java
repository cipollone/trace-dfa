
package automata;

import java.util.*;
import java.io.File;


/**
 * Class for the Augmented Prefix Tree Acceptor (a tree).
 * Each transition is corresponds to a label of type LabelT.
 */
public class APTA<LabelT>
		implements Iterable<APTA.TNode<LabelT>>, Automaton<LabelT>, 
			LatexPrintableGraph {

	// >>> Fields
	
	/* The root */
	private final TNode<LabelT> root = new TNode<LabelT>(0);

	/* Sets and counters */
	private int nodesNum = 1;    // The root is the initial node


	// >>> Private functions
	
	/**
	 * Returns a new node to be added to the tree.
	 * This should be the only function used to add new nodes.
	 * The returned node is not connected to the graph.
	 * @return A new TNode with a new id
	 */
	private TNode<LabelT> newNode() {
		return new TNode<LabelT>(nodesNum++);
	}


	/**
	 * Creates a new child of parent, connected with arc label.
	 * @param parent The parent of the new node
	 * @param label The label of the new arc
	 * @return The created and attached node
	 * @see TNode#addArc
	 */
	private TNode<LabelT> newChild(TNode<LabelT> parent, LabelT label) {

		// Checks
		if (parent == null || label == null) {
			throw new IllegalArgumentException("Null argument");
		}

		// Create
		TNode<LabelT> child = newNode();
		parent.addArc(label, child);
		return child;
	}


	/**
	 * Build LaTex tree representation.
	 * Depth first visit of the tree. Helper function.
	 * NOTE: assuming the labels are not Latex special codes.
	 * NOTE: assuming the labels have a nice string representation.
	 * @param stringB The string representation: modified in place,initally empty
	 * @param node The current node: initially root
	 * @see APTA#getLatexGraphRepresentation
	 */
	private void buildLatexRepresentation(StringBuilder stringB,
			TNode<LabelT> node) {

		stringB.append("\n\t\t");

		// Add the node id
		stringB.append(node.getId()).append(' ');

		// Add the response
		boolean nodeOption = true;
		switch (node.getResponse()) {
			case ACCEPT:
				stringB.append("[accept] ");
				break;
			case REJECT:
				stringB.append("[reject] ");
				break;
			default:
				nodeOption = false;
		}
		
		// If it has a parent (i.e. not the root), add the incoming label
		LabelT parentLabel = node.getParentLabel();
		if (parentLabel != null) {
			if (nodeOption) {
				stringB.delete(stringB.length()-2, stringB.length()).append(',');
			} else {
				stringB.append("[");
			}
			stringB.append(">\"").append(parentLabel.toString()).append("\"] ");
		}

		// Base case: no children
		Set<LabelT> labels = node.getLabels();
		if (labels.isEmpty()) { return; }

		// Recursion
		stringB.append("-> {");
		int i = labels.size();
		for (LabelT l: labels) {
			buildLatexRepresentation(stringB, node.followArc(l));
			--i;
			char sep = (i > 0) ? ',' : '}';
			stringB.append(sep);
		}
	}


	/**
	 * Extends this APTA with this sequence and sets the final response.
	 * If the final response is UNKNOWN, the sequence is discarded.
	 * Sequences are parsed from the root of the tree.
	 * @param sequence A list of labels
	 * @param response The response associated to the string
	 * @return true if the sequence was added, false otherwise.
	 */
	private boolean addSequence(List<LabelT> sequence, Response response) {

		// Checks
		if (sequence == null) {
			throw new IllegalArgumentException("Null sequence");
		}
		if (response == Response.UNKNOWN) {
			return false; // Nothing to do
		}

		// Parse the sequence with the tree
		TNode<LabelT> node = root;
		int i;
		for (i = 0; i < sequence.size(); ++i) {
			TNode<LabelT> nextNode = node.followArc(sequence.get(i));
			if (nextNode == null) { // If there is no such arc
				break;
			}
			node = nextNode; // continue
		}

		// Extend the tree with the remaining sequence
		for (; i < sequence.size(); ++i) {
			node = newChild(node, sequence.get(i));
		}

		// Set the final state
		node.setResponse(response);

		return true;
	}


	// >>> Public functions
	
	/**
	 * Return a new iterator.
	 * Iterates the nodes with DepthPreIterator, a depth first visit.
	 * @return A new DepthPreIterator
	 */
	@Override
	public Iterator<TNode<LabelT>> iterator() {
		return new DepthPreIterator();
	}


	/**
	 * Parse the sequence with this tree.
	 * The tree is not modified.
	 * @param sequence A list of labels
	 * @return The result of parsing
	 */
	public Response parseSequenceAPTA(List<LabelT> sequence) {

		// Check
		if (sequence == null) {
			throw new IllegalArgumentException("Null sequence");
		}

		// Traverse the tree
		TNode<LabelT> node = root;
		for (LabelT label: sequence) {
			node = node.followArc(label);
			if (node == null) {
				return Response.UNKNOWN; // Not expected
			}
		}

		return node.getResponse();
	}


	/**
	 * Parse the sequence with binary output.
	 * Positive just for ACCEPT responses.
	 * @param sequence A list of labels
	 * @return true if the sequence is accepted, false otherwise
	 */
	@Override
	public boolean parseSequence(List<LabelT> sequence) {
		return parseSequenceAPTA(sequence) == Response.ACCEPT;
	}


	/**
	 * Extends this APTA to accept this sequence.
	 * @param sequence A list of labels
	 */
	public void acceptSequence(List<LabelT> sequence) {
		addSequence(sequence, Response.ACCEPT);
	}


	/**
	 * Extends this APTA to reject this sequence
	 * @param sequence A list of labels
	 */
	public void rejectSequence(List<LabelT> sequence) {
		addSequence(sequence, Response.REJECT);
	}


	/**
	 * Returns the body of a tikzpicture in Latex that represents this graph.
	 * @return The string for this graph
	 */
	@Override
	public String getLatexGraphRepresentation() {
		StringBuilder stringB = new StringBuilder();
		buildLatexRepresentation(stringB, root);
		return stringB.toString();
	}


	/**
	 * Debugging
	 */
	public static void test() {

		// Test TNode
		TNode.test();

		// Build a tree
		APTA<Character> tree = new APTA<Character>();

		// Sequences to add
		String[] stringsToAdd = { "ciao", "ciar", "ci", "ca", ""};
		boolean[] ok = { true, false, true, true, true };

		// Convert the sequences
		List<List<Character>> sequencesToAdd = new ArrayList<>();
		for (int i = 0; i < stringsToAdd.length; ++i) {
			List<Character> seq = new ArrayList<>();
			sequencesToAdd.add(seq);
			for (Character c: stringsToAdd[i].toCharArray()) {
				seq.add(c);
			}
		}

		// Strings to parse
		String[] stringsToParse = {"ciao", "c", "ca", "ciar", "cia", "cc", "d", ""};

		// Convert the sequences
		List<List<Character>> sequencesToParse = new ArrayList<>();
		for (int i = 0; i < stringsToParse.length; ++i) {
			List<Character> seq = new ArrayList<>();
			sequencesToParse.add(seq);
			for (Character c: stringsToParse[i].toCharArray()) {
				seq.add(c);
			}
		}

		// Test tree expansion
		for (int i = 0; i < sequencesToAdd.size(); ++i) {
			if (ok[i]) {
				tree.acceptSequence(sequencesToAdd.get(i));
			} else {
				tree.rejectSequence(sequencesToAdd.get(i));
			}
		}

		// Changing a node
		tree.root.followArc('c').setResponse(Response.REJECT);

		// Test parsing
		System.out.println("APTA");
		for (List<Character> sequence: sequencesToParse) {
			System.out.print(sequence + "  ");
			System.out.println(tree.parseSequenceAPTA(sequence));
		}
		System.out.println();

		// Testing Automaton interface
		System.out.println("Automaton");
		Automaton<Character> automaton = tree;
		for (List<Character> sequence: sequencesToParse) {
			System.out.print(sequence + "  ");
			System.out.println(automaton.parseSequence(sequence));
		}
		System.out.println();

		// Testing LatexSaver class and LatexPrintableGraph interface
		LatexPrintableGraph printableGraph = tree;
		LatexSaver.saveLatexFile(printableGraph, new File("latex/apta.tex"), 1);

	}


	// >>> Nested classes

	/**
	 * Enum class for the response in the current state.
	 * accepting, rejecting, or unknown
	 */
	public enum Response {
		ACCEPT, REJECT, UNKNOWN
	}


	/**
	 * Iterator class for depth-first visit of the tree.
	 * Nodes are returned in pre-order.
	 */
	private class DepthPreIterator implements Iterator<TNode<LabelT>> {
		
		/* Stack of remaining nodes */
		private Stack<TNode<LabelT>> nodesLeft = new Stack<TNode<LabelT>>();


		/* Constructor */
		public DepthPreIterator() {
			nodesLeft.add(root); // Next is the root
		}

		
		@Override
		public boolean hasNext() {
			return !nodesLeft.empty();
		}


		@Override
		public TNode<LabelT> next() {

			// Termination
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			// Expand the next node
			TNode<LabelT> node = nodesLeft.pop();
			Set<LabelT> labels = node.getLabels();
			for (LabelT l: labels) {
				nodesLeft.push(node.followArc(l));
			}

			// Return expanded
			return node;
		}
	}


	/**
	 * Class for each node of the Augmented Prefix Tree Acceptor (a tree).
	 * Each node can be accepting, rejecting or unknown.
	 * Labels in outgoing edges are exclusive (determinism).
	 */
	protected static class TNode<LabelT> {

		// >>> Fields

		/* Identifier */
		private final int id;

		/* Each state is labelled with a Response */
		private Response response = Response.UNKNOWN;

		/* Tree structure: labelled arcs */
		private Map<LabelT, TNode<LabelT>> arcs = new HashMap<LabelT, TNode<LabelT>>();

		/* Connection with the parent */
		private TNode<LabelT> parent;
		private LabelT parentLabel;


		// >>> Private functions

		/**
		 * Set the connection with the parent
		 * @param parent The parent node
		 * @param parentLabel The label of the arc
		 */
		private void setParent(TNode<LabelT> parent, LabelT parentLabel) {
			this.parent = parent;
			this.parentLabel = parentLabel;
		}


		/**
		 * Remove parent pointer
		 */
		private void unsetParent() {
			this.parent = null;
			this.parentLabel = null;
		}


		// >>> Public functions

		/**
		 * Constructor: just set an id
		 * @param id Any identifier
		 */
		public TNode(int id) {
			this.id = id;
		}


		/**
		 * Constructor: id and response
		 * @param id Any identifier
		 * @param response Whether the state should accept, reject or not specified
		 */
		public TNode(int id, Response response) {
			this(id);
			this.response = response;
		}


		/**
		 * Get the id
		 * @return The numeric id
		 */
		public int getId() {
			return this.id;
		}


		/**
		 * Set the response variable
		 * @param response Reject, accept or unknown
		 */
		public void setResponse(Response response) {
			this.response = response;
		}


		/**
		 * Returns the response
		 * @return The response at the current node
		 */
		public Response getResponse() {
			return this.response;
		}


		/**
		 * Returns the parent node
		 * @return The parent field
		 */
		public TNode<LabelT> getParent() {
			return this.parent;
		}


		/**
		 * Returns the label of the parent arc
		 * @return The label
		 */
		public LabelT getParentLabel() {
			return this.parentLabel;
		}


		/**
		 * Returns the node connected to the current node through the arc
		 * labelled as label.
		 * @param label The label of the arc
		 * @return Connected node, or null if there was no such arc.
		 */
		public TNode<LabelT> followArc(LabelT label) {
			return arcs.get(label);
		}


		/**
		 * Returns the set of labels available from this node
		 * @return A set of labels
		 */
		public Set<LabelT> getLabels() {
			return arcs.keySet();
		}


		/**
		 * Remove an arc from the current node.
		 * Detach the child connected with the arc labelled as label.
		 * @param label The label of the arc to remove.
		 * @return The connected node or null if there was no edge.
		 */
		public TNode<LabelT> removeArc(LabelT label) {
			
			TNode<LabelT> oldNode = arcs.remove(label);

			if (oldNode != null) {    // Such arc existed
				oldNode.unsetParent();
			}
			return oldNode;
		}


		/**
		 * Add an arc from the current node.
		 * The connection should be and must not create loops. This is not checked.
		 * If a connection with the same label already exists, that connection is
		 * substituted with the new one. The parent field of node is also set.
		 * @param label Label of the new connection
		 * @param node The target node
		 */
		public void addArc(LabelT label, TNode<LabelT> node) {

			// Checks
			if (label == null || node == null) {
				throw new IllegalArgumentException("Null argument");
			}

			// Remove the old connection if it exists
			removeArc(label);

			// Save the new connection
			arcs.put(label, node);
			node.setParent(this, label);
		}


		@Override
		public String toString() {
			return id + ":" + response.toString();
		}


		/**
		 * Debugging
		 */
		public static void test() {

			System.out.println("TNode");

			TNode<Character> n = new TNode<Character>(0);
			TNode<Character> n1 = new TNode<Character>(1, Response.ACCEPT);
			TNode<Character> n2 = new TNode<Character>(2);
			TNode<Character> n3 = new TNode<Character>(3, Response.REJECT);

			n.addArc('a', n1);
			n.addArc('b', n2);
			n2.addArc('a', n3);

			System.out.println(n); // 0
			System.out.println(n.followArc('a')); // 1
			System.out.println(n.followArc('b')); // 2
			System.out.println(n.followArc('b').followArc('a')); // 3
			System.out.println(n.followArc('b').followArc('b')); // null
			System.out.println(n3.getParent()); // 2
			System.out.println(n3.getParentLabel()); // a

			n2.removeArc(null);
			n2.removeArc('b');
			System.out.println(n3.getParent()); // as before
			System.out.println(n3.getParentLabel()); // ^
			n2.removeArc('a');
			System.out.println(n3.getParent()); // null
			System.out.println(n3.getParentLabel()); // null

			System.out.println();
		}
	}
}
