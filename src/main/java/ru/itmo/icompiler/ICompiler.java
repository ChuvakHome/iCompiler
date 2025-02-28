package ru.itmo.icompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Predicate;

import ru.itmo.icompiler.lex.DFALexer;
import ru.itmo.icompiler.lex.LexUtils;
import ru.itmo.icompiler.lex.Lexer;
import ru.itmo.icompiler.syntax.ASTNode;
import ru.itmo.icompiler.syntax.Parser;
import ru.itmo.icompiler.syntax.SimpleParser;

public class ICompiler {
	private static void processNode0(ASTNode node, int tabs) {
		System.out.println(node.toString(tabs));
		
		for (ASTNode child: node.getChildren())
			processNode0(child, tabs + 1);
	}
	
	public static void processNode(ASTNode node) {
		processNode0(node, 0);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		Lexer lexer = new DFALexer(new File("src/test/resources/lexer/good/prog1.ilang"));
		
		while (!lexer.isEndReached())
			System.out.println(lexer.nextToken(Predicate.not(LexUtils::isWhitespace)));
		
		System.exit(0);
		
		{
			String s = ""
					 + "var x: integer is a.my_prop[1].new_prop.inner_prop.matrix[1][1] + 5 * 3; x := x + a * b - c; "
					 + "var y: integer is 1 + 2 > 0 and 6 * 4 = 24 or x < y + 5 * 10\n"
//					 + "var z is q and not -42. or flag\n"
//					 + "var u is (a + b < c or p * q = k) and ((x + y) * t + 1 /= u * w - -g(1, 2, 10 - 5 * 3) or d) xor flag"
//					 + "routine f(x: integer, y: real, flag: boolean)"
					 ;
			
			Parser parser = new SimpleParser(s);
			
			ASTNode n = parser.parse();
			processNode(n);
			
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

//		Lexer lexer = new DFALexer(s);
//		
//		while (!lexer.isEndReached())
//			System.out.println(lexer.nextToken());
	}
}
