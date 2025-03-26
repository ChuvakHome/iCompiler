package ru.itmo.icompiler;

import java.io.FileNotFoundException;

import ru.itmo.icompiler.syntax.Parser;
import ru.itmo.icompiler.syntax.SimpleParser;
import ru.itmo.icompiler.syntax.ast.ASTNode;

public class ICompiler {
	private static void processNode0(ASTNode node, int tabs) {
		System.out.printf("%s%s\n",
				" ".repeat(tabs * 4),
				node.toString(tabs)
			);
		
		for (ASTNode child: node.getChildren())
			processNode0(child, tabs + 1);
	}
	
	public static void processNode(ASTNode node) {
		processNode0(node, 0);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
//		Lexer lexer = new DFALexer("5 + 1");
//		
//		while (!lexer.isEndReached())
//			System.out.println(lexer.nextToken());
//		
//		System.exit(0);
		
		{
			String s = ""
//					 + "var x: integer is a.my_prop[1].new_prop.inner_prop.matrix[1][1] + 5 * 3; "
//					 + "var y is 1 + 2 > x and false\n"
//					 + "var y: integer is 1 + 2 > 0 and 6 * 4 = 24 or x < y + 5 * 10\n"
//					 + "var z is q and not -42. or flag\n"
//					 + "var t is a + b * c - x"
					 + "var u: boolean is (a + b < c or p * q = k) and ((x + y) * t + 1 /= u * w - * -g(1, 2, 10 - 5 * 3) or d) xor flag\n"
					 + "routine f(x: integer, y: real, t, flag: boolean)\n"
					 + "var x is true"
					 ;
			
			Parser parser = new SimpleParser(s);
			
			ASTNode n = parser.parse();
			processNode(n);
			
//			parser.getSyntaxErrors().forEach(System.err::println);
			parser.printErrors();
			
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
