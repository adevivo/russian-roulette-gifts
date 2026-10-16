package com.neovetta.rrgifts.core;

import java.util.List;

public record LootTable(List<TierDef> tiers, CaptureSettings capture) {
	public LootTable {
		tiers = List.copyOf(tiers);
		if (capture == null) capture = CaptureSettings.DEFAULT;
	}

	public LootTable(List<TierDef> tiers) {
		this(tiers, CaptureSettings.DEFAULT);
	}
}
