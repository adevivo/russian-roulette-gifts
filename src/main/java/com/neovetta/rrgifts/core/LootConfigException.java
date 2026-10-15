package com.neovetta.rrgifts.core;

/** Thrown for any invalid loot config; the message starts with a JSON path. */
public class LootConfigException extends RuntimeException {
	public LootConfigException(String message) {
		super(message);
	}

	public LootConfigException(String message, Throwable cause) {
		super(message, cause);
	}
}
