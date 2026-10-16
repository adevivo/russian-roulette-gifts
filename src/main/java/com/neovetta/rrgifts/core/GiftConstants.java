package com.neovetta.rrgifts.core;

import java.util.UUID;

/** Pure-Java constants shared by the game layer and tests. No Minecraft imports. */
public final class GiftConstants {
	/** Profile id that marks a player head as a Mystery Gift. */
	public static final UUID GIFT_PROFILE_ID = UUID.fromString("5b7e2c1a-9d34-4f6e-8a21-c0ffee0b0b01");
	public static final String GIFT_PROFILE_NAME = "MysteryGift";

	/** Base64 "textures" property value; must match the recipe result exactly. */
	public static final String GIFT_TEXTURE_VALUE =
			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzU3ODdjZTExZTU0YWQxNWY3YmFjYTUwMzA5OTQ1N2Y5MWIxODhkY2ZmZmQyMDczMTlmMjZiMWJkNTBmNGU4MyJ9fX0=";
	public static final String GIFT_NAME = "Mystery Gift";
	public static final String GIFT_LORE = "Open at your own risk\u2026";

	private GiftConstants() {}
}
