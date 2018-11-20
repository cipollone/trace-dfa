

public class Main {
	public static void main(String args[]) {
		Formula f = new Formula();
		Clause c1 = new Clause(1);
		Clause c2 = new Clause(2);
		Clause c3 = new Clause(3);
		Clause c4 = new Clause(4);
		Variable x1 = new Variable(1);
		Variable x2 = new Variable(2);
		Variable x3 = new Variable(3);
		Variable x4 = new Variable(4);
		Variable x5 = new Variable(5);
		Variable x6 = new Variable(6);
		Variable x7 = new Variable(7);
		Variable x8 = new Variable(8);
		c1.addPositiveVariable(x1);
		c1.addPositiveVariable(x3);
		c1.addPositiveVariable(x6);
		c1.addPositiveVariable(x7);
		c1.addPositiveVariable(x8);
		c1.addNegatedVariable(x2);
		c1.addNegatedVariable(x4);
		c1.addNegatedVariable(x5);
		c2.addPositiveVariable(x4);
		c2.addPositiveVariable(x6);
		c2.addPositiveVariable(x8);
		c2.addNegatedVariable(x1);
		c3.addNegatedVariable(x2);
		c3.addNegatedVariable(x3);
		c4.addPositiveVariable(x7);
		f.addClause(c1);
		f.addClause(c2);
		f.addClause(c3);
		f.addClause(c4);
		System.out.println(f.toString());
	}
}
