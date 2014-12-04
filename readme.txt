cs253081, 207622103, Ahmed, Imtiaz
cse63038, 208839862, Kasik, Eli
cs243104, 207341837, Shuralyov, Dmitri


---------------------
Special Requirements:
---------------------

Our program is developed with an English-speaking user in mind, and does not have native support for other languages. Its spell check capabilities assume that the English language is used (but can be turned off for other languages).

It also relies on different colours to highlight various piece of text, used to point out spelling mistakes or java keywords/bracket matching. Thus a colour blind user may not be able to take full advantage of those features.


-------
Design:
-------

First I will describe the logical design of our application, and then go into implementation design details.

We were tasked with the creation of a simple text editor, with a twist (or two) of adding spell checking and syntax/keyword highlighting .java files. This is reflected by the applications name.

There are two types of text editors: plain text editors and rich text editors. For this assignment, it was appropriate to create a plain text editor rather than a rich one (due to its complexity). By definition, a plain text editor works with plaintext files and only allows you to edit text characters, but NOT their styles (such as bold, italic, different fonts, sizes, or font colours).

Simple Text Editor attempts to be user friendly, robust, effective and simple to use.

For spell checking, the application takes an approach of doing in-place spell checking. Whenever a document is loaded or modified, each word is checked against a dictionary for correct spelling. If it is deemed to be incorrect (i.e. not in dictionary), it gets underlined with a red wavy line. The user is then able to make corrections, or perhaps right click on the underlined word for more options (word suggestions, 'ignore all', 'add to dictionary').

For .java files, the highlighting mode is changed and no longer does active English spell checking. Instead, it highlights all the Java keywords in blue colour. It also highlights an opening bracket and its matching closing bracket in green, when the cursor is placed nearby. If a matching bracket isn't found (or there are some other improper brackets in the way), then it is highlighted in red.

The highlighting modes can be changed manually (or turned off) in the 'Highlighting' menu.

There is also a very convenient 'quick search' function available. It allows for extremely quick searches of text strings, by using both hands on the keyboard only. Simply press Ctrl+F, and a small overlaying box appears. Type in a few starting characters of the string you're looking for, and it will immediately highlight results. Press Enter or Shift+Enter to go to next/previous result. Press Escape once you're satisfied (either found what you needed, or it wasn't there) and the selection jumps to the found string (only if it was found, of course). This allows for extremely quick navigation of large text files.

There are also all the standard (and some extra) plaintext editing features, such as Copy/Cut/Paste/Select All shortcuts (right click a selection or by going to 'Edit' menu). There is Undo/Redo functionality. Whenever a file has unsaved changes, an '*' appears next to its filename in the title bar (it goes away when file is saved). Whenever you choose to close the editor or open a new file when the current has unsaved changes, you are asked "if you want to save your changes."

For extra convenience, the application saves the last used window state (its size, position, whether it is maximized) in a file, as well as the last opened file directory.

On the implementation side of things, one of the first and most important decisions we've made was our choice of the text editing component to be used.

We had considered JTextArea, JTextPane and JEditorPane as they are the only multi-line components. JEditorPane is more for displaying HTML or other read-only rich content, and not good for editing text (despite its name).

Of the other two, the only difference is that JTextArea is made specifically for plain text editing and doesn't support rich text (different fonts, styles, colours in a single document).

Since this project involves plaintext editing, JTextArea was the best choice. Once we were satisfied with its ability to have a highlighter that was capable of underlining misspelled words, we made the decision to use it.

However, due to the addition of a ".java syntax highlighting" requirement added by assignment 3, this initial JTextArea choice became unsuitable for one simple reason: it does not allow to colour the text in different colours.

As the JTextPane component was made for rich text editing, a number of obstacles appeared as we had to re-customize it for plaintext editing with the .java keyword colouring ability. For example, the ability to enable/disable word-wrapping and control tab size change from being 1-liners to highly complex problems, which require 100+ line of code of non-standard hacky code in order to resolve. These difficulties are described in great detail on the internet, as many people have asked the same questions, with little solutions offered. We have had some success in re-incorporating those features, however the solutions weren't perfect and required extremely complicated/ambiguous code. In the end, we felt it was better not to complicate the code too much with extended StyledEditorKits, ViewFactories, ParagraphElementNames and ParagraphViews, so we had to remove those two features from our original assignment 2 code.

For the overlapping quick find box, LayeredPanes were used to allow it to 'float' on top of the text editor (thus obstructing less text).

As a final note, this text document was written/edited completely in Simple Text Editor. Some of the later a3.java code changes were also done using it.


--------------------
Additional Features:
--------------------

-in-place spell checking underlining
-.java keyword colouring
-.java bracket matching
-ability to manually choose the highlighting mode
-right click context menu in the editor window
-quick find feature
-undo/redo ability
-cut/copy/paste/select all shortcuts
-filename* to indicate unsaved changes present
-asks 'do you want to save changes?' when closing program/opening another file/creating new file
-saves last window position/size/state and last file opened directory, restores on next run


---------------
Communications:
---------------

October 28, 2008; meeting at 3:30pm - 4:30pm;
Imtiaz, Eli and Dmitri met up at the Prism lab.
We discussed the basics of the assignment, what features we wanted
to include, and divided the work amongst ourselves.

November 3, 2008; meeting at 3:30pm - 4:30pm;
Imtiaz and Dmitri met at the Prism lab to discuss ideas on implenting
the underlining feature of the text editor.

November 4, 2008; e-mail
Dmitri e-mail the starting interface for the text editor, and had
worked on a few features such as file opening and closing, as well as
the feature to save the state of the previous window. Also suggestions
and ideas were discussed about doing the underlining for mispelled
words.

November 7, 2008; e-mail
Dmitri addressed issue of the strike and how it would affect the schedule
of our plans.

November 8, 2008; e-mail
Imtiaz e-mailed the basic feature of the spell check underlinding feature.

November 21, 2008; e-mail

Dmitri e-mailed a new version of the program with a simple search function.

January 31, 2009; e-mail
Imtiaz e-mailed Eli and Dmitri regarding the new features of assignment 3.

Febuary 5, 2009; meeting after class
Met to discuss the features of assignment 3, as well as demo our current
version of the program.

Febuary 15, 2009; e-mail
Everyone e-mailing Dmitri our recent contributions as well as the readme
files for latest program.


-----------------
Responsibilities:
-----------------

Imtiaz:
Testing, bugs resolving
Helped with communications section

Eli:
Testing and help readme writing

Dmitri:
Underlining/highlighting code
Undo/redo code
GUI functionality
