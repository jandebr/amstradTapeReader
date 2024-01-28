package org.maia.amstrad.io.tape;

import org.maia.amstrad.io.tape.gui.TapeReaderApplicationViewer;
import org.maia.amstrad.io.tape.gui.UIFactory;

public class TapeReaderMain {

	/**
	 * Starts the Amstrad Tape Reader application
	 */
	public static void main(String[] args) throws Exception {
		TapeReaderApplicationViewer viewer = UIFactory.createApplicationViewer(args);
		viewer.show();
		viewer.openTaskConfigurationDialog();
	}

}