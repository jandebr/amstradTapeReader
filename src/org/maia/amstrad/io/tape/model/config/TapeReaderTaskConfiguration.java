package org.maia.amstrad.io.tape.model.config;

import java.io.File;

import org.maia.amstrad.io.tape.model.AudioRange;
import org.maia.amstrad.io.tape.read.AudioFile;

public class TapeReaderTaskConfiguration implements Cloneable {

	private AudioFile audioFile; // tape recording .WAV file

	private AudioRange selectionInAudioFile; // null for entire file

	private File outputDirectory; // root directory for all generated program folders

	private MetaData defaultProgramMetaData;

	public TapeReaderTaskConfiguration() {
		this.defaultProgramMetaData = new MetaData();
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

	public MetaData getDefaultProgramMetaData() {
		return defaultProgramMetaData;
	}

	public void setDefaultProgramMetaData(MetaData metaData) {
		this.defaultProgramMetaData = metaData;
	}

	public static class MetaData implements Cloneable {

		private String author;

		private String year;

		private String tape;

		private String monitor;

		private String description;

		private String authoring;

		public MetaData() {
		}

		@Override
		public MetaData clone() {
			MetaData clone = new MetaData();
			clone.setAuthor(getAuthor());
			clone.setYear(getYear());
			clone.setTape(getTape());
			clone.setMonitor(getMonitor());
			clone.setDescription(getDescription());
			clone.setAuthoring(getAuthoring());
			return clone;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getYear() {
			return year;
		}

		public void setYear(String year) {
			this.year = year;
		}

		public String getTape() {
			return tape;
		}

		public void setTape(String tape) {
			this.tape = tape;
		}

		public String getMonitor() {
			return monitor;
		}

		public void setMonitor(String monitor) {
			this.monitor = monitor;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getAuthoring() {
			return authoring;
		}

		public void setAuthoring(String authoring) {
			this.authoring = authoring;
		}

	}

}