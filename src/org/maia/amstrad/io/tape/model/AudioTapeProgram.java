package org.maia.amstrad.io.tape.model;

import org.maia.amstrad.io.tape.decorator.AudioTapeBitDecorator;
import org.maia.amstrad.io.tape.decorator.BytecodeAudioDecorator;
import org.maia.amstrad.io.tape.decorator.SourcecodeBytecodeDecorator;
import org.maia.amstrad.io.tape.model.profile.TapeProfile;
import org.maia.amstrad.io.tape.model.sc.SourceCode;
import org.maia.amstrad.io.tape.read.AudioFile;

public class AudioTapeProgram extends TapeProgram {

	private SourceCode sourceCode;

	private SourcecodeBytecodeDecorator sourceCodeDecorator;

	private BytecodeAudioDecorator byteCodeDecorator;

	private AudioFile audioFile;

	private AudioTapeBitDecorator audioDecorator;

	private TapeProfile profileOnTape;

	private AudioTapeProgram() {
	}

	public static AudioTapeProgram createFrom(TapeProgram program, SourceCode sourceCode,
			SourcecodeBytecodeDecorator sourceCodeDecorator, BytecodeAudioDecorator byteCodeDecorator,
			AudioFile audioFile, AudioTapeBitDecorator audioDecorator, TapeProfile profileOnTape) {
		AudioTapeProgram audioTapeProgram = new AudioTapeProgram();
		for (Block block : program.getBlocks()) {
			audioTapeProgram.addBlock(block);
		}
		audioTapeProgram.setSourceCode(sourceCode);
		audioTapeProgram.setSourceCodeDecorator(sourceCodeDecorator);
		audioTapeProgram.setByteCodeDecorator(byteCodeDecorator);
		audioTapeProgram.setAudioFile(audioFile);
		audioTapeProgram.setAudioDecorator(audioDecorator);
		audioTapeProgram.setProfileOnTape(profileOnTape);
		return audioTapeProgram;
	}

	public SourceCode getSourceCode() {
		return sourceCode;
	}

	private void setSourceCode(SourceCode sourceCode) {
		this.sourceCode = sourceCode;
	}

	public SourcecodeBytecodeDecorator getSourceCodeDecorator() {
		return sourceCodeDecorator;
	}

	private void setSourceCodeDecorator(SourcecodeBytecodeDecorator sourceCodeDecorator) {
		this.sourceCodeDecorator = sourceCodeDecorator;
	}

	public BytecodeAudioDecorator getByteCodeDecorator() {
		return byteCodeDecorator;
	}

	private void setByteCodeDecorator(BytecodeAudioDecorator byteCodeDecorator) {
		this.byteCodeDecorator = byteCodeDecorator;
	}

	public AudioFile getAudioFile() {
		return audioFile;
	}

	private void setAudioFile(AudioFile audioFile) {
		this.audioFile = audioFile;
	}

	public AudioTapeBitDecorator getAudioDecorator() {
		return audioDecorator;
	}

	private void setAudioDecorator(AudioTapeBitDecorator audioDecorator) {
		this.audioDecorator = audioDecorator;
	}

	public TapeProfile getProfileOnTape() {
		return profileOnTape;
	}

	private void setProfileOnTape(TapeProfile profileOnTape) {
		this.profileOnTape = profileOnTape;
	}

}