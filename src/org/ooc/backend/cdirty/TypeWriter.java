package org.ooc.backend.cdirty;

import java.io.IOException;

import org.ooc.backend.cdirty.FunctionDeclWriter.ArgsWriteMode;
import org.ooc.frontend.model.ArrayLiteral;
import org.ooc.frontend.model.ClassDecl;
import org.ooc.frontend.model.CoverDecl;
import org.ooc.frontend.model.FunctionDecl;
import org.ooc.frontend.model.Literal;
import org.ooc.frontend.model.Type;
import org.ooc.frontend.model.TypeDecl;
import org.ooc.middle.OocCompilationError;

public class TypeWriter {

	// FIXME state is evil. Make it an argument
	public static boolean doStruct = false;

	public static void write(Type type, CGenerator cgen) throws IOException {
		write(type, cgen, true);		
	}
	
	public static void write(Type typeParam, CGenerator cgen, boolean doPrefix) throws IOException {
		write(typeParam, cgen, doPrefix, true);
	}
		
	public static void write(Type typeParam, CGenerator cgen, boolean doPrefix, boolean doFinale) throws IOException {
		
		Type type = typeParam;
		
		if(type.getRef() == null) {
			if(cgen.params.veryVerbose) {
				Thread.dumpStack();
			}
			throw new OocCompilationError(type, cgen.module, "Unresolved type '"+type.getName()+"' isGeneric? "
					+type.isGeneric());
		}
		
		if(type.getName().equals("Func")) {
			writeFuncPointer(((FunctionDecl) type.getRef()), "", cgen);
			return;
		}
		
		if(type.isGeneric()) {
			cgen.current.append("uint8_t");
			if(!type.isArray()) {
				cgen.current.append("* ");
			}
			return;
		}
		
		if(type.isConst()) cgen.current.app("const ");
		if(doPrefix && type.getRef() instanceof TypeDecl) {
			if(doStruct && type.getRef() instanceof ClassDecl) {
				cgen.current.app("struct _");
			}
			if(type.getRef() instanceof CoverDecl) {
				if(((CoverDecl) type.getRef()).getFromType() != null) {
					Type groundType = type.getGroundType();
					type = groundType;
					cgen.current.app(type.getName());
				} else {
					cgen.current.app(((TypeDecl) type.getRef()).getUnderName());
				}
			} else {
				cgen.current.app(((TypeDecl) type.getRef()).getUnderName());
			}
		} else {
			cgen.current.app(type.getName());
		}
		
		if(!type.isFlat()) {
			cgen.current.app(' ');
		}
		
		if(doFinale) {
			writeFinale(type, cgen);
		}
		
	}
	
	public static void writeFinale(Type type, CGenerator cgen) throws IOException {
		writePreFinale(type, cgen);
		writePostFinale(type, cgen);
	}

	public static void writePreFinale(Type type, CGenerator cgen)
			throws IOException {
		if(type.getRef() instanceof ClassDecl) {
			cgen.current.app("*");
		}
		if(type.isGeneric() && !type.isArray()) {
			cgen.current.app("*");
		}
		// no-VLA workaround.
		if(type.isArray() && type.getArraySize() != null
				&& !(type.getArraySize().isConstant())  && (cgen.params.compiler == null || !cgen.params.compiler.supportsVLAs())) {
			cgen.current.app("*");
		}
	}
	
	public static void writePostFinale(Type type, CGenerator cgen)
			throws IOException {
		writePostFinale(type, cgen, null);
	}
	
	public static void writePostFinale(Type type, CGenerator cgen, ArrayLiteral arrLitParam) throws IOException {
		ArrayLiteral arrLit = arrLitParam; // Java is paranoid. Or at least Eclipse is.
		int level = type.getPointerLevel() + type.getReferenceLevel();
		for(int i = 0; i < level; i++) {
			if(type.isArray()) {
				if(i == 0 && type.getArraySize() != null) {
					if(type.getArraySize() instanceof Literal || (cgen.params.compiler == null || cgen.params.compiler.supportsVLAs())) {
						cgen.current.app('[');
						type.getArraySize().accept(cgen);
						cgen.current.app(']');
					} else {
						// no-VLA workaround
						cgen.current.app(" = GC_malloc(");
						type.getArraySize().accept(cgen);
						cgen.current.app(")");
					}
				} else {
					cgen.current.app('[');
					if(arrLit != null) {
						cgen.current.app(String.valueOf(arrLit.getElements().size()));
						if(i + 1 < level) arrLit = (ArrayLiteral) arrLit.getElements().getFirst();
					}
					cgen.current.app(']');
				}
			} else {
				cgen.current.app("*");
			}
		}
	}
	
	public static void writeSpaced(Type type, CGenerator cgen) throws IOException {
		writeSpaced(type, cgen, true);
	}
	
	public static void writeSpaced(Type type, CGenerator cgen, boolean doPrefix) throws IOException {
		write(type, cgen, doPrefix);
		if(type.getGroundType().isFlat()) cgen.current.app(' ');
	}

	public static void writeFuncPointer(FunctionDecl decl, String name, CGenerator cgen) throws IOException {
		writeFuncPointerStart(decl, cgen);
		cgen.current.app(name);
		writeFuncPointerEnd(decl, cgen);		
	}

	public static AwesomeWriter writeFuncPointerEnd(FunctionDecl decl, CGenerator cgen)
			throws IOException {
		cgen.current.app(")");
		FunctionDeclWriter.writeFuncArgs(decl, ArgsWriteMode.TYPES_ONLY, null, cgen);
		return cgen.current;
	}

	public static AwesomeWriter writeFuncPointerStart(FunctionDecl decl, CGenerator cgen)
			throws IOException {
		if(decl.getReturnType().isVoid()) {
			// special case when covering functions, then lang__Void isn't typedef'd yet and it's all problematic >:|
			cgen.current.app("void");
		} else {
			decl.getReturnType().accept(cgen);
		}
		return cgen.current.app(" (*");
	}
	
}
