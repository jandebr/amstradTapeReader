package org.maia.amstrad.io.tape;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Iterator;

import org.maia.amstrad.basic.BasicDecompilationException;
import org.maia.amstrad.io.AmstradFileType;
import org.maia.amstrad.io.tape.decorate.AudioTapeBitDecorator;
import org.maia.amstrad.io.tape.decorate.BlockAudioDecorator;
import org.maia.amstrad.io.tape.decorate.BlockAudioDecorator.BlockAudioDecoration;
import org.maia.amstrad.io.tape.decorate.BytecodeAudioDecorator;
import org.maia.amstrad.io.tape.decorate.DecoratingLocomotiveBasicDecompiler;
import org.maia.amstrad.io.tape.decorate.SourcecodeBytecodeDecorator;
import org.maia.amstrad.io.tape.decorate.TapeDecorator;
import org.maia.amstrad.io.tape.decorate.TapeDecorator.TapeSectionDecoration;
import org.maia.amstrad.io.tape.model.AudioTapeIndex;
import org.maia.amstrad.io.tape.model.AudioTapeProgram;
import org.maia.amstrad.io.tape.model.Block;
import org.maia.amstrad.io.tape.model.BlockData;
import org.maia.amstrad.io.tape.model.ByteSequence;
import org.maia.amstrad.io.tape.model.TapeProgram;
import org.maia.amstrad.io.tape.model.profile.TapeProfile;
import org.maia.amstrad.io.tape.model.sc.SourceCode;
import org.maia.amstrad.io.tape.read.AudioFile;
import org.maia.amstrad.io.tape.read.AudioTapeInputStream;
import org.maia.amstrad.io.tape.read.TapeReader;
import org.maia.amstrad.io.tape.read.TapeReaderListener;
import org.maia.amstrad.program.AmstradProgramMetaDataConstants;

/**
 * Task that reconstructs programs from an Amstrad audio tape file
 * 
 * <p>
 * The method {@link #readTape()} should be called only once, after which all output is generated in a folder and can be
 * inspected via {@link #getTapeIndex()} and {@link #getTapeProfile()}.
 * </p>
 */
public class TapeReaderTask implements TapeReaderListener, AmstradProgramMetaDataConstants {

	private AudioFile audioFile; // tape recording .WAV file

	private File outputDirectory; // root directory for all generated program folders

	private AudioTapeIndex tapeIndex; // programs found on tape

	private TapeDecorator tapeDecorator; // locates sections on tape

	private BlockAudioDecorator blockDecorator; // locates blocks on tape

	private AudioTapeBitDecorator audioTapeBitDecorator; // locates bits in the input file

	private boolean minimalOutput;

	private static NumberFormat programFolderNumberFormat;

	static {
		programFolderNumberFormat = NumberFormat.getIntegerInstance();
		programFolderNumberFormat.setMinimumIntegerDigits(2);
	}

	public TapeReaderTask(AudioFile audioFile, File outputDirectory) {
		this.audioFile = audioFile;
		this.outputDirectory = outputDirectory;
		this.tapeIndex = new AudioTapeIndex(audioFile);
		this.tapeDecorator = new TapeDecorator();
		this.blockDecorator = new BlockAudioDecorator(tapeDecorator);
		this.audioTapeBitDecorator = new AudioTapeBitDecorator();
	}

	public void readTape() throws Exception {
		AudioTapeInputStream atis = new AudioTapeInputStream(getAudioFile());
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
			decompiler.decompile(byteCode.getBytesArray());
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
		} catch (BasicDecompilationException e) {
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
		try {
			PrintWriter pw = new PrintWriter(
					new File(programFolder, "INFO-ATR" + AmstradFileType.AMSTRAD_METADATA_FILE.getFileExtension()),
					"UTF-8");
			pw.println(AMD_TYPE + ": " + AMD_TYPE_LOCOMOTIVE_BASIC_PROGRAM);
			pw.println(AMD_NAME + ": " + program.getProgramName());
			pw.println(AMD_AUTHOR + ": " + getDefaultMetaDatum(AMD_AUTHOR));
			pw.println(AMD_YEAR + ": " + getDefaultMetaDatum(AMD_YEAR));
			pw.println(AMD_TAPE + ": " + getDefaultMetaDatum(AMD_TAPE));
			pw.println(AMD_BLOCKS + ": " + program.getNumberOfBlocks());
			pw.println(AMD_MONITOR + ": " + getDefaultMetaDatum(AMD_MONITOR));
			pw.println(AMD_DESCRIPTION + ": " + getDefaultMetaDatum(AMD_DESCRIPTION));
			pw.println(AMD_AUTHORING + ": " + getDefaultMetaDatum(AMD_AUTHORING));
			pw.println("#" + AMD_CONTROLS_PREFIX + "[1]" + AMD_CONTROLS_SUFFIX_HEADING + ": ");
			pw.println("#" + AMD_CONTROLS_PREFIX + "[1]" + AMD_CONTROLS_SUFFIX_KEY + ": ");
			pw.println("#" + AMD_CONTROLS_PREFIX + "[1]" + AMD_CONTROLS_SUFFIX_DESCRIPTION + ": ");
			pw.println("#" + AMD_IMAGES_PREFIX + "[1]" + AMD_IMAGES_SUFFIX_FILEREF + ": ");
			pw.println("#" + AMD_IMAGES_PREFIX + "[1]" + AMD_IMAGES_SUFFIX_CAPTION + ": ");
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

	private String getDefaultMetaDatum(String key) {
		return getDefaultMetaDatum(key, "");
	}

	private String getDefaultMetaDatum(String key, String defaultValue) {
		return System.getProperty("amd." + key.toLowerCase(), defaultValue);
	}

	public AudioFile getAudioFile() {
		return audioFile;
	}

	public File getOutputDirectory() {
		return outputDirectory;
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