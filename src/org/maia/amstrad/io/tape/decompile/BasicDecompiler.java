package org.maia.amstrad.io.tape.decompile;

import org.maia.amstrad.io.tape.model.sc.SourceCode;

public interface BasicDecompiler {

	SourceCode decompile(short[] byteCode);

}