package com.neovetta.rrgifts.core;

import static com.neovetta.rrgifts.core.CaptureRules.Result.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class CaptureRulesTest {
	// player, dragon, mob, riding, hostile, hostileAllowed, hit, count, max
	private static CaptureRules.Result c(boolean player, boolean dragon, boolean mob, boolean riding, boolean hostile,
			boolean allowed, boolean hit, int count, int max) {
		return CaptureRules.check(player, dragon, mob, riding, hostile, allowed, hit, count, max);
	}

	@Test
	void okForHitPassiveMob() {
		assertEquals(OK, c(false, false, true, false, false, true, true, 0, 30));
		assertEquals(OK, c(false, false, true, false, false, true, true, 29, 30));
	}

	@Test
	void forbiddenCases() {
		assertEquals(FORBIDDEN, c(true, false, true, false, false, true, true, 0, 30));
		assertEquals(FORBIDDEN, c(false, true, true, false, true, true, true, 0, 30));
		assertEquals(FORBIDDEN, c(false, false, false, false, false, true, true, 0, 30));
		assertEquals(FORBIDDEN, c(false, false, true, true, false, true, true, 0, 30));
	}

	@Test
	void forbiddenBeatsOtherReasons() {
		assertEquals(FORBIDDEN, c(false, true, true, false, true, false, false, 99, 30));
	}

	@Test
	void hostileDisabled() {
		assertEquals(HOSTILE_DISABLED, c(false, false, true, false, true, false, true, 0, 30));
		assertEquals(OK, c(false, false, true, false, true, true, true, 0, 30));
		assertEquals(OK, c(false, false, true, false, false, false, true, 0, 30));
	}

	@Test
	void mustBeHitRecently() {
		assertEquals(NOT_HIT_RECENTLY, c(false, false, true, false, false, true, false, 0, 30));
	}

	@Test
	void fullAtMax() {
		assertEquals(FULL, c(false, false, true, false, false, true, true, 30, 30));
		assertEquals(FULL, c(false, false, true, false, false, true, true, 31, 30));
		assertEquals(OK, c(false, false, true, false, false, true, true, 0, 1));
		assertEquals(FULL, c(false, false, true, false, false, true, true, 1, 1));
	}

	@Test
	void spreadOffsetsStayInRadiusAndAreDistinct() {
		for (int n : new int[] {1, 2, 5, 30, 64}) {
			List<SpreadOffsets.Offset> o = SpreadOffsets.spiral(n, 3.0);
			assertEquals(n, o.size());
			assertEquals(n, new HashSet<>(o).size());
			for (var off : o) assertTrue(Math.hypot(off.dx(), off.dz()) <= 3.0 + 1e-9);
		}
		assertEquals(0, SpreadOffsets.spiral(0, 3.0).size());
		assertEquals(new SpreadOffsets.Offset(0, 0), SpreadOffsets.spiral(1, 3.0).get(0));
	}
}
