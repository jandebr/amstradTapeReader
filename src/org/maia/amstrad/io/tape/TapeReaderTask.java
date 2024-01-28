package org.maia.amstrad.io.tape;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Iterator;

import org.maia.amstrad.AmstradFileType;
import org.maia.amstrad.basic.BasicException;
import org.maia.amstrad.basic.locomotive.LocomotiveBasicByteCode;
import org.maia.amstrad.io.tape.decorate.AudioTapeBitDecorator;
import org.maia.amstrad.io.tape.decorate.BlockAudioDecorator;
import org.maia.amstrad.io.tape.decorate.BlockAudioDecorator.BlockAudioDecoration;
import org.maia.amstrad.io.tape.decorate.BytecodeAudioDecorator;
import org.maia.amstrad.io.tape.decorate.DecoratingLocomotiveBasicDecompiler;
import org.maia.amstrad.io.tape.decorate.SourcecodeBytecodeDecorator;
import org.maia.amstrad.io.tape.decorate.TapeDecorator;
import org.maia.amstrad.io.tape.decorate.TapeDecorator.TapeSectionDecoration;
import org.maia.amstrad.io.tape.model.AudioRange;
import org.maia.amstrad.io.tape.model.AudioTapeIndex;
import org.maia.amstrad.io.tape.model.AudioTapeProgram;
import org.maia.amstrad.io.tape.model.Block;
import org.maia.amstrad.io.tape.model.BlockData;
import org.maia.amstrad.io.tape.model.ByteSequence;
import org.maia.amstrad.io.tape.model.TapeProgram;
import org.maia.amstrad.io.tape.model.config.TapeReaderTaskConfiguration;
import org.maia.amstrad.io.tape.model.config.TapeReaderTaskConfiguration.MetaData;
import org.maia.amstrad.io.tape.model.profile.TapeProfile;
import org.maia.amstrad.io.tape.model.sc.SourceCode;
import org.maia.amstrad.io.tape.read.AudioFile;
import org.maia.amstrad.io.tape.read.AudioTapeInputStream;
import org.maia.amstrad.io.tape.read.ScopedAudioTapeInputStream;
import org.maia.amstrad.io.tape.read.TapeReader;
import org.maia.amstrad.io.tape.read.TapeReaderListener;
import org.maia.amstrad.program.AmstradProgramMetaDataConstants;
import org.maia.util.StringUtils;

/**
 * Task that reconstructs programs from an Amstrad audio tape file
 * 
 * <p>
 * The method {@link #readTape()} should be called only once, after which all output is generated in a folder and can be
 * inspected via {@link #getTapeIndex()} and {@link #getTapeProfile()}.
 * </p>
 */
public class TapeReaderTask implements TapeReaderListener, AmstradProgramMetaDataConstants {

	private TapeReaderTaskConfiguration taskConfiguration;

	private AudioTapeIndex tapeIndex; // programs found on tape

	private TapeDecorator tapeDecorator; // locates sections on tape

	private BlockAudioDecorator blockDecorator; // locates blocks on tape

	private AudioTapeBitDecorator audioTapeBitDecorator; // locates bits in the input file

	private boolean minimalOutput; // omits certain optional outputs

	private static NumberFormat programFolderNumberFormat;

	static {
		programFolderNumberFormat = NumberFormat.getIntegerInstance();
		programFolderNumberFormat.setMinimumIntegerDigits(2);
	}

	public TapeReaderTask(TapeReaderTaskConfiguration taskConfiguration) {
		this.taskConfiguration = taskConfiguration;
		this.tapeIndex = new AudioTapeIndex(taskConfiguration.getAudioFile());
		this.tapeDecorator = new TapeDecorator();
		this.blockDecorator = new BlockAudioDecorator(tapeDecorator);
		this.audioTapeBitDecorator = new AudioTapeBitDecorator();
		this.minimalOutput = true;
	}

	public void readTape() throws Exception {
		AudioTapeInputStream atis = null;
		AudioRange audioRange = getTaskConfiguration().getSelectionInAudioFile();
		if (audioRange != null) {
			atis = new ScopedAudioTapeInputStream(getAudioFile(), audioRange);
		} else {
			atis = new AudioTapeInputStream(getAudioFile());
		}
		atis.addListener(getAudioTapeBitDecorator());
		TapeReader reader = new TapeReader();
		reader.addListener(this);
		reader.addListener(getBlockDecorator());
		reader.read(atis, getTapeDecorator());
		atis.close();
	}

	@Override
	public void startReadingTape() {
		System.out.println("Start reading tape");
		System.out.println();
	}

	@Override
	public void endReadingTape() {
		System.out.println("End reading tape");
		System.out.println();
		System.out.println(getTapeProfile());
		saveProgramIndex();
		if (!isMinimalOutput()) {
			saveTapeProfile();
		}
	}

	@Override
	public void foundNewBlock(Block block) {
		BlockData data = block.getData();
		System.out.println();
		System.out.println("Found " + block + " containing " + data.getByteSequence().getLength() + " bytes ("
				+ data.getNumberOfDataChunks() + " chunks)");
		System.out.println();
	}

	@Override
	public void startReadingProgram(TapeProgram program) {
		System.out.println();
		System.out.println("Start reading program \"" + program.getProgramName() + "\"");
	}

	@Override
	public void endReadingProgram(TapeProgram program, BytecodeAudioDecorator byteCodeDecorator) {
		System.out.println();
		System.out.println("End reading program \"" + program.getProgramName() + "\"");
		System.out.println();
		// Decompile
		ByteSequence byteCode = program.getByteCode();
		DecoratingLocomotiveBasicDecompiler decompiler = new DecoratingLocomotiveBasicDecompiler();
		try {
			decompiler.decompile(new LocomotiveBasicByteCode(byteCode.getBytesArray()));
			SourceCode sourceCode = decompiler.getSourceCode();
			SourcecodeBytecodeDecorator sourceCodeDecorator = decompiler.getSourceCodeDecorator();
			// Assemble
			TapeProfile profileOnTape = getProfileOnTape(program);
			AudioTapeProgram audioTapeProgram = AudioTapeProgram.createFrom(program, sourceCode, sourceCodeDecorator,
					byteCodeDecorator, getAudioFile(), getAudioTapeBitDecorator(), profileOnTape);
			// Add program to index
			int i = getTapeIndex().size();
			getTapeIndex().addProgram(audioTapeProgram);
			// Save program artefacts
			File programFolder = createProgramFolder(audioTapeProgram, i);
			saveMetaData(audioTapeProgram, programFolder);
			saveSourceCode(audioTapeProgram, programFolder);
			if (!isMinimalOutput()) {
				saveByteCode(audioTapeProgram, programFolder);
			}
		} catch (BasicException e) {
			System.err.println(e);
		}
	}

	private TapeProfile getProfileOnTape(TapeProgram program) {
		long audioSampleOffset = -1L;
		long audioSampleEnd = -1L;
		for (Iterator<BlockAudioDecoration> it = getBlockDecorator().getDecorationsInOrderIterator(); it.hasNext();) {
			BlockAudioDecoration decoration = it.next();
			if (program.getBlocks().contains(decoration.getBlock())) {
				if (audioSampleOffset < 0L) {
					audioSampleOffset = decoration.getOffset();
				}
				audioSampleEnd = decoration.getEnd();
			}
		}
		TapeProfile profile = new TapeProfile();
		for (TapeSectionDecoration decoration : getTapeDecorator().getDecorationsInsideRange(audioSampleOffset,
				audioSampleEnd)) {
			profile.addSection(decoration.getSection());
		}
		return profile;
	}

	private File createProgramFolder(AudioTapeProgram program, int programIndex) {
		File folder = getProgramFolder(program, programIndex);
		folder.mkdirs();
		return folder;
	}

	private File getProgramFolder(AudioTapeProgram program, int programIndex) {
		StringBuilder sb = new StringBuilder(20);
		sb.append(programFolderNumberFormat.format(programIndex + 1)).append('_');
		for (int i = 0; i < program.getProgramName().length(); i++) {
			char c = program.getProgramName().charAt(i);
			if (isSafeCharForProgramFolder(c))
				sb.append(c);
		}
		return new File(getOutputDirectory(), sb.toString());
	}

	private boolean isSafeCharForProgramFolder(char c) {
		return Character.isLetterOrDigit(c) || ".-_ ()".indexOf(c) >= 0;
	}

	private void saveMetaData(AudioTapeProgram program, File programFolder) {
		MetaData md = getTaskConfiguration().getDefaultProgramMetaData();
		try {
			PrintWriter pw = new PrintWriter(
					new File(programFolder, "INFO" + AmstradFileType.AMSTRAD_METADATA_FILE.getFileExtension()),
					"UTF-8");
			pw.println(AMD_TYPE + ": " + AMD_TYPE_LOCOMOTIVE_BASIC_PROGRAM);
			pw.println(AMD_NAME + ": " + program.getProgramName());
			pw.println(AMD_AUTHOR + ": " + StringUtils.emptyForNull(md.getAuthor()));
			pw.println(AMD_YEAR + ": " + StringUtils.emptyForNull(md.getYear()));
			pw.println(AMD_TAPE + ": " + StringUtils.emptyForNull(md.getTape()));
			pw.println(AMD_BLOCKS + ": " + program.getNumberOfBlocks());
			pw.println(AMD_MONITOR + ": " + StringUtils.emptyForNull(md.getMonitor()));
			pw.println(AMD_DESCRIPTION + ": " + StringUtils.emptyForNull(md.getDescription()));
			pw.println(AMD_AUTHORING + ": " + StringUtils.emptyForNull(md.getAuthoring()));
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveSourceCode(AudioTapeProgram program, File programFolder) {
		SourceCode sourceCode = program.getSourceCode();
		try {
			sourceCode
					.save(new File(programFolder, "code" + AmstradFileType.BASIC_SOURCE_CODE_FILE.getFileExtension()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveByteCode(AudioTapeProgram program, File programFolder) {
		ByteSequence byteCode = program.getByteCode();
		try {
			byteCode.save(
					new File(programFolder, "bytecode" + AmstradFileType.BASIC_BYTE_CODE_FILE.getFileExtension()));
			byteCode.saveAsText(new File(programFolder, "bytecode.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveTapeProfile() {
		try {
			getTapeProfile().save(new File(getOutputDirectory(), "tapeprofile.dat"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveProgramIndex() {
		try {
			PrintWriter pw = new PrintWriter(new File(getOutputDirectory(), "index.txt"), "UTF-8");
			for (int i = 0; i < getTapeIndex().size(); i++) {
				AudioTapeProgram program = getTapeIndex().getPrograms().get(i);
				pw.print(getProgramFolder(program, i).getName());
				pw.print("\t");
				pw.print(program.getNumberOfBlocks());
				pw.print(" blocks\t");
				pw.println(program.getProgramName());
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private AudioFile getAudioFile() {
		return getTaskConfiguration().getAudioFile();
	}

	private File getOutputDirectory() {
		return getTaskConfiguration().getOutputDirectory();
	}

	public TapeReaderTaskConfiguration getTaskConfiguration() {
		return taskConfiguration;
	}

	public AudioTapeIndex getTapeIndex() {
		return tapeIndex;
	}

	public TapeProfile getTapeProfile() {
		return getTapeDecorator().getTapeProfile();
	}

	private TapeDecorator getTapeDecorator() {
		return tapeDecorator;
	}

	private BlockAudioDecorator getBlockDecorator() {
		return blockDecorator;
	}

	private AudioTapeBitDecorator getAudioTapeBitDecorator() {
		return audioTapeBitDecorator;
	}

	public boolean isMinimalOutput() {
		return minimalOutput;
	}

	public void setMinimalOutput(boolean minimalOutput) {
		this.minimalOutput = minimalOutput;
	}

}