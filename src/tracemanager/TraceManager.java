
package tracemanager;

import java.io.*;
import java.util.*;

import org.deckfour.xes.model.*;
import org.deckfour.xes.in.*;

import automata.APTA;


/**
 * TraceManager class.
 * It loads a single file in XES format, containing a sequence of events.
 * Allows to add the sequences in an APTA data structure.
 * @see automata.APTA
 */
public class TraceManager {

	// >>> Fields

	private File file;
	

	// >>> Constructors

	/**
	 * Constructor
	 * @param pathname The path as a string
	 */
	public TraceManager(String pathname) {
		this.file = new File(pathname);
	}

	/**
	 * Constructor
	 * @param file The path as a File
	 */
	public TraceManager(File file) {
		this.file = file;
	}


	// >>> Private methods

	/**
	 * Returns a list of all the logs on the file.
	 * @return The list of logs
	 */
	private List<XLog> getLogs() {
		XesXmlParser parser = new XesXmlParser();
		List<XLog> xLogs = null;

		try {
			xLogs = parser.parse(file);
		} catch (Exception e) { // XesXmlParser declares to throw Exception
			System.err.println(e.getMessage());  // Usually this is file not found
			System.exit(1);
		}

		return xLogs;
	}

	/**
	 * Returns a list of all the traces in the logs of the file.
	 * Each trace is a list of labels (strings).
	 * @return The parsed traces
	 */
	private List<List<String>> getTraces() {
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

	// >>> Public methods

	/**
	 * Set a new file to be read.
	 * @param pathname The path as a string.
	 */
	public void setFile(String pathname) {
		this.file = new File(pathname);
	}


	/**
	 * Set a new file to be read.
	 * @param file The path as a File.
	 */
	public void setFile(File file) {
		this.file = file;
	}


	/**
	 * Extends the given tree to accept or reject new traces.
	 * Adds the traces of the XES file to the given APTA.
	 * If a file has "OK" in its name, those traces are added as "good" ones,
	 * otherwise they are marked as "bad".
	 * @param tree An APTA structure
	 * @see automata.APTA
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


	/**
	 * Static utility to load all .xes files in a directory.
	 * Every .xes file in this directory (not its subdirs) is parsed an added
	 * to a single APTA. If a file has "OK" in its name, those traces are added
	 * as "good" ones, otherwise they are marked as "bad".
	 * @param dir The directory
	 * @return The complete APTA
	 * @see automata.APTA
	 */
	public static APTA<String> parseTracesFiles(File dir) {

		// Checks
		if (!dir.exists() || !dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a valid directory");
		}

		// Filter the .xes files
		FileFilter xesFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xes");
			}
		};

		// Check some traces exist
		if (dir.listFiles(xesFilter).length == 0) {
			throw new RuntimeException("No .xes files found");
		}

		// New APTA
		APTA<String> apta = new APTA<>();

		// Add all xes files
		for (File file: dir.listFiles(xesFilter)) {
			TraceManager tm = new TraceManager(file);
			tm.addTracesToAPTA(apta);
		}

		return apta;
	}

		/**
	 * Static utility to load all .xes files in a directory.
	 * Every .xes file in this directory (not its subdirs) is added to a list
	 * of traces.
	 * @param dir The directory
	 * @return All the traces
	 */
	public static List<List<String>> getTracesFiles(File dir) {

		// Checks
		if (!dir.exists() || !dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a valid directory");
		}

		// Filter the .xes files
		FileFilter xesFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".xes");
			}
		};

		List<List<String>> traces = new ArrayList<>();

		// Add all xes files
		for (File file: dir.listFiles(xesFilter)) {
			TraceManager tm = new TraceManager(file);
			traces.addAll(tm.getTraces());
		}

		return traces;
	}
}
