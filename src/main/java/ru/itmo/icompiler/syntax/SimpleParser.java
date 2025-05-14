package ru.itmo.icompiler.syntax;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;

import ru.itmo.icompiler.lex.LexUtils;
import ru.itmo.icompiler.lex.Lexer;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.lex.Token.TokenType;
import ru.itmo.icompiler.semantic.ArrayType;
import ru.itmo.icompiler.semantic.ArrayType.SizedArrayType;
import ru.itmo.icompiler.semantic.RecordType;
import ru.itmo.icompiler.semantic.RecordType.RecordProperty;
import ru.itmo.icompiler.semantic.Typealias;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.ast.BreakStatementASTNode;
import ru.itmo.icompiler.syntax.ast.CompoundStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ForEachStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ForInRangeStatementASTNode;
import ru.itmo.icompiler.syntax.ast.IfThenElseStatementASTNode;
import ru.itmo.icompiler.syntax.ast.PrintStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ProgramASTNode;
import ru.itmo.icompiler.syntax.ast.ReturnStatementASTNode;
import ru.itmo.icompiler.syntax.ast.RoutineDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.RoutineDefinitionASTNode;
import ru.itmo.icompiler.syntax.ast.TypeDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.VariableAssignmentASTNode;
import ru.itmo.icompiler.syntax.ast.VariableDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.WhileStatementASTNode;
import ru.itmo.icompiler.syntax.ast.expression.ArrayAccessExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode.BinaryOperatorType;
import ru.itmo.icompiler.syntax.ast.expression.BooleanValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.EmptyExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.ExpressionASTNode;
import ru.itmo.icompiler.syntax.ast.expression.IntegerValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.PropertyAccessExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RealValueExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.RoutineCallExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.UnaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.UnaryOperatorExpressionNode.UnaryOperatorType;
import ru.itmo.icompiler.syntax.ast.expression.VariableExpressionNode;
import ru.itmo.icompiler.syntax.exception.SyntaxException;
import ru.itmo.icompiler.syntax.exception.UnexpectedTokenSyntaxException;
import ru.itmo.icompiler.syntax.exception.UnexpectedTokenSyntaxException.UnexpectedEndOfTextSyntaxException;
import ru.itmo.icompiler.syntax.exception.VariableDeclWithoutTypeSyntaxException;
import ru.itmo.icompiler.syntax.expression.exception.ExpectedAnExpessionSyntaxException;
import ru.itmo.icompiler.syntax.expression.exception.ExpectedAnOperatorSyntaxException;
import ru.itmo.icompiler.syntax.expression.exception.ExpressionSyntaxException;

public class SimpleParser implements Parser {	
	private static final Map<TokenType, BinaryOperatorType> BINOP_TYPE_BY_TOKEN_TYPE = new EnumMap<>(Map.ofEntries(
				Map.entry(TokenType.PLUS_OPERATOR, BinaryOperatorType.ADD_BINOP),
				Map.entry(TokenType.MINUS_OPERATOR, BinaryOperatorType.SUB_BINOP),
				
				Map.entry(TokenType.MULTIPLY_OPERATOR, BinaryOperatorType.MUL_BINOP),
				Map.entry(TokenType.DIVIDE_OPERATOR, BinaryOperatorType.DIV_BINOP),
				Map.entry(TokenType.MODULO_OPERATOR, BinaryOperatorType.MOD_BINOP),
				
				Map.entry(TokenType.AND_OPERATOR, BinaryOperatorType.AND_BINOP),
				Map.entry(TokenType.OR_OPERATOR, BinaryOperatorType.OR_BINOP),
				Map.entry(TokenType.XOR_OPERATOR, BinaryOperatorType.XOR_BINOP),
				
				Map.entry(TokenType.LT_OPERATOR, BinaryOperatorType.LT_BINOP),
				Map.entry(TokenType.LE_OPERATOR, BinaryOperatorType.LE_BINOP),
				Map.entry(TokenType.EQ_OPERATOR, BinaryOperatorType.EQ_BINOP),
				Map.entry(TokenType.NE_OPERATOR, BinaryOperatorType.NE_BINOP),
				Map.entry(TokenType.GT_OPERATOR, BinaryOperatorType.GT_BINOP),
				Map.entry(TokenType.GE_OPERATOR, BinaryOperatorType.GE_BINOP)
			));
	
	private static final Map<TokenType, UnaryOperatorType> UNOP_TYPE_BY_TOKEN_TYPE = new EnumMap<>(Map.ofEntries(
				Map.entry(TokenType.PLUS_OPERATOR, UnaryOperatorType.PLUS_BINOP),
				Map.entry(TokenType.MINUS_OPERATOR, UnaryOperatorType.MINUS_BINOP),
				Map.entry(TokenType.NOT_OPERATOR, UnaryOperatorType.NOT_BINOP)
			));
	
	private static class LexerWrapper implements Lexer {
		private Lexer lexer;
		
		public LexerWrapper(Lexer lexer) {
			this.lexer = lexer;
		}

		@Override
		public Token lookupToken(Predicate<Token> p) {
			return lexer.lookupToken(p.and(
					Predicate.not(LexUtils::isWhitespace)
				));
		}

		@Override
		public Token lookupToken() {
			return lookupToken(LexUtils::truePredicate);
		}

		@Override
		public void skipToken() {
			lookupToken();
			
			lexer.skipToken();
		}

		@Override
		public Token nextToken(Predicate<Token> p) {
			Token tk = lookupToken(p);
			skipToken();
			
			return tk;
		}

		@Override
		public Token nextToken() {
			return nextToken(LexUtils::truePredicate);
		}

		@Override
		public boolean isEndReached() {
			return lexer.isEndReached();
		}
	}
	
//	private static class LexerWrapper implements Lexer {
//		private Lexer lexer;
//		
//		private Token[] tokenBuffer = new Token[2];
//		private int currentTokenIndex = 0;
//		
//		public LexerWrapper(Lexer lexer) {
//			this.lexer = lexer;
//		}
//
//		@Override
//		public Token lookupToken(Predicate<Token> p) {
//			if (tokenBuffer[currentTokenIndex] == null)
//				tokenBuffer[currentTokenIndex] = lexer.lookupToken(p.and(
//						Predicate.not(LexUtils::isWhitespace)
//					));
//			
//			return tokenBuffer[currentTokenIndex];
//		}
//
//		@Override
//		public Token lookupToken() {
//			return lookupToken(LexUtils::truePredicate);
//		}
//
//		@Override
//		public void skipToken() {
//			if (currentTokenIndex > 0)
//				--currentTokenIndex;
//			else {
//				lookupToken();
//				
//				tokenBuffer[1] = tokenBuffer[0];
//				tokenBuffer[0] = null;
//				
//				lexer.skipToken();
//			}		
//		}
//
//		@Override
//		public Token nextToken(Predicate<Token> p) {
//			Token tk = lookupToken(p);
//			skipToken();
//			
//			return tk;
//		}
//
//		@Override
//		public Token nextToken() {
//			return nextToken(LexUtils::truePredicate);
//		}
//		
//		public Token previousToken() {
//			if (currentTokenIndex < tokenBuffer.length - 1)
//				currentTokenIndex++;
//			
//			return tokenBuffer[currentTokenIndex];
//		}
//
//		@Override
//		public boolean isEndReached() {
//			return lexer.isEndReached();
//		}
//	}
	
	private Lexer lexer;
	private List<SyntaxException> syntaxErrors;
	
	public SimpleParser(Lexer lexer) {
//		StringBuilder sb = new StringBuilder();
//		StringBuilder tmp = new StringBuilder();
//		
//		Token prev = null;
//		
//		while (!lexer.isEndReached()) {
//			Token t = lexer.nextToken();
//			
//			if (prev == null || prev.lineNumber < t.lineNumber) {
//				for (int i = prev.lineNumber; i < t.lineNumber; ++i)
//					tmp.append("\n");
//				
//				tmp.append(t.text);
//				
//				sb.append(tmp);
//				tmp.setLength(0);
//			} else
//				tmp.append(t.text);
//			
//			prev = t;
//		}
		
		this.lexer = new LexerWrapper(lexer);
		this.syntaxErrors = new ArrayList<>();
	}
	
	private static void checkToken(Token tok, TokenType... expectedTypes) throws SyntaxException {
		if (tok.type == TokenType.END_OF_TEXT)
			System.out.println("DEBUG: " + expectedTypes);
		
		if (tok.type.noneOf(expectedTypes))
			throwUnexpectedTokenException(tok, expectedTypes);
	}
	
	protected Token expectToken(boolean ignoreDelimeters, TokenType... expectedTypes) throws SyntaxException {
		Token tok = lexer.lookupToken(ignoreDelimeters ? Predicate.not(LexUtils::isDelimeter) : LexUtils::truePredicate);
		
		checkToken(tok, expectedTypes);
		
		return tok;
	}
	
	protected Token expectToken(TokenType... expectedTypes) throws SyntaxException {
		return expectToken(false, expectedTypes);
	}
	
	protected Token skipToken(boolean ignoreDelimiters, TokenType... expectedTypes) throws SyntaxException {
		Token tok = expectToken(ignoreDelimiters, expectedTypes);
			
		lexer.skipToken();
		
		return tok;
	}
	
	protected Token skipToken(TokenType... expectedTypes) throws SyntaxException {
		return skipToken(false, expectedTypes);
	}
	
	protected Token skipDelimeter() {
		return lexer.lookupToken(Predicate.not(LexUtils::isDelimeter));
	}
	
	protected Token goToDelimeter() {
		return lexer.lookupToken(LexUtils::isDelimeter); 
	}
	
	private static void throwUnexpectedTokenException(Token tk, TokenType... expected) throws SyntaxException {
		switch (tk.type) {
			case END_OF_TEXT:
				throw new UnexpectedTokenSyntaxException.UnexpectedEndOfTextSyntaxException(tk.lineNumber, tk.lineOffset);
			default:
				throw new UnexpectedTokenSyntaxException(tk, expected);
		}
	}
	
	private static void throwUnexpectedTokenException(String message, Token tk) throws SyntaxException {
		switch (tk.type) {
			case END_OF_TEXT:
				throw new UnexpectedTokenSyntaxException.UnexpectedEndOfTextSyntaxException(tk.lineNumber, tk.lineOffset);
			default:
				throw new UnexpectedTokenSyntaxException(message, tk);
		}
	}
	
	private static boolean isNotDelimeter(Token tk) {
		return !LexUtils.isDelimeter(tk);
	}
	
	protected ASTNode parseIfStatement() throws SyntaxException {
		skipToken(TokenType.IF_KEYWORD);
		
		ExpressionASTNode conditionNode = null;
		
		try {
			conditionNode = parseExpression();
		} catch (ExpressionSyntaxException e) {
			syntaxErrors.add(e);
			
			lexer.lookupToken(tok -> tok.type == TokenType.THEN_KEYWORD);
		}
		
		skipToken(true, TokenType.THEN_KEYWORD);

		ASTNode ifBody = parseBody(tk -> tk.type.noneOf(TokenType.ELSE_KEYWORD, TokenType.END_KEYWORD));
		
		ASTNode elseBody = null;
		
		if (lexer.lookupToken().type == TokenType.ELSE_KEYWORD) {
			lexer.skipToken();
			
			elseBody = parseBody();
		}
			
		ASTNode stmtNode = new IfThenElseStatementASTNode(null, conditionNode, ifBody, elseBody);
		
		skipToken(TokenType.END_KEYWORD);
		
		return stmtNode;
	}
	
	protected ASTNode parseForStatement() throws SyntaxException {	
		skipToken(TokenType.FOR_KEYWORD);
		
		String iterVar = null;
		
		try {
			iterVar = skipToken(TokenType.IDENTIFIER).text;
		} catch (SyntaxException e) {
			syntaxErrors.add(e);
		}
		
		skipToken(TokenType.IN_KEYWORD);
		
		ExpressionASTNode fromExpr = null, toExpr = null;
		
		try {
			fromExpr = parseExpression();
			
			if (lexer.lookupToken().type == TokenType.RANGE_OPERATOR) {
				lexer.skipToken();
				
				toExpr = parseExpression();
			}
		} catch (SyntaxException e) {
			syntaxErrors.add(e);
			
			lexer.lookupToken(tok -> tok.type.anyOf(TokenType.REVERSE_KEYWORD, TokenType.LOOP_KEYWORD));
		}
		
		boolean reversed = lexer.lookupToken().type == TokenType.REVERSE_KEYWORD;
		
		if (reversed)
			lexer.skipToken();
		
		skipToken(true, TokenType.LOOP_KEYWORD); skipDelimeter();
		
		CompoundStatementASTNode loopBody = parseBody();
		
		skipToken(TokenType.END_KEYWORD);
		
		if (toExpr != null)
			return new ForInRangeStatementASTNode(null, iterVar, fromExpr, toExpr, reversed, loopBody);
		else
			return new ForEachStatementASTNode(null, iterVar, fromExpr, reversed, loopBody);
	}
	
	protected ASTNode parseWhileStatement() throws SyntaxException {
		skipToken(TokenType.WHILE_KEYWORD);
		
		ExpressionASTNode conditionNode = null;
		
		try {
			conditionNode = parseExpression();
		} catch (ExpressionSyntaxException e) {
			syntaxErrors.add(e);
			
			lexer.lookupToken(tok -> tok.type == TokenType.LOOP_KEYWORD);
		}
		
		skipToken(true, TokenType.LOOP_KEYWORD); skipDelimeter();
		
		CompoundStatementASTNode loopBody = parseBody();
		
		skipToken(TokenType.END_KEYWORD);
		
		return new WhileStatementASTNode(null, conditionNode, loopBody);
	}
	
	private void skipRoutineBlock() {
		Token next = lexer.nextToken(t -> LexUtils.isDelimeter(t) || t.type.anyOf(TokenType.IS_KEYWORD, TokenType.ROUTINE_EXPRESSION_OPERATOR));
		
		if (lexer.isEndReached() || LexUtils.isDelimeter(next))
			return;
		else if (next.type == TokenType.ROUTINE_EXPRESSION_OPERATOR) {
			skipDelimeter();
			
			return;
		}
		
		Predicate<Token> pred = tok -> tok.type.anyOf(TokenType.THEN_KEYWORD, TokenType.LOOP_KEYWORD, TokenType.END_KEYWORD);
		
		Token t1, t2;
		
		do {
			t1 = lexer.nextToken(pred);
			
			if (t1.type == TokenType.END_KEYWORD)
				break;
			
			t2 = lexer.nextToken(pred);
		} while (
			t1.type.anyOf(TokenType.THEN_KEYWORD, TokenType.LOOP_KEYWORD) && t2.type == TokenType.END_KEYWORD
		);
	}
	
	protected VariableDeclarationASTNode parseRoutineArgDecl() throws SyntaxException {
		Token tk = skipToken(TokenType.IDENTIFIER);
		String argName = tk.text;
		skipToken(TokenType.COLON_OPERATOR);
		
		VarType argType = parseType();
		
		VariableDeclarationASTNode argDecl = new VariableDeclarationASTNode(null, argType, tk, argName);
		
		return argDecl;
	}
	
	protected CompoundStatementASTNode parseBody(Predicate<Token> pred) throws SyntaxException {
		CompoundStatementASTNode compoundStmtNode = new CompoundStatementASTNode(null);
		
		while (pred.test(
			lexer.lookupToken(SimpleParser::isNotDelimeter)
		)) {
			try {
				ASTNode stmt = parseStatement();
				
				compoundStmtNode.addChild(stmt);
//			} catch (UnexpectedTokenSyntaxException e) {
//				System.out.println("/");
//				
//				goToDelimeter();
//				
//				System.out.println("//");
//				
//				throw e;
			} catch (UnexpectedEndOfTextSyntaxException e) {
				throw e;
			} catch (SyntaxException e) {
				syntaxErrors.add(e);
				
				goToDelimeter();
			}
		}
		
		return compoundStmtNode;
	}
	
	protected CompoundStatementASTNode parseBody() throws SyntaxException {
		return parseBody(tk -> tk.type != TokenType.END_KEYWORD);
	}
	
	protected RoutineDeclarationASTNode parseRoutineHeader() throws SyntaxException {
		skipToken(TokenType.ROUTINE_KEYWORD);
		Token routineId = lexer.nextToken();
		
		if (routineId.type != TokenType.IDENTIFIER) {
			skipRoutineBlock();
			
			throwUnexpectedTokenException(routineId, TokenType.IDENTIFIER); 
		}
			
		String routineName = routineId.text;
		
		Token tk = lexer.lookupToken();
		
		if (tk.type != TokenType.LEFT_PARENTHESIS) {
			skipRoutineBlock();
			
			throwUnexpectedTokenException(tk, TokenType.LEFT_PARENTHESIS); 
		}
			
		skipToken(TokenType.LEFT_PARENTHESIS);

		List<VariableDeclarationASTNode> args = new ArrayList<>();
		
		boolean commaFlag = false;
		
		while (lexer.lookupToken(SimpleParser::isNotDelimeter).type != TokenType.RIGHT_PARENTHESIS) {
			try {
				if (commaFlag)
					skipToken(TokenType.COMMA_OPERATOR);
				else
					commaFlag = true;
				
				skipDelimeter();
				
				args.add(parseRoutineArgDecl());
			} catch (SyntaxException e) {
				syntaxErrors.add(e);
				
				lexer.lookupToken(t -> t.type.anyOf(TokenType.COMMA_OPERATOR, TokenType.RIGHT_PARENTHESIS));
			}
		}
		
		skipToken(TokenType.RIGHT_PARENTHESIS);
		
		VarType resultType = VarType.VOID_TYPE;
		
		if (lexer.lookupToken().type == TokenType.COLON_OPERATOR) {
			lexer.skipToken();
			resultType = parseType();
		}
		
		return new RoutineDeclarationASTNode(null, resultType, routineId, routineName, args);
	}
	
	protected ASTNode parseRoutine() throws SyntaxException {
		RoutineDeclarationASTNode header = parseRoutineHeader();
		
		if (lexer.lookupToken().type == TokenType.IS_KEYWORD) {
			skipToken(TokenType.IS_KEYWORD); skipDelimeter();
			
			ASTNode routineBody = parseBody();
			
			skipToken(TokenType.END_KEYWORD);
			
			return new RoutineDefinitionASTNode(null, header, routineBody); 
		} else if (lexer.lookupToken().type == TokenType.ROUTINE_EXPRESSION_OPERATOR) {
			lexer.skipToken(); skipDelimeter();
			
			try {
				ExpressionASTNode expr = parseExpression();
				
				return new RoutineDefinitionASTNode(null, header, new ReturnStatementASTNode(null, expr));
			} catch (ExpressionSyntaxException e) {
				skipDelimeter();
				
				throw e;
			}
		}
		
		return header;
	}
	
	protected VarType parseType() throws SyntaxException {
		Token posToken = lexer.lookupToken(); 
		
		try {
			Token start = skipToken(
					TokenType.BOOLEAN_KEYWORD,
					TokenType.INTEGER_KEYWORD,
					TokenType.REAL_KEYWORD,
					
					TokenType.ARRAY_KEYWORD,
					TokenType.RECORD_KEYWORD,
					
					TokenType.IDENTIFIER
				);
		
			switch (start.type) {
				case BOOLEAN_KEYWORD:
					return VarType.BOOLEAN_PRIMITIVE_TYPE;
				case INTEGER_KEYWORD:
					return VarType.INTEGER_PRIMITIVE_TYPE;
				case REAL_KEYWORD:
					return VarType.REAL_PRIMITIVE_TYPE;
				case ARRAY_KEYWORD:
					skipToken(TokenType.LEFT_BRACKET);
					
					boolean sizeless = false;
					int arraySize = 0;
					
					if (lexer.lookupToken().type == TokenType.RIGHT_BRACKET)
						sizeless = true;
					else
						arraySize = Integer.parseInt(skipToken(TokenType.INTEGER_NUMERIC_LITERAL).text); // TODO: add constexpr array size calc
					
					skipToken(TokenType.RIGHT_BRACKET);
					
					VarType elementType = parseType();
					
					return sizeless ? new ArrayType(elementType) : new SizedArrayType(elementType, arraySize);
				case RECORD_KEYWORD:
					List<RecordProperty> properties = new ArrayList<>();
					
					while (lexer.lookupToken(Predicate.not(LexUtils::isDelimeter)).type != TokenType.END_KEYWORD) {
						VariableDeclarationASTNode propDecl = parseVarDecl();
						
						ExpressionASTNode expr = null;
						
						if (!propDecl.getChildren().isEmpty())
							expr = ((VariableAssignmentASTNode) propDecl.getChild(0)).getValueNode();
						
						properties.add(new RecordProperty(propDecl.getVarType(), propDecl.getVarName(), expr));
					}
					
					skipToken(true, TokenType.END_KEYWORD);
					
					return new RecordType(properties);
				case IDENTIFIER:
				default:
//					return new VarType(start.text);
					return new Typealias(start.text); // TODO: Add type alias lookup
			}
		} catch (SyntaxException e) {
			throw new SyntaxException("expected a type", posToken.lineNumber, posToken.lineOffset);
		}
	}
	
	protected VarType calculateExprType(ExpressionASTNode exprNode) {
		return VarType.INTEGER_PRIMITIVE_TYPE;
	}
	
	protected ASTNode parseAssignmentOrCall() throws SyntaxException {
		Token idTok = skipToken(TokenType.IDENTIFIER);
		
//		if (LexUtils.isDelimeter(lexer.lookupToken()))
//			return new RoutineCallExpressionNode(null, idTok);
		
		if (lexer.lookupToken().type == TokenType.LEFT_PARENTHESIS) {
			lexer.skipToken();
			
			List<ExpressionASTNode> args = new ArrayList<>();
			
			boolean commaFlag = false;
			
			while (lexer.lookupToken().type != TokenType.RIGHT_PARENTHESIS) {
				if (commaFlag)
					skipToken(TokenType.COMMA_OPERATOR);
				else
					commaFlag = true;
				
				try {
					args.add(parseExpression());
				} catch (ExpressionSyntaxException e) {
					syntaxErrors.add(e);
					
					lexer.lookupToken(tk -> tk.type.anyOf(TokenType.COMMA_OPERATOR, TokenType.RIGHT_PARENTHESIS));
				}
			}
			
			skipToken(TokenType.RIGHT_PARENTHESIS);
			
			return new RoutineCallExpressionNode(null, idTok, idTok.text, args);
		} else {
			ExpressionASTNode lvalueExpr = new VariableExpressionNode(null, idTok);
			
			Token tk = lexer.lookupToken();
			
			while (tk.type.anyOf(TokenType.DOT_OPERATOR, TokenType.LEFT_BRACKET)) {
				lexer.skipToken();
				
				switch (tk.type) {
					case DOT_OPERATOR:
						Token prop = skipToken(TokenType.IDENTIFIER);
						lvalueExpr = new PropertyAccessExpressionNode(null, tk, lvalueExpr, prop);
						break;
					case LEFT_BRACKET:
						ExpressionASTNode indexExpr = parseExpression();
						skipToken(TokenType.RIGHT_BRACKET);
						lvalueExpr = new ArrayAccessExpressionNode(null, tk, lvalueExpr, indexExpr);
						break;
					default:
						break;
				}
				
				tk = lexer.lookupToken();
			}
			
			skipToken(TokenType.ASSIGN_OPERATOR);
			ExpressionASTNode value = parseExpression();
			
			return new VariableAssignmentASTNode(null, lvalueExpr, value);
		}
	}
	
	protected ExpressionASTNode parseExpression() throws SyntaxException {
		Token t = lexer.lookupToken();
		
		ExpressionASTNode expr = stackExpressionParser();
		
		if (expr == null)
			throw new ExpectedAnExpessionSyntaxException(t.lineNumber, t.lineOffset);
			
		return expr;
	}
	
	private ExpressionASTNode parseAtomExpression() throws SyntaxException {
		Stack<UnaryOperatorType> unaryOperatorsStack = new Stack<>();
		Stack<Token> unaryOperatorsTokens = new Stack<>();
		
		ExpressionASTNode newExprNode = null;
		
		Token token = lexer.lookupToken();
		
		while (token.type.anyOf(
					TokenType.PLUS_OPERATOR,
					TokenType.MINUS_OPERATOR,
					TokenType.NOT_OPERATOR
				)) {
			lexer.skipToken();
			
			UnaryOperatorType unopType = UNOP_TYPE_BY_TOKEN_TYPE.get(token.type);
			unaryOperatorsStack.push(unopType);
			unaryOperatorsTokens.push(token);
			
			token = lexer.lookupToken();
		}
		
		if (token.type.noneOf(
				TokenType.TRUE_BOOLEAN_LITERAL,
				TokenType.FALSE_BOOLEAN_LITERAL,
				TokenType.INTEGER_NUMERIC_LITERAL,
				TokenType.REAL_NUMERIC_LITERAL,
				TokenType.IDENTIFIER,
				
				TokenType.LEFT_BRACKET,
				TokenType.DOT_OPERATOR,
				
				TokenType.LEFT_PARENTHESIS
			))
			throw new ExpectedAnExpessionSyntaxException(token.lineNumber, token.lineOffset);
		
		lexer.skipToken();
		
		switch (token.type) {
			case TRUE_BOOLEAN_LITERAL:
			case FALSE_BOOLEAN_LITERAL:
				newExprNode = new BooleanValueExpressionNode(null, token, "true".equals(token.text));
				break;
			case INTEGER_NUMERIC_LITERAL:
				newExprNode = new IntegerValueExpressionNode(null, token, Integer.parseInt(token.text));
				break;
			case REAL_NUMERIC_LITERAL:
				newExprNode = new RealValueExpressionNode(null, token, Float.parseFloat(token.text));
				break;
			case IDENTIFIER:
				Token tok = lexer.lookupToken();
				
				if (tok.type == TokenType.LEFT_PARENTHESIS) {
					lexer.skipToken();
					
					RoutineCallExpressionNode routineCallNode = new RoutineCallExpressionNode(null, token);
					
					boolean commaFlag = false;
					
					while (lexer.lookupToken().type != TokenType.RIGHT_PARENTHESIS) {
						if (commaFlag)
							skipToken(TokenType.COMMA_OPERATOR);
						else
							commaFlag = true;
						
						try {
							routineCallNode.addArguments(parseExpression());
						} catch (ExpressionSyntaxException e) {
							syntaxErrors.add(e);
							
							lexer.lookupToken(tk -> tk.type == TokenType.COMMA_OPERATOR);
						}
					}
					
					checkToken(lexer.lookupToken(), TokenType.RIGHT_PARENTHESIS);
					lexer.skipToken();
					
					newExprNode = routineCallNode;
				} else {
					newExprNode = new VariableExpressionNode(null, token);
					
					while (tok.type.anyOf(TokenType.DOT_OPERATOR, TokenType.LEFT_BRACKET)) {
						lexer.skipToken();
						
						switch (tok.type) {
							case DOT_OPERATOR:
								Token property = skipToken(TokenType.IDENTIFIER);
								
								newExprNode = new PropertyAccessExpressionNode(
											null,
											tok,
											newExprNode,
											property
										);
								break;
							case LEFT_BRACKET:
								ExpressionASTNode operand = newExprNode;
								ExpressionASTNode indexExpr = parseExpression();
								
								skipToken(TokenType.RIGHT_BRACKET);
								
								newExprNode = new ArrayAccessExpressionNode(
											null,
											tok,
											operand,
											indexExpr
										);
								break;
						}
						
						tok = lexer.lookupToken();
					}
				}
				break;
			case LEFT_PARENTHESIS:
				ExpressionASTNode subexpr = stackExpressionParser();

				skipToken(TokenType.RIGHT_PARENTHESIS);
				
				newExprNode = subexpr;
				break;
			default:
				break;
		}
		
		while (!unaryOperatorsStack.isEmpty()) {
			UnaryOperatorType unopType = unaryOperatorsStack.pop();
			Token tk = unaryOperatorsTokens.pop();

			newExprNode = new UnaryOperatorExpressionNode(null, tk, unopType, newExprNode);
		}
		
		return newExprNode;
	}
	
	private ExpressionASTNode stackExpressionParser() throws SyntaxException {
		Stack<ExpressionASTNode> expressionStack = new Stack<>();
		
		Stack<BinaryOperatorType> binaryOperatorsStack = new Stack<>(); 
		Stack<Token> binaryOperatorsTokens = new Stack<>();
		
		expressionStack.push(parseAtomExpression());
		
		Token token = lexer.lookupToken();
		
		while (token.type.anyOf(
				TokenType.TRUE_BOOLEAN_LITERAL,
				TokenType.FALSE_BOOLEAN_LITERAL,
				TokenType.INTEGER_NUMERIC_LITERAL,
				TokenType.REAL_NUMERIC_LITERAL,
				TokenType.IDENTIFIER,
				
				TokenType.PLUS_OPERATOR,
				TokenType.MINUS_OPERATOR,
				TokenType.MULTIPLY_OPERATOR,
				TokenType.DIVIDE_OPERATOR,
				TokenType.MODULO_OPERATOR,
				
				TokenType.NOT_OPERATOR,
				TokenType.AND_OPERATOR,
				TokenType.OR_OPERATOR,
				TokenType.XOR_OPERATOR,
				
				TokenType.LT_OPERATOR,
				TokenType.LE_OPERATOR,
				TokenType.EQ_OPERATOR,
				TokenType.NE_OPERATOR,
				TokenType.GT_OPERATOR,
				TokenType.GE_OPERATOR,
				
				TokenType.LEFT_BRACKET,
				TokenType.DOT_OPERATOR,
				
				TokenType.LEFT_PARENTHESIS
				)) {
			if (!BINOP_TYPE_BY_TOKEN_TYPE.containsKey(token.type))
				throw new ExpectedAnOperatorSyntaxException(token);
			
			lexer.skipToken();
			
			BinaryOperatorType binopType = BINOP_TYPE_BY_TOKEN_TYPE.get(token.type);
			
			ExpressionASTNode newExpr = parseAtomExpression();
				
			while (!binaryOperatorsStack.isEmpty() && expressionStack.size() > 1 && binopType.priority <= binaryOperatorsStack.peek().priority) {
				BinaryOperatorType prevBinopType = binaryOperatorsStack.pop();
				Token tk = binaryOperatorsTokens.pop();
				
				ExpressionASTNode right = expressionStack.pop();
				ExpressionASTNode left = expressionStack.pop();
				
				BinaryOperatorExpressionNode binopNode = new BinaryOperatorExpressionNode(null, tk, prevBinopType, left, right);
				expressionStack.push(binopNode);
			}
			
			expressionStack.push(newExpr);
			
			binaryOperatorsStack.push(binopType);
			binaryOperatorsTokens.push(token);
			
			token = lexer.lookupToken();
		}
		
		while (!binaryOperatorsStack.isEmpty()) {
			BinaryOperatorType binopType = binaryOperatorsStack.pop();
			Token tk = binaryOperatorsTokens.pop();
			
			if (expressionStack.size() < 2) {
				throw new ExpressionSyntaxException(
						String.format("Binary operator \"%s\" has no second operand", tk.text), 
						tk.lineNumber, 
						tk.lineOffset
					);
			}
			
			ExpressionASTNode right = expressionStack.pop();
			ExpressionASTNode left = expressionStack.pop();
			
			expressionStack.push(new BinaryOperatorExpressionNode(null, tk, binopType, left, right));
		}
		
		return expressionStack.isEmpty() ? null : expressionStack.pop();
	}
	
//	private ExpressionASTNode stackExpressionParser(int nestingLevel) throws SyntaxException {
//		boolean opFlag = false;
//		
//		BinaryOperatorType currentOpType = null;
//		
//		Stack<ExpressionASTNode> expressionStack = new Stack<>();
//
//		Stack<UnaryOperatorType> unaryOperatorsStack = new Stack<>();
//		
//		Stack<BinaryOperatorType> binaryOperatorsStack = new Stack<>(); 
//		Stack<Token> binaryOperatorsTokens = new Stack<>();
//		
//		while (true) {
//			boolean switchOpFlag = true;
//			
//			Token token = lexer.lookupToken();
//			
//			if (token.type.noneOf(
//				TokenType.TRUE_BOOLEAN_LITERAL,
//				TokenType.FALSE_BOOLEAN_LITERAL,
//				TokenType.INTEGER_NUMERIC_LITERAL,
//				TokenType.REAL_NUMERIC_LITERAL,
//				TokenType.IDENTIFIER,
//				
//				TokenType.PLUS_OPERATOR,
//				TokenType.MINUS_OPERATOR,
//				TokenType.MULTIPLY_OPERATOR,
//				TokenType.DIVIDE_OPERATOR,
//				TokenType.MODULO_OPERATOR,
//				
//				TokenType.NOT_OPERATOR,
//				TokenType.AND_OPERATOR,
//				TokenType.OR_OPERATOR,
//				TokenType.XOR_OPERATOR,
//				
//				TokenType.LT_OPERATOR,
//				TokenType.LE_OPERATOR,
//				TokenType.EQ_OPERATOR,
//				TokenType.NE_OPERATOR,
//				TokenType.GT_OPERATOR,
//				TokenType.GE_OPERATOR,
//				
//				TokenType.LEFT_BRACKET,
//				TokenType.DOT_OPERATOR,
//				
//				TokenType.LEFT_PARENTHESIS
//			))
//			break;
//			
//			if (opFlag) {
//				if (token.type.noneOf(
//						TokenType.LEFT_BRACKET,
//						TokenType.DOT_OPERATOR
//					) && !BINOP_TYPE_BY_TOKEN_TYPE.containsKey(token.type))
//					throw new ExpectedAnOperatorSyntaxException(token);
//				
//				lexer.skipToken();
//				
//				if (token.type == TokenType.LEFT_BRACKET) {
//					ExpressionASTNode operand = expressionStack.pop();
//					ExpressionASTNode indexExpr = stackExpressionParser(nestingLevel + 1);
//					skipToken(TokenType.RIGHT_BRACKET);
//					
//					expressionStack.add(new BinaryOperatorExpressionNode(
//								null,
//								token,
//								BinaryOperatorType.ARR_ACC_BINOP,
//								operand,
//								indexExpr
//							));
//					
//					switchOpFlag = false;
//				} else if (token.type == TokenType.DOT_OPERATOR) {
//					ExpressionASTNode recordExpr = expressionStack.pop();
//					Token property = skipToken(TokenType.IDENTIFIER);
//					
//					expressionStack.add(new PropertyAccessExpressionNode(
//								null,
//								token,
//								recordExpr,
//								property
//							));
//					
//					switchOpFlag = false;
//				} else {
//					BinaryOperatorType binopType = BINOP_TYPE_BY_TOKEN_TYPE.get(token.type);
//					
//					if (currentOpType == null) {
//						if (currentOpType == null)
//							currentOpType = binopType;
//					} else {
//						while (!binaryOperatorsStack.isEmpty() && binopType.priority <= binaryOperatorsStack.peek().priority) {
//							currentOpType = binaryOperatorsStack.pop();
//							
//							ExpressionASTNode right = expressionStack.pop();
//							ExpressionASTNode left = expressionStack.pop();
//							
//							BinaryOperatorExpressionNode binopNode = new BinaryOperatorExpressionNode(null, currentOpType, left, right);
//							expressionStack.push(binopNode);
//						}
//					}
//					
//					binaryOperatorsStack.push(binopType);
//					binaryOperatorsTokens.push(token);
//					
//					currentOpType = binopType;
//				}
//			} else {
//				if (token.type.noneOf(
//						TokenType.TRUE_BOOLEAN_LITERAL,
//						TokenType.FALSE_BOOLEAN_LITERAL,
//						TokenType.INTEGER_NUMERIC_LITERAL,
//						TokenType.REAL_NUMERIC_LITERAL,
//						TokenType.IDENTIFIER,
//						
//						TokenType.PLUS_OPERATOR,
//						TokenType.MINUS_OPERATOR,
//						TokenType.NOT_OPERATOR,
//						
//						TokenType.LEFT_PARENTHESIS
//					))
//					throw new ExpectedAnExpessionSyntaxException(token.lineNumber, token.lineOffset);
//				
//				lexer.skipToken();
//				
//				ExpressionASTNode newExprNode = null;
//
//				switch (token.type) {
//					case TRUE_BOOLEAN_LITERAL:
//					case FALSE_BOOLEAN_LITERAL:
//						newExprNode = new BooleanValueExpressionNode(null, token, "true".equals(token.text));
//						break;
//					case INTEGER_NUMERIC_LITERAL:
//						newExprNode = new IntegerValueExpressionNode(null, token, Integer.parseInt(token.text));
//						break;
//					case REAL_NUMERIC_LITERAL:
//						newExprNode = new RealValueExpressionNode(null, token, Float.parseFloat(token.text));
//						break;
//					case IDENTIFIER:
//						switch (lexer.lookupToken().type) {
//							case LEFT_PARENTHESIS:
//								lexer.skipToken();
//								
//								RoutineCallExpressionNode routineCallNode = new RoutineCallExpressionNode(null, token);
//								
//								boolean commaFlag = false;
//								
//								while (lexer.lookupToken().type != TokenType.RIGHT_PARENTHESIS) {
//									if (commaFlag)
//										skipToken(TokenType.COMMA_OPERATOR);
//									else
//										commaFlag = true;
//									
//									try {
//										routineCallNode.addArguments(parseExpression());
//									} catch (ExpressionSyntaxException e) {
//										syntaxErrors.add(e);
//										
//										lexer.lookupToken(tk -> tk.type == TokenType.COMMA_OPERATOR);
//									}
//								}
//								
//								skipToken(TokenType.RIGHT_PARENTHESIS);
//								newExprNode = routineCallNode;
//								break;
//							default:
//								newExprNode = new VariableExpressionNode(null, token);
//								break;
//						}
//						break;
//					case PLUS_OPERATOR:
//					case MINUS_OPERATOR:
//					case NOT_OPERATOR:
//						UnaryOperatorType unopType = UNOP_TYPE_BY_TOKEN_TYPE.get(token.type);
//						unaryOperatorsStack.push(unopType);
//						
//						switchOpFlag = false;
//						break;
//					case LEFT_PARENTHESIS:
//						ExpressionASTNode subexpr = stackExpressionParser(nestingLevel + 1);
//						skipToken(TokenType.RIGHT_PARENTHESIS);
//						
//						newExprNode = subexpr;
//						break;
//					default:
//						break;
//				}
//				
//				if (newExprNode != null) {
//					while (!unaryOperatorsStack.isEmpty()) {
//						UnaryOperatorType unopType = unaryOperatorsStack.pop();
//	
//						newExprNode = new UnaryOperatorExpressionNode(null,  unopType, newExprNode);
//					}
//					
//					expressionStack.push(newExprNode);
//				}
//			}
//			
//			if (switchOpFlag)
//				opFlag = !opFlag;
//		}
//		
//		while (!binaryOperatorsStack.isEmpty()) {
//			BinaryOperatorType binopType = binaryOperatorsStack.pop();
//			Token tk = binaryOperatorsTokens.pop();
//			
//			if (expressionStack.size() < 2) {
//				throw new ExpressionSyntaxException(
//						String.format("Binary operator \"%s\" has no second operand", tk.text), 
//						tk.lineNumber, 
//						tk.lineOffset
//					);
//			}
//			
//			ExpressionASTNode right = expressionStack.pop();
//			ExpressionASTNode left = expressionStack.pop();
//			
//			expressionStack.push(new BinaryOperatorExpressionNode(null, binopType, left, right));
//		}
//		
//		return expressionStack.isEmpty() ? null : expressionStack.pop();
//	}
	
	protected ASTNode parsePrintStatement() throws SyntaxException {
		skipToken(TokenType.PRINT_OPERATOR);
		
		PrintStatementASTNode stmtNode = new PrintStatementASTNode(null);
		
		boolean commaExpected = false;
		
		while (!LexUtils.isDelimeter(lexer.lookupToken())) {
			if (commaExpected)
				skipToken(TokenType.COMMA_OPERATOR);
			else
				commaExpected = true;
			
			try {
				stmtNode.addChild(parseExpression());
			} catch (SyntaxException e) {
				syntaxErrors.add(e);
				
				lexer.lookupToken(tk -> LexUtils.isDelimeter(tk) || tk.type == TokenType.COMMA_OPERATOR);
			}
		}
		
		return stmtNode;
	}
	
	protected ReturnStatementASTNode parseReturnStatement() throws SyntaxException {
		skipToken(TokenType.RETURN_KEYWORD);
		
		ExpressionASTNode returnValueNode;
		
		Token tk = lexer.lookupToken();
		
		if (LexUtils.isDelimeter(tk))
			returnValueNode = new EmptyExpressionNode(null, tk);
		else {
			returnValueNode = parseExpression();
			
			expectToken(TokenType.LINE_FEED_DELIMITER, TokenType.SEMICOLON_DELIMITER);
		}
		
		return new ReturnStatementASTNode(null, returnValueNode);
	}
	
	protected ASTNode parseStatement() throws SyntaxException {		
		Token tok = lexer.lookupToken(SimpleParser::isNotDelimeter);
		
		ASTNode stmtNode = null;
		
		switch (tok.type) {
			case VAR_KEYWORD:
			case TYPE_KEYWORD:
				stmtNode = parseSimpleDeclaration();
				break;
			case IDENTIFIER:
				stmtNode = parseAssignmentOrCall();
				break;
			case IF_KEYWORD:
				stmtNode = parseIfStatement();
				break;
			case FOR_KEYWORD:
				stmtNode = parseForStatement();
				break;
			case WHILE_KEYWORD:
				stmtNode = parseWhileStatement();
				break;
			case BREAK_KEYWORD:
				stmtNode = new BreakStatementASTNode(null, tok);
				lexer.skipToken();
				expectToken(TokenType.SEMICOLON_DELIMITER, TokenType.LINE_FEED_DELIMITER);
				break;
			case PRINT_OPERATOR:
				stmtNode = parsePrintStatement();
				break;
			case RETURN_KEYWORD:
				stmtNode = parseReturnStatement();
				break;
			default:
				throwUnexpectedTokenException(tok);
		}
		
		skipDelimeter();
		
		return stmtNode;
	}
	
	protected VariableDeclarationASTNode parseVarDecl() throws SyntaxException {
		skipToken(TokenType.VAR_KEYWORD);
		
		Token varNameTok = skipToken(TokenType.IDENTIFIER);
		String varName = varNameTok.text;
		Token tok = lexer.lookupToken();
		
		VariableDeclarationASTNode varDeclNode = null;
		
		VarType type = null;
		
		if (tok.type == TokenType.COLON_OPERATOR) {
			lexer.skipToken();
			type = parseType();
			varDeclNode = new VariableDeclarationASTNode(null, type, varNameTok, varName);
			
			tok = lexer.lookupToken();
		}
		
		if (tok.type == TokenType.IS_KEYWORD) {
			lexer.skipToken();
			
			ExpressionASTNode node = null;
			
			try {
				node = parseExpression();
				
//				VarType exprType = calculateExprType(node);
				
				/* Check type conformance later */
				
				VarType exprType = VarType.AUTO_TYPE;
				
				type = exprType;
				
				if (varDeclNode == null)
					varDeclNode = new VariableDeclarationASTNode(null, type, varNameTok, varName);
				
				varDeclNode.addChild(
					new VariableAssignmentASTNode(null, varNameTok, node)
				);
			} catch (ExpressionSyntaxException e) {
				syntaxErrors.add(e);
				
				goToDelimeter();
			}
		}
		
		if (type == null)
			throw new VariableDeclWithoutTypeSyntaxException(null, tok.lineNumber, tok.lineOffset);
		
		return varDeclNode;
	}
	
	protected TypeDeclarationASTNode parseTypeDecl() throws SyntaxException {
		skipToken(TokenType.TYPE_KEYWORD);
		Token tk = skipToken(TokenType.IDENTIFIER);
		String typealias = tk.text;
		skipToken(TokenType.IS_KEYWORD);
		
		VarType type = parseType();
		
		return new TypeDeclarationASTNode(null, tk, typealias, type);
	}
	
	protected ASTNode parseSimpleDeclaration() throws SyntaxException {
		Token tok = lexer.lookupToken();
		
		switch (tok.type) {
			case VAR_KEYWORD:
				return parseVarDecl();
			case TYPE_KEYWORD:
				return parseTypeDecl();
			default:
				throwUnexpectedTokenException(tok);
				
				return null;
		}
	}
	
	protected ASTNode parseSingleNode() {
		Token tok = lexer.lookupToken(SimpleParser::isNotDelimeter);
		
		try {
			switch (tok.type) {
				case VAR_KEYWORD:
				case TYPE_KEYWORD:
					return parseSimpleDeclaration();
				case ROUTINE_KEYWORD:
					return parseRoutine();
				case END_OF_TEXT:
					break;
				default:
					goToDelimeter();
					
					throwUnexpectedTokenException("Expected a global declaration", tok);
			}
		} catch (SyntaxException e) {
			syntaxErrors.add(e);
		}
		
		return null;
	}
	
	public ASTNode parse() {
		ProgramASTNode astNode = new ProgramASTNode();
		
		while (!lexer.isEndReached()) {			
			ASTNode node = parseSingleNode();
			
			if (node != null)
				astNode.addChild(node);
		}

		return astNode;
	}
	
	public boolean isEndReached() {
		return lexer.isEndReached();
	}
	
	public List<SyntaxException> getSyntaxErrors() {
		return syntaxErrors;
	}
}
