package org.maia.amstrad.io.tape.read;

import java.io.IOException;

import org.maia.amstrad.io.tape.model.Bit;

public abstract class TapeInputStream {

	public abstract Bit nextBit() throws IOException;

	public Short nextByte() throws IOException {
		Short bite = null;
		short b = 0;
		short v = 128;
		Bit bit = null;
		while (v != 0 && (bit = nextBit()) != null) {
			if (Bit.ONE.equals(bit))
				b += v;
			v /= 2;
		}
		if (v == 0)
			bite = Short.valueOf(b);
		return bite;
	}

	public Integer nextWord() throws IOException {
		Integer word = null;
		Short b1 = nextByte();
		if (b1 != null) {
			Short b2 = nextByte();
			if (b2 != null) {
				word = b1.shortValue() + 256 * b2.shortValue();
			}
		}
		return word;
	}

	public abstract boolean seekSilence() throws IOException;

	public abstract boolean skipSilence() throws IOException;

	public abstract void close() throws IOException;

}