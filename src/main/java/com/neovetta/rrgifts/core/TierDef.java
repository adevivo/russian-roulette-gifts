package com.neovetta.rrgifts.core;

import java.util.List;

/** A loot tier. {@code bonus} outcomes are applied every time the tier is rolled, on top of the weighted pick. */
public record TierDef(String id, int weight, String message, List<WeightedOutcome> outcomes, List<Outcome> bonus) {
	public TierDef {
		outcomes = List.copyOf(outcomes);
		bonus = List.copyOf(bonus);
	}

	public TierDef(String id, int weight, String message, List<WeightedOutcome> outcomes) {
		this(id, weight, message, outcomes, List.of());
	}
}
