package org.maia.amstrad.io.tape.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.maia.amstrad.io.tape.model.AudioTapeIndex;
import org.maia.amstrad.io.tape.model.AudioTapeProgram;
import org.maia.amstrad.io.tape.model.profile.TapeProfile;

@SuppressWarnings("serial")
public class AudioTapeIndexView extends JPanel implements ListSelectionListener {

	private AudioTapeIndex tapeIndex;

	private JTable table;

	private CodeInspectorAction codeInspectorAction;

	private ClearSelectionAction clearSelectionAction;

	private List<IndexSelectionListener> selectionListeners;

	public AudioTapeIndexView(AudioTapeIndex tapeIndex) {
		super(new BorderLayout());
		this.tapeIndex = tapeIndex;
		this.selectionListeners = new Vector<IndexSelectionListener>();
		buildView();
	}

	public void addSelectionListener(IndexSelectionListener listener) {
		getSelectionListeners().add(listener);
	}

	public void removeSelectionListener(IndexSelectionListener listener) {
		getSelectionListeners().remove(listener);
	}

	private void buildView() {
		add(buildActionsPane(), BorderLayout.NORTH);
		add(buildIndexPane(), BorderLayout.CENTER);
	}

	private JComponent buildActionsPane() {
		buildActions();
		Box box = Box.createHorizontalBox();
		box.add(new ProgramButton(getCodeInspectorAction()));
		box.add(Box.createHorizontalStrut(4));
		box.add(new ProgramButton(getClearSelectionAction()));
		return box;
	}

	private JComponent buildIndexPane() {
		JScrollPane scrollPane = new JScrollPane(buildTable());
		return scrollPane;
	}

	private JTable buildTable() {
		JTable table = new JTable(new IndexTableModel());
		table.getColumnModel().getColumn(0).setMaxWidth(40);
		table.getColumnModel().getColumn(1).setMinWidth(120);
		table.getColumnModel().getColumn(2).setMaxWidth(60);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		this.table = table;
		return table;
	}

	private void buildActions() {
		this.codeInspectorAction = new CodeInspectorAction();
		this.clearSelectionAction = new ClearSelectionAction();
	}

	public void clearSelection() {
		getTable().clearSelection();
	}

	public void changeSelection(AudioTapeProgram program) {
		int i = getRowForProgram(program);
		if (i >= 0) {
			getTable().setRowSelectionInterval(i, i);
			getTable().scrollRectToVisible(getTable().getCellRect(i, 0, true));
		} else {
			clearSelection();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {
		if (!event.getValueIsAdjusting()) {
			AudioTapeProgram program = getSelectedProgram();
			getCodeInspectorAction().setEnabled(program != null);
			getClearSelectionAction().setEnabled(program != null);
			for (IndexSelectionListener listener : getSelectionListeners()) {
				listener.indexSelectionUpdate(this);
			}
		}
	}

	public AudioTapeProgram getSelectedProgram() {
		AudioTapeProgram program = null;
		int row = getTable().getSelectedRow();
		if (row >= 0) {
			program = getProgramForRow(row);
		}
		return program;
	}

	private AudioTapeProgram getProgramForRow(int row) {
		return getTapeIndex().getPrograms().get(row);
	}

	private int getRowForProgram(AudioTapeProgram program) {
		return getTapeIndex().getPrograms().indexOf(program);
	}

	public AudioTapeIndex getTapeIndex() {
		return tapeIndex;
	}

	private JTable getTable() {
		return table;
	}

	private CodeInspectorAction getCodeInspectorAction() {
		return codeInspectorAction;
	}

	private ClearSelectionAction getClearSelectionAction() {
		return clearSelectionAction;
	}

	private List<IndexSelectionListener> getSelectionListeners() {
		return selectionListeners;
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

		protected void setToolTipText(String text) {
			putValue(Action.SHORT_DESCRIPTION, text);
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
				UIFactory.createCodeInspectorViewer(program).show();
			}
		}

	}

	private class ClearSelectionAction extends ProgramAction {

		public ClearSelectionAction() {
			super(UIResources.clearSelectionLabel, UIResources.clearSelectionIcon);
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			clearSelection();
		}

	}

	private class IndexTableModel extends AbstractTableModel {

		public IndexTableModel() {
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public int getRowCount() {
			return getTapeIndex().size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			AudioTapeProgram program = getProgramForRow(row);
			if (col == 0) {
				return row + 1;
			} else if (col == 1) {
				return program.getProgramName();
			} else if (col == 2) {
				return program.getNumberOfBlocks();
			} else if (col == 3) {
				return program.getSourceCode().getLines().size();
			} else if (col == 4) {
				TapeProfile programProfile = program.getProfileOnTape();
				return programProfile != null ? programProfile.getAudioRange().getSampleOffset() : 0L;
			} else if (col == 5) {
				TapeProfile programProfile = program.getProfileOnTape();
				return programProfile != null ? programProfile.getAudioRange().getSampleEnd() : 0L;
			} else {
				return null;
			}
		}

		@Override
		public String getColumnName(int col) {
			if (col == 0) {
				return "#";
			} else if (col == 1) {
				return "Name";
			} else if (col == 2) {
				return "Blocks";
			} else if (col == 3) {
				return "Code lines";
			} else if (col == 4) {
				return "Audio start";
			} else if (col == 5) {
				return "Audio end";
			} else {
				return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return Integer.class;
			} else if (col == 1) {
				return String.class;
			} else if (col == 2) {
				return Integer.class;
			} else if (col == 3) {
				return Integer.class;
			} else if (col == 4) {
				return Long.class;
			} else if (col == 5) {
				return Long.class;
			} else {
				return null;
			}
		}

	}

	public static interface IndexSelectionListener {

		void indexSelectionUpdate(AudioTapeIndexView source);

	}

}