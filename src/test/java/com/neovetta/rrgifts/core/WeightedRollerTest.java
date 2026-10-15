package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class WeightedRollerTest {
	private static RandomGenerator rng(long seed) {
		return new java.util.Random(seed);
	}

	@Test
	void distributionMatchesWeights() {
		WeightedRoller<String> r = new WeightedRoller<>(List.of(Map.entry("a", 1), Map.entry("b", 3), Map.entry("c", 6)));
		RandomGenerator g = rng(42);
		Map<String, Integer> counts = new HashMap<>();
		int n = 100_000;
		for (int i = 0; i < n; i++) counts.merge(r.roll(g), 1, Integer::sum);
		assertEquals(10.0, counts.get("a") * 100.0 / n, 1.0);
		assertEquals(30.0, counts.get("b") * 100.0 / n, 1.0);
		assertEquals(60.0, counts.get("c") * 100.0 / n, 1.0);
	}

	@Test
	void zeroWeightNeverPicked() {
		WeightedRoller<String> r = new WeightedRoller<>(List.of(Map.entry("a", 0), Map.entry("b", 1), Map.entry("c", 0)));
		RandomGenerator g = rng(1);
		for (int i = 0; i < 10_000; i++) assertEquals("b", r.roll(g));
	}

	@Test
	void rejectsNegativeWeight() {
		var ex = assertThrows(IllegalArgumentException.class,
				() -> new WeightedRoller<>(List.of(Map.entry("a", 1), Map.entry("b", -2))));
		assertTrue(ex.getMessage().contains("-2"));
	}

	@Test
	void rejectsAllZeroAndEmpty() {
		assertThrows(IllegalArgumentException.class, () -> new WeightedRoller<>(List.of(Map.entry("a", 0))));
		assertThrows(IllegalArgumentException.class, () -> new WeightedRoller<String>(List.of()));
	}

	@Test
	void singleEntryAlwaysReturned() {
		WeightedRoller<String> r = new WeightedRoller<>(List.of(Map.entry("only", 7)));
		RandomGenerator g = rng(3);
		for (int i = 0; i < 1000; i++) assertEquals("only", r.roll(g));
	}

	@Test
	void sameSeedSameSequence() {
		WeightedRoller<Integer> r = new WeightedRoller<>(List.of(Map.entry(1, 1), Map.entry(2, 2), Map.entry(3, 3)));
		List<Integer> a = new ArrayList<>(), b = new ArrayList<>();
		RandomGenerator g1 = rng(99), g2 = rng(99);
		for (int i = 0; i < 200; i++) {
			a.add(r.roll(g1));
			b.add(r.roll(g2));
		}
		assertEquals(a, b);
	}
}
