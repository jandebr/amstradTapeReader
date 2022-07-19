package org.maia.amstrad.io.tape.decorate;

import org.maia.amstrad.io.tape.model.ByteCodeRange;
import org.maia.amstrad.io.tape.model.sc.SourceCode;
import org.maia.amstrad.io.tape.model.sc.SourceCodeLine;
import org.maia.amstrad.io.tape.model.sc.SourceCodePosition;
import org.maia.amstrad.io.tape.model.sc.SourceCodeRange;
import org.maia.amstrad.pc.basic.locomotive.LocomotiveBasicDecompiler;

public class DecoratingLocomotiveBasicDecompiler extends LocomotiveBasicDecompiler {

	private SourceCode sourceCode;

	private SourcecodeBytecodeDecorator sourceCodeDecorator;

	public DecoratingLocomotiveBasicDecompiler() {
	}

	@Override
	protected void init(byte[] byteCode) {
		super.init(byteCode);
		this.sourceCode = new SourceCode();
		this.sourceCodeDecorator = new SourcecodeBytecodeDecorator();
	}

	@Override
	protected void addedSourceCodeToken(int lineNumber, CharSequence lineSoFar, int linePositionFrom,
			int linePositionUntil, int bytecodeOffset, int bytecodeLength) {
		super.addedSourceCodeToken(lineNumber, lineSoFar, linePositionFrom, linePositionUntil, bytecodeOffset,
				bytecodeLength);
		SourceCodePosition from = new SourceCodePosition(lineNumber, linePositionFrom);
		SourceCodePosition until = new SourceCodePosition(lineNumber, linePositionUntil);
		getSourceCodeDecorator().decorate(getSourceCode(), new SourceCodeRange(from, until),
				new ByteCodeRange(bytecodeOffset, bytecodeLength));
	}

	@Override
	protected void addedSourceCodeLine(int lineNumber, CharSequence lineOfCode) {
		super.addedSourceCodeLine(lineNumber, lineOfCode);
		getSourceCode().addLine(new SourceCodeLine(lineNumber, lineOfCode));
	}

	public SourceCode getSourceCode() {
		return sourceCode;
	}

	public SourcecodeBytecodeDecorator getSourceCodeDecorator() {
		return sourceCodeDecorator;
	}

}