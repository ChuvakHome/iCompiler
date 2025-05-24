package ru.itmo.icompiler.common;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class Common {
    public static Stream<Arguments> getTestFiles(String testsPath) throws IOException {
        Path resourceDir = Paths.get(testsPath);
        return Files.list(resourceDir)
                .filter(Files::isRegularFile)
                .sorted((p1, p2) -> {
                    int comp = p1.getFileName().toString().length() - p2.getFileName().toString().length();
                    if (comp == 0) {
                        return p1.getFileName().toString().compareTo(p2.getFileName().toString());
                    } else {
                        return comp;
                    }
                })
                .map(path -> Arguments.of(Named.of(path.getFileName().toString(), path.toUri())));
    }
}
