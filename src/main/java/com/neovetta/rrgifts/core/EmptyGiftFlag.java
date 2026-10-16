package com.neovetta.rrgifts.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.Reader;

/**
 * Reads just include_empty_gift_feature from the raw config text. Datapack recipes load before the mod's
 * own config does, so the recipe condition cannot use the full parser. Anything unexpected means true.
 */
public final class EmptyGiftFlag {
	private EmptyGiftFlag() {}

	public static boolean read(Reader reader) {
		try {
			JsonElement root = JsonParser.parseReader(reader);
			if (root == null || !root.isJsonObject()) return true;
			JsonElement flag = root.getAsJsonObject().get("include_empty_gift_feature");
			if (flag == null || !flag.isJsonPrimitive() || !flag.getAsJsonPrimitive().isBoolean()) return true;
			return flag.getAsBoolean();
		} catch (Exception e) {
			return true;
		}
	}
}
