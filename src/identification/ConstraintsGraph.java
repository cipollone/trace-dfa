
package identification;

import java.util.*;
import java.util.stream.Collectors;
import java.io.File;

import automata.*;
import util.*;


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
		implements Iterable<ConstraintsGraph.CNode>, LatexPrintableGraph {

	// >>> Fields
	
	/* Domain */
	private final APTA<String> apta;

	/* The first node (any) for each connected part of the graph */
	private Set<CNode> firstNodes = null;

	/* Positive/negative nodes */
	private Set<CNode> positiveNodes = null;
	private Set<CNode> negativeNodes = null;

	/* Useful hints for users of this class. These are measures of the APTA */
	private Set<String> labels = new HashSet<String>();
	private int aptaStatesNum = 0;


	// >>> Private functions

	/**
	 * Constructor.
	 * Empty graph, not related to any APTA instance.
	 * Used internally.
	 */
	private ConstraintsGraph() {
		this.apta = null;
		this.firstNodes = new HashSet<>();
	}


	/**
	 * Creates the constraints graph from the apta.
	 * If the parameter is set to true, the graph built contains both the direct
	 * and indirect constraints. With false, just the indirect constraints
	 * are created (the minimal set).
	 * @param full Complete or minimal graph
	 */
	private void createFromAPTA(boolean full) {

		// Clone all nodes
		Map<APTA.ANode<String>,CNode> nodes = new HashMap<>();
		for (APTA.ANode<String> n: apta) {
			nodes.put(n, new CNode(n));
		}
		aptaStatesNum = nodes.size();

		// Save positive and negative
		positiveNodes = nodes.values().stream()
				.filter(n -> n.response == APTA.Response.ACCEPT)
				.collect(Collectors.toSet());
		negativeNodes = nodes.values().stream()
				.filter(n -> n.response == APTA.Response.REJECT)
				.collect(Collectors.toSet());

		// Create all direct constraints
		for (CNode nP: positiveNodes) {
			for (CNode nN: negativeNodes) {
				nP.addArc(nN);
			}
		}

		// Indirect constraints
		if (full) {
			Map<APTA.ANode<String>,List<APTA.ANode<String>>> merged = new HashMap<>();

			// Test each pair of nodes
			for (APTA.ANode<String> n1: nodes.keySet()) {
				for (APTA.ANode<String> n2: nodes.keySet()) {
					if (!n1.equals(n2)) {

						// Test and add an arc if failed
						merged.clear();
						boolean consistent = isAConsistentMerge(n1, n2, merged, nodes);
						if (!consistent) {
							nodes.get(n1).addArc(nodes.get(n2)); // idempotent
						}
					}
				}
			}
		}

		// Detect and save connected components (because the whole graph is not
		// connected)
		firstNodes = connectedComponents(nodes.values());
	}


	/**
	 * Tests if two nodes can be merged.
	 * Two states of this graph must be connected by an arc if at least one
	 * sequence leads from those states to inconsistent nodes.
	 * @param n1 First node to merge
	 * @param n2 Second node to merge
	 * @param merged Map of merged nodes during this test. Initially empty.
	 * If a node n1 is merged with n2; there must be also a map from n2 to n2.
	 * @param nodes Map to nodes of this graph
	 * @return true if they can be merged, false otherwise
	 */
	private boolean isAConsistentMerge(APTA.ANode<String> n1,
			APTA.ANode<String> n2,
			Map<APTA.ANode<String>,List<APTA.ANode<String>>> merged,
			Map<APTA.ANode<String>,CNode> nodes) {

		// If they are known to be inconsistent, fail
		CNode c1 = nodes.get(n1);
		CNode c2 = nodes.get(n2);
		if (c1.hasArc(c2)) {
			return false;
		}

		// Common output Labels from these nodes: set intersection
		Set<String> sharedLabels = new HashSet<String>();
		sharedLabels.addAll(n1.getLabels());
		sharedLabels.retainAll(n2.getLabels());

		// For each output arc with the same label
		for (String label: sharedLabels) {
			boolean consistent = isAConsistentMerge(
					n1.followArc(label), n2.followArc(label), merged, nodes);
			if (!consistent) {
				return false;  // Every child must be mergeable
			}
		}

		// Prepare the map if not ready
		if (!merged.containsKey(n1)) {
			merged.put(n1, new ArrayList<>());
		}
		if (!merged.containsKey(n2)) {
			merged.put(n2, new ArrayList<>());
		}

		// Both must also be consistent with all other merges
		for (APTA.ANode<String> asN1: merged.get(n1)) { // n2 with every n1
			if (c2.hasArc(nodes.get(asN1))) {
				return false;
			}
		}
		for (APTA.ANode<String> asN2: merged.get(n2)) { // n1 with every n2
			if (c1.hasArc(nodes.get(asN2))) {
				return false;
			}
		}
					
		// Succeed: add both to merges and return true
		merged.get(n1).add(n2);
		merged.get(n2).add(n1);
		return true;
	}


	/**
	 * Finds and returns one node for each connected part of the graph.
	 * Storing just the returned set of nodes allows to keep the whole graph
	 * in memory.
	 * @param nodes The collection of all nodes (connected or not).
	 * @return A set of nodes, one for each connected component.
	 */
	private Set<CNode> connectedComponents(Collection<CNode> nodes) {

		// Queue of nodes to reach
		Set<CNode> nodesToReach = new HashSet<>(nodes);
		Set<CNode> minimalSetOfNodes = new HashSet<>();

		// Until every node has been reached
		while (!nodesToReach.isEmpty()) {

			// Start from the next node to reach
			CNode nextFirstNode = nodesToReach.iterator().next();
			Iterator<CNode> it = new DepthPreIterator(nextFirstNode);

			// Mark the connected nodes as reached
			while (it.hasNext()) {
				nodesToReach.remove(it.next());
			}

			// Save the first node
			minimalSetOfNodes.add(nextFirstNode);
		}

		return minimalSetOfNodes;
	}


	/**
	 * Create a spanning trees with a breadth-first visit.
	 * Returns a new graph which mirrors this one with loops removed.
	 * A the new graph contains just a spanning tree, found with a breadth first
	 * visit, one for each connected component.
	 * @param loops A set of arcs, in which the function will add all arcs not
	 * in the spanning trees.
	 * @return A new graph with just the spanning trees
	 */
	private ConstraintsGraph breadthSpanningTree(Set<Pair<CNode,CNode>> loops) {

		// The new graph
		ConstraintsGraph tree = new ConstraintsGraph();

		// For each connected component
		for (CNode firstNode: firstNodes) {

			// Old nodes added to the queue, map to new nodes
			Map<CNode,CNode> added = new HashMap<>();

			// Pairs of (old,new) nodes to process
			Queue<Pair<CNode,CNode>> toExpand = new LinkedList<>();

			// Init
			CNode newFirst = new CNode(firstNode);
			tree.firstNodes.add(newFirst);
			toExpand.offer(new Pair<>(firstNode, newFirst));
			added.put(firstNode, newFirst);

			// Loop
			while (!toExpand.isEmpty()) {

				// Expand next
				Pair<CNode,CNode> next = toExpand.poll();
				CNode oldN = next.left;
				CNode newN = next.right;
				for (CNode child: oldN.getArcs()) {

					// If new child: clone and expand
					if (!added.containsKey(child)) {
						// Clone
						CNode newChild = new CNode(child);
						newN.addArc(newChild);
						// Expand
						toExpand.offer(new Pair<>(child, newChild));
						added.put(child, newChild);
					}
					else {
						// Old. Add to loops if not in the spanning tree already
						CNode newChild = added.get(child);
						if (!newN.hasArc(newChild) &&
								!loops.contains(new Pair<>(newChild, newN))) {
							loops.add(new Pair<>(newN, newChild));
						}
					}
				}
			}
		}

		return tree;
	}


	/**
	 * Returns the Latex tree representation.
	 * Depth first visit of the tree. First part of the helper function.
	 * NOTE: assuming the graph connected to node is a tree!
	 * @param stringB The string representation: modified in place,initally empty
	 * @param node The current node: initially root
	 * @param parent The parent node, initially null.
	 * @return Latex code
	 * @see ConstraintsGraph#getLatexGraphRepresentation
	 */
	private static void buildLatexRepresentation1(StringBuilder stringB,
			CNode node, CNode parent) {

		// Add the node id in a new line
		stringB.append("\n\t\t");
		stringB.append(node.id).append(' ');

		// Add the response
		switch (node.response) {
			case ACCEPT:
				stringB.append("[accept] ");
				break;
			case REJECT:
				stringB.append("[reject] ");
				break;
			default:
		}

		// All other arcs
		Set<CNode> arcs = new HashSet<>();
		arcs.addAll(node.getArcs());
		arcs.remove(parent);

		// Base case: no children
		if (arcs.isEmpty()) { return; }

		// Recursion
		stringB.append("-- {");
		int i = arcs.size();
		for (CNode n: arcs) {
			buildLatexRepresentation1(stringB, n, node);
			--i;
			char sep = (i > 0) ? ',' : '}';
			stringB.append(sep);
		}
	}


	/**
	 * Build LaTex tree representation.
	 * Second part of the helper function: the loops are printed.
	 * @param stringB The string representation, result is appended.
	 * @param loops Arcs to print.
	 * @see DFA#getLatexGraphRepresentation
	 */
	private static void buildLatexRepresentation2(StringBuilder stringB,
			Set<Pair<CNode,CNode>> loops) {

		stringB.append("\n");

		// Writing edges one by one.
		for (Pair<CNode,CNode> arc: loops) {

			// If this is a self loop: impossible for this type of graph
			if (arc.left == arc.right) {
				stringB.append("\t\t").
						append(arc.left.id).
						append(" -- [clear >, self loop] ").
						append(arc.left.id).
						append(",\n");
			}
			// Else, we go to some previous node
			else {
				stringB.append("\t\t").
						append(arc.left.id).
						append(" -- [clear >, backward] ").
						append(arc.right.id).
						append(",\n");
			}
		}
		stringB.delete(stringB.length()-2, stringB.length());
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

		// Create the graph
		createFromAPTA(true);

		// Counts
		for (APTA.ANode<String> node: apta) {
			labels.addAll(node.getLabels());
		}
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
		return new CompleteGraphIterator();
	}


	/**
	 * Accumulates and returns the set of all edges in the graph.
	 * Every edge is a constraint: the two nodes must have different colors.
	 * @return Edges: pairs of nodes
	 */
	public Set<Pair<CNode,CNode>> constraints() {

		// Add all arcs once
		Set<Pair<CNode,CNode>> edges = new HashSet<>();
		for (CNode node: this) {
			for (CNode child: node.getArcs()) {
				if (!edges.contains(new Pair<>(child, node))) {
					edges.add(new Pair<>(node, child));
				}
			}
		}

		return edges;
	}


	/**
	 * Returns the set of all labels in the input APTA.
	 * @return The set of labels
	 */
	public Set<String> allLabels() {
		return Collections.unmodifiableSet(labels);
	}


	/**
	 * The number of states in the APTA.
	 * @return The number of states in the APTA.
	 */
	public int numberOfInputNodes() {
		return aptaStatesNum;
	}


	/**
	 * Returns the Latex graph representation
	 * @return Latex code
	 * @see LatexPrintableGraph
	 */
	@Override
	public String getLatexGraphRepresentation() {

		// Structures
		StringBuilder stringB = new StringBuilder();
		Set<Pair<CNode,CNode>> loops = new HashSet<>();

		// Find the spanning trees
		ConstraintsGraph trees = breadthSpanningTree(loops);

		// Print spanning trees
		for (CNode firstNode: trees.firstNodes) {
			buildLatexRepresentation1(stringB, firstNode, null);
			stringB.append(',');
		}

		// Print loops
		buildLatexRepresentation2(stringB, loops);

		return stringB.toString();
	}


	/**
	 * Nothing needed here.
	 * @see LatexPrintableGraph
	 */
	@Override
	public String extraLatexEnv() {
		return null;
	}


	/**
	 * Nothing needed here.
	 * @see LatexPrintableGraph
	 */
	@Override
	public String standaloneClassLatexOptions() {
		return "";
	}


		/**
	* Return a set of nodes forming a clique in the ConstraintsGraph
	* @return A clique found in ConstrainstGraph
	*/
	public Set<CNode> getClique() {
		Set<CNode> acceptableClique = new HashSet<>();

		int maxDegree = 0;
		int degree = 0;
		CNode nodeMaxDegree = null;

		// Find node with the highest degree
		for (CNode n : getAcceptingNodes()) {
			for (CNode a : n.getArcs()) {
				if (getAcceptingNodes().contains(a)) {
					degree++;
				}
			}
			if (degree >= maxDegree) {
				maxDegree = degree;
				nodeMaxDegree = n;
			}
			degree = 0;
		}

		// Add it to the clique
		acceptableClique.add(nodeMaxDegree);

		// Search between its neighbors to find the other nodes in the clique
		while (nodeMaxDegree != null) {
			nodeMaxDegree = getHighestDegreeNeighbor(nodeMaxDegree, acceptableClique, true);
			if (nodeMaxDegree != null) {
				acceptableClique.add(nodeMaxDegree);
			}
		}

		Set<CNode> rejectableClique = new HashSet<>();

		maxDegree = 0;
		degree = 0;
		nodeMaxDegree = null;

		// Find node with the highest degree
		for (CNode n : getRejectingNodes()) {
			for (CNode a : n.getArcs()) {
				if (getRejectingNodes().contains(a)) {
					degree++;
				}
			}
			if (degree >= maxDegree) {
				maxDegree = degree;
				nodeMaxDegree = n;
			}
			degree = 0;
		}

		// Add it to the clique
		rejectableClique.add(nodeMaxDegree);

		// Search between its neighbors to find the other nodes in the clique
		while (nodeMaxDegree != null) {
			nodeMaxDegree = getHighestDegreeNeighbor(nodeMaxDegree, rejectableClique, false);
			if (nodeMaxDegree != null) {
				rejectableClique.add(nodeMaxDegree);
			}
		}

		acceptableClique.addAll(rejectableClique);

		return acceptableClique;
	}

	/**
	* Return the neighbor with the highest degree in a clique 
	* @param currentNode The node whose neighbor we want to find
	* @param nodesInClique The set of nodes already in the clique
	* @param acc Signals if we want a neighbor of accepting or rejecting nodes
	* @return The neighbor with the highest degree in a clique 
	*/
	public CNode getHighestDegreeNeighbor(CNode currentNode, Set<CNode> nodesInClique, boolean acc) {
		int maxDegree = 0;
		int degree = 0;
		CNode nodeMaxDegree = null;

		for (CNode node : currentNode.getArcs()) {
			if (!getAcceptingNodes().contains(node) && acc) {
				continue;
			}
			if (!getRejectingNodes().contains(node) && !acc) {
				continue;
			}
			boolean inClique = true;
			for (CNode n : nodesInClique) {
				if (n != currentNode) {
					if (!n.getArcs().contains(node)) {
						inClique = false;
						break;
					}
				}
			}
			if (inClique) {
				for (CNode a : node.getArcs()) {
					if (getAcceptingNodes().contains(a) && acc) {
						degree++;
					} else if (getRejectingNodes().contains(a) && !acc) {
						degree++;
					}
				}
				if (degree > maxDegree) {
					maxDegree = degree;
					nodeMaxDegree = node;
				}
				degree = 0;
			}
		}

		return nodeMaxDegree;
	}


	/**
	 * Debugging
	 */
	public static void test() {

		// Build an APTA
		APTA<String> tree = new APTA<>();

		//String[] stringsToAdd = { "ciao", "ciar", "ci", "ca", ""};
		//String[] stringsToAdd = { "aaa", "baa", "ba"};
		String[] stringsToAdd = { "abaa", "abb", "a", "b", "bb"};
		boolean[] ok = { true, false, true, false, true };

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

		// Save in Latex
		LatexSaver.saveLatexFile(graph, new File("latex/graph_c.tex"), 1);

		// Testing set of states: ok
		
		// Testing the set of edges: ok
		System.out.println(graph.constraints().size() + " constraints");

		// Testing iterators: ok
		int nodes = 0;
		for (CNode n: graph) {
			nodes++;
		}
		System.out.println(nodes + " nodes\n");
	}


	// >>> Nested classes
	
	/**
	 * Iterator for all nodes in this graph.
	 * Returns all nodes, even disconnected parts.
	 */
	private class CompleteGraphIterator implements Iterator<CNode> {

		/* Iterator for all components  */
		private Iterator<CNode> partsIt = firstNodes.iterator();

		/* Iterator within each connected component */
		private Iterator<CNode> connectedPartIt = null;

		/* Update iterators */
		private void moveToNext() {
			if (partsIt.hasNext()) {
				if (connectedPartIt == null || !connectedPartIt.hasNext()) {
					connectedPartIt = new DepthPreIterator(partsIt.next());
				}
			}
		}

		@Override
		public boolean hasNext() {
			moveToNext();
			return connectedPartIt.hasNext();
		}

		@Override
		public CNode next() {
			moveToNext();
			return connectedPartIt.next();
		}
	}


	/**
	 * Iterator class for depth-visit of a connected graph.
	 * Nodes are returned in pre-order.
	 * Just the connected component of the given node is returned.
	 */
	private class DepthPreIterator implements Iterator<CNode> {

		/* Stack of remaining nodes */
		private Stack<CNode> nodesLeft = new Stack<>();

		/* Set of visited states */
		private Set<CNode> visited = new HashSet<>();


		/* Constructor */
		public DepthPreIterator(CNode firstNode) {
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
	 * Only the outer class can modify or make new nodes.
	 */
	public static class CNode {

		// >>> Fields
		
		/* Identifier */
		public final int id;

		/* Each state is labelled with a Response, like an APTA */
		public final APTA.Response response;

		/* Graph structure: unlabelled arcs */
		private final Set<CNode> arcs = new HashSet<CNode>();


		// >>> Private functions

		/**
		 * Constructor.
		 * The only way of making a new node is to copy an existing APTA node
		 * @param node An existing APTA node to copy
		 */
		private CNode(APTA.ANode<String> node) {
			this.id = node.id;
			this.response = node.getResponse();
		}
		
		
		/**
		 * Constructor.
		 * A new (disconnected) node with the same response and id.
		 * @param node An existing node to copy
		 */
		private CNode(CNode node) {
			this.id = node.id;
			this.response = node.response;
		}
		
		
		/**
		 * Disconnects two nodes.
		 * @param node The node to disconnect.
		 * @return true if the node has been removed, false if there was no node.
		 */
		private boolean removeArc(CNode node) {

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
		private void addArc(CNode node) {

			// Checks
			if (node == null) {
				throw new IllegalArgumentException("Null argument");
			}

			arcs.add(node);
			node.arcs.add(this);
		}


		// >>> Public functions
		
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


		@Override
		public String toString() {
			return id + "_" + response.toString();
		}
	}
}
