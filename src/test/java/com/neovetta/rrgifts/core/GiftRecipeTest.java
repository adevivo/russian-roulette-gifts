package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GiftRecipeTest {
	private static JsonObject loadRecipe() throws IOException {
		try (InputStream in = GiftRecipeTest.class.getResourceAsStream(
				"/data/russianroulettegifts/recipe/mystery_gift.json")) {
			return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}

	private static UUID fromIntArray(com.google.gson.JsonArray a) {
		ByteBuffer b = ByteBuffer.allocate(16);
		for (JsonElement e : a) b.putInt(e.getAsInt());
		b.flip();
		return new UUID(b.getLong(), b.getLong());
	}

	@Test
	void resultProfileIdMatchesConstant() throws IOException {
		JsonObject profile = loadRecipe().getAsJsonObject("result").getAsJsonObject("components")
				.getAsJsonObject("minecraft:profile");
		assertEquals(GiftConstants.GIFT_PROFILE_ID, fromIntArray(profile.getAsJsonArray("id")));
		assertEquals(GiftConstants.GIFT_PROFILE_NAME, profile.get("name").getAsString());
	}

	@Test
	void ingredientsUseStringFormat() throws IOException {
		JsonObject key = loadRecipe().getAsJsonObject("key");
		for (String k : key.keySet()) {
			assertTrue(key.get(k).isJsonPrimitive(), "ingredient " + k + " must be a plain string");
		}
		assertEquals("#c:dyes", key.get("D").getAsString());
	}
}
