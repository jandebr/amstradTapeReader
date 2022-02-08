package org.maia.amstrad.io.tape.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JComponent;

import org.maia.amstrad.io.tape.model.AudioTapeIndex;
import org.maia.amstrad.io.tape.model.AudioTapeProgram;
import org.maia.amstrad.io.tape.model.ByteSequence;
import org.maia.amstrad.io.tape.model.profile.TapeProfile;
import org.maia.amstrad.io.tape.model.sc.SourceCode;
import org.maia.amstrad.io.tape.read.AudioFile;

public class UIFactory {

	private UIFactory() {
	}

	public static Viewer createAudioFileProfileViewer(AudioFile audioFile, TapeProfile tapeProfile, int pixelsPerSecond)
			throws IOException {
		JComponent view = createExtendedProfileView(audioFile, tapeProfile, pixelsPerSecond);
		String title = "Tape profile of " + audioFile;
		Viewer viewer = new Viewer(view, title);
		viewer.build();
		return viewer;
	}

	public static Viewer createAudioTapeIndexViewer(AudioTapeIndex tapeIndex) {
		JComponent view = new AudioTapeIndexView(tapeIndex);
		String title = "Index of " + tapeIndex.getAudioFile().getSourceFile().getName();
		Viewer viewer = new Viewer(view, title);
		viewer.build();
		return viewer;
	}

	public static Viewer createAudioTapeIndexExtendedViewer(AudioTapeIndex tapeIndex, TapeProfile tapeProfile,
			int pixelsPerSecond) throws IOException {
		JComponent view = new AudioTapeIndexExtendedView(tapeIndex, createExtendedProfileView(tapeIndex.getAudioFile(),
				tapeProfile, pixelsPerSecond));
		String title = "Index of " + tapeIndex.getAudioFile().getSourceFile().getName();
		Viewer viewer = new Viewer(view, title);
		viewer.build();
		return viewer;
	}

	public static Viewer createCodeInspectorViewer(AudioTapeProgram audioTapeProgram) {
		JComponent view = new CodeInspectorView(audioTapeProgram);
		String title = "Code inspection of " + audioTapeProgram.getProgramName();
		Viewer viewer = new Viewer(view, title);
		viewer.build();
		return viewer;
	}

	public static Viewer createSourceCodeViewer(SourceCode sourceCode, String programName) {
		JComponent view = new SourceCodeView(sourceCode);
		String title = "Source code of " + programName;
		Viewer viewer = new Viewer(view, title);
		viewer.build();
		return viewer;
	}

	public static Viewer createByteCodeViewer(ByteSequence byteCode, String programName) {
		JComponent view = new ByteCodeView(byteCode);
		String title = "Byte code of " + programName;
		Viewer viewer = new Viewer(view, title);
		viewer.build();
		return viewer;
	}

	private static AudioFileProfileExtendedView createExtendedProfileView(AudioFile audioFile, TapeProfile tapeProfile,
			int pixelsPerSecond) throws IOException {
		int maxWidth = (int) (getScreenSize().getWidth() * 0.94);
		return new AudioFileProfileExtendedView(audioFile, tapeProfile, pixelsPerSecond, maxWidth);
	}

	private static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

}