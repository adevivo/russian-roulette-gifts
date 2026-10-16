package com.neovetta.rrgifts.game;

import com.neovetta.rrgifts.RussianRouletteGifts;
import com.neovetta.rrgifts.core.ConfigUpgrade;
import com.neovetta.rrgifts.core.DefaultLoot;
import com.neovetta.rrgifts.core.LootConfigParser;
import com.neovetta.rrgifts.core.LootTable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

/** Loads config/russian-roulette-gifts.json, validates it against the live registries, never crashes. */
public final class ConfigLoader {
	public static final String FILE_NAME = "russian-roulette-gifts.json";
	/** Copy of the bundled default as last written; tells an untouched config from an edited one. */
	public static final String REFERENCE_NAME = "russian-roulette-gifts.default.json";
	private static volatile LootTable current;

	private ConfigLoader() {}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> RussianRouletteGifts.LOGGER.info(reload(server)));
	}

	public static LootTable current() {
		LootTable t = current;
		if (t == null) {
			t = DefaultLoot.load();
			current = t;
		}
		return t;
	}

	public static String reload(MinecraftServer server) {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		try {
			upgrade(path, path.resolveSibling(REFERENCE_NAME));
			LootTable table;
			try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				table = LootConfigParser.parse(r);
			}
			LootValidator.validate(table, server.registryAccess());
			current = table;
			return "Loaded loot config from " + path + " (" + table.tiers().size() + " tiers)";
		} catch (Exception e) {
			RussianRouletteGifts.LOGGER.error("Invalid loot config {}: {}. Falling back to bundled defaults.",
					path, e.getMessage());
			return useDefaults(server, e.getMessage());
		}
	}

	private static void upgrade(Path path, Path reference) throws IOException {
		byte[] bundled;
		try (InputStream in = DefaultLoot.class.getResourceAsStream(DefaultLoot.RESOURCE)) {
			if (in == null) throw new IOException("bundled default_loot.json missing");
			bundled = in.readAllBytes();
		}
		String configSha = Files.exists(path) ? ConfigUpgrade.sha256(Files.readAllBytes(path)) : null;
		String referenceSha = Files.exists(reference) ? ConfigUpgrade.sha256(Files.readAllBytes(reference)) : null;
		ConfigUpgrade action = ConfigUpgrade.decide(configSha, referenceSha, ConfigUpgrade.sha256(bundled));
		Files.createDirectories(path.getParent());
		switch (action) {
			case WRITE -> {
				Files.write(path, bundled);
				RussianRouletteGifts.LOGGER.info("Wrote default loot config to {}", path);
			}
			case REPLACE -> {
				Files.write(path, bundled);
				RussianRouletteGifts.LOGGER.info("Loot config {} was an unedited older default; updated it to this version's default", path);
			}
			case KEEP_OUTDATED -> RussianRouletteGifts.LOGGER.warn(
					"Your edited loot config {} is based on an older default. Keeping your edits. Compare it with {} to pick up new loot.",
					path, reference);
			case UP_TO_DATE, KEEP -> { }
		}
		Files.write(reference, bundled);
	}

	private static String useDefaults(MinecraftServer server, String reason) {
		try {
			LootTable def = DefaultLoot.load();
			try {
				LootValidator.validate(def, server.registryAccess());
			} catch (Exception e) {
				RussianRouletteGifts.LOGGER.error("BUNDLED DEFAULT LOOT FAILED VALIDATION: {}", e.getMessage());
			}
			current = def;
			return "Config error: " + reason + " -- using bundled default loot.";
		} catch (Exception e) {
			RussianRouletteGifts.LOGGER.error("Bundled default loot unusable", e);
			return "Config error: " + reason + " -- and the bundled defaults failed to load: " + e.getMessage();
		}
	}
}
