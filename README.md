
# Trace-DFA

Finds the minimum Deterministic Finite-State Automaton (*DFA*) that is consisted the given accepted and rejected sequences.

The program reads .xes files and uses that logs as input sequences.

This project is a small implementation of: Marijn J. H. Heule and Sicco Verwer. 2010. Exact DFA identification using SAT solvers.


## Dependencies

* Install ant
* Download OpenXes
* Download Guava
* Download Sat4j solver

Put OpenXes, Guava and Sat4j in a directory named "lib" in the top level directory of the project (the one containing "build.xml").


## Input sequences

The input of the program is composed of two sets of sequences: one for learning the DFA and one for testing. All sequences must be in XES format. By default, the program will use the directory "traces/train" for learning and "traces/test" for testing. However one may specify arbitrary paths when running the program, using options:

    ant -Dtrain=any_train_dir -Dtest=any_test_dir

Add "OK" in the filename of XES files containing sequences to be accepted by the DFA. Sequences to reject need no modification.

## Run

Run with `ant`. To genereate the source documentation, run `ant doc`.

## Output DFA

The program will create a directory "latex" with two Latex files: apta.tex and dfa.tex. When compiled, they generate two PDFs: one contains the APTA the algorithm uses to represent the input traces (to learn); the other represents the DFA that is the result of learning.
