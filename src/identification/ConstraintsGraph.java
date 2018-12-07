
package identification;

import java.util.*;
import java.util.stream.Collectors;
import java.io.File;

import automata.*;


/**
 * This is the graph of all binary constraints in the coloring problem, for
 * DFA identification.
 *
 * An algorithm which merges states of an input graph (in this case the APTA)
 * needs to know which pair of states can't be merged. This graph exactly
 * represents the same constraints. This class also provides useful measures
 * of the APTA.
 * NOTE: assuming an APTA of String.
 * @see automata.APTA
 */
public class ConstraintsGraph
		implements Iterable<ConstraintsGraph.CNode> {

	// >>> Fields
	
	/* Domain */
	private final APTA<String> apta;

	/* The first node */
	private final CNode firstNode;

	/* Positive/negative nodes */
	private Set<CNode> positiveNodes = null;
	private Set<CNode> negativeNodes = null;


	// >>> Private functions

	/**
	 * Creates the constraints graph from the apta.
	 */
	public void createFromAPTA() {

		// Clone all nodes
		Set<CNode> nodes = new HashSet<CNode>();
		for (APTA.ANode<String> n: apta) {
			CNode cn = (n.id == firstNode.id) ? firstNode : new CNode(n);
			nodes.add(cn);
		}

		// Save positive and negative
		positiveNodes = nodes.stream()
				.filter(n -> n.response == APTA.Response.ACCEPT)
				.collect(Collectors.toSet());
		negativeNodes = nodes.stream()
				.filter(n -> n.response == APTA.Response.REJECT)
				.collect(Collectors.toSet());


		// TODO: just testing for now
		CNode last = null;
		for (CNode n: nodes) {
			if (last != null) {
				n.addArc(last);
			}
			last = n;
		}
		last.addArc(last);

	}


	// >>> Public functions

	/**
	 * Constructor.
	 * This operation has polynomial cost (time) in the number of states of the
	 * APTA, because we need to copy the nodes and find the constraints between
	 * those.
	 * @param apta An APTA which represents the problem domain: the states to be
	 * merged
	 */
	public ConstraintsGraph(APTA<String> apta) {

		// Initialization
		this.apta = apta;
		this.firstNode = new CNode(apta.getFirstNode());
		createFromAPTA();
	}


	/**
	 * Returns the set of accepting nodes
	 * @return An unmodifiable set of nodes
	 */
	public Set<CNode> getAcceptingNodes() {
		return Collections.unmodifiableSet(positiveNodes);
	}


	/**
	 * Returns the set of rejecting nodes
	 * @return An unmodifiable set of nodes
	 */
	public Set<CNode> getRejectingNodes() {
		return Collections.unmodifiableSet(negativeNodes);
	}


	/**
	 * Iterator
	 * @return An iterator of nodes. Depth first visit.
	 */
	@Override
	public Iterator<CNode> iterator() {
		return new DepthPreIterator();
	}


	/**
	 * Debugging
	 */
	public static void test() {

		// Build an APTA
		APTA<String> tree = new APTA<>();

		String[] stringsToAdd = { "ciao", "ciar", "ci", "ca", ""};
		boolean[] ok = { true, false, true, true, true };

		List<List<String>> sequencesToAdd = new ArrayList<>();
		for (int i = 0; i < stringsToAdd.length; ++i) {
			List<String> seq = new ArrayList<>();
			sequencesToAdd.add(seq);
			for (char c: stringsToAdd[i].toCharArray()) {
				seq.add(new String(new char[]{c}));
			}
		}

		for (int i = 0; i < sequencesToAdd.size(); ++i) {
			if (ok[i]) {
				tree.acceptSequence(sequencesToAdd.get(i));
			} else {
				tree.rejectSequence(sequencesToAdd.get(i));
			}
		}

		LatexSaver.saveLatexFile(tree, new File("latex/apta_c.tex"), 1);

		// Create a constraint graph
		ConstraintsGraph graph = new ConstraintsGraph(tree);

		// Testing iterator
		for (CNode n: graph) {
			System.out.println(n);
		}

		// Testing set of states
		System.out.println(graph.getAcceptingNodes());
		System.out.println(graph.getRejectingNodes());

	}


	// >>> Nested classes

	/**
	 * Iterator class for depth-visit of the graph.
	 * Nodes are returned in pre-order.
	 */
	private class DepthPreIterator implements Iterator<CNode> {

		/* Stack of remaining nodes */
		private Stack<CNode> nodesLeft = new Stack<>();

		/* Set of visited states */
		private Set<CNode> visited = new HashSet<>();


		/* Constructor */
		public DepthPreIterator() {
			nodesLeft.add(firstNode);
		}


		@Override
		public boolean hasNext() {
			return !nodesLeft.empty();
		}


		@Override
		public CNode next() {

			// Termination
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			// Get the next new node
			CNode node = nodesLeft.pop();
			visited.add(node);

			// Expand
			Set<CNode> connectedNodes = node.getArcs();
			for (CNode n: connectedNodes) {
				if (!visited.contains(n)) {
					nodesLeft.push(n);
				}
			}

			// Remove visited nodes from the top of the stack
			while ((!nodesLeft.isEmpty()) && visited.contains(nodesLeft.peek())) {
				nodesLeft.pop();
			}

			// Return expanded
			return node;
		}
	}


	/**
	 * Each node of this graph.
	 * Each node is identified by an id.
	 */
	public static class CNode {

		// >>> Fields
		
		/* Identifier */
		public final int id;

		/* Each state is labelled with a Response, like an APTA */
		public final APTA.Response response;

		/* Graph structure: unlabelled arcs */
		private final Set<CNode> arcs = new HashSet<CNode>();


		// >>> Public functions
		
		/**
		 * Constructor.
		 * The only way of making a new node is to copy an existing APTA node
		 * @param node An existing APTA node to copy
		 */
		public CNode(APTA.ANode<String> node) {
			this.id = node.id;
			this.response = node.getResponse();
		}
		
		
		/**
		 * Returns whether node is connected to this node.
		 * @param node The node to find
		 * @return Does this arc exist?
		 */
		public boolean hasArc(CNode node) {
			return arcs.contains(node);
		}


		/**
		 * Returns the set of nodes connected to this node.
		 * @return An unmodifiable set of connected nodes
		 */
		public Set<CNode> getArcs() {
			return Collections.unmodifiableSet(arcs);
		}


		/**
		 * Disconnects two nodes.
		 * @param node The node to disconnect.
		 * @return true if the node has been removed, false if there was no node.
		 */
		public boolean removeArc(CNode node) {

			// Checks
			if (node == null) {
				throw new IllegalArgumentException("Null argument");
			}

			boolean found = arcs.remove(node);
			if (found) {
				node.arcs.remove(this);
			}
			return found;
		}


		/**
		 * Connects two nodes.
		 * @param node The node to connect
		 */
		public void addArc(CNode node) {

			// Checks
			if (node == null) {
				throw new IllegalArgumentException("Null argument");
			}

			arcs.add(node);
			node.arcs.add(this);
		}


		@Override
		public String toString() {
			return id + "_" + response.toString();
		}
	}
}
