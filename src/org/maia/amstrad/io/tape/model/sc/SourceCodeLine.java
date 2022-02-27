package org.maia.amstrad.io.tape.model.sc;

import org.maia.amstrad.io.tape.model.Marker;

public class SourceCodeLine {

	private int lineNumber;

	private CharSequence code;

	public SourceCodeLine(int lineNumber, CharSequence code) {
		this.lineNumber = lineNumber;
		this.code = code;
	}

	public String toString() {
		return String.valueOf(getLineNumber()) + " " + getCode();
	}

	public String toUnderlineString(Marker marker) {
		int offset = String.valueOf(getLineNumber()).length() + 1 + marker.getPosition();
		return toString() + "\n" + Marker.getUnderlineString(offset, 1);
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public CharSequence getCode() {
		return code;
	}

}