
import automata.*;
import tracemanager.TraceManager;

import java.io.File;

public class Main {
	public static void main(String args[]) {

		APTA<String> apta = TraceManager.parseTracesFiles(new File("traces"));
		LatexSaver.saveLatexFile(apta, new File("latex/traces_apta.tex"), 3);

	}
}
