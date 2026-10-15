package com.neovetta.rrgifts.core;

import java.util.List;

public record LootTable(List<TierDef> tiers) {
	public LootTable {
		tiers = List.copyOf(tiers);
	}
}
