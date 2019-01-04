
package cnf;

import java.util.*;

/**
* Class implementing propositional formulas 
*/
public class Formula
		implements Iterable<Clause> {

	// >>> Fields
	
	private Set<Clause> clauseList = new HashSet<>();

	// >>> Public functions

	/**
	* Add clauses to the formula
	* @param clauses The clauses to be added
	*/
	public void addClause(Clause... clauses) {
		for (Clause c: clauses) {
			clauseList.add(c);
		}
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
	* @return if the formula is satisfied or not
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
	* @return the total number of clauses
	*/
	public Set<Clause> getClauseList() {
		return Collections.unmodifiableSet(clauseList);
	}

	/**
	 * Iterator of clauses
	 * @return Iterator
	 */
	public Iterator<Clause> iterator() {
		return clauseList.iterator();
	}

	/**
	* Returns the total number of clauses
	* @return the total number of clauses
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
			separator = " A \n"; // \u2227 for and symbol
			sb.append(c.toString());
		}
		sb.append("}\n");
		return sb.toString();
	}
}
