# Spark-Reader
A tool to assist non-native speakers in reading Japanese. Currently still in **beta**.

Looking to download the latest version?
[Click here to go to the release page!](https://github.com/thatdude624/Spark-Reader/releases)
Feedback is greatly appreciated.

EDICT2 file included for convenience, see [this page](http://www.edrdg.org/jmdict/edict.html)
for more information and its licence.  All other code is released under the included licence.

## What this is
Spark Reader is intended as an alternative to things like Rikaisama and Chiitrans.

Instead of normal translation software that will attempt to give you an English equivalent of Japanese text,
Spark reader and similar programs will give you the tools needed to actually understand the original Japanese sentence
by letting you see the readings of Kanji, lookup words and get help with things like grammar.

It's mainly designed for use with Visual Novels, but anything that will let you copy text from will work with Spark Reader (Websites, books etc.)

## Features
- Edict, Epwing and custom dictionaries supported.
- Customisable: Many options aesthetic available regarding things like colours, fonts, sizes and learning options like Furigana display or showing your Heisig keywords for Kanji.
- Known word tracking: Words you know can be shown in blue and have furigana disabled.
- Heavy Anki integration: Export words, lines and their context for making your own Anki flashcards, and import the words you already know from your existing Anki decks.
- Multiplayer: Read along with your friends over voice chat and Spark Reader will tell you if you're ahead or behind the others.

## Features still in development
- An improved, recursive deconjugator
- Powerful and customisable word splitter: Choose between Kuromoji, 'assisted rikaikun' mode or disable word splitting entirely.
- Import character names and their readings from VNDB
- A built-in, memory based texthooker for programs and games that don't work with tools like ITH.

---

### Build requirements
- Compiled using IntelliJ and Java 8. NetBeans project files present as well.
- Uses eb4j (original, not the one on Github) for Epwing support
- Uses JNA for the memory based text hook and other native features
- Uses JUnit and Hamcrest for tests

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
