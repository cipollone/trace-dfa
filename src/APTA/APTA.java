
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
 * Class for the Augmented Prefix Tree Acceptor (a tree).
 * LabelT should allow a simple conversion with the toString method.
 */
public class APTA<LabelT>
		implements Iterable<TNode<LabelT>> {

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
	 * Build LaTex tree representation.
	 * Depth first visit of the tree. Helper function of saveLatexFile()
	 * NOTE: assuming the labels are not Latex special codes.
	 * NOTE: assuming the labels have a nice string representation.
	 * @param stringB The string representation: modified in place,initally empty
	 * @param node The current node: initially root
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

		// Recursion with one child
		if (labels.size() == 1) {
			stringB.append("->");
			TNode<LabelT> child = node.followArc(labels.iterator().next());
			buildLatexRepresentation(stringB, child);
		}
		// Recurtion with more than one children
		else {
			stringB.append("-> {");

			// Iterate all but the last one
			int labelsNum = labels.size() - 1;
			Iterator<LabelT> labelIt = labels.iterator();
			while (labelsNum > 0) {
				buildLatexRepresentation(stringB, node.followArc(labelIt.next()));
				stringB.append(",");
				labelsNum--;
			}

			// Write the last one
			buildLatexRepresentation(stringB, node.followArc(labelIt.next()));
			stringB.append("}");
		}
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
	 * Utility function to visualize small trees with Latex files.
	 * The code produced is simple: it may only work with small trees.
	 * If any error occurs when writing the file, false is returned.
	 * NOTE: texFilePath is writted/overwritten. Parent folders are created if
	 * necessary.
	 * NOTE: assuming iterator() returns a pre-order depth first collection of
	 * nodes.
	 * @param texFile The path of the .tex file
	 * @return True if no errors occurred
	 */
	public boolean saveLatexFile(File texFile) {

		// Latex file template
		String latexFilePart1 =
			"% Minimal LaTeX file for apta specification:\n" +
			"%   use Tikz graph syntax between the two parts.\n" +
			"% >> part1\n" +
			"\\documentclass[tikz]{standalone}\n" +
			"\\usepackage[T1]{fontenc}\n" +
			"\\usepackage[utf8]{inputenc}\n" +
			"\\usetikzlibrary{graphs}\n" +
			"\\usetikzlibrary{arrows}\n" +
			"\\usetikzlibrary{quotes}\n" +
			"\\begin{document}\n" +
			"\\begin{tikzpicture}[\n" +
			"			accept/.style={double},\n" +
			"			reject/.style={fill=gray}\n" +
			"	]\n" +
			"	\\graph [\n" +
			"			grow right sep=2em,\n" +
			"			nodes={draw,circle},\n" +
			"			edges={>=stealth'},\n" +
			"			edge quotes={auto}\n" +
			"			]{\n" +
			"% >> end of part1\n";
		String latexFilePart2 = 
			"\n" +
			"% >> part2\n" +
			"	};\n" +
			"\\end{tikzpicture}\n" +
			"\\end{document}\n";

		// Write to TeX to file
		BufferedWriter fileW = null;
		try {

			// Create the file
			File parentDir = texFile.getParentFile();
			if (parentDir != null) { parentDir.mkdirs(); }
			fileW = new BufferedWriter(new FileWriter(texFile));

			// Write the first part
			fileW.write(latexFilePart1);

			// Write the representation of the tree
			StringBuilder stringB = new StringBuilder();
			buildLatexRepresentation(stringB, root);
			fileW.write(stringB.toString());

			// Write the second part
			fileW.write(latexFilePart2);

			// just exception handling below
		} catch (IOException e) {
			return false;
		} finally {
			if (fileW != null) {
				try {
					fileW.close();
				} catch (IOException e) {}
			}
		}

		return true;
	}


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
		TNode<Character> n6 = tree.newChild(n4, 'f');
		n5.setResponse(TNode.Response.ACCEPT);
		n2.setResponse(TNode.Response.REJECT);

		// Test iterator
		for (TNode<Character> n: tree) {
			System.out.println(n);
		}

		// Test latex representation
		tree.saveLatexFile(new File("latex-tree/tree.tex"));
	}
}
