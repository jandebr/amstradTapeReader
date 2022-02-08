package org.maia.amstrad.io.tape.model;

public class Marker {

	private int position;

	public static String getUnderlineString(int offset, int length) {
		StringBuilder sb = new StringBuilder(offset + length);
		for (int i = 0; i < offset; i++)
			sb.append(' ');
		for (int i = 0; i < length; i++)
			sb.append('^');
		return sb.toString();
	}

	public Marker(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

}