package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GiftRollerTest {
	@Test
	void tierDistributionMatchesDefaultLoot() {
		LootTable table = DefaultLoot.load();
		Random rng = new Random(2024);
		Map<String, Integer> counts = new HashMap<>();
		int n = 100_000;
		for (int i = 0; i < n; i++) {
			GiftRoller.RolledGift g = GiftRoller.roll(table, rng);
			assertTrue(g.tier().outcomes().stream().anyMatch(o -> o.outcome() == g.outcome()));
			counts.merge(g.tier().id(), 1, Integer::sum);
		}
		Map<String, Double> expected = Map.of("disaster", 20.0, "cursed", 20.0, "junk", 10.0, "good", 35.0, "jackpot", 15.0);
		expected.forEach((id, pct) -> assertEquals(pct, counts.get(id) * 100.0 / n, 1.0, id));
	}
}
