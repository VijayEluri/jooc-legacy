package org.ooc.frontend.parser;

import org.ooc.frontend.model.BinaryOperation;
import org.ooc.frontend.model.ControlStatement;
import org.ooc.frontend.model.Expression;
import org.ooc.frontend.model.FunctionCall;
import org.ooc.frontend.model.Line;
import org.ooc.frontend.model.MemberCall;
import org.ooc.frontend.model.Module;
import org.ooc.frontend.model.Node;
import org.ooc.frontend.model.NodeList;
import org.ooc.frontend.model.Statement;
import org.ooc.frontend.model.VariableAccess;
import org.ooc.frontend.model.VariableDecl;
import org.ooc.frontend.model.tokens.Token;
import org.ooc.frontend.model.tokens.TokenReader;
import org.ooc.frontend.model.tokens.Token.TokenType;
import org.ubi.CompilationFailedError;
import org.ubi.SourceReader;

public class LineParser {
	
	static int seedNumber = 1;

	@SuppressWarnings({ "unchecked", "null" })
	public static boolean fill(Module module, SourceReader sReader, TokenReader reader, NodeList<Line> body) {
		
		int mark = reader.mark();
		
		reader.skipWhitespace();
		
		if(!reader.hasNext()) {
			reader.reset(mark);
			return false;
		}
		
		int mark2 = reader.mark();
		Node node = VariableDeclParser.parseMulti(module, sReader, reader);
		Statement statement = null;
		Line line = null;
		
		if(node != null) {
			if(node instanceof NodeList<?>) {
				for(VariableDecl vDecl : (NodeList<VariableDecl>) node) {
					statement = vDecl;
					line = new Line(vDecl);
					body.add(line);
				}
			} else {
				statement = (Statement) node;
				VariableDecl vDecl = (VariableDecl) node;
				line = new Line(vDecl);
				body.add(line);				
			}
		} else {
			reader.reset(mark2);
			statement = StatementParser.parse(module, sReader, reader);
			if(statement == null) {
				reader.reset(mark);
				return false;
			}
			line = new Line(statement);
			body.add(line);
		}
		
		while(reader.hasNext() && reader.peek().type == TokenType.DOT) {
			Expression expr = null;
			if(statement instanceof MemberCall) {
				MemberCall memberCall = (MemberCall) statement;
				expr = memberCall.getExpression();
				if(expr instanceof FunctionCall || expr instanceof BinaryOperation) {
					VariableDecl vdfe = new VariableDecl(null, "callroot" + (seedNumber++), expr, statement.startToken, null);
					body.addBefore(line, new Line(vdfe));
					expr = new VariableAccess(vdfe, expr.startToken);
					memberCall.setExpression(expr);
				}
			} else if(statement instanceof VariableDecl) {
				VariableDecl varDecl = (VariableDecl) statement;
				expr = new VariableAccess(varDecl, statement.startToken);
			} else if(statement instanceof Expression) {
				expr = (Expression) statement;
			} else {
				throw new CompilationFailedError(sReader.getLocation(reader.peek()),
						"Dots '.' for chain-calls should be used after expressions, not "
						+statement.getClass().getSimpleName()+"s.");
			}
			reader.skip();
			reader.skipWhitespace();
			Token startToken = reader.peek();
			FunctionCall otherCall = FunctionCallParser.parse(module, sReader, reader);
			if(otherCall == null) {
				throw new CompilationFailedError(sReader.getLocation(reader.peek()),
					"Expected function call after a dot '.'");
			}
			statement = new MemberCall(expr, otherCall, startToken);
			body.add(new Line(statement));
		}
		
		if(!(statement instanceof ControlStatement)) {
			Token next = reader.peek();
			if(next.type != TokenType.LINESEP && next.type != TokenType.CLOS_BRACK && next.type != TokenType.CLOS_PAREN) {
				throw new CompilationFailedError(sReader.getLocation(next),
						"Missing semi-colon at the end of a line (got a "+next+" instead)");
			}
			if(next.type == TokenType.LINESEP) reader.skip();
		}
		
		return true;
		
	}

	
}
