package org.maia.amstrad.io.tape.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

public class ByteSequence {

	private List<Short> bytes;

	public ByteSequence() {
		this.bytes = new Vector<Short>(2048);
	}

	public void save(File file) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		for (Short bite : getBytes()) {
			out.write(bite.intValue());
		}
		out.close();
	}

	public static ByteSequence load(File file) throws IOException {
		ByteSequence bytes = new ByteSequence();
		FileInputStream in = new FileInputStream(file);
		byte[] buffer = new byte[2048];
		int bytesRead = in.read(buffer);
		while (bytesRead >= 0) {
			for (int i = 0; i < bytesRead; i++) {
				short s = (short) buffer[i];
				if (s < 0)
					s += 256;
				bytes.addByte(s);
			}
			bytesRead = in.read(buffer);
		}
		in.close();
		return bytes;
	}

	public void saveAsText(File file) throws IOException {
		PrintWriter pw = new PrintWriter(file, "ISO-8859-1");
		pw.print(asText());
		pw.close();
	}

	private CharSequence asText() {
		int n = getLength();
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			byte b = getBytes().get(i).byteValue();
			sb.append((char) b);
		}
		return sb;
	}

	public String toHumanReadableString() {
		return toHumanReadableString(null); // single line, no marker
	}

	public String toHumanReadableString(Marker marker) {
		return toHumanReadableString(Integer.MAX_VALUE, marker); // single line, with marker
	}

	public String toHumanReadableString(int maxLineLength) {
		return toHumanReadableString(maxLineLength, null); // multi line, no marker
	}

	public String toHumanReadableString(int maxLineLength, Marker marker) {
		return toHumanReadableString(maxLineLength, marker, null); // multi line, with marker
	}

	public String toHumanReadableString(int maxLineLength, Marker marker, int[] positions) {
		StringBuilder page = new StringBuilder(getLength() + 256);
		StringBuilder line = new StringBuilder(Math.min(maxLineLength, getLength() + 256));
		int mpos = marker == null ? -1 : marker.getPosition();
		int moff = -1;
		int mlen = 0;
		for (int i = 0; i < getLength(); i++) {
			short b = getBytes().get(i).shortValue();
			String bs;
			if (b < 32 || b > 126) {
				bs = "[" + b + "]";
			} else {
				bs = String.valueOf((char) b);
			}
			if (line.length() + bs.length() > maxLineLength) {
				page.append(line).append('\n');
				if (moff >= 0) {
					page.append(Marker.getUnderlineString(moff, mlen)).append('\n');
					moff = -1;
				}
				line.setLength(0);
			}
			if (i == mpos) {
				moff = line.length();
				mlen = bs.length();
			}
			if (positions != null) {
				positions[i] = page.length() + line.length();
			}
			line.append(bs);
		}
		if (line.length() > 0) {
			page.append(line);
			if (moff >= 0) {
				page.append('\n').append(Marker.getUnderlineString(moff, mlen));
			}
		}
		if (positions != null && positions.length >= getLength() + 1) {
			positions[getLength()] = page.length();
		}
		return page.toString();
	}

	public void addByte(Short bite) {
		getBytes().add(bite);
	}

	public void addBytes(List<Short> bytes) {
		getBytes().addAll(bytes);
	}

	public void addBytes(ByteSequence bytes) {
		addBytes(bytes.getBytes());
	}

	public void truncate(int length) {
		if (length > getLength())
			throw new IllegalArgumentException("Cannot truncate beyond the end of this sequence: " + length + " > "
					+ getLength());
		else if (length < getLength()) {
			this.bytes = new Vector<Short>(getBytes().subList(0, length));
		}
	}

	public ByteSequence subSequence(int fromIndex, int toIndex) {
		ByteSequence sub = new ByteSequence();
		sub.addBytes(getBytes().subList(fromIndex, toIndex));
		return sub;
	}

	public int findSubSequence(ByteSequence subSequence) {
		short[] ba = getBytesArray();
		short[] sa = subSequence.getBytesArray();
		for (int i = 0; i <= ba.length - sa.length; i++) {
			for (int j = 0; j <= sa.length; j++) {
				if (j == sa.length)
					return i;
				if (ba[i + j] != sa[j])
					break;
			}
		}
		return -1;
	}

	public int getLength() {
		return getBytes().size();
	}

	public short[] getBytesArray() {
		short[] array = new short[getLength()];
		for (int i = 0; i < array.length; i++) {
			array[i] = getBytes().get(i);
		}
		return array;
	}

	public List<Short> getBytes() {
		return bytes;
	}

}