package com.neovetta.rrgifts.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/** Picks items proportionally to integer weights. */
public final class WeightedRoller<T> {
	private final List<T> items = new ArrayList<>();
	private final List<Long> cumulative = new ArrayList<>();
	private final long total;

	public WeightedRoller(List<Map.Entry<T, Integer>> entries) {
		long sum = 0;
		for (Map.Entry<T, Integer> e : entries) {
			int w = e.getValue();
			if (w < 0) {
				throw new IllegalArgumentException("Negative weight " + w + " for " + e.getKey());
			}
			if (w == 0) {
				continue;
			}
			sum += w;
			items.add(e.getKey());
			cumulative.add(sum);
		}
		if (sum <= 0) {
			throw new IllegalArgumentException("Total weight must be greater than 0");
		}
		this.total = sum;
	}

	public T roll(RandomGenerator rng) {
		long r = rng.nextLong(total);
		for (int i = 0; i < items.size(); i++) {
			if (r < cumulative.get(i)) {
				return items.get(i);
			}
		}
		throw new IllegalStateException("unreachable");
	}
}
