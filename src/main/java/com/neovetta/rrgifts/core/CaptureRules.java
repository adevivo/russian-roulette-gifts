package com.neovetta.rrgifts.core;

/** Whether an Empty Gift Box may capture a given entity. Pure decision logic. */
public final class CaptureRules {
	public enum Result { OK, FULL, HOSTILE_DISABLED, NOT_HIT_RECENTLY, FORBIDDEN }

	private CaptureRules() {}

	public static Result check(boolean isPlayer, boolean isEnderDragonOrPart, boolean isMob, boolean isPassengerOrVehicle,
			boolean isHostile, boolean hostileAllowed, boolean hitByThisPlayerRecently, int currentCount, int max) {
		if (isPlayer || isEnderDragonOrPart || !isMob || isPassengerOrVehicle) return Result.FORBIDDEN;
		if (isHostile && !hostileAllowed) return Result.HOSTILE_DISABLED;
		if (!hitByThisPlayerRecently) return Result.NOT_HIT_RECENTLY;
		if (currentCount >= max) return Result.FULL;
		return Result.OK;
	}
}
