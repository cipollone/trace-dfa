
package automata;

import java.util.*;
import java.io.File;

import util.Pair;


/**
 * Deterministic Finite-States Automaton class.
 * Each transition has a label of type LabelT.
 */
public class DFA<LabelT>
		extends AbstractGraph<LabelT, DFA.DNode<LabelT>>
		implements Automaton<LabelT>, LatexPrintableGraph {
	
	// >>> Fields

	// >>> Private functions
	
	/**
	 * Creates a new node instance.
	 * Just the override is important.
	 * @param id the id
	 * @return A new DNode
	 */
	@Override
	DNode<LabelT> newNodeObj(int id) {
		return new DNode<LabelT>(id);
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
			DNode<LabelT> parent, LabelT outLabel, Set<DNode<LabelT>> visited,
			Set<Pair<DNode<LabelT>, LabelT>> loops) {

		stringB.append("\n\t\t");

		// Get the current node
		DNode<LabelT> node;
		if (parent != null) {
			node = parent.followArc(outLabel);
		} else {
			node = firstNode;
		}

		// If this is already drawn, save for later
		if (visited.contains(node)) {
			Pair<DNode<LabelT>, LabelT> arc = new Pair<>(parent, outLabel);
			loops.add(arc);
			return;
		}

		// Read this node
		visited.add(node);
		Set<LabelT> labels = node.getLabels();

		// Add the node id
		stringB.append(node.id).append(' ');

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
			Set<Pair<DNode<LabelT>, LabelT>> loops) {

		stringB.append(",\n");

		// Writing edges one by one.
		for (Pair<DNode<LabelT>, LabelT> arc: loops) {
			DNode<LabelT> node = arc.left;
			LabelT l = arc.right;

			// If this is a self loop
			if (node.followArc(l) == node) {
				stringB.append("\t\t").
						append(node.id).
						append(" -> [clear >, \"" + l + "\",self loop] ").
						append(node.id).
						append(",\n");
			}
			// Else, we go to some previous node
			else {
				stringB.append("\t\t").
						append(node.id).
						append(" -> [clear >, \"" + l + "\",backward] ").
						append(node.followArc(l).id).
						append(",\n");
			}
		}
		stringB.delete(stringB.length()-2, stringB.length());
	}


	// >>> Public functions
	
	/**
	 * Parse this sequence and return the result.
	 * If the sequence has some nonexistent transitions, false is returned.
	 * @param sequence A list of labels
	 * @return true if the sequence is accepted, false otherwise
	 */
	@Override
	public boolean parseSequence(List<LabelT> sequence) {

		// Traverse the automaton
		DNode<LabelT> node = followPath(sequence);

		if (node == null) {
			return false;
		}
		return node.getFinalFlag();
	}


	/**
	 * Returns the body of a tikzpicture in Latex that represents this graph.
	 * @return The string for this graph
	 */
	@Override
	public String getLatexGraphRepresentation() {

		// Data structures
		StringBuilder stringB = new StringBuilder();
		HashSet<DNode<LabelT>> visited = new HashSet<>();
		Set<Pair<DNode<LabelT>,LabelT>> loops = new HashSet<>();

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
		
		DNode.test();

		System.out.println("Testing DFA");
		
		// Define a graph
		DFA<Character> dfa = new DFA<>();
		DNode<Character> n1 = dfa.newChild(dfa.firstNode, 'a');
		DNode<Character> n2 = dfa.newChild(n1, 'b');
		DNode<Character> n3 = dfa.newChild(n1, 'c');
		n3.addArc('a', n1);
		n3.addArc('g', n3);
		n2.setFinalFlag(true);
		dfa.firstNode.setFinalFlag(true);
		DNode<Character> n4 = dfa.newChild(n2, 'c');
		n4.setFinalFlag(true);

		// Test parsing
		List<Character> l;
		l = new ArrayList<Character>();
		System.out.println(dfa.parseSequence(l));
		l = Arrays.asList('a','c');
		System.out.println(dfa.parseSequence(l));
		l = Arrays.asList('a','h');
		System.out.println(dfa.parseSequence(l));
		l = Arrays.asList('a','c','a','b');
		System.out.println(dfa.parseSequence(l));


		// Test Latex
		LatexPrintableGraph printableGraph = dfa;
		LatexSaver.saveLatexFile(printableGraph, new File("latex/dfa.tex"), 1);

		System.out.println();
	}


	// >>> Nested classes

	/**
	 * Class for each node of the DFA.
	 * Each node can be final (i.e. accepting), or not.
	 */
	public static class DNode<LabelT> 
			extends AbstractNode<LabelT,DNode<LabelT>> {

		// >>> Fields
		
		/* Each state can be final or not */
		private boolean isFinal = false;


		// >>> Public functions
		
		/**
		 * Constructor: just set the id
		 * @param id Any identifier
		 */
		public DNode(int id) {
			super(id);
		}


		/**
		 * Constructor: id and final
		 * @param id Any identifier
		 * @param isFinal Whether this is a final state
		 */
		public DNode(int id, boolean isFinal) {
			super(id);
			this.isFinal = isFinal;
		}


		/**
		 * Set if this state is accepting or not.
		 * @param isFinal Final flag
		 */
		public void setFinalFlag(boolean isFinal) {
			this.isFinal = isFinal;
		}


		/**
		 * Returns whether this is a final state
		 * @return Wether this is a final state
		 */
		public boolean getFinalFlag() {
			return this.isFinal;
		}


		/**
		 * String representation
		 * @return A string with the id and '_final' if that is the case
		 */
		@Override
		public String toString() {
			if (isFinal) {
				return id + "_Final";
			} else {
				return Integer.toString(id);
			}
		}


		/**
		 * Debugging
		 */
		public static void test() {

			System.out.println("Testing DFA.DNode");

			// Testing basic methods
			DNode<Character> n1 = new DNode<>(1, true);
			DNode<Character> n2 = new DNode<>(2);
			DNode<Character> n3 = new DNode<>(3, true);

			n1.addArc('a', n2);
			n2.addArc('b', n3);
			n2.addArc('c', n2);
			n2.addArc('d', n1);

			System.out.println(n2.getLabels()); // ['b', 'c', 'd']
			System.out.println(n3.getLabels()); // []

			System.out.println(n1.followArc('a').followArc('c')); // 2

			n2.removeArc('h');
			n2.removeArc('c');
			System.out.println(n1.followArc('a').followArc('c')); // null
			
			System.out.println();
		}
	}
}
