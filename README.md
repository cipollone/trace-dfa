
# Trace-DFA

Finds the minimum Deterministic Finite-State Automaton (*DFA*) that is consistent with the given sequences to accept and to reject.

The program reads .xes files and uses these logs as input sequences.

This project implements: Marijn J. H. Heule and Sicco Verwer. 2010. Exact DFA identification using SAT solvers.


## Dependencies

* Install ant
* Download OpenXes
* Download Guava
* Download Sat4j solver
* Download LTL2Automaton

Put the downloaded libraries (usually as jars) in a directory named "lib" in the top level directory of the project (the one containing "build.xml").
LTL2Automaton has been added as a dependency just because a DFA can be exported as an automaton of this library. This requirement can be deleted by removing the import/export functionality in DFA.java.

## Input sequences

The input of the program is composed of two sets of sequences: one for learning the DFA and one for testing. All sequences must be in XES format. By default, the program will use the directory "traces/train" for learning and "traces/test" for testing. However one may specify arbitrary paths when running the program, using options:

    ant -Dtrain=any_train_dir -Dtest=any_test_dir

Each sequence in the log is a trace in XES format composed of events. Just the name of the events are used to identify transitions.  Add "OK" in the filename of XES files containing sequences to be accepted by the DFA. Sequences to reject need no modification.

## Run

Run with `ant`. To genereate the documentation, run `ant doc`.

## Output DFA

The program will create a directory "output" with tree Latex files:
* *dfa.tex* is the main output of the program. This is the extracted Finite State Automaton which is consistent with the given traces.
* *apta.tex* is the APTA the algorithm uses internally to represent the input traces (to learn). For many traces you won't be able to compile this, due to space limitation on the page.
* *constraints.tex* is the graph of constraints the algorithm internally uses to represent the constraints in the coloring problem. This also could exceed Tex limitations.
