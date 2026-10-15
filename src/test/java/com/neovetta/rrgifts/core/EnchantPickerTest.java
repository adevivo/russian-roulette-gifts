package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EnchantPickerTest {
	private static EnchantDef e(String id, int max, String... excl) {
		return new EnchantDef("minecraft:" + id, max, false, Set.of(excl));
	}

	private static final EnchantDef SHARP = e("sharpness", 5, "minecraft:smite");
	// smite does not list sharpness: symmetry must still hold
	private static final EnchantDef SMITE = e("smite", 5);
	private static final EnchantDef LOOT = e("looting", 3);
	private static final EnchantDef UNB = e("unbreaking", 3);
	private static final EnchantDef MEND = e("mending", 1);
	private static final EnchantDef CURSE = new EnchantDef("minecraft:vanishing_curse", 1, true, Set.of());
	private static final List<EnchantDef> ALL = List.of(SHARP, SMITE, LOOT, UNB, MEND, CURSE);

	@Test
	void conflictsNeverCoexist() {
		for (long seed = 0; seed < 2000; seed++) {
			Map<String, Integer> r = EnchantPicker.pickRandom(ALL, Map.of(), new Outcome.RandomEnchants(3, 5, 0), new Random(seed));
			assertFalse(r.containsKey("minecraft:sharpness") && r.containsKey("minecraft:smite"), "seed " + seed);
		}
	}

	@Test
	void conflictsWithExistingRespected() {
		for (long seed = 0; seed < 500; seed++) {
			Map<String, Integer> r = EnchantPicker.pickRandom(ALL, Map.of("minecraft:smite", 2),
					new Outcome.RandomEnchants(4, 4, 0), new Random(seed));
			assertFalse(r.containsKey("minecraft:sharpness"));
			assertEquals(2, r.get("minecraft:smite"));
		}
		for (long seed = 0; seed < 500; seed++) {
			Map<String, Integer> r = EnchantPicker.pickRandom(ALL, Map.of("minecraft:sharpness", 2),
					new Outcome.RandomEnchants(4, 4, 0), new Random(seed));
			assertFalse(r.containsKey("minecraft:smite"));
		}
	}

	@Test
	void cursesNeverPicked() {
		for (long seed = 0; seed < 500; seed++) {
			Map<String, Integer> r = EnchantPicker.pickRandom(ALL, Map.of(), new Outcome.RandomEnchants(5, 5, 0), new Random(seed));
			assertFalse(r.containsKey("minecraft:vanishing_curse"));
		}
	}

	@Test
	void levelsWithinBounds() {
		boolean sawOverVanilla = false;
		for (long seed = 0; seed < 1000; seed++) {
			Map<String, Integer> r = EnchantPicker.pickRandom(ALL, Map.of(), new Outcome.RandomEnchants(2, 4, 2), new Random(seed));
			for (var en : r.entrySet()) {
				EnchantDef d = ALL.stream().filter(x -> x.id().equals(en.getKey())).findFirst().orElseThrow();
				assertTrue(en.getValue() >= 1 && en.getValue() <= d.maxLevel() + 2);
				if (en.getValue() > d.maxLevel()) sawOverVanilla = true;
			}
		}
		assertTrue(sawOverVanilla);
		for (long seed = 0; seed < 500; seed++) {
			Map<String, Integer> r = EnchantPicker.pickRandom(ALL, Map.of(), new Outcome.RandomEnchants(2, 4, 0), new Random(seed));
			assertEquals(1, r.getOrDefault("minecraft:mending", 1));
		}
	}

	@Test
	void countWithinRangeAndCapped() {
		// candidates: one of sharpness/smite, looting, unbreaking, mending => at most 4 distinct
		boolean saw2 = false, saw4 = false;
		for (long seed = 0; seed < 1000; seed++) {
			int n = EnchantPicker.pickRandom(ALL, Map.of(), new Outcome.RandomEnchants(2, 4, 0), new Random(seed)).size();
			assertTrue(n >= 2 && n <= 4, "n=" + n);
			saw2 |= n == 2;
			saw4 |= n == 4;
		}
		assertTrue(saw2 && saw4);
		for (long seed = 0; seed < 100; seed++) {
			int n = EnchantPicker.pickRandom(ALL, Map.of(), new Outcome.RandomEnchants(8, 10, 0), new Random(seed)).size();
			assertEquals(4, n);
		}
		assertEquals(0, EnchantPicker.pickRandom(List.of(CURSE), Map.of(), new Outcome.RandomEnchants(1, 3, 0), new Random(1)).size());
		assertEquals(0, EnchantPicker.pickRandom(ALL, Map.of(), new Outcome.RandomEnchants(0, 0, 0), new Random(1)).size());
	}

	@Test
	void existingPreservedAndNotRepicked() {
		Map<String, Integer> existing = new LinkedHashMap<>();
		existing.put("minecraft:unbreaking", 9);
		existing.put("minecraft:custom_thing", 1);
		for (long seed = 0; seed < 300; seed++) {
			Map<String, Integer> r = EnchantPicker.pickRandom(ALL, existing, new Outcome.RandomEnchants(2, 2, 0), new Random(seed));
			assertEquals(9, r.get("minecraft:unbreaking"));
			assertEquals(1, r.get("minecraft:custom_thing"));
			assertEquals(4, r.size());
		}
		assertEquals(2, existing.size(), "input must not be mutated");
	}

	@Test
	void deterministicForSeed() {
		var spec = new Outcome.RandomEnchants(1, 4, 1);
		assertEquals(EnchantPicker.pickRandom(ALL, Map.of(), spec, new Random(77)).toString(),
				EnchantPicker.pickRandom(ALL, Map.of(), spec, new Random(77)).toString());
	}

	@Test
	void singleLevelEnchantsIgnoreOverMax() {
		for (long seed = 0; seed < 500; seed++) {
			Map<String, Integer> r = EnchantPicker.pickRandom(ALL, Map.of(), new Outcome.RandomEnchants(5, 5, 3), new Random(seed));
			assertEquals(1, r.getOrDefault("minecraft:mending", 1));
		}
	}
}
