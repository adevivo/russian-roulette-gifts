package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
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

class EmptyBoxRecipeTest {
	private static final String CONDITION = "russianroulettegifts:empty_gift_enabled";

	private static JsonObject load(String path) throws IOException {
		try (InputStream in = EmptyBoxRecipeTest.class.getResourceAsStream(path)) {
			return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}

	private static JsonObject recipe() throws IOException {
		return load("/data/russianroulettegifts/recipe/empty_gift_box.json");
	}

	private static void assertConditional(JsonObject o) {
		JsonArray conds = o.getAsJsonArray("fabric:load_conditions");
		assertEquals(1, conds.size());
		assertEquals(CONDITION, conds.get(0).getAsJsonObject().get("condition").getAsString());
	}

	@Test
	void recipeAndAdvancementCarryTheCondition() throws IOException {
		assertConditional(recipe());
		assertConditional(load("/data/russianroulettegifts/advancement/recipes/misc/empty_gift_box.json"));
	}

	@Test
	void shapeAndIngredients() throws IOException {
		JsonObject r = recipe();
		assertEquals("PSP", r.getAsJsonArray("pattern").get(0).getAsString());
		assertEquals("D D", r.getAsJsonArray("pattern").get(1).getAsString());
		assertEquals("PSP", r.getAsJsonArray("pattern").get(2).getAsString());
		JsonObject key = r.getAsJsonObject("key");
		for (String k : key.keySet()) assertTrue(key.get(k).isJsonPrimitive(), "ingredient " + k + " must be a plain string");
		assertEquals("#c:dyes", key.get("D").getAsString());
		assertEquals(3, key.size());
	}

	@Test
	void resultMatchesConstants() throws IOException {
		JsonObject comps = recipe().getAsJsonObject("result").getAsJsonObject("components");
		JsonObject profile = comps.getAsJsonObject("minecraft:profile");
		ByteBuffer b = ByteBuffer.allocate(16);
		for (JsonElement e : profile.getAsJsonArray("id")) b.putInt(e.getAsInt());
		b.flip();
		assertEquals(GiftConstants.EMPTY_BOX_PROFILE_ID, new UUID(b.getLong(), b.getLong()));
		assertEquals(GiftConstants.EMPTY_BOX_PROFILE_NAME, profile.get("name").getAsString());
		JsonObject prop = profile.getAsJsonArray("properties").get(0).getAsJsonObject();
		assertEquals("textures", prop.get("name").getAsString());
		assertEquals(GiftConstants.EMPTY_BOX_TEXTURE_VALUE, prop.get("value").getAsString());
		assertEquals(GiftConstants.EMPTY_BOX_NAME, comps.get("minecraft:custom_name").getAsString());
		assertEquals(GiftConstants.EMPTY_BOX_LORE, comps.getAsJsonArray("minecraft:lore").get(0).getAsString());
	}

	@Test
	void profileIdsAreDistinct() {
		assertTrue(!GiftConstants.EMPTY_BOX_PROFILE_ID.equals(GiftConstants.GIFT_PROFILE_ID));
	}

	@Test
	void advancementUnlocksEmptyBoxRecipe() throws IOException {
		JsonObject adv = load("/data/russianroulettegifts/advancement/recipes/misc/empty_gift_box.json");
		String id = "russianroulettegifts:empty_gift_box";
		assertEquals(id, adv.getAsJsonObject("rewards").getAsJsonArray("recipes").get(0).getAsString());
		assertEquals(id, adv.getAsJsonObject("criteria").getAsJsonObject("has_the_recipe")
				.getAsJsonObject("conditions").get("recipe").getAsString());
	}

	@Test
	void flagReaderDefaultsToTrue() {
		assertTrue(EmptyGiftFlag.read(new java.io.StringReader("{}")));
		assertTrue(EmptyGiftFlag.read(new java.io.StringReader("not json")));
		assertTrue(EmptyGiftFlag.read(new java.io.StringReader("[1]")));
		assertTrue(EmptyGiftFlag.read(new java.io.StringReader("{\"include_empty_gift_feature\":\"no\"}")));
		assertTrue(EmptyGiftFlag.read(new java.io.StringReader("{\"include_empty_gift_feature\":true}")));
		assertTrue(!EmptyGiftFlag.read(new java.io.StringReader("{\"include_empty_gift_feature\":false,\"tiers\":[]}")));
	}
}
