
/* Class implementing binary variables */
public class Variable {

		private int index;

		private boolean assignment = false;

		public Variable(int i) {
			this.index = i;
		}

		public int getIndex() {
			return index;
		}

		public boolean isTrue() {
			return assignment;
		}

		public boolean isFalse() {
			return !assignment;
		}

		public void assign(boolean value) {
			assignment = value;
		}

		@Override
		public String toString() {
			return "x_" + index;
		}

		public boolean equal(Object o) {
			if (o == null) {
          return false;
      }

      if (o == this) {
          return true;
      }

      if (!getClass().equals(o.getClass())) {
          return false;
      }

      return index == ((Variable) o).index;
		}
}