package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TierFeedbackTest {
	@Test
	void knownTiers() {
		assertEquals(TierFeedback.DISASTER, TierFeedback.of("disaster"));
		assertEquals(TierFeedback.CURSED, TierFeedback.of("cursed"));
		assertEquals(TierFeedback.JUNK, TierFeedback.of("junk"));
		assertEquals(TierFeedback.GOOD, TierFeedback.of("good"));
		assertEquals(TierFeedback.JACKPOT, TierFeedback.of("jackpot"));
	}

	@Test
	void unknownTiersAreUnknown() {
		assertEquals(TierFeedback.UNKNOWN, TierFeedback.of("legendary"));
		assertEquals(TierFeedback.UNKNOWN, TierFeedback.of(null));
	}
}
