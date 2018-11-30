
package cnf;

import java.util.*;

/* Class implementing propositional formulas */
public class Formula {

	// >>> Fields
	
	private Set<Clause> clauseList = new HashSet<>();

	// >>> Public functions

	/**
	* Add a new clause to the formula
	* @param c The clause to be added
	*/
	public void addClause(Clause c) {
		clauseList.add(c);
	}

	/**
	* Remove a clause from the formula
	* @param c The clause to be removed
	*/
	public void removeClause(Clause c) {
		clauseList.remove(c);
	}

	/**
	* Returns if the formula is satisfied or not
	*/
	public boolean isSatisfied() {
		for (Clause c : clauseList) {
			if (!c.isSatisfied()) {
				return false;
			}
		}
		return true;
	}

	/**
	* Returns the total number of clauses
	*/
	public int getClauseNum() {
		return clauseList.size();
	}

	/**
	* Override of the toString method
	*/
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		String separator = "";
		for (Clause c : clauseList) {
			sb.append(separator);
			separator = " and ";
			sb.append(c.toString());
		}
		sb.append("}");
		return sb.toString();
	}
}
