package com.neovetta.rrgifts.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/** Loads the bundled default loot table from the classpath. */
public final class DefaultLoot {
	public static final String RESOURCE = "/russianroulettegifts/default_loot.json";

	private DefaultLoot() {}

	public static LootTable load() {
		try (InputStream in = DefaultLoot.class.getResourceAsStream(RESOURCE)) {
			if (in == null) {
				throw new LootConfigException("Bundled default loot missing: " + RESOURCE);
			}
			try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
				return LootConfigParser.parse(r);
			}
		} catch (IOException e) {
			throw new LootConfigException("Cannot read bundled default loot: " + e.getMessage(), e);
		}
	}
}
