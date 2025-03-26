package ru.itmo.icompiler.syntax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;

import ru.itmo.icompiler.lex.DFALexer;
import ru.itmo.icompiler.lex.LexUtils;
import ru.itmo.icompiler.lex.Lexer;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.lex.Token.TokenType;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.ast.IfThenElseStatementASTNode;
import ru.itmo.icompiler.syntax.ast.ProgramASTNode;
import ru.itmo.icompiler.syntax.ast.RoutineDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.TypeDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.VariableAssignmentASTNode;
import ru.itmo.icompiler.syntax.ast.VariableDeclarationASTNode;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.ast.expression.BinaryOperatorExpressionNode.BinaryOperatorType;
import ru.itmo.icompiler.syntax.ast.expression.BooleanValueExpressionNode;
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
				
//				Map.entry(TokenType.DOT_OPERATOR, BinaryOperatorType.PROP_ACC_BINOP)
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
	
	private Lexer lexer;
	private List<SyntaxException> syntaxErrors;
	
	private String[] lines;
	
	public SimpleParser(InputStream in) {
		StringBuilder sb = new StringBuilder();
		
		this.lines = new BufferedReader(new InputStreamReader(in)).lines().peek(sb::append).toArray(String[]::new);
		
		this.lexer = new LexerWrapper(new DFALexer(sb.toString()));
		this.syntaxErrors = new ArrayList<>();
	}
	
	public SimpleParser(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		this.lines = br.lines().peek(sb::append).toArray(String[]::new);
		br.close();
		
		this.lexer = new LexerWrapper(new DFALexer(sb.toString()));
		this.syntaxErrors = new ArrayList<>();
	}

	public SimpleParser(String s) {
		this.lines = s.split("\n");
		
		this.lexer = new LexerWrapper(new DFALexer(s));
		this.syntaxErrors = new ArrayList<>();
	}
	
	public SimpleParser(Lexer lexer) {
		StringBuilder sb = new StringBuilder();
		StringBuilder tmp = new StringBuilder();
		
		Token prev = null;
		
		while (!lexer.isEndReached()) {
			Token t = lexer.nextToken();
			
			if (prev == null || prev.lineNumber < t.lineNumber) {
				for (int i = prev.lineNumber; i < t.lineNumber; ++i)
					tmp.append("\n");
				
				tmp.append(t.text);
				
				sb.append(tmp);
				tmp.setLength(0);
			} else
				tmp.append(t.text);
			
			prev = t;
		}
		
		this.lexer = new LexerWrapper(new DFALexer(sb.toString()));
		this.syntaxErrors = new ArrayList<>();
	}
	
	protected Token expectToken(boolean ignoreDelimeters, TokenType... expectedTypes) throws SyntaxException {
		Token tok = lexer.lookupToken(ignoreDelimeters ? Predicate.not(LexUtils::isDelimeter) : LexUtils::truePredicate);
		
		if (tok.type.noneOf(expectedTypes)) {
			if (tok.type == TokenType.END_OF_TEXT)
				throw new UnexpectedTokenSyntaxException.UnexpectedEndOfTextSyntaxException(tok.lineNumber, tok.lineOffset);
			else
				throw new UnexpectedTokenSyntaxException(null, tok, expectedTypes);
		}
		
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
	
	protected ASTNode parseIfStatement() throws SyntaxException {
		ASTNode stmtNode;
		
		skipToken(TokenType.IF_KEYWORD);
		ExpressionASTNode conditionNode = parseExpression();
		skipToken(TokenType.THEN_KEYWORD);

		ASTNode ifBody = null;
		
		while (lexer.lookupToken().type.noneOf(
					TokenType.ELSE_KEYWORD,
					TokenType.END_KEYWORD
				)) {
			
		}
		
		ASTNode elseBody = null;
		
		if (lexer.nextToken().type == TokenType.ELSE_KEYWORD) {
			elseBody = parseStatement();
	
			stmtNode = new IfThenElseStatementASTNode(null, conditionNode, ifBody, elseBody);
		}
			
		stmtNode = new IfThenElseStatementASTNode(null, conditionNode, ifBody, null);
		
		skipToken(TokenType.END_KEYWORD);
		
		return stmtNode;
	}
	
	private void skipRoutineBlock() {
		lexer.nextToken(tok -> tok.type == TokenType.RIGHT_PARENTHESIS);
		
		if (lexer.isEndReached())
			return;
		
		if (lexer.nextToken().type != TokenType.IS_KEYWORD)
			return;
		
		Predicate<Token> pred = tok -> tok.type.anyOf(TokenType.THEN_KEYWORD, TokenType.LOOP_KEYWORD, TokenType.END_KEYWORD);
		
		Token t1, t2;
		
		do {
			t1 = lexer.nextToken(pred);
			t2 = lexer.nextToken(pred);
		} while (
			t1.type.anyOf(TokenType.THEN_KEYWORD, TokenType.LOOP_KEYWORD) && t2.type == TokenType.END_KEYWORD
		);
	}
	
	protected RoutineDeclarationASTNode parseRoutineHeader() throws SyntaxException {
		skipToken(TokenType.ROUTINE_KEYWORD);
		Token routineId = lexer.nextToken();
		
		if (routineId.type != TokenType.IDENTIFIER) {
			skipRoutineBlock();
			
			throw new UnexpectedTokenSyntaxException(routineId, TokenType.IDENTIFIER); 
		}
			
		String routineName = routineId.text;
		
		skipToken(TokenType.LEFT_PARENTHESIS);

		List<VariableDeclarationASTNode> args = new ArrayList<>();
		
		while (lexer.lookupToken().type != TokenType.RIGHT_PARENTHESIS) {
			String argName = skipToken(TokenType.IDENTIFIER).text;
			skipToken(TokenType.COLON_OPERATOR);
			VarType argType = parseType();
			
			VariableDeclarationASTNode argDecl = new VariableDeclarationASTNode(null, argType, argName);
			args.add(argDecl);
			
			if (lexer.lookupToken().type == TokenType.COMMA_OPERATOR)
				lexer.skipToken();
		}
		
		skipToken(TokenType.RIGHT_PARENTHESIS);
		
		VarType resultType = VarType.VOID_TYPE;
		
		if (lexer.lookupToken().type == TokenType.COLON_OPERATOR) {
			lexer.skipToken();
			resultType = parseType();
		}
		
		return new RoutineDeclarationASTNode(null, resultType, routineName, args);
	}
	
	protected ASTNode parseRoutine() throws SyntaxException {
		RoutineDeclarationASTNode header = parseRoutineHeader();
		
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
					
					int arraySize = -1;
					
					if (lexer.lookupToken().type != TokenType.RIGHT_BRACKET) {
						parseExpression();
						
						arraySize = 1;
					}
					
					skipToken(TokenType.RIGHT_BRACKET);
					
					VarType elementType = parseType();
					
					return new VarType.ArrayType(elementType, arraySize);
				default:
					return new VarType(start.text);
			}
		} catch (SyntaxException e) {
			throw new SyntaxException("Expected a type", posToken.lineNumber, posToken.lineOffset);
		}
	}
	
	protected VarType calculateExprType(ExpressionASTNode exprNode) {
		return VarType.INTEGER_PRIMITIVE_TYPE;
	}
	
	protected VariableAssignmentASTNode parseVarAssign() throws SyntaxException {
		String varName = skipToken(TokenType.IDENTIFIER).text;
		skipToken(TokenType.ASSIGN_OPERATOR);
		ExpressionASTNode exprNode = parseExpression();
		
		return new VariableAssignmentASTNode(null, varName, exprNode);
	}
	
	protected ExpressionASTNode parseExpression() throws SyntaxException {
		Token t = lexer.lookupToken();
		
		ExpressionASTNode node = stackExpressionParser(0);
		
		if (node == null)
			throw new ExpectedAnExpessionSyntaxException(t.lineNumber, t.lineOffset);
		
		return node;
	}
	
	private ExpressionASTNode stackExpressionParser(int nestingLevel) throws SyntaxException {
		boolean opFlag = false;
		
		BinaryOperatorType currentOpType = null;
		
		Stack<ExpressionASTNode> expressionStack = new Stack<>();
		Stack<UnaryOperatorType> unaryOperatorsStack = new Stack<>();
		Stack<BinaryOperatorType> binaryOperatorsStack = new Stack<>();
		
		while (true) {
			boolean switchOpFlag = true;
			
			Token token = lexer.lookupToken();
			
			if (token.type.noneOf(
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
			))
			break;
			
			if (opFlag) {
				if (token.type.noneOf(
						TokenType.LEFT_BRACKET,
						TokenType.DOT_OPERATOR
					) && !BINOP_TYPE_BY_TOKEN_TYPE.containsKey(token.type))
					throw new ExpectedAnOperatorSyntaxException(token);
				
				lexer.skipToken();
				
				if (token.type == TokenType.LEFT_BRACKET) {
					ExpressionASTNode operand = expressionStack.pop();
					ExpressionASTNode indexExpr = stackExpressionParser(nestingLevel + 1);
					skipToken(TokenType.RIGHT_BRACKET);
					
					expressionStack.add(new BinaryOperatorExpressionNode(
								null,
								BinaryOperatorType.ARR_ACC_BINOP,
								operand,
								indexExpr
							));
					
					switchOpFlag = false;
				} else if (token.type == TokenType.DOT_OPERATOR) {
					ExpressionASTNode recordExpr = expressionStack.pop();
					Token property = skipToken(TokenType.IDENTIFIER);
					
					expressionStack.add(new PropertyAccessExpressionNode(
								null,
								recordExpr,
								property.text
							));
					
					switchOpFlag = false;
				} else {
					BinaryOperatorType binopType = BINOP_TYPE_BY_TOKEN_TYPE.get(token.type);
					
					if (currentOpType == null) {
						if (currentOpType == null)
							currentOpType = binopType;
					} else {
						while (!binaryOperatorsStack.isEmpty() && binopType.priority <= binaryOperatorsStack.peek().priority) {
							currentOpType = binaryOperatorsStack.pop();
							
							ExpressionASTNode right = expressionStack.pop();
							ExpressionASTNode left = expressionStack.pop();
							
							BinaryOperatorExpressionNode binopNode = new BinaryOperatorExpressionNode(null, currentOpType, left, right);
							expressionStack.push(binopNode);
						}
					}
					
					binaryOperatorsStack.push(binopType);
					currentOpType = binopType;
				}
			} else {
				if (token.type.noneOf(
						TokenType.TRUE_BOOLEAN_LITERAL,
						TokenType.FALSE_BOOLEAN_LITERAL,
						TokenType.INTEGER_NUMERIC_LITERAL,
						TokenType.REAL_NUMERIC_LITERAL,
						TokenType.IDENTIFIER,
						
						TokenType.PLUS_OPERATOR,
						TokenType.MINUS_OPERATOR,
						TokenType.NOT_OPERATOR,
						
						TokenType.LEFT_PARENTHESIS
					))
					throw new ExpectedAnExpessionSyntaxException(token.lineNumber, token.lineOffset);
				
				lexer.skipToken();
				
				ExpressionASTNode newExprNode = null;

				switch (token.type) {
					case TRUE_BOOLEAN_LITERAL:
					case FALSE_BOOLEAN_LITERAL:
						newExprNode = new BooleanValueExpressionNode(null, "true".equals(token.text));
						break;
					case INTEGER_NUMERIC_LITERAL:
						newExprNode = new IntegerValueExpressionNode(null, Integer.parseInt(token.text));
						break;
					case REAL_NUMERIC_LITERAL:
						newExprNode = new RealValueExpressionNode(null, Float.parseFloat(token.text));
						break;
					case IDENTIFIER:
						String iden = token.text;
						
						switch (lexer.lookupToken().type) {
							case LEFT_PARENTHESIS:
								lexer.skipToken();
								
								RoutineCallExpressionNode routineCallNode = new RoutineCallExpressionNode(null, iden);
								
								if (lexer.lookupToken().type != TokenType.RIGHT_PARENTHESIS)
									routineCallNode.addArguments(parseExpression());
								
								while (lexer.lookupToken().type != TokenType.RIGHT_PARENTHESIS) {
									skipToken(false, TokenType.COMMA_OPERATOR);
									routineCallNode.addArguments(parseExpression());
								}
								
								skipToken(false, TokenType.RIGHT_PARENTHESIS);
								newExprNode = routineCallNode;
								break;
							default:
								newExprNode = new VariableExpressionNode(null, iden);
								break;
						}
						break;
					case PLUS_OPERATOR:
					case MINUS_OPERATOR:
					case NOT_OPERATOR:
						UnaryOperatorType unopType = UNOP_TYPE_BY_TOKEN_TYPE.get(token.type);
						unaryOperatorsStack.push(unopType);
						
						switchOpFlag = false;
						break;
					case LEFT_PARENTHESIS:
						ExpressionASTNode subexpr = stackExpressionParser(nestingLevel + 1);
						skipToken(TokenType.RIGHT_PARENTHESIS);
						
						newExprNode = subexpr;
						break;
					default:
						break;
				}
				
				if (newExprNode != null) {
					while (!unaryOperatorsStack.isEmpty()) {
						UnaryOperatorType unopType = unaryOperatorsStack.pop();
	
						newExprNode = new UnaryOperatorExpressionNode(null, unopType, newExprNode);
					}
					
					expressionStack.push(newExprNode);
				}
			}
			
			if (switchOpFlag)
				opFlag = !opFlag;
		}
		
		while (!binaryOperatorsStack.isEmpty()) {
			BinaryOperatorType binopType = binaryOperatorsStack.pop();
			
			ExpressionASTNode right = expressionStack.pop();
			ExpressionASTNode left = expressionStack.pop();
			
			expressionStack.push(new BinaryOperatorExpressionNode(null, binopType, left, right));
		}
		
		return expressionStack.isEmpty() ? null : expressionStack.pop();
	}
	
	protected ASTNode parseStatement() {
		return null;
	}
	
	protected VariableDeclarationASTNode parseVarDecl() throws SyntaxException {
		skipToken(TokenType.VAR_KEYWORD);
		
		String varName = skipToken(TokenType.IDENTIFIER).text;
		Token tok = lexer.lookupToken();
		
		VariableDeclarationASTNode varDeclNode = null;
		
		VarType type = null;
		
		if (tok.type == TokenType.COLON_OPERATOR) {
			lexer.skipToken();
			type = parseType();
			varDeclNode = new VariableDeclarationASTNode(null, type, varName);
			
			tok = lexer.lookupToken();
		}
		
		if (tok.type == TokenType.IS_KEYWORD) {
			lexer.skipToken();
			
			ExpressionASTNode node = null;
			
			try {
				node = parseExpression();
				
				VarType exprType = calculateExprType(node);
				
				/* Check type conformance later */
				
				type = exprType;
				
				if (varDeclNode == null)
					varDeclNode = new VariableDeclarationASTNode(null, type, varName);
				
				varDeclNode.addChild(
						new VariableAssignmentASTNode(null, varName, node)
					);
			} catch (ExpressionSyntaxException e) {
				syntaxErrors.add(e);
				
				lexer.nextToken(LexUtils::isDelimeter);
			}
		}
		
		if (type == null)
			throw new VariableDeclWithoutTypeSyntaxException(null, tok.lineNumber, tok.lineOffset);
		
		return varDeclNode;
	}
	
	protected TypeDeclarationASTNode parseTypeDecl() throws SyntaxException {
		skipToken(TokenType.TYPE_KEYWORD);
		String typealias = skipToken(TokenType.IDENTIFIER).text;
		skipToken(TokenType.IS_KEYWORD);
		
		return new TypeDeclarationASTNode(null, typealias, parseType());
	}
	
	protected ASTNode parseSimpleGlobalDeclaration() throws SyntaxException {
		Token tok = lexer.lookupToken();
		
		try {
			switch (tok.type) {
				case VAR_KEYWORD:
					return parseVarDecl();
				case TYPE_KEYWORD:
					return parseTypeDecl();
				default:
					throw new UnexpectedTokenSyntaxException(null, tok);
			}
		} catch (SyntaxException e) {
			lexer.nextToken(LexUtils::isDelimeter);
			
			throw e;
		}
	}
	
	protected ASTNode parseSingleNode() {
		Token tok = lexer.lookupToken(Predicate.not(LexUtils::isDelimeter));
		
		try {
			switch (tok.type) {
				case ROUTINE_KEYWORD: {
					return parseRoutine();
				}
				case END_OF_TEXT:
					return null;
				default:
					return parseSimpleGlobalDeclaration();
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
	
	public void printErrors() {
		for (SyntaxException e: syntaxErrors) {
			int line = e.getErrorLine() - 1;
			int spaces = e.getErrorOffset() - 1;
			
			System.err.printf(
					"<source>:%d:%d: %s\n" +
					"%d:%s\n" +
					"%s^\n",
					
					e.getErrorLine(), e.getErrorOffset(), e.getMessage(),
					e.getErrorLine(), lines[line],
					" ".repeat(spaces + String.valueOf(e.getErrorLine()).length() + 1)
				);
		}
	}
	
	public boolean isEndReached() {
		return lexer.isEndReached();
	}
	
	public List<SyntaxException> getSyntaxErrors() {
		return syntaxErrors;
	}
}
