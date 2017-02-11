# Spark-Reader
A tool to assist non-naitive speakers in reading Japanese

Also my first project on Github

EDICT2 file included for convenience, see this page for more information and licence: http://www.edrdg.org/jmdict/edict.html  All other code is released under the included licence.

## Build requirements
- Compiled using IntelliJ and Java 8. NetBeans project files present as well.
- Uses eb4j (original, not the one on Github) for EPWING loading
- Uses JNA for the memory based text hook  
- Uses JUnit and Hamcrest for tests (yes, all 2 of them)

## Stuff that still needs doing
somewhoat sorted in order of importance:

- EPWING support
- Support for the special case verbs in the deconjugator or dictionary
- Misc. work on deconjugator (more tests etc.)
- a built-in text hooker or some sort, probably using JNA
- Multiplayer testing/repair (code is there, but it's broken right now)
- More JUnit tests
- UI for settings, adding words to dictionary and such

Please report bugs here if you find them!