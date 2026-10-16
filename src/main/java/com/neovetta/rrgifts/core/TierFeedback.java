package com.neovetta.rrgifts.core;

/** Maps a tier id to a feedback category; the game layer picks colours, sounds and particles per kind. */
public enum TierFeedback {
	DISASTER, CURSED, JUNK, GOOD, JACKPOT, UNKNOWN;

	public static TierFeedback of(String tierId) {
		if (tierId == null) return UNKNOWN;
		return switch (tierId) {
			case "disaster" -> DISASTER;
			case "cursed" -> CURSED;
			case "junk" -> JUNK;
			case "good" -> GOOD;
			case "jackpot" -> JACKPOT;
			default -> UNKNOWN;
		};
	}
}
