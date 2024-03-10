package org.maia.amstrad.io.tape.config;

import java.io.File;

import org.maia.amstrad.io.tape.model.AudioRange;
import org.maia.amstrad.io.tape.model.TapeProgramMetaData;
import org.maia.amstrad.io.tape.read.AudioFile;

public class TapeReaderTaskConfiguration implements Cloneable {

	private AudioFile audioFile; // tape recording .WAV file

	private AudioRange selectionInAudioFile; // null for entire file

	private File outputDirectory; // root directory for all generated program folders

	private TapeProgramMetaData defaultProgramMetaData;

	public TapeReaderTaskConfiguration() {
		this.defaultProgramMetaData = new TapeProgramMetaData();
	}

	@Override
	public TapeReaderTaskConfiguration clone() {
		TapeReaderTaskConfiguration clone = new TapeReaderTaskConfiguration();
		clone.setAudioFile(getAudioFile());
		clone.setOutputDirectory(getOutputDirectory());
		clone.setSelectionInAudioFile(getSelectionInAudioFile());
		clone.setDefaultProgramMetaData(getDefaultProgramMetaData().clone());
		return clone;
	}

	public AudioFile getAudioFile() {
		return audioFile;
	}

	public void setAudioFile(AudioFile audioFile) {
		this.audioFile = audioFile;
	}

	public AudioRange getSelectionInAudioFile() {
		return selectionInAudioFile;
	}

	public void setSelectionInAudioFile(AudioRange selectionInAudioFile) {
		this.selectionInAudioFile = selectionInAudioFile;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		if (outputDirectory != null && !outputDirectory.isDirectory())
			throw new IllegalArgumentException("Not a directory");
		this.outputDirectory = outputDirectory;
	}

	public TapeProgramMetaData getDefaultProgramMetaData() {
		return defaultProgramMetaData;
	}

	public void setDefaultProgramMetaData(TapeProgramMetaData metaData) {
		this.defaultProgramMetaData = metaData;
	}

}