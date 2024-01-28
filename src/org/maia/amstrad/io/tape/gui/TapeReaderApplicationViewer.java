package org.maia.amstrad.io.tape.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.maia.amstrad.io.tape.model.config.TapeReaderTaskConfiguration;

public class TapeReaderApplicationViewer extends Viewer {

	public TapeReaderApplicationViewer(TapeReaderApplicationView view) {
		super(view, "Amstrad Tape Reader", true);
		build();
		setJMenuBar(createMenuBar());
		maximize();
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.add(new JMenuItem(new ReadAudioFileAction()));
		menu.add(new JMenuItem(new QuitAction()));
		menuBar.add(menu);
		return menuBar;
	}

	public void openTaskConfigurationDialog() {
		getApplicationView().openTaskConfigurationDialog("Read audio file");
	}

	public TapeReaderTaskConfiguration getTaskConfiguration() {
		return getApplicationView().getTaskConfiguration();
	}

	public TapeReaderApplicationView getApplicationView() {
		return (TapeReaderApplicationView) getView();
	}

	@SuppressWarnings("serial")
	private class ReadAudioFileAction extends AbstractAction {

		public ReadAudioFileAction() {
			super("Read audio file...");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			openTaskConfigurationDialog();
		}

	}

	@SuppressWarnings("serial")
	private class QuitAction extends AbstractAction {

		public QuitAction() {
			super("Quit");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			close();
		}

	}

}