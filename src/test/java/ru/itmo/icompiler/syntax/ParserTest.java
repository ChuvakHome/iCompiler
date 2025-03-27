package ru.itmo.icompiler.syntax;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import ru.itmo.icompiler.syntax.ast.ASTNode;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static ru.itmo.icompiler.common.Common.getTestFiles;

class ParserTest {

    @Test
    void parse() {
        String s = "var x is 42";

        Parser parser = new SimpleParser(s);
        parser.parse();

        assertEquals(new ArrayList<>(), parser.getSyntaxErrors());
    }

    static Stream<Arguments> provideGoodTestCases() throws IOException {
        return getTestFiles("src/test/resources/parser/good");
    }

    static Stream<Arguments> provideBadTestCases() throws IOException {
        return getTestFiles("src/test/resources/parser/bad");
    }

    @ParameterizedTest
    @MethodSource("provideGoodTestCases")
    void parseGood(String fileName, String content) {
        Parser parser = new SimpleParser(content);
        parser.parse();

        assertEquals(new ArrayList<>(), parser.getSyntaxErrors());
    }

    @ParameterizedTest
    @MethodSource("provideBadTestCases")
    void parseBad(String fileName, String content) {
        Parser parser = new SimpleParser(content);
        parser.parse();

        assertNotEquals(new ArrayList<>(), parser.getSyntaxErrors());
    }
}