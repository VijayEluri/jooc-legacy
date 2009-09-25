package org.ooc.frontend.model;

import java.io.IOException;

import org.ooc.frontend.Visitor;
import org.ooc.frontend.model.VariableDecl.VariableDeclAtom;
import org.ooc.frontend.model.interfaces.MustBeResolved;
import org.ooc.frontend.model.tokens.Token;
import org.ooc.middle.OocCompilationError;
import org.ooc.middle.hobgoblins.Resolver;

public class Foreach extends ControlStatement implements MustBeResolved {

	protected Expression variable;
	protected Expression collection; // must be of type Range or Iterable
	
	public Foreach(Expression variable, Expression collection, Token startToken) {
		super(startToken);
		this.variable = variable;
		this.collection = collection;
	}
	
	public Expression getVariable() {
		return variable;
	}
	
	public void setVariable(VariableDecl variable) {
		this.variable = variable;
	}
	
	public Expression getCollection() {
		return collection;
	}
	
	public void setCollection(Expression range) {
		this.collection = range;
	}
	
	public void accept(Visitor visitor) throws IOException {
		visitor.visit(this);
	}
	
	public boolean hasChildren() {
		return true;
	}
	
	public void acceptChildren(Visitor visitor) throws IOException {
		variable.accept(visitor);
		collection.accept(visitor);
		body.accept(visitor);
	}
	
	@Override
	public boolean replace(Node oldie, Node kiddo) {
		if(oldie == variable) {
			variable = (Expression) kiddo;
			return true;
		}
		
		if(oldie == collection) {
			collection = (Expression) kiddo;
			return true;
		}
		
		return false;
	}
	
	@Override
	public VariableDecl getVariable(String name) {
		if(variable instanceof VariableDecl) {
			VariableDecl varDecl = (VariableDecl) variable;
			if(varDecl.hasAtom(name)) return varDecl;
		} else if(variable instanceof VariableAccess) {
			VariableAccess varAcc = (VariableAccess) variable;
			if(varAcc.getName().equals(name)) return (VariableDecl) varAcc.getRef();
		}
		return super.getVariable(name);
	}
	
	@Override
	public void getVariables(NodeList<VariableDecl> variables) {
		if(variable instanceof VariableDecl) {
			VariableDecl varDecl = (VariableDecl) variable;
			variables.add(varDecl);
		} else if(variable instanceof VariableAccess) {
			VariableAccess varAcc = (VariableAccess) variable;
			if(varAcc.getRef() != null)
				variables.add((VariableDecl) varAcc.getRef());
		}
		super.getVariables(variables);
	}

	public boolean isResolved() {
		return false;
	}

	@SuppressWarnings("unchecked")
	public Response resolve(NodeList<Node> stack, Resolver res, boolean fatal) {
		
		/*
		if(collection.getType().getRef() == null) {
			if(fatal) {
				throw new OocCompilationError(collection, stack, "Couldn't resolve type of foreach's collection.");
			}
			return Response.LOOP;
		}
		*/
		
		if(collection.getType().getRef() instanceof ClassDecl) {
			ClassDecl classDecl = (ClassDecl) collection.getType().getRef();
			if(classDecl.isChildOf("Iterable")) {
				FunctionDecl iterFunc = classDecl.getFunction("iterator", "", null);
				
				Type iterType = iterFunc.getReturnType();
				iterType.resolve(stack, res, false);
				if(iterType.getRef() == null) {
					if(fatal) throw new OocCompilationError(this, stack, "couldn't resolve iterType "+iterType);
					return Response.LOOP;
				}
				
				int lineIndex = stack.find(Line.class);
				Line line = (Line) stack.get(lineIndex);
				NodeList<Line> list = (NodeList<Line>) stack.get(lineIndex - 1);
				
				Block block = new Block(startToken);
				
				MemberCall iterCall = new MemberCall(collection, "iterator", "", startToken);
				iterCall.resolve(stack, res, true);
				
				VariableDecl vdfe = new VariableDecl(iterCall.getType(), false, startToken);
				vdfe.setType(iterType);
				vdfe.getAtoms().add(new VariableDeclAtom(generateTempName("iter"), iterCall, startToken));

				VariableAccess iterAcc = new VariableAccess(vdfe.getName(), startToken);
				iterAcc.setRef(vdfe);
				
				MemberCall hasNextCall = new MemberCall(iterAcc, "hasNext", "", startToken);
				hasNextCall.resolve(stack, res, true);
				While while1 = new While(hasNextCall, startToken);
				
				MemberCall nextCall = new MemberCall(iterAcc, "next", "", startToken);
				nextCall.resolve(stack, res, true);
				
				if(variable instanceof VariableAccess) {
					VariableAccess varAcc = (VariableAccess) variable;
					if(varAcc.getRef() == null) {
						VariableDecl varDecl = new VariableDecl(iterCall.getType().getTypeParams().getFirst().getType(), false, varAcc.startToken);
						varDecl.getAtoms().add(new VariableDeclAtom(varAcc.getName(), null, varAcc.startToken));
						block.getBody().add(0, new Line(varDecl));
					}
				}
				
				// FIXME what if variable isn't an Access?
				while1.getBody().add(new Line(new Assignment(variable, nextCall, startToken)));
				while1.getBody().addAll(getBody());
				
				list.replace(line, new Line(block));
				block.getBody().add(new Line(vdfe));
				block.getBody().add(new Line(while1));
				
				return Response.RESTART;
			}
		}
		return Response.OK;
		
	}
	
}
