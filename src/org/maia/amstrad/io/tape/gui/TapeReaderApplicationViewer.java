package org.maia.amstrad.io.tape.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.maia.amstrad.io.tape.gui.editor.ProgramEditorKit;
import org.maia.amstrad.io.tape.model.config.TapeReaderTaskConfiguration;
import org.maia.swing.text.pte.PlainTextEditor;
import org.maia.swing.text.pte.model.PlainTextFileDocument;

public class TapeReaderApplicationViewer extends Viewer {

	private static PlainTextEditor textEditor;

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

	public static PlainTextEditor getTextEditor() {
		if (textEditor == null) {
			textEditor = createTextEditor();
		}
		return textEditor;
	}

	private static PlainTextEditor createTextEditor() {
		PlainTextFileDocument.setFileNameExtensionFilter(
				new FileNameExtensionFilter("Text files (*.txt, *.bas, *.amd)", "txt", "bas", "amd"));
		// UIManager.put("TabbedPane.selected", Color.WHITE);
		Dimension screenSize = UIFactory.getScreenSize();
		Dimension editorSize = new Dimension((int) Math.floor(screenSize.getWidth() * 0.8),
				(int) Math.floor(screenSize.getHeight() * 0.8));
		PlainTextEditor editor = new PlainTextEditor(new ProgramEditorKit(), editorSize, true);
		return editor;
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