package org.maia.amstrad.io.tape.read;

import org.maia.amstrad.io.tape.decorate.BytecodeAudioDecorator;
import org.maia.amstrad.io.tape.model.Block;
import org.maia.amstrad.io.tape.model.TapeProgram;

public interface TapeReaderListener {

	void startReadingTape();

	void endReadingTape();

	void foundNewBlock(Block block);

	void startReadingProgram(TapeProgram program);

	void endReadingProgram(TapeProgram program, BytecodeAudioDecorator byteCodeDecorator);

}