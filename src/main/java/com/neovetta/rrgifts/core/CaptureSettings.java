package com.neovetta.rrgifts.core;

/** Empty Gift Box settings; all optional in the config file. */
public record CaptureSettings(boolean enabled, boolean hostileCapture, int maxMobs) {
	public static final int DEFAULT_MAX_MOBS = 30;
	public static final CaptureSettings DEFAULT = new CaptureSettings(true, true, DEFAULT_MAX_MOBS);
}
