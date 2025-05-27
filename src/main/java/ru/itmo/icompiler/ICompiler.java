package ru.itmo.icompiler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import jasmin.ClassFile;
import ru.itmo.icompiler.codegen.jvm.JVMBytecodeClass;
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
		
		globalScope.clear();
		SimpleASTVisitor vis = new SimpleASTVisitor(exprVisitor);
		parseResult.accept(vis, new SemanticContext(compilerErrors, globalScope));

		CFGASTVisitor checker = new CFGASTVisitor(exprVisitor);
		parseResult.accept(checker, new SemanticContext(compilerErrors, globalScope));
	}
	
	public List<JVMBytecodeEntity> emitCode() {
		if (parseResult == null)
			return null;
		
		JVMCodeEmitterVisitor codeEmitVisitor = new JVMCodeEmitterVisitor(sourceName);
		
		return parseResult.accept(codeEmitVisitor, new ExpressionVisitorContext());
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
	
	private static String replaceFileExtension(String originalFile, String extension) {
		int index = originalFile.indexOf('.');
		
		if (index >= 0)
			originalFile = originalFile.substring(0, index);
		
		return originalFile + "." + extension;
	}
	
	public static void main(String[] args) throws IOException {
		File inputProgram = new File(args[0]);
		File outputFile = new File(
					replaceFileExtension(
						inputProgram.getName(),
						"jar"
					)
				);
		
		if (!inputProgram.exists()) {
			System.err.printf("No such file '%s'.", inputProgram.getPath());
			
			System.exit(-1);
		} else if (!inputProgram.canRead()) {
			System.err.printf("Permissions denied for file '%s'.", inputProgram.getPath());
			
			System.exit(-2);
		}
		
		ICompiler compiler = new ICompiler(inputProgram);
		
		compiler.parseProgram();
		compiler.checkSemantic();
		
		if (!compiler.compilerErrors.isEmpty()) {
			compiler.printCompilerErrors();
			
			System.exit(-3);
		}
		
		List<JVMBytecodeEntity> entities = compiler.emitCode();
		File classFilesTmpDir = Files.createTempDirectory(null).toFile();
		File ilangPackageDir = new File(classFilesTmpDir, "ilang");
		ilangPackageDir.mkdir();
		
		class ClassFileEntity {
			private final String name;
			private final byte[] data;
			
			public ClassFileEntity(String name, byte[] data) {
				this.name = name;
				this.data = data;
			}
		}
		
		List<ClassFileEntity> emittedClassFiles = entities.stream().map(jvmEntity -> {
			JVMBytecodeClass jvmClass = (JVMBytecodeClass) jvmEntity;
			
			ClassFileEntity classFile = null;
			
			try {
				ClassFile jasminClassFile = new ClassFile();
				jasminClassFile.readJasmin(
					new BufferedReader(
						new CharArrayReader(
							jvmClass.toString().toCharArray()
						)
					), 
					jvmClass.getSourceName(), 
					false
				);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				jasminClassFile.write(baos);
				
				classFile = new ClassFileEntity(jvmClass.getClassName(), baos.toByteArray());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return classFile;
		}).toList();
		
		String manifestContent = "Manifest-Version: 1.0\r\nMain-Class: ilang.Program\r\n";
		Manifest man = new Manifest(new ByteArrayInputStream(manifestContent.getBytes()));
		
		try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputFile), man)) {
			emittedClassFiles.forEach(classFile -> {
				ZipEntry e = new ZipEntry(classFile.name + ".class");
				
				try {
					jos.putNextEntry(e);
					
					OutputStream out = new BufferedOutputStream(jos);
					out.write(classFile.data);
					out.flush();
					
					jos.closeEntry();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
		}
	}
}
