
package tracemanager;

import java.io.*;
import java.util.*;

import org.deckfour.xes.model.*;
import org.deckfour.xes.in.*;

import automata.APTA;
import automata.LatexSaver;


public class Test	{
	
	public static void test() {

		TraceManager tm = new TraceManager("traces/log_OK.xes");

		// TraceManager tm1 = new TraceManager("log_BAD_1.xes");

		// TraceManager tm2 = new TraceManager("log_BAD_2.xes");

		// TraceManager tm3 = new TraceManager("log_BAD_3.xes");

		// TraceManager tm4 = new TraceManager("log_BAD_4.xes");

		// System.out.println(tm.getTraces());

		APTA<String> tree = new APTA<String>();

		tm.addTracesToAPTA(tree);
		// tm1.addTracesToAPTA(tree);
		// tm2.addTracesToAPTA(tree);
		// tm3.addTracesToAPTA(tree);
		// tm4.addTracesToAPTA(tree);

		LatexSaver.saveLatexFile(tree, new File("latex/traces_apta.tex"), 2);
	}
}
