package org.maia.amstrad.io.tape.read;

import java.io.File;
import java.io.IOException;

/**
 * An audio file as the recording of an Amstrad tape
 */
public abstract class AudioFile {

	private File sourceFile;

	public AudioFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}

	public String toString() {
		return getSourceFile().getPath();
	}

	public abstract void close() throws IOException;

	public abstract int getSampleRate() throws IOException;

	public abstract long getNumberOfSamples() throws IOException;

	public abstract short getSample(long index) throws IOException;

	public short getAbsoluteSample(long index) throws IOException {
		return (short) Math.abs(getSample(index));
	}

	public File getSourceFile() {
		return sourceFile;
	}

}
