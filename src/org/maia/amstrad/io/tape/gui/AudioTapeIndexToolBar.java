package org.maia.amstrad.io.tape.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.maia.amstrad.io.tape.gui.AmstradPcPlugin.AmstradPcListener;
import org.maia.amstrad.io.tape.gui.AudioTapeIndexView.IndexSelectionListener;
import org.maia.amstrad.io.tape.model.AudioTapeProgram;
import org.maia.amstrad.program.AmstradProgramException;

@SuppressWarnings("serial")
public class AudioTapeIndexToolBar extends Box implements IndexSelectionListener, AmstradPcListener {

	private AudioTapeIndexView indexView;

	private CodeInspectorAction codeInspectorAction;

	private CodeRevertAction codeRevertAction;

	private CodeEditAction codeEditAction;

	private MetadataEditAction metadataEditAction;

	private ProgramLoadAction programLoadAction;

	private ProgramRunAction programRunAction;

	private ClearSelectionAction clearSelectionAction;

	private AmstradPcPlugin amstradPcPlugin;

	private List<ToolBarListener> listeners;

	public AudioTapeIndexToolBar(AudioTapeIndexView indexView) {
		super(BoxLayout.X_AXIS);
		this.indexView = indexView;
		this.codeInspectorAction = new CodeInspectorAction();
		this.codeRevertAction = new CodeRevertAction();
		this.codeEditAction = new CodeEditAction();
		this.metadataEditAction = new MetadataEditAction();
		this.programLoadAction = new ProgramLoadAction();
		this.programRunAction = new ProgramRunAction();
		this.clearSelectionAction = new ClearSelectionAction();
		this.amstradPcPlugin = new AmstradPcPlugin();
		this.listeners = new Vector<ToolBarListener>();
		indexView.addSelectionListener(this);
		getAmstradPcPlugin().addListener(this);
		buildUI();
	}

	private void buildUI() {
		add(new ProgramButton(getCodeInspectorAction()));
		add(Box.createHorizontalStrut(4));
		add(new ProgramButton(getProgramLoadAction()));
		add(Box.createHorizontalStrut(4));
		add(new ProgramButton(getProgramRunAction()));
		add(Box.createHorizontalStrut(4));
		add(new ProgramButton(getMetadataEditAction()));
		add(Box.createHorizontalStrut(4));
		add(new ProgramButton(getCodeEditAction()));
		add(Box.createHorizontalStrut(4));
		add(new ProgramButton(getCodeRevertAction()));
		add(Box.createHorizontalStrut(4));
		add(new ProgramButton(getClearSelectionAction()));
	}

	public void addListener(ToolBarListener listener) {
		getListeners().add(listener);
	}

	public void removeListener(ToolBarListener listener) {
		getListeners().remove(listener);
	}

	@Override
	public void indexSelectionUpdate(AudioTapeIndexView source) {
		boolean programSelection = getSelectedProgram() != null;
		getCodeInspectorAction().setEnabled(programSelection);
		// TODO getCodeEditAction().setEnabled(programSelection);
		// TODO getMetadataEditAction().setEnabled(programSelection);
		getProgramLoadAction().setEnabled(programSelection);
		getProgramRunAction().setEnabled(programSelection);
		getClearSelectionAction().setEnabled(programSelection);
		updateCodeRevertEnablement();
	}

	private void updateCodeRevertEnablement() {
		AudioTapeProgram tapeProgram = getSelectedProgram();
		getCodeRevertAction().setEnabled(tapeProgram != null && tapeProgram.hasModifiedSourceCode());
	}

	@Override
	public void notifyModifiedSourceCodeSaved(AudioTapeProgram tapeProgram) {
		fireModifiedSourceCodeSaved(tapeProgram); // propagate
		updateCodeRevertEnablement();
	}

	private void fireModifiedSourceCodeSaved(AudioTapeProgram tapeProgram) {
		for (ToolBarListener listener : getListeners()) {
			listener.notifyModifiedSourceCodeSaved(tapeProgram);
		}
	}

	private void fireModifiedSourceCodeReverted(AudioTapeProgram tapeProgram) {
		for (ToolBarListener listener : getListeners()) {
			listener.notifyModifiedSourceCodeReverted(tapeProgram);
		}
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

	private CodeRevertAction getCodeRevertAction() {
		return codeRevertAction;
	}

	private CodeEditAction getCodeEditAction() {
		return codeEditAction;
	}

	private MetadataEditAction getMetadataEditAction() {
		return metadataEditAction;
	}

	private ProgramLoadAction getProgramLoadAction() {
		return programLoadAction;
	}

	private ProgramRunAction getProgramRunAction() {
		return programRunAction;
	}

	private ClearSelectionAction getClearSelectionAction() {
		return clearSelectionAction;
	}

	private AmstradPcPlugin getAmstradPcPlugin() {
		return amstradPcPlugin;
	}

	private List<ToolBarListener> getListeners() {
		return listeners;
	}

	private class ProgramButton extends JButton {

		public ProgramButton(ProgramAction action) {
			super(action);
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
					BorderFactory.createEmptyBorder(2, 6, 2, 6)));
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
			setToolTipText(UIResources.clearSelectionTooltip);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			getIndexView().clearSelection();
		}

	}

	private class CodeInspectorAction extends ProgramAction {

		public CodeInspectorAction() {
			super(UIResources.openCodeInspectorLabel, UIResources.openCodeInspectorIcon);
			setToolTipText(UIResources.openCodeInspectorTooltip);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			AudioTapeProgram program = getSelectedProgram();
			if (program != null) {
				new Viewer(createView(program), "Code inspection of " + program.getProgramName(), false).buildAndShow();
			}
		}

		private JComponent createView(AudioTapeProgram program) {
			JPanel panel = new JPanel(new BorderLayout());
			if (program.hasModifiedSourceCode()) {
				panel.add(createModifiedSourceCodeWarning(), BorderLayout.NORTH);
			}
			panel.add(new CodeInspectorView(program), BorderLayout.CENTER);
			return panel;
		}

		private JComponent createModifiedSourceCodeWarning() {
			JLabel label = new JLabel("Changes were made to the original source code on tape, shown here",
					UIResources.pencilIcon, SwingConstants.LEADING);
			label.setOpaque(true);
			label.setBackground(Color.BLACK);
			label.setForeground(Color.YELLOW);
			label.setFont(label.getFont().deriveFont(Font.ITALIC));
			label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			return label;
		}

	}

	private class CodeRevertAction extends ProgramAction {

		public CodeRevertAction() {
			super(UIResources.revertCodeLabel, UIResources.revertCodeIcon);
			setToolTipText(UIResources.revertCodeTooltip);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			AudioTapeProgram tapeProgram = getSelectedProgram();
			if (tapeProgram != null) {
				tapeProgram.revertSourceCodeModifications();
				fireModifiedSourceCodeReverted(tapeProgram);
				updateCodeRevertEnablement();
			}
		}

	}

	private class CodeEditAction extends ProgramAction {

		public CodeEditAction() {
			super(UIResources.editCodeLabel, UIResources.editCodeIcon);
			setToolTipText(UIResources.editCodeTooltip);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			// TODO
		}

	}

	private class MetadataEditAction extends ProgramAction {

		public MetadataEditAction() {
			super(UIResources.editMetadataLabel, UIResources.editMetadataIcon);
			setToolTipText(UIResources.editMetadataTooltip);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			// TODO
		}

	}

	private abstract class ProgramLaunchAction extends ProgramAction {

		protected ProgramLaunchAction(String name, Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			final AudioTapeProgram tapeProgram = getSelectedProgram();
			if (tapeProgram != null) {
				setEnabled(false);
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							launchProgram(tapeProgram);
						} catch (AmstradProgramException e) {
							System.err.println(e);
						} finally {
							setEnabled(true);
						}
					}
				}).start();
			}
		}

		protected abstract void launchProgram(AudioTapeProgram tapeProgram) throws AmstradProgramException;

	}

	private class ProgramLoadAction extends ProgramLaunchAction {

		public ProgramLoadAction() {
			super(UIResources.loadProgramLabel, UIResources.loadProgramIcon);
			setToolTipText(UIResources.loadProgramTooltip);
		}

		@Override
		protected void launchProgram(AudioTapeProgram tapeProgram) throws AmstradProgramException {
			getAmstradPcPlugin().load(tapeProgram);
		}

	}

	private class ProgramRunAction extends ProgramLaunchAction {

		public ProgramRunAction() {
			super(UIResources.runProgramLabel, UIResources.runProgramIcon);
			setToolTipText(UIResources.runProgramTooltip);
		}

		@Override
		protected void launchProgram(AudioTapeProgram tapeProgram) throws AmstradProgramException {
			getAmstradPcPlugin().runStaged(tapeProgram);
		}

	}

	public static interface ToolBarListener {

		void notifyModifiedSourceCodeSaved(AudioTapeProgram tapeProgram);

		void notifyModifiedSourceCodeReverted(AudioTapeProgram tapeProgram);

	}

}