
package identification;

import java.util.*;
import java.io.File;

import automata.*;
import util.Pair;


/**
 * This class represents the graph of direct constraints in a DFA
 * identification problem.
 *
 * An algorithm which merges states of an input graph (in this case the APTA)
 * needs to know which pair of states can't be merged. The direct constraints
 * are a subset of those: they connect states with different labellings
 * (accepting and rejecting).
 * Every query on the constraints graph, is processed with queries on the
 * APTA.
 * This class also provides useful iterators in order to simplify the addition
 * of further constraints.
 * Nodes are returned by their ID not Node objects.
 * NOTE: assuming the labels of the APTA are strings.
 * NOTE: Do not change the APTA after constructing this object, or the
 * DirectConstraintsGraph will no longer reflect the APTA.
 * @see automata.APTA
 */
public class DirectConstraintsGraph {

	// >>> Fields
	
	/* Domain */
	private final APTA<String> apta;

	/* Preprocessed info */
	private int statesNum;
	private int labelsNum;
	private int acceptingStatesNum;
	private int rejectingStatesNum;
	private Set<String> labels = new HashSet<String>();


	// >>> Private functions

	/**
	 * This function sets some members of this class to reflect the APTA.
	 * It simply counts the number of labels and states in the APTA.
	 * This operation has a linear cost (time) in the number of states in the
	 * APTA.
	 */
	private void preprocessAPTA() {

		// Initialize quantities to set
		statesNum = 0;
		labelsNum = 0;
		acceptingStatesNum = 0;
		rejectingStatesNum = 0;

		// Set of distinct labels
		labels.clear();

		// Scan the graph
		for (APTA.ANode<String> node: apta) {

			// Counting states
			statesNum++;
			if (node.getResponse() == APTA.Response.ACCEPT) {
				acceptingStatesNum++;
			} else if (node.getResponse() == APTA.Response.REJECT) {
				rejectingStatesNum++;
			}

			// Counting labels
			Set<String> nodeLabels = node.getLabels();
			labels.addAll(nodeLabels);
		}

		labelsNum = labels.size();
	}


	// >>> Public functions
	
	/**
	 * Constructor.
	 * This operation has a linear cost (time) in the number of states in the APTA.
	 * @param apta An APTA which represents the problem domain: the states to be
	 * merged
	 */
	public DirectConstraintsGraph(APTA<String> apta) {

		// Initialization
		this.apta = apta;
		preprocessAPTA();
	}
	

	/**
	 * Size of the input domain: number of nodes.
	 * Remember: positive states + negative states != states.
	 * @return The number of states
	 */
	public int numberOfStates() {
		return statesNum;
	}

	
	/**
	 * Number of distinct labels among all transitions.
	 * @return Number of labels
	 */
	public int numberOfLabels() {
		return labelsNum;
	}


	/**
	 * Number of accepting states
	 * @return Number of states
	 */
	public int numberOfAcceptingStates() {
		return acceptingStatesNum;
	}

	
	/**
	 * Number of rejecting states
	 * @return Number of states
	 */
	public int numberOfRejectingStates() {
		return rejectingStatesNum;
	}


	/**
	 * Returns the set of all labels in the input graph
	 * @return The set of labels
	 */
	public Set<String> allLabels() {
		return Collections.unmodifiableSet(labels);
	}


	/**
	 * Returns an Iterable object containing all positive states.
	 * @return An Iterable
	 */
	public Iterable<Integer> acceptingStates() {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new AcceptingStatesIterator();
			}
		};
	}


	/**
	 * Returns an Iterable object containing all negative states.
	 * @return An Iterable
	 */
	public Iterable<Integer> rejectingStates() {
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new RejectingStatesIterator();
			}
		};
	}


	/**
	 * Returns an Iterable with all direct constraints (arcs).
	 * Returns a positive and a negative state, in this order
	 * @return A positive and a negative pair
	 */
	public Iterable<Pair<Integer,Integer>> directConstraints() {
		return new Iterable<Pair<Integer,Integer>>() {
			public Iterator<Pair<Integer,Integer>> iterator() {
				return new DirectConstraintsIterator();
			}
		};
	}
	

	/**
	 * Debugging
	 */
	public static void test() {

		System.out.println("DirectConstraintsGraph");

		// Build a tree
		APTA<String> apta = new APTA<>();

		// Sequences to add
		String[][] stringsToAdd = {
				{"ciao,", "come", "stai", "?"},
				{"ciao,", "come", "va"},
				{"ciao,", "tutto", "bene", "?"},
				{"ciao,", "sei", "sicuro"}
		};

		// Add the sequences
		for (String[] seq: stringsToAdd) {
			List<String> seqL = new ArrayList<>();
			for (String s: seq) {
				seqL.add(s);
			}

			if (seqL.get(seqL.size()-1).equals("?")) {
				apta.acceptSequence(seqL);
			} else {
				apta.rejectSequence(seqL);
			}
		}

		// Testing LatexSaver class and LatexPrintableGraph interface
		LatexSaver.saveLatexFile(apta, new File("latex/apta.tex"), 1);

		// DirectConstraintsGraph
		DirectConstraintsGraph graph = new DirectConstraintsGraph(apta);
		System.out.println("statesNum " + graph.numberOfStates());
		System.out.println("labelsNum " + graph.numberOfLabels());
		System.out.println("acceptingStatesNum " + graph.numberOfAcceptingStates());
		System.out.println("rejectingStatesNum " + graph.numberOfRejectingStates());
		System.out.println("labels " + graph.allLabels());

		// Testing iterators
		System.out.print("Accepting: ");
		for (Integer id: graph.acceptingStates()) {
			System.out.print(id + "  ");
		}
		System.out.println();

		System.out.print("Rejecting: ");
		for (Integer id: graph.rejectingStates()) {
			System.out.print(id + "  ");
		}
		System.out.println();

		System.out.print("Direct constraints: ");
		for (Pair<Integer,Integer> pair: graph.directConstraints()) {
			System.out.print(pair + "  ");
		}
		System.out.println();
	}


	// >>> Nested classes
	
	/**
	 * This iterator returns just the accepting states of this graph
	 */
	private class AcceptingStatesIterator 
			implements Iterator<Integer> {

		Iterator<APTA.ANode<String>> aptaIt = apta.iterator();
		Integer nextId = null;

		/* Moves the internal iterator to the next valid element */
		private void moveToNext() {

			nextId = null;
			while (aptaIt.hasNext()) {
				APTA.ANode<String> node = aptaIt.next();
				if (node.getResponse() == APTA.Response.ACCEPT) {
					nextId = node.id;
					break;
				}
			}
		}

		public AcceptingStatesIterator() {
			moveToNext();
		}

		@Override
		public boolean hasNext() {
			return nextId != null;
		}

		
		@Override
		public Integer next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			Integer id = nextId;
			moveToNext();
			return id;
		}
	}


	/**
	 * This iterator returns just the rejecting states of this graph
	 */
	private class RejectingStatesIterator 
			implements Iterator<Integer> {

		Iterator<APTA.ANode<String>> aptaIt = apta.iterator();
		Integer nextId = null;

		/* Moves the internal iterator to the next valid element */
		private void moveToNext() {

			nextId = null;
			while (aptaIt.hasNext()) {
				APTA.ANode<String> node = aptaIt.next();
				if (node.getResponse() == APTA.Response.REJECT) {
					nextId = node.id;
					break;
				}
			}
		}

		public RejectingStatesIterator() {
			moveToNext();
		}

		@Override
		public boolean hasNext() {
			return nextId != null;
		}

		
		@Override
		public Integer next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			Integer id = nextId;
			moveToNext();
			return id;
		}
	}


	/**
	 * This iterator returns pairs of nodes (IDs) which appear in a direct 
	 * constraint (positive and negative).
	 * Returns a positive and a negative state, in this order
	 */
	private class DirectConstraintsIterator 
			implements Iterator<Pair<Integer,Integer>> {

		                                                               // Cycles:
		Iterator<Integer> acceptingIt = new AcceptingStatesIterator(); // outer
		Iterator<Integer> rejectingIt = new RejectingStatesIterator(); // inner
		Integer nextAccepting = null;
		Integer nextRejecting = null;

		/* Moves the internal iterators to the next valid elements */
		private void moveToNext() {

			nextRejecting = null;

			if (!rejectingIt.hasNext()) {
				rejectingIt = new RejectingStatesIterator();
				nextAccepting = null;
			}
			if (rejectingIt.hasNext()) { // NOTE: this test is not useless
				nextRejecting = rejectingIt.next();
			}
			if (nextAccepting == null && acceptingIt.hasNext()) {
				nextAccepting = acceptingIt.next();
			}
		}

		public DirectConstraintsIterator() {
			moveToNext();
		}

		@Override
		public boolean hasNext() {
			return (nextAccepting != null && nextRejecting != null);
		}
		
		@Override
		public Pair<Integer,Integer> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			Pair<Integer,Integer> pair = new Pair<>(nextAccepting, nextRejecting);
			moveToNext();
			return pair;
		}
	}
}
