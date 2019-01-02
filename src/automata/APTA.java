
package automata;

import java.util.*;
import java.io.File;


/**
 * Class for the Augmented Prefix Tree Acceptor (a tree).
 * Each transition is corresponds to a label of type LabelT.
 */
public class APTA<LabelT> 
		extends AbstractGraph<LabelT, APTA.ANode<LabelT>>
		implements Automaton<LabelT>, LatexPrintableGraph {

	// >>> Private functions

	/**
	 * Creates a new node instance.
	 * Just the override is important.
	 * @param id the id
	 * @return A new ANode
	 */
	@Override
	ANode<LabelT> newNodeObj(int id) {
		return new ANode<LabelT>(id);
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
		if (sequence == null || response == null) {
			throw new IllegalArgumentException("Null argument");
		}
		if (response == Response.UNKNOWN) {
			return false; // Nothing to do
		}

		// Parse the sequence with the tree
		ANode<LabelT> node = firstNode;
		int i;
		for (i = 0; i < sequence.size(); ++i) {
			ANode<LabelT> nextNode = node.followArc(sequence.get(i));
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
			ANode<LabelT> node) {

		stringB.append("\n\t\t");

		// Add the node id
		stringB.append(node.id).append(' ');

		// Add the response
		boolean nodeOption = true;
		ANode<LabelT> aNode = node;
		switch (aNode.getResponse()) {
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
		LabelT parentLabel = aNode.getParentLabel();
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


	// >>> Public functions

	/**
	 * Returns the body of a tikzpicture in Latex that represents this graph.
	 * @return The string for this graph
	 */
	@Override
	public String getLatexGraphRepresentation() {
		StringBuilder stringB = new StringBuilder();
		buildLatexRepresentation(stringB, firstNode);
		return stringB.toString();
	}


	/**
	 * Doing nothing. No extra latex code needed.
	 * @return null
	 */
	@Override
	public String extraLatexEnv() {
		return null;
	}


	/**
	 * No further options.
	 * @return No Latex options for standalone document class: null.
	 */
	@Override
	public String standaloneClassLatexOptions() {
		return null;
	}
	

	/**
	 * Parse the sequence with this tree.
	 * @param sequence A list of labels
	 * @return The result of parsing
	 */
	public Response parseSequenceAPTA(List<LabelT> sequence) {

		// Traverse the tree
		ANode<LabelT> node = followPath(sequence);

		// Return the response of the final node
		if (node == null) {
			return Response.UNKNOWN;
		}
		return node.getResponse();
	}


	/**
	 * Returns true for accepted sequences, false otherwise.
	 * @param sequence A list of labels
	 * @param strict If true, for any sequence leading to impossible transitions,
	 * a RuntimeException is thrown; if false, the sequence is just rejected.
	 * @return Result of parsing
	 */
	@Override
	public boolean parseSequence(List<LabelT> sequence, boolean strict) {

		// Traverse the tree
		ANode<LabelT> node = followPath(sequence);

		// Return the response of the final node
		if (node == null) {
			if (strict) {
				throw new RuntimeException("Can't parse " + sequence +
						" : impossible transitions.");
			} else {
				return false;
			}
		}

		return node.getResponse() == Response.ACCEPT;
	}


	/**
	 * Extends this APTA to accept this sequence.
	 * @param sequence A list of labels
	 * @return This APTA
	 */
	public APTA<LabelT> acceptSequence(List<LabelT> sequence) {
		addSequence(sequence, Response.ACCEPT);
		return this;
	}


	/**
	 * Extends this APTA to reject this sequence
	 * @param sequence A list of labels
	 * @return This APTA
	 */
	public APTA<LabelT> rejectSequence(List<LabelT> sequence) {
		addSequence(sequence, Response.REJECT);
		return this;
	}


	/**
	 * Debugging
	 */
	public static void test() {
		
		ANode.test();

		System.out.println("Testing APTA");

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
		tree.firstNode.followArc('c').setResponse(Response.REJECT);

		// Test parsing
		for (List<Character> sequence: sequencesToParse) {
			System.out.print(sequence + "  ");
			System.out.println(tree.parseSequenceAPTA(sequence));
		}

		// Testing Automaton interface
		System.out.println("Automaton");
		Automaton<Character> automaton = tree;
		for (List<Character> sequence: sequencesToParse) {
			System.out.print(sequence + "  ");
			System.out.println(automaton.parseSequence(sequence, false));
		}

		// Testing LatexSaver class and LatexPrintableGraph interface
		LatexPrintableGraph printableGraph = tree;
		LatexSaver.saveLatexFile(printableGraph, new File("test/apta.tex"), 1);

		System.out.println();
	}


	// >>> Nested classes

	/**
	 * Enum class for the response in the each state.
	 * Possible choices: accepting, rejecting, or unknown response.
	 */
	public enum Response {
		ACCEPT, REJECT, UNKNOWN;

		@Override
		public String toString() {
			switch (this) {
				case ACCEPT:
					return "ACC";
				case REJECT:
					return "REJ";
				case UNKNOWN:
					return "UNK";
			}
			return "UNK";
		}
	}


	/**
	 * Class for each node of the Augmented Prefix Tree Acceptor (a tree).
	 * Each node can be accepting, rejecting or unknown.
	 */
	public static class ANode<LabelT> 
			extends AbstractNode<LabelT,ANode<LabelT>> {

		// >>> Fields

		/* Each state is labelled with a Response */
		private Response response = Response.UNKNOWN;

		/* Connection with the parent */
		private ANode<LabelT> parent = null;
		private LabelT parentLabel = null;


		// >> Private functions

		/**
		 * Set the connection with the parent.
		 * Use null values to unset.
		 * @param parent The parent node
		 * @param parentLabel The label of the arc
		 */
		private void setParent(ANode<LabelT> parent, LabelT parentLabel) {
			this.parent = parent;
			this.parentLabel = parentLabel;
		}


		// >>> Public functions

		/**
		 * Returns the parent node
		 * @return The parent field
		 */
		public ANode<LabelT> getParent() {
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
		 * Constructor: just set an id
		 * @param id Any identifier
		 */
		public ANode(int id) {
			super(id);
		}


		/**
		 * Constructor: id and response
		 * @param id Any identifier
		 * @param response Whether the state should accept, reject or unknown
		 */
		public ANode(int id, Response response) {
			super(id);
			this.response = response;
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
		 * Add an arc from the current node.
		 * If a connection with the same label already exists, that connection is
		 * substituted with the new one. The parent field of node is also set.
		 * @param label Label of the new connection
		 * @param node The target node
		 */
		@Override
		public void addArc(LabelT label, ANode<LabelT> node) {

			super.addArc(label, node);
			node.setParent(this, label);
		}


		/**
		 * Remove an arc from the current node.
		 * Detach the child connected with the arc labelled as label.
		 * @param label The label of the arc to remove.
		 * @return The connected node or null if there was no edge.
		 */
		@Override
		public ANode<LabelT> removeArc(LabelT label) {
			
			ANode<LabelT> oldNode = super.removeArc(label);

			if (oldNode != null) {    // Such arc existed
				oldNode.setParent(null, null);
			}
			return oldNode;
		}


		@Override
		public String toString() {
			return id + "_" + response.toString();
		}


		/**
		 * Debugging
		 */
		public static void test() {

			System.out.println("Testing APTA.ANode");

			ANode<Character> n = new ANode<Character>(0);
			ANode<Character> n1 = new ANode<Character>(1, Response.ACCEPT);
			ANode<Character> n2 = new ANode<Character>(2);
			ANode<Character> n3 = new ANode<Character>(3, Response.REJECT);

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
