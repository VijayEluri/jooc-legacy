package org.ooc.frontend.model;

import java.io.IOException;

import org.ooc.frontend.Levenshtein;
import org.ooc.frontend.Visitor;
import org.ooc.frontend.model.VariableDecl.VariableDeclAtom;
import org.ooc.frontend.model.interfaces.MustBeResolved;
import org.ooc.frontend.model.tokens.Token;
import org.ooc.middle.OocCompilationError;
import org.ooc.middle.hobgoblins.Resolver;

public class VariableAccess extends Access implements MustBeResolved {

	protected String name;
	protected Declaration ref;
	
	public VariableAccess(String variable, Token startToken) {
		super(startToken);
		this.name = variable;
	}

	public VariableAccess(VariableDecl varDecl, Token startToken) {
		super(startToken);
		assert(varDecl.atoms.size() == 1);
		this.name = varDecl.getName();
		ref = varDecl;
	}

	public String getName() {
		return name;
	}
	
	public Declaration getRef() {
		return ref;
	}
	
	public void setRef(Declaration ref) {
		this.ref = ref;
	}

	@Override
	public Type getType() {
		if(ref != null) {
			return ref.getType();
		}
		return null;
	}
	
	@Override
	public void accept(Visitor visitor) throws IOException {
		visitor.visit(this);
	}
	
	@Override
	public boolean hasChildren() {
		return true;
	}
	
	@Override
	public void acceptChildren(Visitor visitor) throws IOException {}
	
	@Override
	public boolean replace(Node oldie, Node kiddo) {
		if(oldie == ref) {
			ref = (VariableDecl) kiddo;
			return true;
		}
		return false;
	}

	@Override
	public boolean isResolved() {
		return ref != null;
	}

	@Override
	public boolean resolve(final NodeList<Node> stack, final Resolver res, final boolean fatal) throws IOException {

		if(isResolved()) return false;
		
		{
			VariableDecl varDecl = getVariable(name, stack);
			if(varDecl != null) {
				if(varDecl.isMember()) {
					VariableAccess thisAccess = new VariableAccess("this", startToken);
					thisAccess.setRef(varDecl);
					MemberAccess membAcc =  new MemberAccess(thisAccess, name, startToken);
					membAcc.resolve(stack, res, fatal);
					membAcc.setRef(varDecl);
					if(!stack.peek().replace(VariableAccess.this, membAcc)) {
						throw new Error("Couldn't replace a VariableAccess with a MemberAccess!");
					}
				}
				ref = varDecl;
				return false;
			}
		}
		
		{
			FunctionDecl func = getFunction(name, "", null, stack);
			if(func != null) {
				ref = func;
				return false;
			}
		}
		
		if(ref != null) return false;
		int typeIndex = stack.find(TypeDecl.class);
		if(typeIndex != -1) {
			TypeDecl typeDecl = (TypeDecl) stack.get(typeIndex);
			if(name.equals("This")) {
				name = typeDecl.getName();
				ref = typeDecl;
				return true;
			}
			VariableDecl varDecl = typeDecl.getVariable(name);
			if(varDecl != null) {
				VariableAccess thisAccess = new VariableAccess("this", startToken);
				thisAccess.setRef(varDecl);
				MemberAccess membAccess = new MemberAccess(thisAccess, name, startToken);
				membAccess.setRef(varDecl);
				if(!stack.peek().replace(this, membAccess)) {
					throw new Error("Couldn't replace a VariableAccess with a MemberAccess! Stack = "+stack);
				}
				return true;
			}
		}
		
		ref = res.module.getType(name);
		if(ref != null) return true;
		
		GenericType genType = getGenericType(stack, name);
		if(genType != null) {
			ref = genType.getArgument();
			return false;
		}
		
		if(fatal && ref == null) {
			String message = "Couldn't resolve access to variable "+name;
			String guess = guessCorrectName(stack, res);
			if(guess != null) {
				message += " Did you mean "+guess+" ?";
			}
			throw new OocCompilationError(this, stack, message);
		}
		
		return ref == null;
		
	}

	private String guessCorrectName(NodeList<Node> mainStack, Resolver res) {
		
		int bestDistance = Integer.MAX_VALUE;
		String bestMatch = null;
		
		NodeList<VariableDecl> variables = new NodeList<VariableDecl>();
		
		for(int i = mainStack.size() - 1; i >= 0; i--) {
			Node node = mainStack.get(i);
			if(!(node instanceof Scope)) continue;
			Scope scope = (Scope) node;
			scope.getVariables(variables);
		}
		
		for(VariableDecl decl: variables) {
			for(VariableDeclAtom atom: decl.getAtoms()) {
				int distance = Levenshtein.distance(name, atom.getName());
				if(distance < bestDistance) {
					bestDistance = distance;
					bestMatch = atom.getName();
				}
			}
		}
		
		return bestMatch;
		
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+" : "+name;
	}

}
