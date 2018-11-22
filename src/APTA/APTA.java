
package APTA;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Set;


/**
 * Class for the Augmented Prefix Tree Acceptor (a tree)
 */
public class APTA<LabelT> {

	// >>> Fields
	
	/* The root */
	private final TNode<LabelT> root = new TNode<LabelT>(0);

	/* Sets and counters */
	private long nodesNum = 1;    // The root is the initial node


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


	// >>> Public functions

	/**
	 * Debugging
	 */
	public static void test() {

		// Build a tree
		APTA<Character> tree = new APTA<Character>();
		TNode<Character> n1 = tree.newChild(tree.root, 'a');
		TNode<Character> n2 = tree.newChild(tree.root, 'b');
		TNode<Character> n3 = tree.newChild(n1, 'c');
		TNode<Character> n4 = tree.newChild(n2, 'c');
		TNode<Character> n5 = tree.newChild(n2, 'd');
		n5.setResponse(TNode.Response.ACCEPT);
		n2.setResponse(TNode.Response.REJECT);

		// Test connections: traverse all the tree
		TNode<Character> n = tree.root.followArc('a').followArc('c');
		System.out.println(n); // Should be 3
		n = n.getParent().getParent();
		System.out.println(n); // Should be root: 0
		System.out.println(n.getParent()); // Should be null
		n = n.followArc('b').followArc('d');
		System.out.println(n.getParentLabel()); // Should be 'd'
		System.out.println(n.followArc('n')); // Should be null
		System.out.println();

		// Test determinism
		TNode<Character> n6 = tree.newChild(n2, 'c');
		n = tree.root.followArc('b').followArc('c');
		System.out.println(n); // Should be 6
		System.out.println(n.getParent()); // Should be 2
		System.out.println(n4.getParent()); // Should be null

	}
}
