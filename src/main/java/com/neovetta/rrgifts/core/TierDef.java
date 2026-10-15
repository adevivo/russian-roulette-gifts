package com.neovetta.rrgifts.core;

import java.util.List;

public record TierDef(String id, int weight, String message, List<WeightedOutcome> outcomes) {
	public TierDef {
		outcomes = List.copyOf(outcomes);
	}
}
