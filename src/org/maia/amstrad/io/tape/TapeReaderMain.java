package org.maia.amstrad.io.tape;

import java.io.File;

import org.maia.amstrad.AmstradFactory;
import org.maia.amstrad.io.tape.read.AudioFile;
import org.maia.amstrad.io.tape.read.AudioWaveFile;
import org.maia.amstrad.io.tape.ui.UIFactory;

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
			if (!outputDirectory.exists())
				outputDirectory.mkdirs();
			AmstradFactory.getInstance().getAmstradContext().setProgramRepositoryRootFolder(outputDirectory);
			TapeReaderTask task = new TapeReaderTask(audioFile, outputDirectory);
			task.setMinimalOutput(true);
			task.readTape();
			UIFactory.createAudioTapeIndexExtendedViewer(task.getTapeIndex(), task.getTapeProfile(), 20, true).show();
		}
	}

}