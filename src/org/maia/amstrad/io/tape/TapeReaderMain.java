package org.maia.amstrad.io.tape;

import java.io.File;

import org.maia.amstrad.io.tape.read.AudioFile;
import org.maia.amstrad.io.tape.read.AudioWaveFile;
import org.maia.amstrad.io.tape.ui.UIFactory;
import org.maia.amstrad.pc.AmstradFactory;

public class TapeReaderMain {

	/**
	 * Starts the Amstrad Tape Reader application
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Usage: TapeReaderMain <inputFile> <outputDir>");
		} else {
			AudioFile audioFile = new AudioWaveFile(new File(args[0]));
			File outputDirectory = new File(args[1]);
			AmstradFactory.getInstance().getAmstradContext().setAmstradProgramRepositoryRootFolder(outputDirectory);
			TapeReaderTask task = new TapeReaderTask(audioFile, outputDirectory);
			task.readTape();
			UIFactory.createAudioTapeIndexExtendedViewer(task.getTapeIndex(), task.getTapeProfile(), 20, true).show();
		}
	}

}