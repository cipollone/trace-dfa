
package cnf;

import java.util.*;

import java.io.*;
import java.nio.charset.*;


/**
 * This class allows to save a Formula to a test file in Dimacs format.
 * Dimacs is a simple plain text format for representing formulas in
 * Conjunctive Normal Form (an ASCII file).
 */
public class DimacsSaver {

	// >>> Fields

	private Formula cnf;

	private Map<Variable,Integer> varsToId = new HashMap<>();
	private Map<Integer,Variable> idToVars = new HashMap<>();
	private int clausesNum = 0;
	private int maxClauseSize = 0;
	private int variablesNum = 0;


	// >>> Private functions
	
	/**
	 * Sets the internal members
	 */
	private void gatherInformations() {

		int id = 1;		// IDs in a Dimacs file start from 1
		maxClauseSize = 0;
		varsToId.clear();
		idToVars.clear();

		// For each clause
		for (Clause clause: cnf) {

			// Counts
			if (clause.getVarNum() > maxClauseSize) {
				maxClauseSize = clause.getVarNum();
			}

			// Vars id
			for (Variable v: clause.positiveVars()) {
				if (!varsToId.containsKey(v)) {
					varsToId.put(v, id);
					idToVars.put(id, v);
					++id;
				}
			}
			for (Variable v: clause.negatedVars()) {
				if (!varsToId.containsKey(v)) {
					varsToId.put(v, id);
					idToVars.put(id, v);
					++id;
				}
			}
		}

		clausesNum = cnf.getClauseNum();
		variablesNum = varsToId.size();
	}
	

	/**
	 * Returns the preamble of the file: comments and declaration.
	 * @return The first section of the file
	 */
	private String getPreamble() {

		// Description
		String comments = 
				"c This file has been produced with Trace-DFA: cnf.DimacsSaver class\n" +
				"c This is the representation in a Dimacs file of a CNF formula.\n" +
				"c   variables:      " + variablesNum + "\n" +
				"c   clauses:        " + clausesNum + "\n" + 
				"c   size of the biggest clause:    " + maxClauseSize + "\n";

		// Declaration
		String preamble = "p cnf " + variablesNum + " " + clausesNum + "\n";

		return comments + preamble;
	}


	/**
	 * Returns one line representing the given clause using the unique ids.
	 * A line ends with a 0.
	 * @return A single line representing the clause
	 */
	private String clauseRepresentation(Clause c) {

		StringBuilder line = new StringBuilder();

		for (Variable v: c.positiveVars()) {
			line.append(varsToId.get(v)).append("    ");
		}
		for (Variable v: c.negatedVars()) {
			line.append('-').append(varsToId.get(v)).append("    ");
		}
		line.append("0\n");

		return line.toString();
	}


	// >>> Public functions
	
	/**
	 * Constructor.
	 * No file is written yet.
	 * @param cnf The formula to write
	 */
	public DimacsSaver(Formula cnf) {
		this.cnf = cnf;
		gatherInformations();
	}

	
	/**
	 * Save the Formula to File.
	 * @param file The file path to use
	 * @return True if no errors occurred
	 */
	public boolean saveToDimacsFile(File file) throws IOException {

		file.getParentFile().mkdirs(); // Create the path if needed

		// Create a new file
		try (FileOutputStream fileStream = new FileOutputStream(file)) {

			// Create the writer
			Charset charset = Charset.forName("ASCII"); // Dimacs is an ASCII file
			OutputStreamWriter fileWriter = new OutputStreamWriter(fileStream,
					charset);
			BufferedWriter fileW = new BufferedWriter(fileWriter);

			// Write the preamble
			fileW.write(getPreamble());

			// Write every clause
			for (Clause c: cnf) {
				fileW.write(clauseRepresentation(c));
			}

			fileW.flush();
		}

		return true;
	}


	/**
	 * Returns the map used to translate variables to IDs.
	 * @return The map
	 */
	public Map<Variable,Integer> varsToIdMap() {
		return Collections.unmodifiableMap(varsToId);
	}


	/**
	 * Returns the map used to translate IDs to variables.
	 * @return The map
	 */
	public Map<Integer,Variable> idToVarsMap() {
		return Collections.unmodifiableMap(idToVars);
	}
}
