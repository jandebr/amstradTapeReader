package org.maia.amstrad.io.tape.gui.editor;

import javax.swing.Icon;

import org.maia.amstrad.io.tape.gui.UIResources;
import org.maia.amstrad.io.tape.model.AudioTapeProgram;
import org.maia.swing.text.pte.model.PlainTextFileDocument;

public class ProgramMetadataDocument extends PlainTextFileDocument implements ProgramDocument {

	private AudioTapeProgram program;

	public ProgramMetadataDocument(AudioTapeProgram program) {
		super(program.getFileStoringProgramMetadata());
		this.program = program;
	}

	@Override
	public int hashCode() {
		return getProgram().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProgramMetadataDocument other = (ProgramMetadataDocument) obj;
		return getProgram().equals(other.getProgram());
	}

	@Override
	public String getLongDocumentName() {
		return "Metadata of " + getProgram().getProgramName();
	}

	@Override
	public String getShortDocumentName() {
		return getProgram().getProgramName();
	}

	@Override
	public Icon getLargeDocumentIcon() {
		return UIResources.metadataDocumentLargeIcon;
	}

	@Override
	public Icon getSmallDocumentIcon() {
		return UIResources.metadataDocumentSmallIcon;
	}

	@Override
	public AudioTapeProgram getProgram() {
		return program;
	}

}