package org.maia.amstrad.io.tape.read;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * An audio WAVE file as the recording of an Amstrad tape.
 * 
 * <h3>Assumptions</h3>
 * <ul>
 * <li>The number of sound channels is 1 (mono).</li>
 * <li>The sound data consists of 16-bit samples, stored as 2's-complement signed integers, ranging from -32768 to
 * 32767.</li>
 * <li>The byte ordering is little-endian.</li>
 * </ul>
 */
public class AudioWaveFile extends AudioFile {

	private RandomAccessFile file;

	private long numberOfSamples = -1L;

	private byte[] buffer;

	private int bufferLength;

	private long bufferOffset;

	private static final long HEADER_LENGTH = 44L; // in bytes

	private static final long SAMPLE_SIZE = 2L; // in bytes

	public AudioWaveFile(File sourceFile) throws FileNotFoundException {
		super(sourceFile);
		this.file = new RandomAccessFile(sourceFile, "r");
		this.buffer = new byte[2048];
	}

	@Override
	public void close() throws IOException {
		file.close();
	}

	@Override
	public int getSampleRate() throws IOException {
		byte[] data = new byte[4];
		file.seek(24);
		file.read(data);
		return ((data[3] & 0xff) << 24) | ((data[2] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[0] & 0xff);
	}

	@Override
	public long getNumberOfSamples() throws IOException {
		if (numberOfSamples < 0L) {
			numberOfSamples = (file.length() - HEADER_LENGTH) / SAMPLE_SIZE;
		}
		return numberOfSamples;
	}

	@Override
	public short getSample(long index) throws IOException {
		long offset = HEADER_LENGTH + index * SAMPLE_SIZE;
		if (offset >= bufferOffset && offset + SAMPLE_SIZE <= bufferOffset + bufferLength) {
			// read from buffer
			int bi = (int) (offset - bufferOffset);
			return (short) ((buffer[bi + 1] << 8) | (buffer[bi] & 0xff));
		} else {
			// fill buffer
			file.seek(offset);
			bufferOffset = offset;
			bufferLength = file.read(buffer);
			if (bufferLength < SAMPLE_SIZE) {
				return 0;
			} else {
				return (short) ((buffer[1] << 8) | (buffer[0] & 0xff));
			}
		}
	}

}