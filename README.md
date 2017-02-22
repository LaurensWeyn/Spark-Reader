# Spark-Reader
A tool to assist non-naitive speakers in reading Japanese

Also my first project on Github

EDICT2 file included for convenience, see this page for more information and licence: http://www.edrdg.org/jmdict/edict.html  All other code is released under the included licence.

## Build requirements
- Compiled using IntelliJ and Java 8. NetBeans project files present as well.
- Uses eb4j (original, not the one on Github) for EPWING loading
- Uses JNA for the memory based text hook  
- Uses JUnit and Hamcrest for tests (yes, all 2 of them)

## Building

The contents of src must be built against eb4j (from osdn) and jna (from maven).
The correct versions of the libraries are indicated in the IntelliJ project files.

IntelliJ has to be configured correctly. This includes the location of the JDK. The official JDK 8 download from oracle is known to work. 

The IntelliJ project will compile, but it assumes that the libraries are located in a particular place on your filesystem already.
You can get the libraries from osdn and maven with help from google. Make sure to get the correct versions. 

The compiled output must be linked into a .jar file. The IntelliJ project has this set up already, but again, it assumes the libraries are present in the right place on your filesystem already. The project will extract the contents of the libraries into the .jar file whole.

You don't have to follow the instructions below if you know what you're doing, but they'll work:

- Download the correct versions of eb4j and jna and place them in the location that the IntelliJ project wants them
- In IntelliJ, ensure that the Spark Reader.jar artifact is set to be included in the project build
- Press the project build button
- If you encounter any build errors, scrap and re-clone the repository (but not the libraries you downloaded) and try again. If you have any source changes, back them up first, but not any project file changes. 

## Stuff that still needs doing
Somewhat sorted in order of importance:

- EPWING support
- Support for the special case verbs in the deconjugator or dictionary
- Misc. work on deconjugator (more tests etc.)
- a built-in text hooker or some sort, probably using JNA
- Multiplayer testing/repair (code is there, but it's broken right now)
- More JUnit tests
- UI for settings, adding words to dictionary and such

Please report bugs here if you find them!