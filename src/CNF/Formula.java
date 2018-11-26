
import java.util.*;

/* Class implementing propositional formulas */
public class Formula {
	
	private List<Clause> clauseList = new ArrayList<>();

	public void addClause(Clause c) {
		clauseList.add(c);
	}

	public void removeClause(Clause c) {
		clauseList.remove(c);
	}

	public boolean isSatisfied() {
		for (Clause c : clauseList) {
			if (!c.isSatisfied()) {
				return false;
			}
		}
		return true;
	}

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