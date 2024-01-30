package org.maia.amstrad.io.tape.gui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JSeparator;

import org.maia.amstrad.AmstradFactory;
import org.maia.amstrad.basic.BasicException;
import org.maia.amstrad.io.tape.gui.AudioTapeIndexView.IndexSelectionListener;
import org.maia.amstrad.io.tape.model.AudioTapeProgram;
import org.maia.amstrad.pc.AmstradPc;
import org.maia.amstrad.pc.AmstradPcFrame;
import org.maia.amstrad.pc.menu.AmstradMenuBar;
import org.maia.amstrad.pc.menu.maker.AmstradMenuBarMaker;
import org.maia.amstrad.pc.menu.maker.AmstradMenuDefaultLookAndFeel;
import org.maia.amstrad.program.AmstradProgram;
import org.maia.amstrad.program.AmstradProgramException;
import org.maia.amstrad.program.load.AmstradProgramLoader;
import org.maia.amstrad.program.load.AmstradProgramLoaderFactory;

@SuppressWarnings("serial")
public class AudioTapeIndexToolBar extends Box implements IndexSelectionListener {

	private AudioTapeIndexView indexView;

	private CodeInspectorAction codeInspectorAction;

	private CodeLoadAction codeLoadAction;

	private CodeRunAction codeRunAction;

	private ClearSelectionAction clearSelectionAction;

	private AmstradPc amstradPc;

	private AmstradPcFrame amstradPcFrame;

	public AudioTapeIndexToolBar(AudioTapeIndexView indexView) {
		super(BoxLayout.X_AXIS);
		this.indexView = indexView;
		this.codeInspectorAction = new CodeInspectorAction();
		this.codeLoadAction = new CodeLoadAction();
		this.codeRunAction = new CodeRunAction();
		this.clearSelectionAction = new ClearSelectionAction();
		indexView.addSelectionListener(this);
		buildUI();
	}

	private void buildUI() {
		add(new ProgramButton(getCodeInspectorAction()));
		add(Box.createHorizontalStrut(4));
		add(new ProgramButton(getCodeLoadAction()));
		add(Box.createHorizontalStrut(4));
		add(new ProgramButton(getCodeRunAction()));
		add(Box.createHorizontalStrut(4));
		add(new ProgramButton(getClearSelectionAction()));
	}

	@Override
	public void indexSelectionUpdate(AudioTapeIndexView source) {
		boolean programSelection = getSelectedProgram() != null;
		getCodeInspectorAction().setEnabled(programSelection);
		getCodeLoadAction().setEnabled(programSelection);
		getCodeRunAction().setEnabled(programSelection);
		getClearSelectionAction().setEnabled(programSelection);
	}

	private synchronized AmstradPc getResetAmstradPc() {
		AmstradPc amstradPc = getAmstradPc();
		if (amstradPc == null) {
			amstradPc = AmstradFactory.getInstance().createAmstradPc();
			AmstradPcFrame frame = amstradPc.displayInFrame(false);
			frame.addWindowListener(new AmstradPcTerminator());
			setAmstradPc(amstradPc);
			setAmstradPcFrame(frame);
			new AmstradMenuBarMakerImpl().createMenuBar().install();
			amstradPc.start(true, false);
		} else {
			amstradPc.reboot(true, false);
		}
		return amstradPc;
	}

	public AudioTapeProgram getSelectedProgram() {
		return getIndexView().getSelectedProgram();
	}

	public AudioTapeIndexView getIndexView() {
		return indexView;
	}

	private CodeInspectorAction getCodeInspectorAction() {
		return codeInspectorAction;
	}

	private CodeLoadAction getCodeLoadAction() {
		return codeLoadAction;
	}

	private CodeRunAction getCodeRunAction() {
		return codeRunAction;
	}

	private ClearSelectionAction getClearSelectionAction() {
		return clearSelectionAction;
	}

	private synchronized AmstradPc getAmstradPc() {
		return amstradPc;
	}

	private synchronized void setAmstradPc(AmstradPc amstradPc) {
		this.amstradPc = amstradPc;
	}

	private AmstradPcFrame getAmstradPcFrame() {
		return amstradPcFrame;
	}

	private void setAmstradPcFrame(AmstradPcFrame frame) {
		this.amstradPcFrame = frame;
	}

	private class ProgramButton extends JButton {

		public ProgramButton(ProgramAction action) {
			super(action);
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));
			setFocusPainted(false);
		}

	}

	private abstract class ProgramAction extends AbstractAction {

		protected ProgramAction(Icon icon) {
			this(null, icon);
		}

		protected ProgramAction(String name, Icon icon) {
			super(name, icon);
			setEnabled(false);
		}

		protected void setName(String name) {
			putValue(Action.NAME, name);
		}

		protected void setToolTipText(String text) {
			putValue(Action.SHORT_DESCRIPTION, text);
		}

	}

	private class ClearSelectionAction extends ProgramAction {

		public ClearSelectionAction() {
			super(UIResources.clearSelectionLabel, UIResources.clearSelectionIcon);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			getIndexView().clearSelection();
		}

	}

	private class CodeInspectorAction extends ProgramAction {

		public CodeInspectorAction() {
			super(UIResources.openCodeInspectorLabel, UIResources.openCodeInspectorIcon);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			AudioTapeProgram program = getSelectedProgram();
			if (program != null) {
				UIFactory.createCodeInspectorViewer(program, false).show();
			}
		}

	}

	private abstract class AmstradPcAction extends ProgramAction {

		protected AmstradPcAction(String name, Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			final AudioTapeProgram program = getSelectedProgram();
			if (program != null) {
				setEnabled(false);
				new Thread(new Runnable() {
					@Override
					public void run() {
						AmstradPc amstradPc = getResetAmstradPc();
						getAmstradPcFrame().setTitle(program.getProgramName());
						performAction(amstradPc, program);
						setEnabled(true);
					}
				}).start();
			}
		}

		protected abstract void performAction(AmstradPc amstradPc, AudioTapeProgram program);

	}

	private class CodeLoadAction extends AmstradPcAction {

		public CodeLoadAction() {
			super(UIResources.loadCodeLabel, UIResources.loadCodeIcon);
		}

		@Override
		protected void performAction(AmstradPc amstradPc, AudioTapeProgram program) {
			try {
				amstradPc.getBasicRuntime().load(program.getSourceCode());
			} catch (BasicException e) {
				System.err.println(e);
			}
		}

	}

	private class CodeRunAction extends AmstradPcAction {

		public CodeRunAction() {
			super(UIResources.runCodeLabel, UIResources.runCodeIcon);
		}

		@Override
		protected void performAction(AmstradPc amstradPc, AudioTapeProgram program) {
			try {
				File codeFile = null;
				File metadataFile = null;
				AmstradProgram amstradProgram = AmstradFactory.getInstance()
						.createBasicDescribedProgram(program.getProgramName(), codeFile, metadataFile);
				AmstradProgramLoader loader = AmstradProgramLoaderFactory.getInstance()
						.createStagedBasicProgramLoader(amstradPc);
				loader.load(amstradProgram).run();
			} catch (AmstradProgramException e) {
				System.err.println(e);
			}
		}

	}

	private class AmstradPcTerminator extends WindowAdapter {

		public AmstradPcTerminator() {
		}

		@Override
		public void windowClosed(WindowEvent e) {
			AmstradPc amstradPc = getAmstradPc();
			if (amstradPc != null) {
				amstradPc.terminate();
				setAmstradPc(null);
			}
		}

	}

	private class AmstradMenuBarMakerImpl extends AmstradMenuBarMaker {

		public AmstradMenuBarMakerImpl() {
			super(AudioTapeIndexToolBar.this.getAmstradPc(), new AmstradMenuDefaultLookAndFeel());
		}

		@Override
		protected AmstradMenuBar doCreateMenu() {
			AmstradMenuBar menuBar = new AmstradMenuBar(getAmstradPc());
			menuBar.add(createRestrictedFileMenu());
			menuBar.add(createEmulatorMenu());
			menuBar.add(createMonitorMenu());
			menuBar.add(createWindowMenu());
			return updateMenuBarLookAndFeel(menuBar);
		}

		protected JMenu createRestrictedFileMenu() {
			// no browser setup, no file loading, no poweroff
			JMenu menu = new JMenu("File");
			menu.add(createProgramBrowserMenuItem());
			menu.add(new JSeparator());
			menu.add(createSaveBasicSourceFileMenuItem());
			menu.add(createSaveBasicBinaryFileMenuItem());
			menu.add(createSaveSnapshotFileMenuItem());
			return updateMenuLookAndFeel(menu);
		}

	}

}