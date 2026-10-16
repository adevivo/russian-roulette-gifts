package com.neovetta.rrgifts.core;

import java.util.function.IntPredicate;

/** Pure search for where a dropped block should start falling from. */
public final class DropSearch {
	private DropSearch() {}

	/**
	 * Highest offset in [0, maxHeight] above the origin such that every offset from 0 up to it is air.
	 * Offset 0 is the origin itself, which is always treated as valid (the gift block was removed).
	 */
	public static int highestClear(int maxHeight, IntPredicate isAirAtOffset) {
		int best = 0;
		for (int h = 1; h <= maxHeight; h++) {
			if (!isAirAtOffset.test(h)) {
				break;
			}
			best = h;
		}
		return best;
	}
}
