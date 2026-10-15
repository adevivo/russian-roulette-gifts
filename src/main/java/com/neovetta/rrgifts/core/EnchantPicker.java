package com.neovetta.rrgifts.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public final class EnchantPicker {
	private EnchantPicker() {}

	public static Map<String, Integer> pickRandom(List<EnchantDef> applicable, Map<String, Integer> existing,
			Outcome.RandomEnchants spec, RandomGenerator rng) {
		Map<String, Integer> result = new LinkedHashMap<>(existing);
		int target = spec.min() + rng.nextInt(spec.max() - spec.min() + 1);

		List<EnchantDef> chosen = new ArrayList<>();
		List<EnchantDef> pool = new ArrayList<>();
		List<EnchantDef> existingDefs = new ArrayList<>();
		for (EnchantDef d : applicable) {
			if (existing.containsKey(d.id())) {
				existingDefs.add(d);
			} else if (!d.curse()) {
				pool.add(d);
			}
		}
		// Existing enchants not in the applicable list still conflict by their own declared sets only if listed;
		// we can only check those we have definitions for.
		pool.removeIf(c -> existingDefs.stream().anyMatch(c::conflictsWith)
				|| existing.keySet().stream().anyMatch(id -> c.exclusiveWith().contains(id)));

		while (chosen.size() < target && !pool.isEmpty()) {
			EnchantDef pick = pool.remove(rng.nextInt(pool.size()));
			chosen.add(pick);
			pool.removeIf(pick::conflictsWith);
			// Over-max only applies to levelled enchants; "Mending III" would mean nothing.
			int cap = pick.maxLevel() == 1 ? 1 : pick.maxLevel() + spec.overMax();
			int level = 1 + rng.nextInt(cap);
			result.put(pick.id(), level);
		}
		return result;
	}
}
