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

	/** Profile id that marks a player head as an Empty Gift Box. */
	public static final UUID EMPTY_BOX_PROFILE_ID = UUID.fromString("c01175dd-5457-477e-bb76-4751d52e57fc");
	public static final String EMPTY_BOX_PROFILE_NAME = "EmptyGiftBox";
	/** Pale pink box with a lavender ribbon (textures.minecraft.net/texture/cffa4cf8…dde4), distinct from the red gift. */
	public static final String EMPTY_BOX_TEXTURE_VALUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2ZmYTRjZjhhZjdkYmJjMzM2ODUxYWY5NWJlNDUwMTJiMWFiOTYwNmIzNGZlZmNkMDk2MTRmZTRhNDQ3ZGRlNCJ9fX0=";
	public static final String EMPTY_BOX_NAME = "Empty Gift Box";
	public static final String EMPTY_BOX_LORE = "Hit a mob, then right-click it to box it up.";
	/** Extra profile property that carries a filled box's storage id. */
	public static final String BOX_ID_PROPERTY = "rrgifts_box";

	private GiftConstants() {}
}
