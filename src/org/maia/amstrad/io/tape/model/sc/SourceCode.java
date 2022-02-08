package org.maia.amstrad.io.tape.model.sc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class SourceCode {

	private List<SourceCodeLine> lines;

	private Map<Integer, SourceCodeLine> linesIndex;

	public SourceCode() {
		this.lines = new Vector<SourceCodeLine>(1000);
		this.linesIndex = new HashMap<Integer, SourceCodeLine>(1000);
	}

	public void save(File file) throws IOException {
		PrintWriter pw = new PrintWriter(file, "UTF-8");
		pw.print(toString());
		pw.close();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(2048);
		for (SourceCodeLine line : getLines()) {
			sb.append(line.toString());
			sb.append('\n');
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