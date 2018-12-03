
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


	/**
	 * Debug
	 */
	public static void test() {

		List<Variable> x = Variable.newVars("x_0" , "x_1" , "x_2" , "x_3" , "x_4"
				, "x_5" , "x_6" , "x_7");
		Clause c1 = new Clause();
		Clause c2 = new Clause();
		Clause c3 = new Clause();
		Clause c4 = new Clause();
		c1.addPositiveVariable(x.get(0), x.get(2), x.get(5), x.get(6), x.get(7));
		c1.addNegatedVariable(x.get(1), x.get(3), x.get(4));
		c2.addPositiveVariable(x.get(3), x.get(5), x.get(7));
		c2.addNegatedVariable(x.get(0));
		c3.addNegatedVariable(x.get(1), x.get(2));
		c4.addPositiveVariable(x.get(6));
		Formula f = new Formula();
		f.addClause(c1, c2, c3, c4);
		System.out.println(f.toString());

		// Testing Clause iterators
		for (Variable v: c1.positiveVars()) {
			System.out.print("  " + v);
		}
		System.out.println();
		for (Variable v: c1.negatedVars()) {
			System.out.print("  " + v);
		}
		System.out.println("\n");

		// Testing Formula iterator
		for (Clause c: f) {
			System.out.println(c);
		}
	}
}
