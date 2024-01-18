package org.maia.amstrad.io.tape.gui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.maia.amstrad.io.tape.gui.AudioFileProfileView.TapeSectionListener;
import org.maia.amstrad.io.tape.gui.AudioTapeIndexView.IndexSelectionListener;
import org.maia.amstrad.io.tape.model.AudioTapeIndex;
import org.maia.amstrad.io.tape.model.AudioTapeProgram;
import org.maia.amstrad.io.tape.model.profile.TapeSection;

@SuppressWarnings("serial")
public class AudioTapeIndexExtendedView extends JPanel implements IndexSelectionListener, TapeSectionListener {

	private AudioTapeIndex tapeIndex;

	private AudioTapeIndexView indexView;

	private AudioFileProfileExtendedView profileExtendedView;

	private JComponent detailPane;

	public AudioTapeIndexExtendedView(AudioTapeIndex tapeIndex, AudioFileProfileExtendedView profileExtendedView) {
		super(new BorderLayout());
		this.tapeIndex = tapeIndex;
		this.profileExtendedView = profileExtendedView;
		profileExtendedView.showTapeIndex(tapeIndex);
		profileExtendedView.addSectionListener(this);
		buildView();
	}

	private void buildView() {
		add(buildIndexView(), BorderLayout.WEST);
		add(getProfileExtendedView(), BorderLayout.SOUTH);
		updateDetailPane(buildStubDetailPane());
	}

	private AudioTapeIndexView buildIndexView() {
		AudioTapeIndexView view = new AudioTapeIndexView(getTapeIndex());
		view.addSelectionListener(this);
		this.indexView = view;
		return view;
	}

	private JComponent buildProgramDetailPane(AudioTapeProgram program) {
		SourceCodeView scv = new SourceCodeView(program.getSourceCode());
		return scv;
	}

	private JComponent buildStubDetailPane() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(UIResources.amstradIcon), BorderLayout.CENTER);
		return panel;
	}

	private void updateDetailPane(JComponent newDetailPane) {
		if (getDetailPane() != null) {
			remove(getDetailPane());
		}
		setDetailPane(newDetailPane);
		add(newDetailPane, BorderLayout.CENTER);
		revalidate();
	}

	@Override
	public void indexSelectionUpdate(AudioTapeIndexView source) {
		AudioTapeProgram program = source.getSelectedProgram();
		if (program != null) {
			getProfileExtendedView().changeSelection(program.getProfileOnTape(), true);
			updateDetailPane(buildProgramDetailPane(program));
		} else {
			getProfileExtendedView().clearSelection();
			updateDetailPane(buildStubDetailPane());
		}
	}

	@Override
	public void sectionClicked(TapeSection section, AudioFileProfileView source) {
		AudioTapeProgram program = getTapeIndex().findProgramContaining(section);
		if (program != null) {
			getIndexView().changeSelection(program);
		} else {
			getIndexView().clearSelection();
		}
	}

	public AudioTapeIndex getTapeIndex() {
		return tapeIndex;
	}

	public AudioTapeIndexView getIndexView() {
		return indexView;
	}

	public AudioFileProfileExtendedView getProfileExtendedView() {
		return profileExtendedView;
	}

	public JComponent getDetailPane() {
		return detailPane;
	}

	private void setDetailPane(JComponent detailPane) {
		this.detailPane = detailPane;
	}

}