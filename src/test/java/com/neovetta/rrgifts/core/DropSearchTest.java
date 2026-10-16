package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DropSearchTest {
	@Test
	void openSkyReachesMaxHeight() {
		assertEquals(10, DropSearch.highestClear(10, h -> true));
	}

	@Test
	void ceilingStopsSearch() {
		assertEquals(2, DropSearch.highestClear(10, h -> h < 3));
	}

	@Test
	void blockedImmediatelyFallsBackToOrigin() {
		assertEquals(0, DropSearch.highestClear(10, h -> false));
	}

	@Test
	void gapAboveCeilingIsNotReached() {
		assertEquals(1, DropSearch.highestClear(10, h -> h != 2));
	}
}
