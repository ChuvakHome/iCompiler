package ru.itmo.icompiler.syntax;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import ru.itmo.icompiler.lex.DFALexer;
import ru.itmo.icompiler.lex.LexUtils;
import ru.itmo.icompiler.lex.Lexer;
import ru.itmo.icompiler.lex.Token;
import ru.itmo.icompiler.lex.Token.TokenType;
import ru.itmo.icompiler.semantic.VarType;
import ru.itmo.icompiler.syntax.expression.BinaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.expression.BinaryOperatorExpressionNode.BinaryOperatorType;
import ru.itmo.icompiler.syntax.expression.BooleanValueExpressionNode;
import ru.itmo.icompiler.syntax.expression.ExpressionASTNode;
import ru.itmo.icompiler.syntax.expression.ExpressionASTNode.ExpressionNodeType;
import ru.itmo.icompiler.syntax.expression.IntegerValueExpressionNode;
import ru.itmo.icompiler.syntax.expression.PropertyAccessExpressionNode;
import ru.itmo.icompiler.syntax.expression.RealValueExpressionNode;
import ru.itmo.icompiler.syntax.expression.RoutineCallExpressionNode;
import ru.itmo.icompiler.syntax.expression.UnaryOperatorExpressionNode;
import ru.itmo.icompiler.syntax.expression.UnaryOperatorExpressionNode.UnaryOperatorType;
import ru.itmo.icompiler.syntax.expression.VariableExpressionNode;

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
	
	public SimpleParser(InputStream in) {
		this.lexer = new LexerWrapper(new DFALexer(in));
	}
	
	public SimpleParser(File file) throws FileNotFoundException {
		this.lexer = new LexerWrapper(new DFALexer(file));
	}

	public SimpleParser(String s) {
		this.lexer = new LexerWrapper(new DFALexer(s));
	}
	
	public SimpleParser(Lexer lexer) {
		this.lexer = new LexerWrapper(lexer);
	}
	
	protected Token expectToken(boolean ignoreDelimeters, TokenType... expectedTypes) {
		Token tok = lexer.lookupToken(ignoreDelimeters ? Predicate.not(LexUtils::isDelimeter) : LexUtils::truePredicate);
		
		if (tok.type.noneOf(expectedTypes)) {
			// report syntax error
			System.err.println("Syntax error: unexpected token " + tok.text);
			
			return null;
		}
		
		return tok;
	}
	
	protected Token expectToken(TokenType... expectedTypes) {
		return expectToken(true, expectedTypes);
	}
	
	protected Token skipToken(boolean ignoreDelimiters, TokenType... expectedTypes) {
		Token tok = expectToken(ignoreDelimiters, expectedTypes);
		
		lexer.skipToken();
		
		return tok;
	}
	
	protected Token skipToken(TokenType... expectedTypes) {
		return skipToken(true, expectedTypes);
	}
	
	protected ASTNode parseIfStatement() {
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
		
		if (lexer.nextToken().type == TokenType.ELSE_KEYWORD) {
			ASTNode elseBody = null;
			
			stmtNode = new IfElseStatementASTNode(null, conditionNode, ifBody, elseBody);
		} else
			stmtNode = new IfStatementASTNode(null, conditionNode, ifBody);
		
		skipToken(TokenType.END_KEYWORD);
		
		return stmtNode;
	}
	
	protected ASTNode parseRoutine() {
		skipToken(TokenType.ROUTINE_KEYWORD);
		String routineName = skipToken(TokenType.IDENTIFIER).text;
		skipToken(TokenType.LEFT_PARENTHESIS);
		
		VarType resultType = VarType.VOID_TYPE;
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
		
		if (lexer.lookupToken().type == TokenType.COMMA_OPERATOR) {
			lexer.skipToken();
			resultType = parseType();
		}
		
		ASTNode routineDeclNode = new RoutineDeclarationASTNode(null, resultType, routineName, args);
		
		return routineDeclNode;
	}
	
	protected VarType parseType() {
		Token tok = skipToken(
					TokenType.BOOLEAN_KEYWORD,
					TokenType.INTEGER_KEYWORD,
					TokenType.REAL_KEYWORD,
					
					TokenType.IDENTIFIER
				);
		
		switch (tok.type) {
			case BOOLEAN_KEYWORD:
				return VarType.BOOLEAN_PRIMITIVE_TYPE;
			case INTEGER_KEYWORD:
				return VarType.INTEGER_PRIMITIVE_TYPE;
			case REAL_KEYWORD:
				return VarType.REAL_PRIMITIVE_TYPE;
			default:
				return new VarType(tok.text);
		}
	}
	
	protected VarType calculateExprType(ExpressionASTNode exprNode) {
		return VarType.INTEGER_PRIMITIVE_TYPE;
	}
	
	protected VariableAssignmentASTNode parseVarAssign() {
		String varName = skipToken(TokenType.IDENTIFIER).text;
		skipToken(TokenType.ASSIGN_OPERATOR);
		ExpressionASTNode exprNode = parseExpression();
		
		return new VariableAssignmentASTNode(null, varName, exprNode);
	}
	
	protected ExpressionASTNode parseExpression() {
		return parseExpressionNaive(0);
	}
	
	private ExpressionASTNode parseExpressionNaive(int nestingLevel) {
		boolean opFlag = false;
		
		ExpressionASTNode rootNode = null;
		ExpressionASTNode rootOpNode = null;
		
		ExpressionASTNode currentNode = null;
		ExpressionASTNode currentOpNode = null;
		ExpressionASTNode currentBinopNode = null;
		
		boolean doParse = true; 
		
		while (doParse) {
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
			
			lexer.skipToken();
			
			if (opFlag) {
				BinaryOperatorType binopType = BINOP_TYPE_BY_TOKEN_TYPE.get(token.type);
				
				BinaryOperatorExpressionNode binopNode = new BinaryOperatorExpressionNode(null, binopType);
				binopNode.setLeftChild(currentNode);
				
				if (currentBinopNode != null && currentBinopNode.getExpressionNodeType() == ExpressionNodeType.BINOP_EXPR_NODE) {
					BinaryOperatorExpressionNode curBinopNode = (BinaryOperatorExpressionNode) currentBinopNode;
					
					if (curBinopNode.getBinaryOperatorType().priority < binopNode.getBinaryOperatorType().priority) {
						ExpressionASTNode prevRightChild = curBinopNode.getRightChild();
						binopNode.setLeftChild(prevRightChild);
						
						curBinopNode.setRightChild(binopNode);
					} else {
						BinaryOperatorExpressionNode topLevelExprNode = curBinopNode; 
						
						while (topLevelExprNode.getBinaryOperatorType().priority > binopNode.getBinaryOperatorType().priority && topLevelExprNode.getParentNode() != null)
							topLevelExprNode = (BinaryOperatorExpressionNode) topLevelExprNode.getParentNode();
						
						if (topLevelExprNode.getBinaryOperatorType().priority < binopNode.getBinaryOperatorType().priority) {
							ExpressionASTNode prevRightChild =  topLevelExprNode.getRightChild();
							binopNode.setLeftChild(prevRightChild);
							topLevelExprNode.setRightChild(binopNode);
						} else { 
							ASTNode parentNode = topLevelExprNode.getParentNode();
							binopNode.setLeftChild(topLevelExprNode);
							
							if (parentNode != null)
								binopNode.setParentNode(parentNode);
							
							if (rootOpNode == topLevelExprNode)
								rootOpNode = binopNode;
						}
					}
				}
				
				currentBinopNode = currentOpNode = binopNode;
			} else {
				switch (token.type) {
					case TRUE_BOOLEAN_LITERAL:
					case FALSE_BOOLEAN_LITERAL:
						currentNode = new BooleanValueExpressionNode(currentOpNode, "true".equals(token.text));
						break;
					case INTEGER_NUMERIC_LITERAL:
						currentNode = new IntegerValueExpressionNode(currentOpNode, Integer.parseInt(token.text));
						break;
					case REAL_NUMERIC_LITERAL:
						currentNode = new RealValueExpressionNode(currentOpNode, Float.parseFloat(token.text));
						break;
					case IDENTIFIER:
						String iden = token.text;
						
						switch (lexer.lookupToken().type) {
							case LEFT_PARENTHESIS:
								lexer.skipToken();
								
								RoutineCallExpressionNode exprNode = new RoutineCallExpressionNode(currentOpNode, iden);
								
								if (lexer.lookupToken().type != TokenType.RIGHT_PARENTHESIS)
									exprNode.addArguments(parseExpression());
								
								while (lexer.lookupToken().type != TokenType.RIGHT_PARENTHESIS) {
									skipToken(false, TokenType.COMMA_OPERATOR);
									exprNode.addArguments(parseExpression());
								}
								
								skipToken(false, TokenType.RIGHT_PARENTHESIS);
								currentNode = exprNode;
								break;
							default:
								currentNode = new VariableExpressionNode(currentOpNode, iden);
								break;
						}
						break;
					case PLUS_OPERATOR:
					case MINUS_OPERATOR:
					case NOT_OPERATOR:
						UnaryOperatorType unopType = UNOP_TYPE_BY_TOKEN_TYPE.get(token.type);
						currentOpNode = currentNode = new UnaryOperatorExpressionNode(currentOpNode, unopType, null);
						switchOpFlag = false;
						break;
					case LEFT_PARENTHESIS:
						ExpressionASTNode subexpr = parseExpressionNaive(nestingLevel + 1);
						skipToken(TokenType.RIGHT_PARENTHESIS);
						
						currentNode = subexpr;
						break;
					default:
						break;
				}
				
				ExpressionASTNode iterNode = currentNode;
				
				while (lexer.lookupToken().type.anyOf(
							TokenType.DOT_OPERATOR,
							TokenType.LEFT_BRACKET
						)) {
					switch (lexer.nextToken().type) {
						case DOT_OPERATOR: {
							String propName = skipToken(TokenType.IDENTIFIER).text;
							PropertyAccessExpressionNode exprNode = new PropertyAccessExpressionNode(currentOpNode, iterNode, propName);
							iterNode = exprNode;
							break;
						}
						case LEFT_BRACKET: {
							ExpressionASTNode indexExpr = parseExpressionNaive(nestingLevel + 1);
							skipToken(TokenType.RIGHT_BRACKET);
							
							BinaryOperatorExpressionNode exprNode = new BinaryOperatorExpressionNode(currentOpNode,
										BinaryOperatorType.ARR_ACC_BINOP,
										iterNode,
										indexExpr
									);
							iterNode = exprNode;
							break;
						}
						default:
							break;
					}
				}
				
				currentNode = iterNode;
			}
			
			if (rootNode == null)
				rootNode = currentNode;
			else if (rootOpNode == null)
				rootOpNode = currentOpNode;
			
			if (currentOpNode != null) {
				switch (currentOpNode.getExpressionNodeType()) {
					case UNOP_EXPR_NODE:
						if (currentOpNode != currentNode) {
							UnaryOperatorExpressionNode unopNode = (UnaryOperatorExpressionNode) currentOpNode;
							unopNode.setValue(currentNode);
						}
						
						break;
					case BINOP_EXPR_NODE:
						BinaryOperatorExpressionNode binopNode = (BinaryOperatorExpressionNode) currentOpNode;
						
						if (binopNode.getBinaryOperatorType() != BinaryOperatorType.PROP_ACC_BINOP)
							binopNode.setRightChild(currentNode);
						
						break;
					default:
						break;
				}
			}
			
			if (switchOpFlag)
				opFlag = !opFlag;
		}
		
		return Optional.ofNullable(rootOpNode).orElse(rootNode);
	}
	
	protected ASTNode parseStatement() {
		return null;
	}
	
	protected VariableDeclarationASTNode parseVarDecl() {
		skipToken(TokenType.VAR_KEYWORD);
		String varName = skipToken(TokenType.IDENTIFIER).text;
		Token tok = skipToken(TokenType.COLON_OPERATOR, TokenType.IS_KEYWORD);
		
		VariableDeclarationASTNode varDeclNode = null;
		
		VarType type = null;
		
		if (tok.type == TokenType.COLON_OPERATOR) {
			type = parseType();
			varDeclNode = new VariableDeclarationASTNode(null, type, varName);
			
			Token t = lexer.lookupToken();
			
			if (t.type == TokenType.IS_KEYWORD) {
				tok = t;
				lexer.skipToken();
			}
		}
		
		if (tok.type == TokenType.IS_KEYWORD) {
			ExpressionASTNode node = parseExpression();
			
			VarType exprType = calculateExprType(node);
			
			if (varDeclNode == null)
				varDeclNode = new VariableDeclarationASTNode(null, exprType, varName);
			
			VariableAssignmentASTNode varAssignNode = new VariableAssignmentASTNode(null, varName, node);
			varDeclNode.addChild(varAssignNode);
		}
		
		return varDeclNode;
	}
	
	protected TypeDeclarationASTNode parseTypeDecl() {
		skipToken(TokenType.TYPE_KEYWORD);
		String typealias = skipToken(TokenType.IDENTIFIER).text;
		skipToken(TokenType.IS_KEYWORD);
		
		return new TypeDeclarationASTNode(null, typealias, parseType());
	}
	
	public ASTNode parse() {
		ProgramASTNode astNode = new ProgramASTNode();
		
		while (!lexer.isEndReached()) {
			ASTNode newNode;
			Token tok = lexer.lookupToken(Predicate.not(LexUtils::isDelimeter));
			
			switch (tok.type) {
				case VAR_KEYWORD: {
					newNode = parseVarDecl();
					break;
				}
				case TYPE_KEYWORD: {
					newNode = parseTypeDecl();
					break;
				}
				case ROUTINE_KEYWORD: {
					newNode = parseRoutine();
					break;
				}
				case IDENTIFIER: {
					newNode = parseVarAssign();
					break;
				}
				case END_OF_TEXT:
				default:
					newNode = null;
					break;
			}
			
//			System.out.println(newNode);
			
			if (newNode != null)
				astNode.addChild(newNode);
		}

		return astNode;
	}
}
