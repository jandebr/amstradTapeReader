package org.maia.amstrad.io.tape.model.sc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

public class SourceCode {

	private List<SourceCodeLine> lines;

	private Map<Integer, SourceCodeLine> linesIndex;

	public SourceCode() {
		this.lines = new Vector<SourceCodeLine>(1000);
		this.linesIndex = new HashMap<Integer, SourceCodeLine>(1000);
	}

	public static SourceCode parseFromExternalForm(CharSequence externalForm) {
		SourceCode sourceCode = new SourceCode();
		StringTokenizer st = new StringTokenizer(externalForm.toString(), "\r\n");
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			int i = line.indexOf(' ');
			if (i >= 0) {
				try {
					int lineNumber = Integer.parseInt(line.substring(0, i));
					CharSequence lineCode = line.subSequence(i + 1, line.length());
					sourceCode.addLine(new SourceCodeLine(lineNumber, lineCode));
				} catch (NumberFormatException e) {
					System.err.println("Failed to parse line number: " + e);
				}
			}
		}
		return sourceCode;
	}

	public void save(File file) throws IOException {
		PrintWriter pw = new PrintWriter(file, "UTF-8");
		pw.println(toString());
		pw.close();
	}

	public String toString() {
		return toExternalForm();
	}

	public String toExternalForm() {
		StringBuilder sb = new StringBuilder(40 * (1 + getLines().size()));
		for (int i = 0; i < getLines().size(); i++) {
			if (i > 0)
				sb.append('\n');
			SourceCodeLine line = getLines().get(i);
			sb.append(line.getLineNumber());
			sb.append(' ');
			sb.append(line.getCode());
		}
		return sb.toString();
	}

	public void addLine(SourceCodeLine line) {
		getLines().add(line);
		getLinesIndex().put(line.getLineNumber(), line);
	}

	public SourceCodeLine getLine(int lineNumber) {
		return getLinesIndex().get(lineNumber);
	}

	public List<SourceCodeLine> getLines() {
		return lines;
	}

	private Map<Integer, SourceCodeLine> getLinesIndex() {
		return linesIndex;
	}

}