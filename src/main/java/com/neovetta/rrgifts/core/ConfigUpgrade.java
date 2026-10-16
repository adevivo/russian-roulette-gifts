package com.neovetta.rrgifts.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

/**
 * Decides what to do with an existing loot config when the bundled default may have changed.
 * The game layer keeps a reference copy of the default it last wrote; comparing the config against
 * that copy tells an untouched config (safe to replace) from one the admin edited (keep, warn).
 */
public enum ConfigUpgrade {
	/** No config yet: write the bundled default. */
	WRITE,
	/** Config already equals the bundled default. */
	UP_TO_DATE,
	/** Config is an unedited older default: replace it with the bundled one. */
	REPLACE,
	/** Config was edited, and was based on the current default: keep it quietly. */
	KEEP,
	/** Config was edited, and the bundled default has changed since: keep it, but warn. */
	KEEP_OUTDATED;

	/** SHA-256 of default_loot.json as shipped in 0.2.0, which wrote no reference copy. */
	public static final Set<String> PRE_REFERENCE_DEFAULTS =
			Set.of("67ba4aacd45a123227928a1cbd2b43390feac18e6c8513f9d02db39ba553bd1f");

	/**
	 * @param configSha    hash of the current config file, or null if it does not exist
	 * @param referenceSha hash of the reference copy written last time, or null if there is none
	 * @param bundledSha   hash of the default bundled in this jar
	 */
	public static ConfigUpgrade decide(String configSha, String referenceSha, String bundledSha) {
		if (configSha == null) return WRITE;
		if (configSha.equals(bundledSha)) return UP_TO_DATE;
		boolean untouched = configSha.equals(referenceSha)
				|| (referenceSha == null && PRE_REFERENCE_DEFAULTS.contains(configSha));
		if (untouched) return REPLACE;
		return bundledSha.equals(referenceSha) ? KEEP : KEEP_OUTDATED;
	}

	public static String sha256(byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	public static String sha256(String s) {
		return sha256(s.getBytes(StandardCharsets.UTF_8));
	}
}
