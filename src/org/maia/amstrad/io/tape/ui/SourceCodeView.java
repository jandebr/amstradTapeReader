package org.maia.amstrad.io.tape.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.maia.amstrad.io.tape.model.sc.SourceCode;
import org.maia.amstrad.io.tape.model.sc.SourceCodeLine;
import org.maia.amstrad.io.tape.model.sc.SourceCodePosition;
import org.maia.amstrad.io.tape.model.sc.SourceCodeRange;

@SuppressWarnings("serial")
public class SourceCodeView extends JPanel implements CaretListener {

	private SourceCode sourceCode;

	private int[] sourceCodeLineOffsets; // JTextPane document offsets by line index

	private StyleContext styleContext;

	private JTextPane sourceCodePane;

	private SourceCodeRange sourceCodeSelection;

	private List<SourceCodeCaretListener> caretListeners;

	private static final String NAMED_STYLE_LINENUMBER = "lineNumber";

	private static final String NAMED_STYLE_SOURCECODE = "sourceCode";

	private static final String NAMED_STYLE_SOURCECODE_SELECTED = "sourceCodeSelected";

	private static Color SOURCE_CODE_BACKGROUND = new Color(0, 10, 0);

	public SourceCodeView(SourceCode sourceCode) {
		super(new BorderLayout());
		this.sourceCode = sourceCode;
		this.sourceCodeLineOffsets = new int[sourceCode.getLines().size()];
		this.styleContext = createStyleContext();
		this.caretListeners = new Vector<SourceCodeCaretListener>();
		buildView();
		setPreferredSize(UIResources.sourceCodeViewSize);
	}

	public void addCaretListener(SourceCodeCaretListener listener) {
		getCaretListeners().add(listener);
	}

	public void removeCaretListener(SourceCodeCaretListener listener) {
		getCaretListeners().remove(listener);
	}

	private StyleContext createStyleContext() {
		StyleContext ctx = new StyleContext();
		installLineNumberStyle(ctx);
		installSourceCodeStyle(ctx);
		installSourceCodeSelectedStyle(ctx);
		return ctx;
	}

	private void installLineNumberStyle(StyleContext ctx) {
		Style style = ctx.addStyle(NAMED_STYLE_LINENUMBER, null);
		StyleConstants.setForeground(style, new Color(0, 240, 0));
		StyleConstants.setFontFamily(style, Font.MONOSPACED);
		StyleConstants.setBold(style, true);
		StyleConstants.setItalic(style, true);
		StyleConstants.setFontSize(style, 16);
	}

	private void installSourceCodeStyle(StyleContext ctx) {
		Style style = ctx.addStyle(NAMED_STYLE_SOURCECODE, null);
		StyleConstants.setForeground(style, new Color(0, 174, 0));
		StyleConstants.setFontFamily(style, Font.MONOSPACED);
		StyleConstants.setBold(style, true);
		StyleConstants.setFontSize(style, 16);
	}

	private void installSourceCodeSelectedStyle(StyleContext ctx) {
		Style style = ctx.addStyle(NAMED_STYLE_SOURCECODE_SELECTED, ctx.getStyle(NAMED_STYLE_SOURCECODE));
		StyleConstants.setForeground(style, Color.WHITE);
	}

	private void buildView() {
		add(buildSourceCodePane(), BorderLayout.CENTER);
	}

	private JComponent buildSourceCodePane() {
		JScrollPane scrollPane = new JScrollPane(buildSourceCodeLinesPane());
		scrollPane.setRowHeaderView(buildSourceCodeLineNumbersPane());
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);
		return scrollPane;
	}

	private JComponent buildSourceCodeLineNumbersPane() {
		JTextPane pane = new JTextPane(new DefaultStyledDocument(getStyleContext()));
		pane.setBackground(SOURCE_CODE_BACKGROUND);
		pane.setBorder(new LineBorder(pane.getBackground(), 8));
		Document doc = pane.getDocument();
		Style style = pane.getStyle(NAMED_STYLE_LINENUMBER);
		try {
			int n = getSourceCode().getLines().size();
			for (int i = 0; i < n; i++) {
				if (i > 0)
					doc.insertString(doc.getLength(), "\n", style);
				SourceCodeLine line = getSourceCode().getLines().get(i);
				doc.insertString(doc.getLength(), String.valueOf(line.getLineNumber()), style);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		pane.setEditable(false);
		pane.setCaretPosition(0);
		return pane;
	}

	private JComponent buildSourceCodeLinesPane() {
		JTextPane pane = new JTextPane(new DefaultStyledDocument(getStyleContext()));
		pane.setBackground(SOURCE_CODE_BACKGROUND);
		pane.setBorder(new LineBorder(pane.getBackground(), 8));
		Document doc = pane.getDocument();
		Style style = pane.getStyle(NAMED_STYLE_SOURCECODE);
		try {
			int n = getSourceCode().getLines().size();
			for (int i = 0; i < n; i++) {
				if (i > 0)
					doc.insertString(doc.getLength(), "\n", style);
				this.sourceCodeLineOffsets[i] = doc.getLength();
				SourceCodeLine line = getSourceCode().getLines().get(i);
				doc.insertString(doc.getLength(), line.getCode().toString(), style);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		pane.setEditable(false);
		pane.setCaretPosition(0);
		pane.addCaretListener(this);
		this.sourceCodePane = pane;
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(pane, BorderLayout.CENTER);
		return panel;
	}

	@Override
	public void caretUpdate(CaretEvent event) {
		SourceCodePosition position = mapSourceCodePosition(event.getDot());
		for (SourceCodeCaretListener listener : getCaretListeners()) {
			listener.caretUpdate(position, this);
		}
	}

	public void selectSourceCode(SourceCodeRange range) {
		clearSourceCodeSelection();
		if (range != null) {
			setSourceCodeSelection(range);
			applyStyle(range, NAMED_STYLE_SOURCECODE_SELECTED);
		}
	}

	public void clearSourceCodeSelection() {
		if (getSourceCodeSelection() != null) {
			applyStyle(getSourceCodeSelection(), NAMED_STYLE_SOURCECODE);
			setSourceCodeSelection(null);
		}
	}

	private void applyStyle(SourceCodeRange range, String styleName) {
		StyledDocument doc = getSourceCodePane().getStyledDocument();
		int p0 = mapDocumentOffset(range.getStartPosition());
		int p1 = mapDocumentOffset(range.getEndPosition());
		if (p0 >= 0 && p1 >= p0) {
			doc.setCharacterAttributes(p0, p1 - p0 + 1, doc.getStyle(styleName), true);
		}
	}

	private SourceCodePosition mapSourceCodePosition(int documentOffset) {
		SourceCodePosition position = null;
		int row = Arrays.binarySearch(sourceCodeLineOffsets, documentOffset);
		if (row < 0)
			row = -row - 2;
		if (row >= 0 && row < sourceCodeLineOffsets.length) {
			int col = documentOffset - sourceCodeLineOffsets[row];
			SourceCodeLine line = getSourceCode().getLines().get(row);
			if (col < line.getCode().length()) {
				position = new SourceCodePosition(line.getLineNumber(), col);
			}
		}
		return position;
	}

	private int mapDocumentOffset(SourceCodePosition position) {
		int offset = -1;
		SourceCodeLine line = getSourceCode().getLine(position.getLineNumber());
		if (line != null) {
			int lineIndex = getSourceCode().getLines().indexOf(line);
			offset = sourceCodeLineOffsets[lineIndex] + position.getLinePosition();
		}
		return offset;
	}

	public SourceCode getSourceCode() {
		return sourceCode;
	}

	public SourceCodeRange getSourceCodeSelection() {
		return sourceCodeSelection;
	}

	private void setSourceCodeSelection(SourceCodeRange sourceCodeSelection) {
		this.sourceCodeSelection = sourceCodeSelection;
	}

	private JTextPane getSourceCodePane() {
		return sourceCodePane;
	}

	private StyleContext getStyleContext() {
		return styleContext;
	}

	private List<SourceCodeCaretListener> getCaretListeners() {
		return caretListeners;
	}

	public static interface SourceCodeCaretListener {

		void caretUpdate(SourceCodePosition position, SourceCodeView source);

	}

}