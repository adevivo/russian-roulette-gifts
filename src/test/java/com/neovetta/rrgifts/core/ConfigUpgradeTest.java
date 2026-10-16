package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfigUpgradeTest {
	private static final String OLD = "old-default";
	private static final String NEW = "new-default";
	private static final String EDITED = "edited";
	private static final String V020 = ConfigUpgrade.PRE_REFERENCE_DEFAULTS.iterator().next();

	@Test
	void missingConfigIsWritten() {
		assertEquals(ConfigUpgrade.WRITE, ConfigUpgrade.decide(null, null, NEW));
		assertEquals(ConfigUpgrade.WRITE, ConfigUpgrade.decide(null, OLD, NEW));
	}

	@Test
	void configMatchingBundledIsUpToDate() {
		assertEquals(ConfigUpgrade.UP_TO_DATE, ConfigUpgrade.decide(NEW, OLD, NEW));
		assertEquals(ConfigUpgrade.UP_TO_DATE, ConfigUpgrade.decide(NEW, null, NEW));
	}

	@Test
	void untouchedOldDefaultIsReplaced() {
		assertEquals(ConfigUpgrade.REPLACE, ConfigUpgrade.decide(OLD, OLD, NEW));
	}

	@Test
	void untouched020ConfigWithoutReferenceIsReplaced() {
		assertEquals(ConfigUpgrade.REPLACE, ConfigUpgrade.decide(V020, null, NEW));
	}

	@Test
	void editedConfigIsKept() {
		assertEquals(ConfigUpgrade.KEEP, ConfigUpgrade.decide(EDITED, NEW, NEW));
		assertEquals(ConfigUpgrade.KEEP_OUTDATED, ConfigUpgrade.decide(EDITED, OLD, NEW));
		// An edited 0.2.0-era config has no reference: we cannot prove it is current, so warn.
		assertEquals(ConfigUpgrade.KEEP_OUTDATED, ConfigUpgrade.decide(EDITED, null, NEW));
	}

	@Test
	void sha256IsHex() {
		assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", ConfigUpgrade.sha256("abc"));
	}
}
