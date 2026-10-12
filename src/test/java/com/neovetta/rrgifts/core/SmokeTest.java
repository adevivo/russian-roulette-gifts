package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SmokeTest {
	@Test
	void giftNameMatchesProfileName() {
		assertEquals("MysteryGift", GiftConstants.GIFT_PROFILE_NAME);
	}
}
