
package automata;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;


/**
 * Utility class for visualizing graphs with Latex files.
 * @see LatexPrintableGraph
 */
public final class LatexSaver {


	/* Can't instantiate this class: utility */
	private LatexSaver() {}


	/**
	 * Writes a Latex file representing the given graph.
	 * If any error occurs when writing the file, false is returned.
	 * NOTE: This may not work with large trees due to constraints on the page size.
	 * NOTE: texFilePath is writted/overwritten. Parent folders are created if
	 * necessary.
	 * @param graph A printable graph
	 * @param texFile The path of the .tex file
	 * @param spaceCm The horizontal spacing of the nodes, in cm.
	 * @return True if no errors occurred
	 */
	public static boolean saveLatexFile(LatexPrintableGraph graph,
			File texFile, float spaceCm) {

		// Package options
		String classOptions = graph.standaloneClassLatexOptions();

		// Latex file template
		String latexFilePart1 =
			"% Minimal LaTeX file for apta specification: \n" +
			"%   use Tikz graph syntax between the two parts. \n" +
			"% >> part1 \n" +
			"\\documentclass" + classOptions + "{standalone} \n" +
			"\\usepackage[T1]{fontenc} \n" +
			"\\usepackage[utf8]{inputenc} \n" +
			"\\usepackage{tikz} \n" +
			"\\usetikzlibrary{graphs,arrows,quotes,positioning} \n" +
			"\\standaloneconfig{margin=2em} \n" +
			"\\begin{document} \n" +
			"\\centering \n" +
			"\\begin{tikzpicture}[ \n" +
			"			>=stealth', \n" +
			"			accept/.style={double}, \n" +
			"			reject/.style={fill=gray}, \n" +
			"			backward/.style={bend right}, \n" +
			"			self loop/.style={to path={ \n" +
			"			.. controls +(70:1) and +(110:1) .. (\\tikztotarget) \\tikztonodes}} \n" +
			"	] \n" +
			"	\\graph [ \n" +
			"			grow right sep=" + spaceCm + "cm, \n" +
			"			branch down sep=0.6cm, \n" +
			"			nodes={draw,circle}, \n" +
			"			edge quotes={font={\\scriptsize}, inner sep=0.3ex, auto=right, pos=0.35} \n" +
			"			]{ \n" +
			"% >> end of part1 \n";
		String latexFilePart2 = 
			"\n" +
			"% >> part2 \n" +
			"	}; \n" +
			"\\end{tikzpicture} \n" +
			"% >> end of part2 \n";

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
			fileW.write(graph.getLatexGraphRepresentation());

			// Write the second part
			fileW.write(latexFilePart2);

			// Write extra stuff
			String extraEnv = graph.extraLatexEnv();
			if (extraEnv != null) {
				fileW.write(extraEnv);
			}

			// End of file
			fileW.write("\n\\end{document}\n");

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
}
