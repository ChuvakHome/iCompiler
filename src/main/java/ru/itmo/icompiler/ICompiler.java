package ru.itmo.icompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import ru.itmo.icompiler.codegen.jvm.JVMBytecodeEntity;
import ru.itmo.icompiler.codegen.jvm.visitor.JVMCodeEmitterVisitor;
import ru.itmo.icompiler.codegen.jvm.visitor.JVMCodeEmitterVisitor.ExpressionVisitorContext;
import ru.itmo.icompiler.exception.CompilerException;
import ru.itmo.icompiler.lex.DFALexer;
import ru.itmo.icompiler.lex.LexUtils;
import ru.itmo.icompiler.lex.Lexer;
import ru.itmo.icompiler.semantic.SemanticContext;
import ru.itmo.icompiler.semantic.SemanticContext.Scope;
import ru.itmo.icompiler.semantic.visitor.CFGASTVisitor;
import ru.itmo.icompiler.semantic.visitor.SimpleASTVisitor;
import ru.itmo.icompiler.semantic.visitor.SimpleExpressionVisitor;
import ru.itmo.icompiler.semantic.visitor.TypealiasResolverASTVisitor;
import ru.itmo.icompiler.syntax.Parser;
import ru.itmo.icompiler.syntax.SimpleParser;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import ru.itmo.icompiler.syntax.exception.UnexpectedTokenSyntaxException.UnexpectedEndOfTextSyntaxException;

public class ICompiler {
	private String sourceName = "<source>";
	private String[] sourceLines;

	public List<CompilerException> getCompilerErrors() {
		return compilerErrors;
	}

	private List<CompilerException> compilerErrors = new ArrayList<>();
	
	private ASTNode parseResult;
	private Parser parser;
	
	public ICompiler(InputStream in) {
		StringBuilder sb = new StringBuilder();
		this.sourceLines = new BufferedReader(new InputStreamReader(in))
				.lines()
				.map(line -> line.replace("\t", LexUtils.tabToSpaces(DFALexer.DEFAULT_TAB_SIZE)))
				.peek(line -> sb
								.append(line)
								.append("\n")
					).toArray(String[]::new);
	}
	
	public ICompiler(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
		
		this.sourceName = file.getName();
	}
	
	public ASTNode parseProgram() {
		if (parseResult == null) {
			StringBuilder sb = new StringBuilder();
			
			Arrays.stream(sourceLines).forEachOrdered(line -> sb.append(line).append('\n'));
			
			Lexer lexer = new DFALexer(sb.toString());
			
			parser = new SimpleParser(lexer);
			
			parseResult = parser.parse(); 
			
			compilerErrors.addAll(parser.getParseErrors());
		}
		
		return parseResult; 
	}
	
	public void checkSemantic() {
		if (parseResult == null)
			return;
		
		SimpleExpressionVisitor exprVisitor = new SimpleExpressionVisitor();
		
		Scope globalScope = new Scope(null);
		
		TypealiasResolverASTVisitor resolver = new TypealiasResolverASTVisitor(exprVisitor);
		parseResult.accept(resolver, new SemanticContext(compilerErrors, globalScope));
		
		System.out.println("AFTER TYPEALIAS RESOLVING:\n" + parseResult.toString(0));
		
		globalScope.clear();
		SimpleASTVisitor vis = new SimpleASTVisitor(exprVisitor);
		parseResult.accept(vis, new SemanticContext(compilerErrors, globalScope));
		
		System.out.println("AFTER SEMANTIC RESOLVING:\n" + parseResult.toString(0));

		CFGASTVisitor checker = new CFGASTVisitor(exprVisitor);
		parseResult.accept(checker, new SemanticContext(compilerErrors, globalScope));
	}
	
	public void emitCode() {
		if (parseResult == null)
			return;
		
		JVMCodeEmitterVisitor codeEmitVisitor = new JVMCodeEmitterVisitor(sourceName);
		
		List<JVMBytecodeEntity> entities = parseResult.accept(codeEmitVisitor, new ExpressionVisitorContext());
		
		entities.forEach(System.out::println);
	}
	
	public void printCompilerErrors() {
		StringBuilder sb = new StringBuilder();
		
		compilerErrors.stream()
			.sorted((err1, err2) -> Integer.compare(err1.getErrorLine(), err2.getErrorLine()))
			.forEachOrdered(e -> {
				int line = e.getErrorLine() - 1;
				int spaces = e.getErrorOffset() - 1;
				
				if (e.getClass() == UnexpectedEndOfTextSyntaxException.class) {
					line = sourceLines.length - 1;
					spaces = sourceLines[sourceLines.length - 1].length();
				}
				
				sb.append(
					String.format(
						"%s:%d:%d: %s\n" +
						"%s\n" +
						"%s^\n",
						
						sourceName,
						e.getErrorLine(), e.getErrorOffset(), e.getMessage(),
						sourceLines[line],
						" ".repeat(spaces)
					)
				);
				
				if (e.getAdditionalLines() != null) {
					e.getAdditionalLines().forEach((note, lines) -> {
						sb.append(
							String.format(
								"note: %s:\n" + 
								"%s\n",
								
								note,
								String.join(
									"\n", 
									Arrays.stream(lines)
										.mapToObj(linenum -> String.format("%s:%d:%s", sourceName, linenum, sourceLines[linenum - 1])).toList()
								)
							)
						).append("\n");
					});
				}
			});
		
		System.err.println(sb.toString());
	}
	
	private static void processNode0(ASTNode node, int tabs) {
		Stack<ASTNode> nodes = new Stack<>();
		nodes.push(node);
		
		while (!nodes.isEmpty()) {
			ASTNode n = nodes.pop();
			
			System.out.printf("%s%s\n",
					" ".repeat(tabs * 4),
					n.toString(tabs)
				);
			
			List<ASTNode> children = n.getChildren();
			Collections.reverse(children);
			
			for (ASTNode child: children)
				nodes.push(child);
		}
	}
	
	public static void processNode(ASTNode node) {
		processNode0(node, 0);
	}
	
	public static void main(String[] args) throws IOException {
//		Lexer lexer = new DFALexer("x: integer, y, flag: boolean");
		
//		while (!lexer.isEndReached() && lexer.lookupToken().type != TokenType.COMMA_OPERATOR)
//			lexer.skipToken();
//		
//		System.exit(0);
		
		{	
			String s = ""
					 + "var x: integer is a.my_prop[1].new_prop.inner_prop.matrix[1][1] + 5 * 3; "
//					 + "var y is 1 + 2 > x and false\n"
//					 + "var y: integer is 1 + 2 > 0 and 6 * 4 = 24 or x < y + 5 * 10\n"
//					 + "var z is q and not -42. or flag\n"
//					 + "var t is a + b * c - x"
					 + "var u: boolean is (a + b < c or p * q = k) and ((x + y) * t + 1 /= u * w - * -g(1, 2, 10 - 5 * 3) or d) xor flag\n"
//					 + "routine f(x: integer, y: real, t, flag flf: boolean, arg: 0)\n"
					 + "\n\n\n"
					 + "routine g(x: integer): integer => x + 1\n"
//					 + "var x is true false\n"
					 + "routine ff(x: integer): integer => x op 0\n" 
					 + "routine true(x: integer): integer\n"
					 + "routine f(x: integer, y: real, flag: boolean, arg: real): integer is\n"
					 + "  if x > 0 then\n"
					 + "     var t is true\n"
					 + "  else "
					 + "	if y < 0 then"
					 + "	  return false\n"
					 + "    end\n"
					 + "  end\n"
					 + "  while loop\n"
					 + "    var t is true\n"
					 + "    if cond then break end\n"
					 + "    print 123, boolean\n"
					 + "  end\n"
					 + "  for i in 1..(10 + 5 - * 3) loop\n"
					 + "    print i, 123, boolean\n"
					 + "  end\n"
					 + "  return true\n"
					 + "end\n"
					 + "var y: boolean is true 0\n"
					 ;
			
			s = "type int is integer\n"
					+ "\n"
					+ "routine main() is\n"
					+ "  var a: int; a := 1\n"
					+ "  var b: int; b := 2\n"
					+ "  var c: int; c := 3\n"
					+ "  var переменная: int; переменная := 42\n"
					+ "  var flag0: boolean; flag0 := false\n"
					+ "  var flag1: boolean; flag1 := true\n"
					+ "  var f: real; f := 5000.4040004040\n"
					+ "  var вэрибл: real; вэрибл := .5\n"
					+ "  for i in 1..5 loop\n"
					+ "    print i;"
					+ "  end\n"
					+ "\n"
					+ "  print a, b, c\n"
					+ "end\n"
					+ "";
			
//			s = "routine f() is\n"
//			  + "  rec.prop := 1\n"
//			  + "  if x > 0 then\n"
//			  + "    print 1\n"
//			  + "  end\n"
//			  + "end";
			
			s = "routine bubble_sort(arr : array[] integer) is\n"
					+ "  while not is_sorted(arr)\n"
					+ "  loop\n"
					+ "    for i in 1..arr.length - 1\n"
					+ "    loop\n"
					+ "      if arr[i] > arr[i+1]\n"
					+ "      then\n"
					+ "        var tmp is arr[i]\n"
					+ "        arr[i] := arr[i+1]\n"
					+ "        arr[i+1] := tmp\n"
					+ "      end\n"
					+ "    end\n"
					+ "  end\n"
					+ "end";
			
			// ICompiler compiler = new ICompiler(new File("src/test/resources/sem/bad/experiment.ilang"));
			ICompiler compiler = new ICompiler(new File("src/test/resources/sem/test.ilang"));
//			ICompiler compiler = new ICompiler(new File("src/test/resources/sem/good/prog15.ilang"));
			
			ASTNode n = compiler.parseProgram();
			System.out.println(n.toString(0));
			
//			List<CompilerException> errors = new ArrayList<>();
//			
//			TypealiasResolverASTVisitor vis = new TypealiasResolverASTVisitor(new SimpleExpressionVisitor());
//			n.accept(vis, new SemanticContext(errors, new Scope(null)));
			
//			System.out.println(n.toString(0));
			
//			errors.forEach(System.out::println);
			
			compiler.checkSemantic();
			
//			if (!parser.getSyntaxErrors().isEmpty()) {
//				System.err.println();
//				parser.printErrors();
//			}
			
			compiler.printCompilerErrors();
			
			if (compiler.compilerErrors.isEmpty())
				compiler.emitCode();
			
			if (true)
				return;
		}
	
		String s = "type int is integer\n"
				+ "\n"
				+ "routine main() is\n"
				+ "  var a: int; a := 1\n"
				+ "  var b: int; b := 2\n"
				+ "  var c: int; c := 3\n"
				+ "  var переменная: int; переменная := 42\n"
				+ "  var flag0: boolean; flag0 := false\n"
				+ "  var flag1: boolean; flag1 := true\n"
				+ "  var f: real; f := 5000.4040004040\n"
				+ "  var вэрибл: real; вэрибл := .5\n"
				+ "  for i in 1..5 loop\n"
				+ "    print i"
				+ "  end\n"
				+ "\n"
				+ "  print a, b, c\n"
				+ "end\n"
				+ "";
	}
}
