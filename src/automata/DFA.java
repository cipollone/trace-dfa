
package automata;

import java.util.*;
import java.io.*;
import util.Pair;
import org.processmining.ltl2automaton.plugins.automaton.DefaultAutomatonFactory;
import org.processmining.ltl2automaton.plugins.automaton.DeterministicAutomaton;
import org.processmining.ltl2automaton.plugins.automaton.State;
import org.processmining.ltl2automaton.plugins.automaton.Transition;
import org.processmining.ltl2automaton.plugins.automaton.DOTExporter;


/**
 * Deterministic Finite-States Automaton class.
 * Each transition has a label of type LabelT.
 * To create instances of this class use {@link DFABuilder}
 */
public class DFA<LabelT>
		extends AbstractGraph<LabelT, DFA.DNode<LabelT>>
		implements Automaton<LabelT>, LatexPrintableGraph {
	
	// >>> Fields

	/* Auxiliary variable, used for printing a legend of the labels.
	 * It's used with (short_label, long_label) couples. See the NOTE in
	 * getLatexGraphRepresentation() */
	private Map<Integer,String> legendMap = null;
	private boolean usingLegend = false; // Call useLegend(true) to enable


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
	 * Create a new DFA with merged arcs for each pair of nodes.
	 * This is useful for printing in Latex the graph. Arcs won't be overlapped.
	 * NOTE: LabelT should have a good string representation.
	 * @return A new DFA representing this object. The two DFA are not
	 * equivalent.
	 */
	private DFA<String> simpleArcsRepresentation() {

		DFA<String> simpleDFA = new DFA<>();  // Create a new DFA
		Map<DNode<LabelT>, DNode<String>> nodesMap =  // Old -> new map
				new HashMap<>();

		// Initialize the legend, if used
		int nextIntLabel = 0;
		if (usingLegend) {
			legendMap = new HashMap<>();
		}
	
		// Create all nodes in advance
		for (DNode<LabelT> node: this) {
			DNode<String> newNode = new DNode<String>(node.id, node.getFinalFlag());
			nodesMap.put(node, newNode);
		}

		// Set initial state
		simpleDFA.firstNode = nodesMap.get(this.firstNode);

		// Traverse this graph for the arcs
		for (DNode<LabelT> node: this) {

			// Map child -> merged arcs
			Map<DNode<String>, String> mergedArcs = new HashMap<>();

			// List and merge all arcs
			for (LabelT arc: node.getLabels()) {
				DNode<String> newChild = nodesMap.get(node.followArc(arc));
				String mergedArc = mergedArcs.get(newChild);
				if (mergedArc == null) { // Add new
					mergedArc = arc.toString();
				} else {                 // Merge new
					mergedArc = mergedArc + " | " + arc.toString();
				}
				mergedArcs.put(newChild, mergedArc);
			}
			
			// Add the merged arcs to the new node
			for (DNode<String> newChild: mergedArcs.keySet()) {
				String finalLabel = mergedArcs.get(newChild);

				// Abbreviation?
				if (usingLegend) {
					legendMap.put(nextIntLabel, finalLabel);
					finalLabel = Integer.toString(nextIntLabel);
					++nextIntLabel;
				}

				// Add the arc
				DNode<String> newNode = nodesMap.get(node);
				newNode.addArc(finalLabel, newChild);
			}
		}

		return simpleDFA;
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
		stringB.append("\n\t\t");
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
						append(" -> [clear >, \"" + l + "\", self loop] ").
						append(node.id).
						append(",\n");
			}
			// Else, we go to some previous node
			else {
				stringB.append("\t\t").
						append(node.id).
						append(" -> [clear >, \"" + l + "\", backward] ").
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
	 * @param strict If true, for any sequence leading to impossible transitions,
	 * a RuntimeException is thrown; if false, the sequence is just rejected.
	 * @return true if the sequence is accepted, false otherwise
	 */
	@Override
	public boolean parseSequence(List<LabelT> sequence, boolean strict) {

		// Traverse the automaton
		DNode<LabelT> node = followPath(sequence);

		if (node == null) {
			if (strict) {
				throw new RuntimeException("Can't parse " + sequence +
						" : impossible transitions.");
			} else {
				return false;
			}
		}
		return node.getFinalFlag();
	}


	/**
	 * Whether to use numbers instead of the true labels in the Latex
	 * representation.
	 * Call this function before saving the Latex file.
	 * @param flag The boolean flag. Default false.
	 */
	public void useLegend(boolean flag) {
		usingLegend = flag;
	}


	/**
	 * Returns the body of a tikzpicture in Latex that represents this graph.
	 * Call {@link DFA#useLegend} in advance to enable/disable the legend in the
	 * final document. This is useful when there are long or many labels.
	 * @return The string for this graph
	 */
	@Override
	public String getLatexGraphRepresentation() {

		// Simplify the graph.
		DFA<String> dfa = simpleArcsRepresentation();

		// Data structures
		StringBuilder stringB = new StringBuilder();
		HashSet<DNode<String>> visited = new HashSet<>();
		Set<Pair<DNode<String>,String>> loops = new HashSet<>();

		// Recursive call
		dfa.buildLatexRepresentation1(stringB, null, null, visited, loops);

		// Adding remaining edges
		dfa.buildLatexRepresentation2(stringB, loops);

		return stringB.toString();
	}


	/**
	 * Extra latex code used for printing the legend, if used.
	 * A new environment in which the legend of the labels is printed.
	 * @return The legend as Latex code
	 */
	@Override
	public String extraLatexEnv() {

		if (!usingLegend) { return null; }

		// Open
		StringBuilder stringB = new StringBuilder();
		stringB.append("\n");
		stringB.append("\\textbf{Transitions}\n");
		stringB.append("\\begin{flushleft}\n");
		stringB.append("\\begin{description}\n");
		
		// Sorted legend
		List<Integer> shortLabels = new ArrayList<Integer>(legendMap.keySet());
		Collections.sort(shortLabels);

		for (Integer shortLabel: shortLabels) {
			String longLabel = legendMap.get(shortLabel);
			stringB.append("\t\\item [" + shortLabel + "] " + longLabel + "\n");
		}
		
		// Close
		stringB.append("\\end{description}\n");
		stringB.append("\\end{flushleft}\n");

		return stringB.toString();
	}


	/**
	 * This function is used to pass options to standalone document class.
	 * Return the empty string to pass nothing. Otherwise return a
	 * list of options like: [varwidth]
	 * @return Latex options for standalone document class
	 */
	@Override
	public String standaloneClassLatexOptions() {
		// Fixed width when using the legend
		if (usingLegend) {
			return "[varwidth=40em]"; // NOTE: assuming the DFA is small enough
		} else {
			return null;
		}
	}


	/**
	 * Returns this Automaton as a {@link
	 * org.processmining.ltl2automaton.plugins.automaton.DeterministicAutomaton}
	 * of the LTL2Automaton library.
	 * Each label is represented as a {@link
	 * org.processmining.ltl2automaton.plugins.automaton.TransitionLabel} with a
	 * single true preposition with the same name as the label.
	 * NOTE: assuming each LabelT defines an unique string representation.
	 * @return An Automaton representing this DFA.
	 */
	public DeterministicAutomaton asLTLAutomaton() {

		// Initialize the automaton
		DefaultAutomatonFactory automatonFactory = new DefaultAutomatonFactory();

		// Convert all nodes
		Map<DNode<LabelT>,State> nodesConversion = new HashMap<>();
		for (DNode<LabelT> node: this) {
			State s = new State();
			automatonFactory.updateState(s, node.id, node.getFinalFlag());
			nodesConversion.put(node, s);
		}

		// Convert all arcs
		for (DNode<LabelT> node: this) {
			for (LabelT arc: node.getLabels()) {
				State s1 = nodesConversion.get(node);
				State s2 = nodesConversion.get(node.followArc(arc));
				automatonFactory.addPropositionTransition(s1, s2, arc.toString());
			}
		}

		// Set initial state and return
		automatonFactory.initialState(nodesConversion.get(this.firstNode));
		return new DeterministicAutomaton(automatonFactory.getAutomaton(), false);
	}


	/**
	 * Returns a new DFA built from the given {@link
	 * org.processmining.ltl2automaton.plugins.automaton.DeterministicAutomaton}
	 * of the LTL2Automaton library.
	 * The retured DFA is equivalent to the given DeterministicAutomaton (they 
	 * accept the same language), but nodes won't keep the same ID (by design).
	 * The returned DFA shares the same transition objects as the input
	 * automaton.
	 * @param ltlAutomaton The automaton to convert. Its transitions must define
	 * a deterministic DFA. IDs must be unique.
	 * @return A new DFA
	 */
	public static DFA<Transition> fromLTLAutomaton(
			DeterministicAutomaton ltlAutomaton) {

		// Build a new DFA
		DFABuilder<Transition> builder = new DFABuilder<>();

		// Just convert all transitions
		for (Transition t: ltlAutomaton.transitions()) {
			builder.newArc(t.getSource().getId(), t, t.getTarget().getId());
		}

		// Set final states
		for (State s: ltlAutomaton) {
			if (s.isAccepting()) {
				builder.setFinalState(s.getId());
			}
		}

		// Set initial
		builder.setInitialState(ltlAutomaton.getInit().getId());

		return builder.getDFA();
	}


	/**
	 * Converts this DFA in a new DFA with arcs represented by strings.
	 * Uses the toString() function of LabelT to convert the edges: returned
	 * strings must preserve the equals() relation between every pair of labels.
	 * @return A new DFA that uses strings
	 */
	public DFA<String> asStringDFA() {

		// Build a new DFA
		DFABuilder<String> builder = new DFABuilder<String>();

		// Initialize all states to preserve IDs
		int newId = 0;
		for (DNode<LabelT> state: this) {
			builder.touchState(newId++);
		}
		
		// Convert all transitions
		for (DNode<LabelT> state: this) {
			for (LabelT label: state.getLabels()) {
				builder.newArc(state.id, label.toString(), state.followArc(label).id);
			}

			// Set final
			if (state.getFinalFlag()) {
				builder.setFinalState(state.id);
			}
		}

		// Set initial
		builder.setInitialState(firstNode.id);

		return builder.getDFA();
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
	}
}
