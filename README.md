
# Trace-DFA

Finds the minimum Deterministic Finite-State Automaton (*DFA*) that is
consisted the given accepted and rejected sequences.

The program reads .xes files and uses that logs as input sequences.

This project is a small implementation of: Marijn J. H. Heule and Sicco Verwer.
2010. Exact DFA identification using SAT solvers.


## Dependencies

* Install ant
* Download OpenXes
* Download Guava

Put OpenXes and Guava in a directory named "lib" in the top level directory of
the project (the one containing "build.xml").


## Input sequences
Add "OK" in the filename of each sequence to be accepted by the DFA. Sequences
to reject need no modification. All sequces must be in XES format.  Put all
.xes files in the "trace" directory in the top-level directory.

## Run
Run with `ant`.
To genereate the source documentation, run `ant doc`.

## Output DFA
TODO:
