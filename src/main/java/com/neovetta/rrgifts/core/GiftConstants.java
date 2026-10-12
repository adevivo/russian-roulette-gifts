package com.neovetta.rrgifts.core;

import java.util.UUID;

/** Pure-Java constants shared by the game layer and tests. No Minecraft imports. */
public final class GiftConstants {
	/** Profile id that marks a player head as a Mystery Gift. */
	public static final UUID GIFT_PROFILE_ID = UUID.fromString("5b7e2c1a-9d34-4f6e-8a21-c0ffee0b0b01");
	public static final String GIFT_PROFILE_NAME = "MysteryGift";

	private GiftConstants() {}
}
