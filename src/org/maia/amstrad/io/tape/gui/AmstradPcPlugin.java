package org.maia.amstrad.io.tape.gui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.maia.amstrad.AmstradFactory;
import org.maia.amstrad.basic.BasicSourceCode;
import org.maia.amstrad.io.tape.model.AudioTapeProgram;
import org.maia.amstrad.pc.AmstradPc;
import org.maia.amstrad.pc.AmstradPcFrame;
import org.maia.amstrad.pc.action.AmstradPcAction;
import org.maia.amstrad.pc.menu.AmstradMenuBar;
import org.maia.amstrad.pc.menu.maker.AmstradMenuBarMaker;
import org.maia.amstrad.pc.menu.maker.AmstradMenuDefaultLookAndFeel;
import org.maia.amstrad.pc.monitor.AmstradMonitorMode;
import org.maia.amstrad.program.AmstradProgram;
import org.maia.amstrad.program.AmstradProgramException;
import org.maia.amstrad.program.load.AmstradProgramLoader;
import org.maia.amstrad.program.load.AmstradProgramLoaderFactory;
import org.maia.amstrad.program.load.basic.staged.EndingBasicCodeDisclosure;

@SuppressWarnings("serial")
public class AmstradPcPlugin {

	private AmstradPc amstradPc;

	private AmstradProgram programInAmstradPc;

	private List<AmstradPcListener> listeners;

	public AmstradPcPlugin() {
		this.listeners = new Vector<AmstradPcListener>();
	}

	public synchronized void load(AudioTapeProgram tapeProgram) throws AmstradProgramException {
		AmstradProgram program = tapeProgram.asAmstradProgram();
		AmstradPc amstradPc = getResetAmstradPc(program);
		new ProgramLoadMenuBarMaker(amstradPc, tapeProgram).createMenuBar().install();
		AmstradProgramLoader loader = AmstradProgramLoaderFactory.getInstance()
				.createOriginalBasicProgramLoader(amstradPc);
		loader.load(program);
	}

	public synchronized void runStaged(AudioTapeProgram tapeProgram) throws AmstradProgramException {
		AmstradProgram program = tapeProgram.asAmstradProgram();
		AmstradPc amstradPc = getResetAmstradPc(program);
		new ProgramRunMenuBarMaker(amstradPc, tapeProgram).createMenuBar().install();
		AmstradProgramLoader loader = AmstradProgramLoaderFactory.getInstance()
				.createStagedBasicProgramLoader(amstradPc, null, EndingBasicCodeDisclosure.HIDE_CODE, false);
		loader.load(program).run();
	}

	public synchronized void closeAmstradPc() {
		AmstradPc amstradPc = getAmstradPc();
		if (amstradPc != null) {
			amstradPc.terminate();
			setAmstradPc(null);
		}
	}

	public void addListener(AmstradPcListener listener) {
		getListeners().add(listener);
	}

	public void removeListener(AmstradPcListener listener) {
		getListeners().remove(listener);
	}

	private void fireModifiedSourceCodeSaved(AudioTapeProgram tapeProgram) {
		for (AmstradPcListener listener : getListeners()) {
			listener.notifyModifiedSourceCodeSaved(tapeProgram);
		}
	}

	private AmstradPc getResetAmstradPc(AmstradProgram program) {
		AmstradMonitorMode mode = program.getPreferredMonitorMode();
		AmstradPc amstradPc = getAmstradPc();
		if (amstradPc == null) {
			amstradPc = AmstradFactory.getInstance().createAmstradPc();
			setAmstradPc(amstradPc);
			AmstradPcFrame frame = amstradPc.displayInFrame(false);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					closeAmstradPc();
				}
			});
			if (mode != null)
				amstradPc.getMonitor().setMode(mode);
			amstradPc.start(true, false);
		} else {
			boolean sameProgram = getProgramInAmstradPc() != null
					&& getProgramInAmstradPc().getProgramName().equals(program.getProgramName());
			if (mode != null && !sameProgram)
				amstradPc.getMonitor().setMode(mode);
			amstradPc.reboot(true, false);
		}
		amstradPc.getFrame().setTitle(program.getProgramName());
		amstradPc.getActions().getProgramInfoAction().updateProgram(program);
		setProgramInAmstradPc(program);
		return amstradPc;
	}

	private AmstradPc getAmstradPc() {
		return amstradPc;
	}

	private void setAmstradPc(AmstradPc amstradPc) {
		this.amstradPc = amstradPc;
	}

	private AmstradProgram getProgramInAmstradPc() {
		return programInAmstradPc;
	}

	private void setProgramInAmstradPc(AmstradProgram program) {
		this.programInAmstradPc = program;
	}

	private List<AmstradPcListener> getListeners() {
		return listeners;
	}

	private abstract class AmstradAbstractMenuBarMaker extends AmstradMenuBarMaker {

		private AudioTapeProgram tapeProgram;

		protected AmstradAbstractMenuBarMaker(AmstradPc amstradPc, AudioTapeProgram tapeProgram) {
			super(amstradPc, new AmstradMenuDefaultLookAndFeel());
			this.tapeProgram = tapeProgram;
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

		protected abstract JMenu createRestrictedFileMenu();

		protected AudioTapeProgram getTapeProgram() {
			return tapeProgram;
		}

	}

	private class ProgramLoadMenuBarMaker extends AmstradAbstractMenuBarMaker {

		public ProgramLoadMenuBarMaker(AmstradPc amstradPc, AudioTapeProgram tapeProgram) {
			super(amstradPc, tapeProgram);
		}

		@Override
		protected JMenu createRestrictedFileMenu() {
			JMenu menu = new JMenu("File");
			menu.add(createProgramInfoMenuItem());
			menu.add(updateMenuItemLookAndFeel(new JMenuItem(new ProgramSaveAction(getTapeProgram()))));
			menu.add(new JSeparator());
			menu.add(updateMenuItemLookAndFeel(new JMenuItem(new AmstradCloseAction())));
			return updateMenuLookAndFeel(menu);
		}

	}

	private class ProgramRunMenuBarMaker extends AmstradAbstractMenuBarMaker {

		public ProgramRunMenuBarMaker(AmstradPc amstradPc, AudioTapeProgram tapeProgram) {
			super(amstradPc, tapeProgram);
		}

		@Override
		protected JMenu createRestrictedFileMenu() {
			JMenu menu = new JMenu("File");
			menu.add(createProgramInfoMenuItem());
			menu.add(updateMenuItemLookAndFeel(new JMenuItem(new ProgramRerunAction(getTapeProgram()))));
			menu.add(new JSeparator());
			menu.add(updateMenuItemLookAndFeel(new JMenuItem(new AmstradCloseAction())));
			return updateMenuLookAndFeel(menu);
		}

	}

	private abstract class AmstradAbstractAction extends AmstradPcAction {

		protected AmstradAbstractAction(String name) {
			super(AmstradPcPlugin.this.getAmstradPc(), name);
		}

	}

	private class AmstradCloseAction extends AmstradAbstractAction {

		public AmstradCloseAction() {
			super("Close");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			closeAmstradPc();
		}

	}

	private abstract class ProgramAbstractAction extends AmstradAbstractAction {

		private AudioTapeProgram tapeProgram;

		protected ProgramAbstractAction(AudioTapeProgram tapeProgram, String name) {
			super(name);
			this.tapeProgram = tapeProgram;
		}

		protected AudioTapeProgram getTapeProgram() {
			return tapeProgram;
		}

	}

	private class ProgramSaveAction extends ProgramAbstractAction {

		public ProgramSaveAction(AudioTapeProgram tapeProgram) {
			super(tapeProgram, "Save modified code");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			runInSeparateThread(new Runnable() {

				@Override
				public void run() {
					try {
						BasicSourceCode modifiedSourceCode = getAmstradPc().getBasicRuntime().exportSourceCode();
						getTapeProgram().saveModifiedSourceCode(modifiedSourceCode);
						fireModifiedSourceCodeSaved(getTapeProgram());
					} catch (Exception e) {
						System.err.println(e);
					}
				}
			});
		}

	}

	private class ProgramRerunAction extends ProgramAbstractAction {

		public ProgramRerunAction(AudioTapeProgram tapeProgram) {
			super(tapeProgram, "Run again");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			runInSeparateThread(new Runnable() {

				@Override
				public void run() {
					try {
						runStaged(getTapeProgram());
					} catch (AmstradProgramException e) {
						System.err.println(e);
					}
				}
			});
		}

	}

	public static interface AmstradPcListener {

		void notifyModifiedSourceCodeSaved(AudioTapeProgram tapeProgram);

	}

}