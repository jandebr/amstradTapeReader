package org.maia.amstrad.io.tape.gui;

import javax.swing.JPanel;

import org.maia.amstrad.io.tape.read.AudioFile;

@SuppressWarnings("serial")
public abstract class AudioFilePositionSource extends JPanel {

	private AudioFile audioFile;

	protected AudioFilePositionSource(AudioFile audioFile) {
		this.audioFile = audioFile;
	}

	public abstract int getWidthForDisplayRange();

	public AudioFile getAudioFile() {
		return audioFile;
	}

}