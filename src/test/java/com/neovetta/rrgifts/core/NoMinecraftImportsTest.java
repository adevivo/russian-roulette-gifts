package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class NoMinecraftImportsTest {
	@Test
	void coreHasNoGameImports() throws IOException {
		Path dir = Path.of("src/main/java/com/neovetta/rrgifts/core");
		assertTrue(Files.isDirectory(dir), "run from project root: " + dir.toAbsolutePath());
		List<String> bad = new ArrayList<>();
		try (Stream<Path> files = Files.walk(dir)) {
			for (Path f : files.filter(p -> p.toString().endsWith(".java")).toList()) {
				int n = 0;
				for (String line : Files.readAllLines(f)) {
					n++;
					if (line.matches("\\s*import\\s+(static\\s+)?(net\\.minecraft|net\\.fabricmc|com\\.mojang)\\b.*")) {
						bad.add(f + ":" + n + ": " + line.trim());
					}
				}
			}
		}
		assertTrue(bad.isEmpty(), "Forbidden imports in core: " + bad);
	}
}
