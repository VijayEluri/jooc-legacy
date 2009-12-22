package org.ooc.backend.cdirty;

import java.io.IOException;

import org.ooc.frontend.model.*;

public class CoverDeclWriter {

	public static void write(CoverDecl cover, CGenerator cgen) throws IOException {
		
		cgen.current = cgen.hw;

		if(cover.getVersion() != null) {
			VersionBlockWriter.writeVersionBlockStart(cover.getVersion(), cgen);
		}
		
		// addons only add functions to an already imported cover, so
		// we don't need to struct it again, it would confuse the C compiler
		if(!cover.isAddon() && !cover.isExtern() && cover.getFromType() == null) {
			cgen.current.app("struct _").app(cover.getUnderName()).app(' ').openBlock();
			for(VariableDecl decl: cover.getVariables()) {
				cgen.current.nl();
				if(VariableDeclWriter.write(decl, cgen)) {
					cgen.current.app(';');
				}
			}
			cgen.current.closeBlock().app(';').nl();
		}
		
		if(cover.getVersion() != null) {
			VersionBlockWriter.writeVersionBlockEnd(cgen);
		}
		
		/* and now the functions */
		
		cgen.current = cgen.cw;
		
		cgen.current.app("/* cover " + cover.getName() + " has version " +
				(cover.getVersion() != null ? cover.getVersion().getVersion().toString() : "null") + " */");
		if(cover.getVersion() != null) {
			VersionBlockWriter.writeVersionBlockStart(cover.getVersion(), cgen);
		}
		
		for(FunctionDecl decl: cover.getFunctions()) {
			decl.accept(cgen);
			cgen.current.nl();
		}
		
		if(cover.getVersion() != null) {
			VersionBlockWriter.writeVersionBlockEnd(cgen);
		}
	}

	public static  void writeTypedef(CoverDecl cover, CGenerator cgen) throws IOException {
		
		if(cover.getVersion() != null) {
			VersionBlockWriter.writeVersionBlockStart(cover.getVersion(), cgen);
		}
		
		if(!cover.isAddon() && !cover.isExtern()) {
			Type fromType = cover.getFromType();
			if(fromType == null) {
				cgen.current.nl().app("typedef struct _").app(cover.getUnderName())
					.app(' ').app(cover.getUnderName()).app(';');
			} else {
				cgen.current.nl().app("typedef ");
				if(fromType instanceof FuncType) {
					TypeWriter.writeFuncPointer(((FuncType) fromType).getDecl(), cover.getUnderName(), cgen);
				} else {
					TypeWriter.writeSpaced(fromType.getGroundType(), cgen, true);
					cgen.current.app(cover.getUnderName());
				}
				cgen.current.app(';');
			}
		}
		
		if(cover.getVersion() != null) {
			VersionBlockWriter.writeVersionBlockEnd(cgen);
		}
	}
	
}
