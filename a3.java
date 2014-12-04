import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.undo.UndoManager;

class TextEditorFrame extends JFrame implements ActionListener, ChangeListener, DocumentListener, CaretListener, FlavorListener, UndoableEditListener
{
	final private String applicationTitle = "Simple Text Editor";

	private JTextPane textPane;					// Main editable text area
	private JScrollPane textPaneScrollPane;		// A scoll pane container

	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem newMenuItem;
	private JMenuItem openMenuItem;
	private JMenuItem saveMenuItem;
	private JMenuItem saveAsMenuItem;
	private JMenuItem exitMenuItem;
	private JMenu editMenu;
	private JMenuItem undoMenuItem;
	private JMenuItem redoMenuItem;
	private JMenuItem cutMenuItem;
	private JMenuItem copyMenuItem;
	private JMenuItem pasteMenuItem;
	private JMenuItem deleteMenuItem;
	private JMenuItem selectAllMenuItem;
	private JMenu searchMenu;
	private JMenuItem quickFindMenuItem;
	/*private JMenu viewMenu;
	private JMenuItem wordWrapMenuItem;*/
	private JMenu highlightingMenu;
	private JMenuItem noneMenuItem;
	private JMenuItem englishSpellcheckingMenuItem;
	private JMenuItem javaSourceCodeMenuItem;
	private ButtonGroup highlightingButtonGroup = new ButtonGroup();
	private ButtonModel lastHighlightingOption = null;

	private MyPopupMenu popupMenu;

	private QuickFindPanel quickFindPanel;

	private Properties properties = new Properties();
	private int frameMaximized = 0;
	private int frameLocationX = Integer.MIN_VALUE, frameLocationY = Integer.MIN_VALUE;
	private int frameSizeX = -1, frameSizeY = -1;

	private HashSet<String> dictionary = new HashSet<String>();
	private Vector<String> addedToDictionary = new Vector<String>();
	private String textPaneContent = "";
	private Tuple<Integer> shouldBeHighlighted = null;

	private HashSet<String> javaKeywords = new HashSet<String>(Arrays.asList( "abstract", "assert", "boolean", "break", "byte", "case", "catch", "catch", "char",
			"class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import",
			"instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
			"switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null"));

	private UndoManager undoManager = new UndoManager();

	private File documentFile = null;		// The currently opened file
	private boolean documentHasUnsavedChanges;
	private File lastDirectory = null;		// Keeps track of the last current directory

	final private int defaultTabSize = 4;	// Legacy: It was used in assignment 2

	private class MyPopupMenu extends JPopupMenu implements ActionListener
	{
		private JMenuItem ignoreAllMenuItem;
		private JMenuItem addToDictionaryMenuItem;

		private JMenuItem cutPopupMenuItem;
		private JMenuItem copyPopupMenuItem;
		private JMenuItem pastePopupMenuItem;
		private JMenuItem deletePopupMenuItem;
		private JMenuItem selectAllPopupMenuItem;

		MyPopupMenu(ActionListener listener)
		{
			super();

			ignoreAllMenuItem = new JMenuItem("Ignore All");
			ignoreAllMenuItem.setMnemonic('I');
			ignoreAllMenuItem.addActionListener(this);

			addToDictionaryMenuItem = new JMenuItem("Add to Dictionary");
			addToDictionaryMenuItem.setMnemonic('D');
			addToDictionaryMenuItem.addActionListener(this);

			cutPopupMenuItem = new JMenuItem("Cut");
			cutPopupMenuItem.setMnemonic('T');
			cutPopupMenuItem.addActionListener(listener);

			copyPopupMenuItem = new JMenuItem("Copy");
			copyPopupMenuItem.setMnemonic('C');
			copyPopupMenuItem.addActionListener(listener);

			pastePopupMenuItem = new JMenuItem("Paste");
			pastePopupMenuItem.setMnemonic('P');
			pastePopupMenuItem.addActionListener(listener);
			pastePopupMenuItem.setEnabled(Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.plainTextFlavor));

			deletePopupMenuItem = new JMenuItem("Delete");
			deletePopupMenuItem.setMnemonic('D');
			deletePopupMenuItem.addActionListener(listener);

			selectAllPopupMenuItem = new JMenuItem("Select All");
			selectAllPopupMenuItem.setMnemonic('A');
			selectAllPopupMenuItem.addActionListener(listener);
		}

		public JMenuItem getPastePopupMenuItem() { return pastePopupMenuItem; }

		@Override
		public void show(Component invoker, int x, int y) {
			JPopupMenu popupMenu = new JPopupMenu();

			JTextPane textPane = (JTextPane)invoker;
			int caretPosition = textPane.viewToModel(new Point(x, y));

			if (caretPosition != textPane.getText().length()) {
				Tuple<Integer> loc = findIncorrectWord(textPane, caretPosition);
				if (loc != null) try
				{
					String incorrectWord = textPane.getText(loc.getFirst(), loc.getSecond() - loc.getFirst());
					incorrectWord = incorrectWord.trim();

					Vector<String> suggestions = findCorrections(incorrectWord.toLowerCase());
					for (Iterator<String> it = suggestions.iterator(); it.hasNext(); )
					{
						JMenuItem suggestionMenuItem = new JMenuItem(transferWordCase(incorrectWord, it.next()));
						suggestionMenuItem.addActionListener(this);
						suggestionMenuItem.setActionCommand(loc.toString());
						popupMenu.add(suggestionMenuItem);
					}

					ignoreAllMenuItem.setActionCommand(incorrectWord.toLowerCase() + " " + loc.toString());
					addToDictionaryMenuItem.setActionCommand(incorrectWord.toLowerCase() + " " + loc.toString());
					popupMenu.addSeparator();
					popupMenu.add(ignoreAllMenuItem);
					popupMenu.add(addToDictionaryMenuItem);
					popupMenu.addSeparator();
				} catch (BadLocationException ble) {}
			}

			if (textPane.getSelectionStart() != textPane.getSelectionEnd() && caretPosition >= textPane.getSelectionStart() && caretPosition <= textPane.getSelectionEnd())
			{
				cutPopupMenuItem.setEnabled(true);
				copyPopupMenuItem.setEnabled(true);
				deletePopupMenuItem.setEnabled(true);
			} else {
				cutPopupMenuItem.setEnabled(false);
				copyPopupMenuItem.setEnabled(false);
				deletePopupMenuItem.setEnabled(false);
				textPane.setCaretPosition(caretPosition);
			}

			popupMenu.add(cutPopupMenuItem);
			popupMenu.add(copyPopupMenuItem);
			popupMenu.add(pastePopupMenuItem);
			popupMenu.add(deletePopupMenuItem);
			popupMenu.addSeparator();
			popupMenu.add(selectAllPopupMenuItem);
			popupMenu.show(invoker, x, y);
		}

		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource() == ignoreAllMenuItem)
			{
				String word = ae.getActionCommand().split(" ")[0];
				//Tuple<Integer> loc = Tuple.valueOfInteger(ae.getActionCommand().substring(word.length() + 1));

				dictionary.add(word);

				//removeHighlightAt(loc);
				fullCheckSpelling();
			}
			else if (ae.getSource() == addToDictionaryMenuItem)
			{
				String word = ae.getActionCommand().split(" ")[0];
				//Tuple<Integer> loc = Tuple.valueOfInteger(ae.getActionCommand().substring(word.length() + 1));

				dictionary.add(word);
				addedToDictionary.add(word);

				//removeHighlightAt(loc);
				fullCheckSpelling();
			}
			else
			{
				String suggestion = ((JMenuItem)ae.getSource()).getText();
				Tuple<Integer> loc = Tuple.valueOfInteger(ae.getActionCommand());

				try {
					textPane.getDocument().remove(loc.getFirst(), loc.getSecond() - loc.getFirst());
					textPane.getDocument().insertString(loc.getFirst(), suggestion, null);
				} catch (BadLocationException ble) {}
			}
		}

		private Tuple<Integer> findIncorrectWord(JTextPane textPane, int caretPosition)
		{
			Highlighter.Highlight[] highlights = textPane.getHighlighter().getHighlights();
			for (int i = 0; i < highlights.length; i++)
			{
				Highlighter.Highlight h = highlights[i];
				if (h.getPainter() instanceof UnderlineHighlightPainter) {
					if (caretPosition >= h.getStartOffset() && caretPosition <= h.getEndOffset()) {
						return new Tuple<Integer>(h.getStartOffset(), h.getEndOffset());
					}
				}
			}
			return null;
		}

		private void removeHighlightAt(Tuple<Integer> loc)
		{
			Highlighter.Highlight[] highlights = textPane.getHighlighter().getHighlights();
			for (int i = 0; i < highlights.length; ++i)
			{
				Highlighter.Highlight h = highlights[i];
				if (h.getPainter() instanceof UnderlineHighlightPainter) {
					if (loc.getFirst() == h.getStartOffset() && loc.getSecond() == h.getEndOffset()) {
						textPane.getHighlighter().removeHighlight(h);
					}
				}
			}
		}
	}

	// This function takes a String representing a misspelled word (i.e. word not in the dictionary)
	// and tries to look for similar words in the dictionary as suggestions
	// It returns all found suggestions (no more than 5) in a Vector of Strings.
	// Pre-condition: misspelledWord is a string not in dictionary
	// Post-condition: returns a vector of suggestions, i.e. similar words that are in dictionary
	private Vector<String> findCorrections(String misspelledWord)
	{
		final int maxSuggestions = 5;
		Vector<String> suggestions = new Vector<String>();

		if (dictionary.contains(misspelledWord))
		{
			suggestions.add(misspelledWord);
		}
		else
		{
			for (int i = 1; i <= Math.abs(misspelledWord.hashCode() % 3) + 1; ++i) {
				//suggestions.add(misspelledWord + "_suggestion" + i);
				// Pick random words from the dictionary as 'suggestions'... not very clever
				Random r = new Random(misspelledWord.hashCode() + i);
				suggestions.add((String)dictionary.toArray()[r.nextInt(dictionary.size())]);
			}
		}

		if (suggestions.size() > maxSuggestions) suggestions.setSize(maxSuggestions);
		return suggestions;
	}

	// This function transfers the case from one word to another
	private String transferWordCase(String originalWord, String newWord)
	{
		if (originalWord.substring(0, 1).equals(originalWord.substring(0, 1).toLowerCase()))
			return newWord.toLowerCase();
		else if (originalWord.equals(originalWord.toUpperCase()))
			return newWord.toUpperCase();
		else
			return newWord.substring(0, 1).toUpperCase() + newWord.substring(1).toLowerCase();
	}

	private void loadDictionary()
	{
		try {
			Scanner s = new Scanner(new BufferedReader(new FileReader("dictionary.txt")));

			while (s.hasNext()) {
				dictionary.add(s.next().trim().toLowerCase());
			}

			s.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void saveDictionary()
	{
		if (addedToDictionary.size() == 0)
			return;

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("dictionary.txt", true)));

			for (Iterator<String> it = addedToDictionary.iterator(); it.hasNext(); )
				out.print("\r\n" + it.next());		// Windows newline on purpose, so that the dictionary format stays consistent across platforms

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void performFileOpen() {
		JFileChooser chooser = createChooser(JFileChooser.OPEN_DIALOG);
		int response = chooser.showDialog(this, null);
		if (response == JFileChooser.APPROVE_OPTION) {
			File file = null;
			try {
				file = chooser.getSelectedFile();
				BufferedReader br = new BufferedReader(new FileReader(file));
				textPane.read(br, null);
				br.close();
				lastFileFilter = chooser.getFileFilter();
				if (file.getName().toLowerCase().endsWith(".java")) {
					highlightingButtonGroup.setSelected(noneMenuItem.getModel(), true);		// Force fullCheckSpelling() to get executed
					highlightingButtonGroup.setSelected(javaSourceCodeMenuItem.getModel(), true);
				} else {
					highlightingButtonGroup.setSelected(noneMenuItem.getModel(), true);		// Force fullCheckSpelling() to get executed
					highlightingButtonGroup.setSelected(englishSpellcheckingMenuItem.getModel(), true);
				}
				textPaneWasReset();

				documentFile = file;
				setDocumentHasUnsavedChanges(false);
				lastDirectory = file.getParentFile();
			} catch (java.io.FileNotFoundException fnfe) {
				System.err.println("Warning: File '" + file + "' not found.");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	private boolean performFileSave() {
		if (documentFile != null) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(documentFile));
				textPane.write(bw);
				bw.close();

				setDocumentHasUnsavedChanges(false);
			} catch (IOException e) {
				System.err.println("Warning: Failed to save file '" + documentFile + "'.");
				e.printStackTrace();
			}

			return true;
		} else {
			return performFileSaveAs();
		}
	}

	private boolean performFileSaveAs()
	{
		JFileChooser chooser = createChooser(JFileChooser.SAVE_DIALOG);

		while (true) {
			int response = chooser.showDialog(this, null);
			if (response == JFileChooser.APPROVE_OPTION) {
				File file = null;
				try {
					file = chooser.getSelectedFile();
					if (file.exists()) {
						// Ask the user if he wants to replace the existing file
						int replaceResponse = JOptionPane.showConfirmDialog(this, "The file '" + file.getName() +
								"' already exists.\nDo you want to replace it?", "Save As", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						if (replaceResponse == JOptionPane.NO_OPTION)
						   continue;		// Bring up the file chooser again and let the user select another file or cancel
					}
					BufferedWriter bw = new BufferedWriter(new FileWriter(file));
					textPane.write(bw);
					bw.close();
					lastFileFilter = chooser.getFileFilter();
					if (file.getName().toLowerCase().endsWith(".java")) {
						highlightingButtonGroup.setSelected(javaSourceCodeMenuItem.getModel(), true);
					} else {
						highlightingButtonGroup.setSelected(englishSpellcheckingMenuItem.getModel(), true);
					}

					documentFile = file;
					setDocumentHasUnsavedChanges(false);
					lastDirectory = file.getParentFile();
				} catch (IOException e) {
					System.err.println("Warning: Failed to 'save as' file '" + file + "'.");
					e.printStackTrace();
				}

				return true;
			}
			else
				return false;
		}
	}

	private void textPaneWasReset()
	{
		textPane.getDocument().addDocumentListener(this);
		textPane.getDocument().addUndoableEditListener(this);

		undoManager.discardAllEdits();
		updateUnredoMenuItems();
		quickFindPanel.hidePanel(false);
	}

	private boolean wordAcceptedByDictionary(String word)
	{
		boolean isWordInDictionary = dictionary.contains(word) || dictionary.contains(word.substring(0, 1).toLowerCase() + word.substring(1));
		boolean isNumber = word.matches("\\d+");
		boolean isAcronym = word.matches("[A-Z]+");

		return isWordInDictionary || isNumber || isAcronym;
	}

	private boolean isJavaKeyword(String text) {
		return javaKeywords.contains(text);
	}

	private Highlighter.HighlightPainter painter = new UnderlineHighlightPainter(Color.red);
	private void checkSpelling(DocumentEvent evt)
	{
		System.out.println("=====Searching..===============================");

		final Highlighter highlighter = textPane.getHighlighter();
		String textPaneCaseContent = "";		// Text pane content with character case preserved (not all lower-case)

		try {
			Document d = textPane.getDocument();
			textPaneCaseContent = d.getText(0, d.getLength());
			textPaneContent = textPaneCaseContent.toLowerCase();
		} catch (BadLocationException e) {
			// Shouldn't happen
			return;
		}

		boolean currentWordWasHighlighted = false;
		int caretPosition = -1;
		//if (evt.getType() == DocumentEvent.EventType.INSERT && evt.getLength() == 1) caretPosition = evt.getOffset() + evt.getLength();
		if (evt.getType() == DocumentEvent.EventType.INSERT) caretPosition = evt.getOffset() + evt.getLength();
		else if (evt.getType() == DocumentEvent.EventType.REMOVE) caretPosition = evt.getOffset();

Tuple<Integer> b4exp = new Tuple<Integer>(0, 0);
		Tuple<Integer> range = null;
		if (evt.getType() == DocumentEvent.EventType.INSERT) { range = expandRange(textPaneContent, evt.getOffset(), evt.getOffset() + evt.getLength()); b4exp.setFirst(evt.getOffset()); b4exp.setSecond(evt.getOffset() + evt.getLength()); }
		else if (evt.getType() == DocumentEvent.EventType.REMOVE) { range = expandRange(textPaneContent, evt.getOffset(), evt.getOffset()); b4exp.setFirst(evt.getOffset()); b4exp.setSecond(evt.getOffset()); }
		System.out.println(new Tuple<Integer>(b4exp.getFirst(), b4exp.getSecond()) + " expanded to " + range);
		System.out.println("'" + textPaneContent.substring(b4exp.getFirst(), b4exp.getSecond()) + "' expanded to '" + textPaneContent.substring(range.getFirst(), range.getSecond()) + "'");

		// Clear the in-range previous English Dictionary Spellchecking
		if (highlightingButtonGroup.getSelection() == englishSpellcheckingMenuItem.getModel())
		{
			// Remove any existing highlights for last word
			Highlighter.Highlight[] highlights = highlighter.getHighlights();
			for (int i = 0; i < highlights.length; ++i) {
				Highlighter.Highlight h = highlights[i];
				if (h.getPainter() instanceof UnderlineHighlightPainter) {
					if (h.getStartOffset() >= range.getFirst() && h.getEndOffset() <= range.getSecond()) {
						highlighter.removeHighlight(h);
						if (evt.getLength() == 1 && inRange(caretPosition, new Tuple<Integer>(h.getStartOffset(), h.getEndOffset()))) {
							currentWordWasHighlighted = true;
						}
						//System.out.println("removed highlight " + h.getStartOffset() + ", " + h.getEndOffset());
					}
				}
			}
		}
		// Clear the in-range previous Java Source Code Syntax Highlighting
		else if (highlightingButtonGroup.getSelection() == javaSourceCodeMenuItem.getModel())
		{
			// Remove Java keyword highlighting
			//textPane.getStyledDocument().setCharacterAttributes(range.getFirst(), range.getSecond() - range.getFirst(), textPane.getStyledDocument().getDefaultRootElement().getAttributes(), true);
			final int start = range.getFirst();
			final int length = range.getSecond() - range.getFirst();
			SwingUtilities.invokeLater(new Runnable() {
				public void run()
				{
					textPane.getStyledDocument().setCharacterAttributes(start, length, textPane.getStyledDocument().getDefaultRootElement().getAttributes(), true);
				}
			});
		}

		Pattern p = Pattern.compile("[\\w'-]+");
		Matcher m = p.matcher(textPaneContent);
		m.region(range.getFirst(), range.getSecond());

		// English Dictionary Spellchecking
		if (highlightingButtonGroup.getSelection() == englishSpellcheckingMenuItem.getModel())
		{
			System.out.println("Event type = " + evt.getType());
			System.out.println("offset = " + evt.getOffset() + ", length = " + evt.getLength());
			while (m.find())
			{
				System.out.println("looping " + m.start() + " to " + m.end() + "; caret at " + caretPosition);
				try {
					if (!wordAcceptedByDictionary(textPaneCaseContent.substring(m.start(), m.end()))) {
						//if (currentWordWasHighlighted || (evt.getLength() == 1 && !inRange(caretPosition, new Tuple<Integer>(m.start(), m.end()))))
						if (evt.getLength() == 1 && inRange(caretPosition, new Tuple<Integer>(m.start(), m.end()))) {
							if (currentWordWasHighlighted) {
								highlighter.addHighlight(m.start(), m.end(), painter);
								System.out.println("highlighting " + m.start() + " to " + m.end());
							} else {
								shouldBeHighlighted = new Tuple<Integer>(m.start(), m.end());
								System.out.println("shouldBeHighlighted = " + shouldBeHighlighted);
							}
						} else {
							highlighter.addHighlight(m.start(), m.end(), painter);
							System.out.println("highlighting " + m.start() + " to " + m.end());
						}
					} else {
						//if (shouldBeHighlighted != null && shouldBeHighlighted.getFirst() >= m.start() && shouldBeHighlighted.getSecond() <= m.end())
						Tuple<Integer> shouldNotBeHighlighted = new Tuple<Integer>(m.start(), m.end());
						if (shouldBeHighlighted != null && shouldBeHighlighted.doesOverlap(shouldNotBeHighlighted))
							shouldBeHighlighted = null;
					}
				} catch (BadLocationException e) {
					// Nothing to do
				}
			}
		}
		// Java Source Code Syntax Highlighting
		else if (highlightingButtonGroup.getSelection() == javaSourceCodeMenuItem.getModel())
		{
			final SimpleAttributeSet keywordText = new SimpleAttributeSet();
			StyleConstants.setForeground(keywordText, Color.blue);
			//StyleConstants.setBold(keywordText, true);

			while (m.find())
			{
				//System.out.println("looping " + m.start() + " to " + m.end());
				if (isJavaKeyword(textPaneCaseContent.substring(m.start(), m.end()))) {
					//textPane.getStyledDocument().setCharacterAttributes(m.start(), m.end() - m.start(), keywordText, true);
					final int start = m.start();
					final int length = m.end() - m.start();
					SwingUtilities.invokeLater(new Runnable() {
						public void run()
						{
							textPane.getStyledDocument().setCharacterAttributes(start, length, keywordText, true);
						}
					});
				}
			}
		}
	}

	private boolean isWordChar(char ch)
	{
		if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '\'' || ch == '-' || ch == '_')
			return true;
		else
			return false;
	}

	private Tuple<Integer> expandRange(String content, int start, int end)
	{
		while (start > 0 && isWordChar(content.charAt(start - 1))) {
			--start;
		}
		while (end < content.length() && isWordChar(content.charAt(end))) {
			++end;
		}

		return new Tuple<Integer>(start, end);
	}

	private boolean inRange(int position, Tuple<Integer> range)
	{
		return (position >= range.getFirst() && position <= range.getSecond());
	}

	private void fullCheckSpelling()
	{
		//System.out.println("full spell Searching..");

		final Highlighter highlighter = textPane.getHighlighter();
		String textPaneCaseContent = "";		// Text pane content with character case preserved (not all lower-case)

		// Clear all of the previous English Dictionary Spellchecking
		if (lastHighlightingOption == englishSpellcheckingMenuItem.getModel())
		{
			// Remove any existing highlights
			Highlighter.Highlight[] highlights = highlighter.getHighlights();
			for (int i = 0; i < highlights.length; ++i) {
				Highlighter.Highlight h = highlights[i];
				if (h.getPainter() instanceof UnderlineHighlightPainter) {
					highlighter.removeHighlight(h);
				}
			}
		}
		// Clear all of the previous Java Source Code Syntax Highlighting
		else if (lastHighlightingOption == javaSourceCodeMenuItem.getModel())
		{
			// Remove Java keyword highlighting
			textPane.getStyledDocument().setCharacterAttributes(0, textPane.getStyledDocument().getLength(), textPane.getStyledDocument().getDefaultRootElement().getAttributes(), true);

			// Remove bracket highlighting
			Highlighter.Highlight[] highlights = highlighter.getHighlights();
			for (int i = 0; i < highlights.length; ++i) {
				Highlighter.Highlight h = highlights[i];
				if (h.getPainter() == painterBracketMatched || h.getPainter() == painterBracketUnmatched) {
					highlighter.removeHighlight(h);
				}
			}
		}

		try {
			Document d = textPane.getDocument();
			textPaneCaseContent = d.getText(0, d.getLength());
			textPaneContent = textPaneCaseContent.toLowerCase();
		} catch (BadLocationException e) {
			// Shouldn't happen
			return;
		}

		Pattern p = Pattern.compile("[\\w'-]+");
		Matcher m = p.matcher(textPaneContent);

		// English Dictionary Spellchecking
		if (highlightingButtonGroup.getSelection() == englishSpellcheckingMenuItem.getModel())
		{
			while (m.find())
			{
				//System.out.println("looping " + m.start() + " to " + m.end());
				try {
					if (!wordAcceptedByDictionary(textPaneCaseContent.substring(m.start(), m.end()))) {
						highlighter.addHighlight(m.start(), m.end(), painter);
						//System.out.println("highlighting " + m.start() + " to " + m.end());
					}
				} catch (BadLocationException e) {
					// Nothing to do
				}
			}
		}
		// Java Source Code Syntax Highlighting
		else if (highlightingButtonGroup.getSelection() == javaSourceCodeMenuItem.getModel())
		{
			SimpleAttributeSet keywordText = new SimpleAttributeSet();
			StyleConstants.setForeground(keywordText, Color.blue);
			//StyleConstants.setBold(keywordText, true);

			while (m.find())
			{
				//System.out.println("looping " + m.start() + " to " + m.end());
				if (isJavaKeyword(textPaneCaseContent.substring(m.start(), m.end()))) {
					textPane.getStyledDocument().setCharacterAttributes(m.start(), m.end() - m.start(), keywordText, true);
				}
			}

			// Highlight a bracket pair if caret is over a bracket
			highlightBracketPair(textPane.getCaretPosition());
		}
	}

	public void insertUpdate(DocumentEvent e) { quickFindPanel.hidePanel(false); checkSpelling(e); setDocumentHasUnsavedChanges(true); }
	public void removeUpdate(DocumentEvent e) { quickFindPanel.hidePanel(false); checkSpelling(e); setDocumentHasUnsavedChanges(documentFile != null || textPane.getText().length() != 0); }
	public void changedUpdate(DocumentEvent e) {}

	private void updateUnredoMenuItems()
	{
		if (undoMenuItem != null) undoMenuItem.setEnabled(undoManager.canUndo());
		if (redoMenuItem != null) redoMenuItem.setEnabled(undoManager.canRedo());
	}

	public void undoableEditHappened(UndoableEditEvent e) { undoManager.undoableEditHappened(e); updateUnredoMenuItems(); }

	private Highlighter.HighlightPainter painterBracketMatched = new DefaultHighlighter.DefaultHighlightPainter(Color.green);
	private Highlighter.HighlightPainter painterBracketUnmatched = new DefaultHighlighter.DefaultHighlightPainter(Color.red);
	public void caretUpdate(CaretEvent e)
	{
		int caretPosition = e.getDot();
		boolean selectionExists = e.getDot() != e.getMark();

		cutMenuItem.setEnabled(selectionExists);
		copyMenuItem.setEnabled(selectionExists);
		deleteMenuItem.setEnabled(selectionExists);

		// If there is a selection
		if (selectionExists)
		{
			// Leave focus of current word
			if (shouldBeHighlighted != null) {
				try {
					textPane.getHighlighter().addHighlight(shouldBeHighlighted.getFirst(), shouldBeHighlighted.getSecond(), painter);
					System.out.println("highlighting shouldBeHighlighted " + shouldBeHighlighted.getFirst() + " to " + shouldBeHighlighted.getSecond());
				} catch (BadLocationException ble) {}
				shouldBeHighlighted = null;
			}
		} else {
			if (shouldBeHighlighted != null && !inRange(caretPosition, shouldBeHighlighted)) {
				try {
					textPane.getHighlighter().addHighlight(shouldBeHighlighted.getFirst(), shouldBeHighlighted.getSecond(), painter);
					System.out.println("highlighting shouldBeHighlighted " + shouldBeHighlighted.getFirst() + " to " + shouldBeHighlighted.getSecond());
				} catch (BadLocationException ble) {}
				shouldBeHighlighted = null;
			}
		}

		// Bracket highlighter/matcher
		if (highlightingButtonGroup.getSelection() == javaSourceCodeMenuItem.getModel())		// Java Source Code Syntax Highlighting
		{
			// Clear old brackets
			Highlighter highlighter = textPane.getHighlighter();
			Highlighter.Highlight[] highlights = highlighter.getHighlights();
			for (int i = 0; i < highlights.length; ++i)
			{
				Highlighter.Highlight h = highlights[i];
				if (h.getPainter() == painterBracketMatched || h.getPainter() == painterBracketUnmatched) {
					highlighter.removeHighlight(h);
				}
			}

			// Highlight new pair
			highlightBracketPair(caretPosition);
		}
	}

	private int isBracket(char ch)
	{
		final String openBracket = "{([<";
		final String closeBracket = "})]>";
		int bracket;

		if ((bracket = openBracket.indexOf(ch) + 1) != 0)
			return +bracket;
		else if ((bracket = closeBracket.indexOf(ch) + 1) != 0)
			return -bracket;
		else return 0;
	}

	private void highlightBracketPair(int caretPosition)
	{
		if (quickFindPanel.isVisible())
			return;

		Highlighter highlighter = textPane.getHighlighter();
		int bracket, bracketDirection, startPos, endPos;
		Stack<Integer> brackets = new Stack<Integer>();

		if ((caretPosition >= 1 && (bracket = isBracket(textPaneContent.charAt(startPos = caretPosition - 1))) != 0) ||
			(caretPosition < textPaneContent.length() && (bracket = isBracket(textPaneContent.charAt(startPos = caretPosition))) != 0))
		{
			endPos = startPos;		// Assume the other bracket will not be found first, then search for it
			bracketDirection = bracket > 0 ? +1 : -1;		// Figure out the direction of the scan

			brackets.push(bracket);
			for (int pos = startPos + bracketDirection; pos >= 0 && pos < textPaneContent.length(); pos += bracketDirection)
			{
				bracket = isBracket(textPaneContent.charAt(pos));
				if (bracket != 0) {
					if (bracket * brackets.peek() > 0) {
						// Same direction bracket, push on stack
						brackets.push(bracket);
					} else {
						// Opposite direction bracket, make sure it matches the previous one
						if (bracket + brackets.peek() == 0) {
							// Match successful, so pop the bracket from stack
							brackets.pop();

							if (brackets.empty()) {
								// Found the endBracket
								endPos = pos;
								break;
							}
						} else {
							// Match unsuccessful, therefore the starting braket was broken
							break;
						}
					}
				}
			}

			// Paint the found brackets
			try {
				if (startPos != endPos) {
					highlighter.addHighlight(startPos, startPos + 1, painterBracketMatched);
					highlighter.addHighlight(endPos, endPos + 1, painterBracketMatched);
				} else
					highlighter.addHighlight(startPos, startPos + 1, painterBracketUnmatched);
			} catch (BadLocationException ble) { ble.printStackTrace(); }
		}
	}

	// Creates a fresh new JFileChooser
	// This is needed to reset it if the user presses cancel, which doesn't happen in Java if you
	// reuse the same instance
	// But that's how all other Windows applications behave, so I wanted it to be consistent
	private FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Text Files (*.txt)", "txt");
	private FileNameExtensionFilter javaFilter = new FileNameExtensionFilter("Java Source Files (*.java)", "java");
	private FileFilter lastFileFilter = null;
	private JFileChooser createChooser(int chooserType)
	{
		JFileChooser chooser = new JFileChooser();

		if (lastDirectory != null)
			chooser.setCurrentDirectory(lastDirectory);

		// This is needed to get the file filters in correct order and the text filter active by default
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(txtFilter);
		chooser.addChoosableFileFilter(javaFilter);
		chooser.setAcceptAllFileFilterUsed(true);
		if (lastFileFilter == null)
			chooser.setFileFilter(txtFilter);				// Set Text Files filter selected by default
		else
			chooser.setFileFilter(lastFileFilter);

		if (chooserType == JFileChooser.OPEN_DIALOG) {
			// Nothing to do
		} else if (chooserType == JFileChooser.SAVE_DIALOG) {
			// Change the text from 'Save' to 'Save As'
			chooser.setDialogTitle("Save As");
			chooser.setApproveButtonText("Save As");
		}

		return chooser;
	}

	// Returns true if this stage passes, and it should move on to the next step
	// i.e. it only returns false if the user presses Cancel button
	private boolean dealWithUnsavedChanges()
	{
		if (getDocumentHasUnsavedChanges())
		{
			String fileName = "Untitled";
			try {
				if (documentFile != null)
					fileName = documentFile.getCanonicalPath();
			} catch (Exception e) {}

			int response = JOptionPane.showConfirmDialog(this, "There are unsaved changes in the '"
					+ fileName + "' file.\n\nDo you want to save the changes?",
					applicationTitle, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

			if (response == JOptionPane.YES_OPTION)
			{
				return performFileSave();
			}
			else if (response == JOptionPane.NO_OPTION)
				return true;
			else if (response == JOptionPane.CANCEL_OPTION)
				return false;
			else
				return false;
		} else
			return true;
	}

	private class QuickFindPanel extends JPanel implements ActionListener, KeyListener, DocumentListener
	{
		private int panelHeight;
		private JTextField findTextField;
		private JLabel resultsCountLabel;
		private JButton previousResultButton;
		private JButton nextResultButton;
		private JButton closeButton;
		private Tuple<Integer> searchResults = new Tuple<Integer>(0, 0);
		private boolean performedSearch = false;
		private int originalViewport;

		QuickFindPanel()
		{
			super();
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

			add(Box.createHorizontalStrut(3));
			findTextField = new JTextField();
			findTextField.setBorder(null);
			findTextField.addActionListener(this);
			findTextField.addKeyListener(this);
			findTextField.getDocument().addDocumentListener(this);
			add(findTextField);
			add(Box.createHorizontalStrut(3));
			resultsCountLabel = new JLabel();
			resultsCountLabel.setOpaque(true);
			add(resultsCountLabel);
			add(Box.createHorizontalStrut(3));
			previousResultButton = new JButton(new ImageIcon("Previous.gif"));
			previousResultButton.setPreferredSize(new Dimension(24, 22));
			previousResultButton.setBorder(null);
			previousResultButton.addActionListener(this);
			add(previousResultButton);
			nextResultButton = new JButton(new ImageIcon("Next.gif"));
			nextResultButton.setPreferredSize(new Dimension(24, 22));
			nextResultButton.setBorder(null);
			nextResultButton.addActionListener(this);
			add(nextResultButton);
			add(Box.createHorizontalGlue());
			closeButton = new JButton(new ImageIcon("Close.gif"));
			closeButton.setPreferredSize(new Dimension(24, 22));
			closeButton.setBorder(null);
			closeButton.addActionListener(this);
			add(closeButton);

			setBorder(new LineBorder(Color.gray));
			setBackground(findTextField.getBackground());

			//width = findTextField.getX() * 3 + findTextField.getWidth() + closeButton.getWidth();
			//height = findTextField.getY() * 2 + Math.max(findTextField.getHeight(), closeButton.getHeight());
			panelHeight = Math.max(findTextField.getPreferredSize().height, closeButton.getPreferredSize().height);

			setVisible(false);
		}

		public void updateSize()
		{
			//int width = 200;//findTextField.getX() * 3 + findTextField.getWidth() + closeButton.getWidth();
			//int height = 50;//findTextField.getY() * 2 + Math.max(findTextField.getHeight(), closeButton.getHeight());
			int width = 250;

			Rectangle containerBounds = textPaneScrollPane.getParent().getBounds();
			setBounds(Math.max(containerBounds.width - 325, 0), containerBounds.y, Math.min(275, containerBounds.width), panelHeight);
		}

		public void insertUpdate(DocumentEvent e) { searchResults = highlightString(findTextField.getText()); performedSearch = true; updateResultsCount(0); }
		public void removeUpdate(DocumentEvent e) { searchResults = highlightString(findTextField.getText()); performedSearch = true; updateResultsCount(0); }
		public void changedUpdate(DocumentEvent e) {}

		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource() == previousResultButton)
			{
				traverseFindResults(-1);
				updateResultsCount(-1);
			}
			else if (ae.getSource() == nextResultButton)
			{
				traverseFindResults(+1);
				updateResultsCount(+1);
			}
			else if (ae.getSource() == closeButton)
			{
				hidePanel(true);
			}
		}

		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				if (!performedSearch) {
					searchResults = highlightString(findTextField.getText());
					performedSearch = true;
					updateResultsCount(0);
				} else {
					if (e.isShiftDown() == false) {
						traverseFindResults(+1);
						updateResultsCount(+1);
					} else {
						traverseFindResults(-1);
						updateResultsCount(-1);
					}
				}
			}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}

		// If traverseResults is 0, the currently select result is unchanged,
		// If traverseResults is +1, select the next result,
		// If traverseResults is -1, select the previous result
		private void updateResultsCount(int traverseResults)
		{
			if (findTextField.getText().length() == 0) {
				//previousResultButton.setEnabled(false);
				//nextResultButton.setEnabled(false);
				resultsCountLabel.setBackground(findTextField.getBackground());
				resultsCountLabel.setText("");
				return;
			}

			if (searchResults.getSecond() > 0)
			{
				//resultsCountLabel.setBackground(Color.lightGray);
				resultsCountLabel.setBackground(findTextField.getBackground());

				// Change the currently selected result
				searchResults.setFirst((searchResults.getFirst() - 1 + traverseResults + searchResults.getSecond()) % searchResults.getSecond() + 1);
			} else
				resultsCountLabel.setBackground(Color.red);

			resultsCountLabel.setText(" " + searchResults.getFirst() + " of " + searchResults.getSecond());
		}

		public void showPanel()
		{
			performedSearch = false;
			originalViewport = textPaneScrollPane.getVerticalScrollBar().getValue();
			findTextField.selectAll();
			if (!this.isVisible()) resultsCountLabel.setText("");
			setVisible(true);
			transferFocus();
		}

		public void hidePanel(boolean gentleHide)
		{
			if (isVisible()) {
				if (gentleHide && searchResults.getSecond() == 0) textPaneScrollPane.getVerticalScrollBar().setValue(originalViewport);
				clearHighlights(gentleHide);
				setVisible(false);
			}
		}
	}

	// Search for a given string and selects it
	/*private int findString(String word)
	{
		int firstOffset = -1;

		if (word == null || word.equals("")) {
			return -1;
		}

		word = word.toLowerCase();
		int lastIndex = textPane.getCaretPosition();
		int wordSize = word.length();

		if ((lastIndex = textPaneContent.indexOf(word, lastIndex)) != -1 || (lastIndex = textPaneContent.indexOf(word, 0)) != -1)
		{
			int endIndex = lastIndex + wordSize;
			if (firstOffset == -1) {
				firstOffset = lastIndex;
			}

			textPane.select(lastIndex, endIndex);
			try {
				textPane.scrollRectToVisible(textPane.modelToView(lastIndex));
			} catch (BadLocationException e) {}

			lastIndex = endIndex;
		}

		return firstOffset;
	}*/

	// Search for a given string and selects all occurances of it
	// Returns the number of matches
	private Highlighter.HighlightPainter painterOrange = new DefaultHighlighter.DefaultHighlightPainter(Color.orange);
	private Highlighter.HighlightPainter painterYellow = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
	private Tuple<Integer> highlightString(String string)
	{
		if (textPaneContent.length() == 0)		// Make sure there is some text entered
			return new Tuple<Integer>(0, 0);

		clearHighlights(false);

		if (string == null || string.equals("")) return new Tuple<Integer>(0, 0);

		Highlighter highlighter = textPane.getHighlighter();
		string = string.toLowerCase();
		int startPosition = textPane.getCaretPosition();
		int lastIndex = startPosition;
		int stringLength = string.length();
		int resultNumber = 1;
		int highlightedStrings = 0;

		for (int repetition = 1; repetition <= 2; ++repetition) {
			while ((lastIndex = textPaneContent.indexOf(string, lastIndex)) != -1 && (repetition == 1 || lastIndex < startPosition))
			{
				++highlightedStrings;
				if (repetition == 2) ++resultNumber;
				Highlighter.HighlightPainter highlightPainter;
				int endIndex = lastIndex + stringLength;
				if (highlightedStrings == 1) {
					highlightPainter = painterOrange;
				} else {
					highlightPainter = painterYellow;
				}

				try {
					highlighter.addHighlight(lastIndex, endIndex, highlightPainter);
					if (highlightedStrings == 1) {
						textPane.scrollRectToVisible(textPane.modelToView(lastIndex));
					}
				} catch (BadLocationException e) {}

				lastIndex = endIndex;
			}
			lastIndex = 0;
		}

		if (highlightedStrings > 0)
			return new Tuple<Integer>(resultNumber, highlightedStrings);
		else
			return new Tuple<Integer>(0, 0);
	}

	private void traverseFindResults(int nextIndex)
	{
		Highlighter highlighter = textPane.getHighlighter();
		Highlighter.Highlight[] highlights = highlighter.getHighlights();
		Vector<Highlighter.Highlight> searchHighlights = new Vector<Highlighter.Highlight>();
		int activeHighlight = -1;
		for (int i = 0; i < highlights.length; ++i)
		{
			Highlighter.Highlight h = highlights[i];
			if (h.getPainter() instanceof DefaultHighlighter.DefaultHighlightPainter) {
				if (h.getPainter() == painterOrange) activeHighlight = searchHighlights.size();
				searchHighlights.add(h);
				highlighter.removeHighlight(h);
			}
		}
		if (searchHighlights.size() == 0) return;		// If there are no found results, there's nothing to do

		int nextResult = (activeHighlight + nextIndex + searchHighlights.size()) % searchHighlights.size();
		Highlighter.Highlight h2 = searchHighlights.elementAt(nextResult);

		try {
			for (Iterator<Highlighter.Highlight> it = searchHighlights.iterator(); it.hasNext(); )
			{
				Highlighter.Highlight h = it.next();
				if (h == h2) {
					highlighter.addHighlight(h.getStartOffset(), h.getEndOffset(), painterOrange);
					textPane.setCaretPosition(h.getEndOffset());
					textPane.moveCaretPosition(h.getStartOffset());
				} else {
					highlighter.addHighlight(h.getStartOffset(), h.getEndOffset(), painterYellow);
				}
			}
			textPane.scrollRectToVisible(textPane.modelToView(h2.getStartOffset()));
		} catch (BadLocationException e) {}
	}

	public void clearHighlights(boolean gentleHide)
	{
		Highlighter highlighter = textPane.getHighlighter();
		Highlighter.Highlight[] highlights = highlighter.getHighlights();
		for (int i = 0; i < highlights.length; ++i)
		{
			Highlighter.Highlight h = highlights[i];
			if (h.getPainter() instanceof DefaultHighlighter.DefaultHighlightPainter) {
				if (h.getPainter() == painterOrange && gentleHide) {
					textPane.setCaretPosition(h.getEndOffset());
					textPane.moveCaretPosition(h.getStartOffset());
				}
				highlighter.removeHighlight(h);
			}
		}
	}

	TextEditorFrame()
	{
		loadProperties();

		this.addWindowListener(new MyWindowAdapter());
		this.addWindowStateListener((WindowAdapter)getWindowListeners()[0]);
		this.addComponentListener(new MyComponentAdapter());

		textPane = new JTextPane();
		//textPane.setEditorKit(new WrapEditorKit());
		textPane.setDragEnabled(true);
		textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
		////textArea.setTabSize(defaultTabSize);
		////textArea.setWrapStyleWord(true);		// Wrap lines at word boundaries (whitespace)
		//textPane.setColumns(80);
		//textPane.setRows(20);
		textPaneScrollPane = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textPaneScrollPane.setPreferredSize(new Dimension(640, 400));
		getContentPane().add(textPaneScrollPane);
		textPaneScrollPane.addComponentListener(this.getComponentListeners()[0]);

		quickFindPanel = new QuickFindPanel();
		getLayeredPane().add(quickFindPanel, new Integer(1), 0);

		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		focusManager.addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getID() == KeyEvent.KEY_PRESSED) {
					if (e.getSource() != getRootPane()) {
						quickFindPanel.hidePanel(true);
					}
				}

				return false;
			}
		});

		textPane.addCaretListener(this);
		textPaneWasReset();

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		newMenuItem = new JMenuItem("New");
		newMenuItem.setMnemonic('N');
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		newMenuItem.addActionListener(this);
		fileMenu.add(newMenuItem);

		openMenuItem = new JMenuItem("Open...");
		openMenuItem.setMnemonic('O');
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		openMenuItem.addActionListener(this);
		fileMenu.add(openMenuItem);

		saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setMnemonic('S');
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		saveMenuItem.addActionListener(this);
		fileMenu.add(saveMenuItem);

		saveAsMenuItem = new JMenuItem("Save As...");
		saveAsMenuItem.setMnemonic('A');
		saveAsMenuItem.addActionListener(this);
		fileMenu.add(saveAsMenuItem);

		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setMnemonic('X');
		exitMenuItem.addActionListener(this);
		fileMenu.add(exitMenuItem);

		editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		menuBar.add(editMenu);

		undoMenuItem = new JMenuItem("Undo");
		undoMenuItem.setMnemonic('U');
		undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
		undoMenuItem.addActionListener(this);
		undoMenuItem.setEnabled(false);
		editMenu.add(undoMenuItem);

		redoMenuItem = new JMenuItem("Redo");
		redoMenuItem.setMnemonic('R');
		redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
		redoMenuItem.addActionListener(this);
		redoMenuItem.setEnabled(false);
		editMenu.add(redoMenuItem);
		editMenu.addSeparator();

		cutMenuItem = new JMenuItem("Cut");
		cutMenuItem.setMnemonic('T');
		cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
		cutMenuItem.addActionListener(this);
		cutMenuItem.setEnabled(false);
		editMenu.add(cutMenuItem);

		copyMenuItem = new JMenuItem("Copy");
		copyMenuItem.setMnemonic('C');
		copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		copyMenuItem.addActionListener(this);
		copyMenuItem.setEnabled(false);
		editMenu.add(copyMenuItem);

		pasteMenuItem = new JMenuItem("Paste");
		pasteMenuItem.setMnemonic('P');
		pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		pasteMenuItem.addActionListener(this);
		pasteMenuItem.setEnabled(Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.plainTextFlavor));
		editMenu.add(pasteMenuItem);

		deleteMenuItem = new JMenuItem("Delete");
		deleteMenuItem.setMnemonic('D');
		deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		deleteMenuItem.addActionListener(this);
		deleteMenuItem.setEnabled(false);
		editMenu.add(deleteMenuItem);
		editMenu.addSeparator();

		selectAllMenuItem = new JMenuItem("Select All");
		selectAllMenuItem.setMnemonic('A');
		selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
		selectAllMenuItem.addActionListener(this);
		editMenu.add(selectAllMenuItem);

		searchMenu = new JMenu("Search");
		searchMenu.setMnemonic('S');
		menuBar.add(searchMenu);

		quickFindMenuItem = new JMenuItem("Quick Find");
		quickFindMenuItem.setMnemonic('F');
		quickFindMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
		quickFindMenuItem.addActionListener(this);
		searchMenu.add(quickFindMenuItem);

		// Legacy: It was used in assignment 2
		/*viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');
		menuBar.add(viewMenu);

		wordWrapMenuItem = new JCheckBoxMenuItem("Word Wrap");
		wordWrapMenuItem.setMnemonic('W');
		wordWrapMenuItem.addActionListener(this);
		viewMenu.add(wordWrapMenuItem);*/

		highlightingMenu = new JMenu("Highlighting");
		highlightingMenu.setMnemonic('H');
		menuBar.add(highlightingMenu);

		noneMenuItem = new JRadioButtonMenuItem("None");
		noneMenuItem.getModel().setActionCommand(noneMenuItem.getActionCommand());
		noneMenuItem.setMnemonic('N');
		noneMenuItem.addChangeListener(this);
		highlightingMenu.add(noneMenuItem);

		englishSpellcheckingMenuItem = new JRadioButtonMenuItem("English Spellchecking");
		englishSpellcheckingMenuItem.getModel().setActionCommand(englishSpellcheckingMenuItem.getActionCommand());
		englishSpellcheckingMenuItem.setMnemonic('E');
		englishSpellcheckingMenuItem.addChangeListener(this);
		highlightingMenu.add(englishSpellcheckingMenuItem);

		javaSourceCodeMenuItem = new JRadioButtonMenuItem("Java Source Code");
		javaSourceCodeMenuItem.getModel().setActionCommand(javaSourceCodeMenuItem.getActionCommand());
		javaSourceCodeMenuItem.setMnemonic('J');
		javaSourceCodeMenuItem.addChangeListener(this);
		highlightingMenu.add(javaSourceCodeMenuItem);

		// Automatically populate the Button Group with all Highlighting choices
		for (int highlightingChoice = 0; highlightingChoice < highlightingMenu.getMenuComponentCount(); ++highlightingChoice) {
			highlightingButtonGroup.add((JRadioButtonMenuItem)highlightingMenu.getMenuComponent(highlightingChoice));
		}
		// Set English Spellchecking as the default highlighting option
		lastHighlightingOption = englishSpellcheckingMenuItem.getModel();
		highlightingButtonGroup.setSelected(lastHighlightingOption, true);

		Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(this);

		popupMenu = new MyPopupMenu(this);
		textPane.setComponentPopupMenu(popupMenu);

		setDocumentHasUnsavedChanges(false);

		loadDictionary();

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		pack();
	Insets insets = getInsets();
	//System.out.println(insets);
	Rectangle bounds = getContentPane().getBounds();
	//System.out.println(bounds);
	textPaneScrollPane.setPreferredSize(new Dimension(640 - insets.left - insets.right - bounds.x, 480 - insets.top - insets.bottom - bounds.y));
	pack();
		if (frameLocationX == Integer.MIN_VALUE && frameLocationY == Integer.MIN_VALUE) {
			setLocationRelativeTo(null);		// Start centered on screen by default
		} else {
			setLocation(frameLocationX, frameLocationY);	// Use saved position from the last run
		}
		if (frameSizeX != -1 && frameSizeY != -1) {
			setSize(frameSizeX, frameSizeY);	// Use saved frame size from the last run
		}
		if (frameMaximized != 0) this.setExtendedState(JFrame.MAXIMIZED_BOTH);

		//final Dimension minSize = new Dimension(mainFrame.getWidth(), mainFrame.getHeight());
		//mainFrame.setMinimumSize(minSize);			// Set a minimum size, so that it can't be resized to a very tiny window
		setVisible(true);
	}

	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getActionCommand().equals("New")) {
			if (dealWithUnsavedChanges() == true)
			{
				documentFile = null;

				textPane.setText("");

				setDocumentHasUnsavedChanges(false);

				textPaneWasReset();
			}
		}
		else if (ae.getActionCommand().equals("Open..."))
		{
			if (dealWithUnsavedChanges() == true)
			{
				performFileOpen();
			}
		}
		else if (ae.getActionCommand().equals("Save"))
		{
			performFileSave();
		}
		else if (ae.getActionCommand().equals("Save As..."))
		{
			performFileSaveAs();
		}
		else if (ae.getActionCommand().equals("Exit"))
		{
			//System.exit(0);
			java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
		else if (ae.getActionCommand().equals("Undo"))
		{
			undoManager.undo();
			updateUnredoMenuItems();
		}
		else if (ae.getActionCommand().equals("Redo"))
		{
			undoManager.redo();
			updateUnredoMenuItems();
		}
		else if (ae.getActionCommand().equals("Cut"))
		{
			textPane.cut();
		}
		else if (ae.getActionCommand().equals("Copy"))
		{
			textPane.copy();
		}
		else if (ae.getActionCommand().equals("Paste"))
		{
			textPane.paste();
		}
		else if (ae.getActionCommand().equals("Delete"))
		{
			try {
				textPane.getDocument().remove(textPane.getSelectionStart(), textPane.getSelectionEnd() - textPane.getSelectionStart());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		else if (ae.getActionCommand().equals("Select All"))
		{
			textPane.selectAll();
		}
		else if (ae.getActionCommand().equals("Quick Find"))
		{
			quickFindPanel.showPanel();
		}
		else if (ae.getActionCommand().equals("Word Wrap"))
		{
			////textArea.setLineWrap(wordWrapMenuItem.isSelected());
		}
		else
			System.err.println("Warning: Unknown action command '" + ae.getActionCommand() + "'.");
	}

	// Tracks the changes of Highlighting type
	public void stateChanged(ChangeEvent ce) {
		if (highlightingButtonGroup.getSelection() != lastHighlightingOption) {
			//System.out.println("changing to " + highlightingButtonGroup.getSelection().getActionCommand() + ",\n  doing fullCheckSpelling();");
			//SwingUtilities.invokeLater(new Runnable() { public void run() { fullCheckSpelling(); } });
			fullCheckSpelling();

			lastHighlightingOption = highlightingButtonGroup.getSelection();
		}
	}

	public boolean getDocumentHasUnsavedChanges() {
		return documentHasUnsavedChanges;
	}

	public void setDocumentHasUnsavedChanges(boolean documentHasUnsavedChanges) {
		this.documentHasUnsavedChanges = documentHasUnsavedChanges;
		saveMenuItem.setEnabled(documentHasUnsavedChanges || documentFile == null);
		updateTitle();
	}

	private void updateTitle() {
		setTitle((documentFile == null ? "Untitled" : documentFile.getName()) + (getDocumentHasUnsavedChanges() ? "*" : "") + " - " + applicationTitle);
	}

	private void loadProperties() {
		// Load saved program settings
		try {
			properties.load(new FileInputStream("Application_Configuration.properties"));
			frameMaximized = Integer.parseInt(properties.getProperty("FrameMaximized", Integer.toString(frameMaximized)));
			frameLocationX = Integer.parseInt(properties.getProperty("FrameLocationX", Integer.toString(frameLocationX)));
			frameLocationY = Integer.parseInt(properties.getProperty("FrameLocationY", Integer.toString(frameLocationY)));
			frameSizeX = Integer.parseInt(properties.getProperty("FrameSizeX", Integer.toString(frameSizeX)));
			frameSizeY = Integer.parseInt(properties.getProperty("FrameSizeY", Integer.toString(frameSizeY)));
			if (properties.getProperty("LastDirectory") != null && properties.getProperty("LastDirectory").equals("") == false) {
				lastDirectory = new File(properties.getProperty("LastDirectory"));
			}
		} catch (java.io.FileNotFoundException fnfe) {
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void saveProperties() {
		// Save settings
		try {
			properties.setProperty("FrameMaximized", Integer.toString(frameMaximized));
			properties.setProperty("FrameLocationX", Integer.toString(frameLocationX));
			properties.setProperty("FrameLocationY", Integer.toString(frameLocationY));
			properties.setProperty("FrameSizeX", Integer.toString(frameSizeX));
			properties.setProperty("FrameSizeY", Integer.toString(frameSizeY));
			properties.setProperty("LastDirectory", lastDirectory == null ? "" : lastDirectory.getCanonicalPath());
			properties.store(new FileOutputStream("Application_Configuration.properties"), "");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void flavorsChanged(FlavorEvent e) {
		// Checks if there is text in Clipboard
		boolean textAvaliable = Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.plainTextFlavor);

		pasteMenuItem.setEnabled(textAvaliable);
		popupMenu.getPastePopupMenuItem().setEnabled(textAvaliable);
	}

	private class MyWindowAdapter extends WindowAdapter
	{
		@Override
		public void windowClosing(WindowEvent we)
		{
			boolean shouldExit = (dealWithUnsavedChanges() == true);

			if (shouldExit) {
				((TextEditorFrame)we.getComponent()).saveProperties();
				((TextEditorFrame)we.getComponent()).saveDictionary();

				// Exit
				we.getWindow().dispose();
			}
		}

		@Override
		public void windowStateChanged(WindowEvent e)
		{
			int windowState = e.getNewState();
			if (windowState == JFrame.NORMAL) frameMaximized = 0;
			else if ((windowState & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) frameMaximized = 1;
		}
	}

	private class MyComponentAdapter extends ComponentAdapter
	{
		@Override
		public void componentMoved(ComponentEvent e)
		{
			JFrame frame = (JFrame)e.getComponent();
			if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH
					&& frame.getLocation().x != -4		// This is a hack, but it's needed because there seems to be a bug where
					&& frame.getLocation().y != -4) {	// sometimes the State isn't reported as maximized, but location is (-4, -4)
				frameLocationX = frame.getLocation().x;
				frameLocationY = frame.getLocation().y;
			}
		}

		@Override
		public void componentResized(ComponentEvent e)
		{
			if (e.getComponent() == textPaneScrollPane) {
				quickFindPanel.updateSize();
			} else {
				JFrame frame = (JFrame)e.getComponent();
				if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
					frameSizeX = frame.getSize().width;
					frameSizeY = frame.getSize().height;
				}
			}
		}
	}

	// Painter for underlined highlights
	private class UnderlineHighlightPainter extends LayeredHighlighter.LayerPainter
	{
		public UnderlineHighlightPainter(Color c) {
			color = c;
		}

		public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
		  // Do nothing: this method will never be called
		}

		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view)
		{
			g.setColor(color == null ? c.getSelectionColor() : color);

			Rectangle alloc = null;
			if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
				if (bounds instanceof Rectangle) {
					alloc = (Rectangle) bounds;
				} else {
					alloc = bounds.getBounds();
				}
			} else {
				try {
					Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
					alloc = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
				} catch (BadLocationException e) {
					return null;
				}
			}

			FontMetrics fm = c.getFontMetrics(c.getFont());
			int baseline = alloc.y + alloc.height - fm.getDescent() + 1;
			Graphics2D g2 = (Graphics2D)g;
			Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 2, 2 }, 0);
			g2.setStroke(stroke);
			g2.drawLine(alloc.x, baseline, alloc.x + alloc.width, baseline);
			g2.drawLine(alloc.x - 2, baseline + 1, alloc.x + alloc.width, baseline + 1);

			return alloc;
		}

		protected Color color; // The color for the underline
	}
}
public class a3
{
	public static void main(String[] args)
	{
		// Use Win32 look and feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame mainFrame = new TextEditorFrame();
			}
		});
	}
}
