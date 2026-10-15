package com.neovetta.rrgifts.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/** Two-stage roll: tier by tier weight, then outcome by outcome weight. */
public final class GiftRoller {
	public record RolledGift(TierDef tier, Outcome outcome) {}

	private GiftRoller() {}

	public static RolledGift roll(LootTable table, RandomGenerator rng) {
		List<Map.Entry<TierDef, Integer>> tiers = new ArrayList<>();
		for (TierDef t : table.tiers()) {
			tiers.add(Map.entry(t, t.weight()));
		}
		TierDef tier = new WeightedRoller<>(tiers).roll(rng);
		List<Map.Entry<Outcome, Integer>> outs = new ArrayList<>();
		for (WeightedOutcome o : tier.outcomes()) {
			outs.add(Map.entry(o.outcome(), o.weight()));
		}
		return new RolledGift(tier, new WeightedRoller<>(outs).roll(rng));
	}
}
