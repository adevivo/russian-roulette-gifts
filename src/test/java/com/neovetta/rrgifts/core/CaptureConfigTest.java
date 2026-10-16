package com.neovetta.rrgifts.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import org.junit.jupiter.api.Test;

class CaptureConfigTest {
	private static final String TIERS = "\"tiers\":[{\"id\":\"t\",\"weight\":1,\"outcomes\":[{\"type\":\"explode\"}]}]";

	private static LootTable parse(String extra) {
		return LootConfigParser.parse(new StringReader("{\"version\":1," + extra + TIERS + "}"));
	}

	private static void rejects(String extra, String... fragments) {
		LootConfigException ex = assertThrows(LootConfigException.class, () -> parse(extra));
		for (String f : fragments) assertTrue(ex.getMessage().contains(f), ex.getMessage());
	}

	@Test
	void defaultsWhenAbsent() {
		assertEquals(new CaptureSettings(true, true, 30), parse("").capture());
	}

	@Test
	void valuesParsed() {
		CaptureSettings c = parse("\"include_empty_gift_feature\":false,\"hostile_mob_capture\":false,\"max_mobs_per_box\":5,").capture();
		assertEquals(new CaptureSettings(false, false, 5), c);
		assertEquals(64, parse("\"max_mobs_per_box\":64,").capture().maxMobs());
		assertEquals(1, parse("\"max_mobs_per_box\":1,").capture().maxMobs());
	}

	@Test
	void wrongTypesAndRangesRejectedWithPath() {
		rejects("\"include_empty_gift_feature\":\"yes\",", "include_empty_gift_feature", "true or false");
		rejects("\"hostile_mob_capture\":1,", "hostile_mob_capture", "true or false");
		rejects("\"max_mobs_per_box\":0,", "max_mobs_per_box", "out of range");
		rejects("\"max_mobs_per_box\":65,", "max_mobs_per_box", "out of range");
		rejects("\"max_mobs_per_box\":2.5,", "max_mobs_per_box", "integer");
		rejects("\"max_mobs_per_box\":\"3\",", "max_mobs_per_box", "integer");
	}

	@Test
	void bundledDefaultHasTheKeys() {
		assertEquals(CaptureSettings.DEFAULT, DefaultLoot.load().capture());
	}
}
