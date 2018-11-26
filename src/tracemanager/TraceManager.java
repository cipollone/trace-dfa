
package tracemanager;

import java.io.*;
import java.util.*;

import org.deckfour.xes.model.*;
import org.deckfour.xes.in.*;

import automata.APTA;


/* TraceManager class */
public class TraceManager {

	private File file;
	
	// >>> Constructors

	public TraceManager(String pathname) {
		this.file = new File(pathname);
	}

	public TraceManager(File file) {
		this.file = file;
	}

	// >>> Public methods

	/**
	 * Set a new file to be read 
	 */
	public void setFile(String pathname) {
		this.file = new File(pathname);
	}

	/**
	 * Set a new file to be read 
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * Returns a list of all the logs on the file. In our case we have one log per file 
	 */
	public List<XLog> getLogs() {
		XesXmlParser parser = new XesXmlParser();
		List<XLog> xLogs = null;

		try {
			xLogs = parser.parse(file);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return xLogs;
	}

	/**
	 * Returns a list of all the traces in the logs of the file, 
	 * each a list of labels (strings) 
	 */
	public List<List<String>> getTraces() {
		List<XLog> xLogs = getLogs();
		List<List<String>> traces = new ArrayList<>();

		for (XLog xl : xLogs) {
			for (XTrace xt : xl) {
				List<String> trace = new ArrayList<>();
				for (XEvent xe : xt) {
					trace.add(xe.getAttributes().get("concept:name").toString());
				}
				traces.add(trace);
			}
		}

		return traces;
	}

	/**
	 * Extends the tree passed to accept or reject new traces
	 */
	public void addTracesToAPTA(APTA<String> tree) {
		List<List<String>> traces = getTraces();

		for (List<String> trace : traces) {
			if (this.file.getName().contains("OK")) {
				tree.acceptSequence(trace);
			}
			else {
				tree.rejectSequence(trace);
			}
		}
	}

}
