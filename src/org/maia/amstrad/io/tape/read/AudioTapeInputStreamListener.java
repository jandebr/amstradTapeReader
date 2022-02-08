package org.maia.amstrad.io.tape.read;

import org.maia.amstrad.io.tape.model.Bit;

public interface AudioTapeInputStreamListener {

	void readBit(Bit bit, long sampleOffset, long sampleLength, AudioTapeInputStream is);

}