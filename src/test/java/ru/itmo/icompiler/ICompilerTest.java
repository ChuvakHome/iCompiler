package ru.itmo.icompiler;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.itmo.icompiler.syntax.ast.ASTNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static ru.itmo.icompiler.common.Common.getTestFiles;

class ICompilerTest {

    static Stream<Arguments> provideGoodTestCases() throws IOException {
        return getTestFiles("src/test/resources/sem/good");
    }

    static Stream<Arguments> provideBadTestCases() throws IOException {
        return getTestFiles("src/test/resources/sem/bad");
    }

    static Stream<Arguments> provideGoodOtherTestCases() throws IOException {
        return getTestFiles("src/test/resources/sem/good_other");
    }

    static Stream<Arguments> provideBadOtherTestCases() throws IOException {
        return getTestFiles("src/test/resources/sem/bad_other");
    }

    @ParameterizedTest
    @MethodSource("provideGoodTestCases")
    void testGood(URI file) throws FileNotFoundException {
        ICompiler compiler = new ICompiler(new File(file));

        ASTNode n = compiler.parseProgram();
        compiler.checkSemantic();

        assertEquals(new ArrayList<>(), compiler.getCompilerErrors());
    }

    @ParameterizedTest
    @MethodSource("provideBadTestCases")
    void testBad(URI file) throws FileNotFoundException {
        ICompiler compiler = new ICompiler(new File(file));

        ASTNode n = compiler.parseProgram();
        compiler.checkSemantic();
        assertNotEquals(new ArrayList<>(), compiler.getCompilerErrors());
    }


    @ParameterizedTest
    @MethodSource("provideGoodOtherTestCases")
    void testGoodOther(URI file) throws FileNotFoundException {
        ICompiler compiler = new ICompiler(new File(file));

        ASTNode n = compiler.parseProgram();
        compiler.checkSemantic();

        assertEquals(new ArrayList<>(), compiler.getCompilerErrors());
    }

    @ParameterizedTest
    @MethodSource("provideBadOtherTestCases")
    void testBadOther(URI file) throws FileNotFoundException {
        ICompiler compiler = new ICompiler(new File(file));

        ASTNode n = compiler.parseProgram();
        compiler.checkSemantic();
        assertNotEquals(new ArrayList<>(), compiler.getCompilerErrors());
    }
}