# CRFKKT

CRFKKT is a (probably the first) [chronofuck](https://esolangs.org/wiki/Chronofuck) interpreter!

Currently, doesn't build a standalone JAR, but feel free to add it yourself.

### Usage
To pass in a file, the first 2 arguments should be `-f` and the filename respectively, for example, `-f examples/cat.crfk` to run the cat example

Other arguments: 
1. `-v`: verbose mode, basically debugging, prints out the state of the interpreter before each execution
2. `-nzr`: non-zero jump on end, inverts the behavior of `}` to jump if the value is *not* zero
3. `-eaj`: erase allows jumps, erase will not force the instruction pointer back to before time was "erased"

Examples are located in the examples directory
