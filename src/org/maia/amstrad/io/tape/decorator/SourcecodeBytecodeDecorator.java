package org.maia.amstrad.io.tape.decorator;

import java.util.List;

import org.maia.amstrad.io.tape.decorator.SourcecodeBytecodeDecorator.SourcecodeBytecodeDecoration;
import org.maia.amstrad.io.tape.model.ByteCodeRange;
import org.maia.amstrad.io.tape.model.sc.SourceCode;
import org.maia.amstrad.io.tape.model.sc.SourceCodeLine;
import org.maia.amstrad.io.tape.model.sc.SourceCodePosition;
import org.maia.amstrad.io.tape.model.sc.SourceCodeRange;

public class SourcecodeBytecodeDecorator extends SequenceDecorator<SourcecodeBytecodeDecoration> {

	public SourcecodeBytecodeDecorator() {
		super(2000);
	}

	public void decorate(SourceCode sourceCode, SourceCodeRange sourceCodeRange, ByteCodeRange byteCodeRange) {
		addDecoration(new SourcecodeBytecodeDecoration(sourceCode, sourceCodeRange, byteCodeRange));
	}

	public List<SourcecodeBytecodeDecoration> getDecorationsInsideRange(SourceCodePosition from,
			SourceCodePosition until) {
		return getDecorationsInRange(convertToSequentialOffset(from), convertToSequentialOffset(until), false);
	}

	public List<SourcecodeBytecodeDecoration> getDecorationsOverlappingRange(SourceCodePosition from,
			SourceCodePosition until) {
		return getDecorationsInRange(convertToSequentialOffset(from), convertToSequentialOffset(until), true);
	}

	private static long convertToSequentialOffset(SourceCodePosition position) {
		// line numbers in Basic are in range [1, 65535] meaning 16 bit integer
		return ((long) position.getLineNumber() << 16) | (long) position.getLinePosition();
	}

	public static class SourcecodeBytecodeDecoration extends SequenceDecoration {

		private SourceCode sourceCode;

		private SourceCodeRange sourceCodeRange;

		private ByteCodeRange byteCodeRange;

		public SourcecodeBytecodeDecoration(SourceCode sourceCode, SourceCodeRange sourceCodeRange,
				ByteCodeRange byteCodeRange) {
			super(convertToSequentialOffset(sourceCodeRange.getStartPosition()),
					convertToSequentialOffset(sourceCodeRange.getEndPosition())
							- convertToSequentialOffset(sourceCodeRange.getStartPosition()) + 1L);
			this.sourceCode = sourceCode;
			this.sourceCodeRange = sourceCodeRange;
			this.byteCodeRange = byteCodeRange;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(48);
			SourceCodePosition from = getSourceCodeRange().getStartPosition();
			SourceCodePosition until = getSourceCodeRange().getEndPosition();
			sb.append(from);
			sb.append(" -> ");
			sb.append(until);
			sb.append(" : ");
			if (from.getLineNumber() == until.getLineNumber()) {
				// on same line
				SourceCodeLine line = getSourceCode().getLine(from.getLineNumber());
				CharSequence code = line.getCode().subSequence(from.getLinePosition(), until.getLinePosition() + 1);
				sb.append(code);
			} else {
				sb.append("<multi-line>");
			}
			sb.append(" (bytecode@");
			sb.append(getByteCodeRange().getByteCodeOffset());
			sb.append("->");
			sb.append(getByteCodeRange().getByteCodeEnd());
			sb.append(")");
			return sb.toString();
		}

		@Override
		protected String getHumanReadableDecoration() {
			return null;
		}

		public SourceCode getSourceCode() {
			return sourceCode;
		}

		public SourceCodeRange getSourceCodeRange() {
			return sourceCodeRange;
		}

		public ByteCodeRange getByteCodeRange() {
			return byteCodeRange;
		}

	}

}