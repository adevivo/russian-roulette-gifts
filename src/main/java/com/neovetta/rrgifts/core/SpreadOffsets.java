package com.neovetta.rrgifts.core;

import java.util.ArrayList;
import java.util.List;

/** Horizontal offsets that spread N things around an origin (sunflower spiral, so they never stack). */
public final class SpreadOffsets {
	public record Offset(double dx, double dz) {}

	private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));

	private SpreadOffsets() {}

	public static List<Offset> spiral(int n, double maxRadius) {
		List<Offset> out = new ArrayList<>(Math.max(n, 0));
		for (int i = 0; i < n; i++) {
			// i == 0 sits at the centre; the rest fill the disc evenly out to maxRadius.
			double r = n <= 1 ? 0.0 : maxRadius * Math.sqrt((double) i / (n - 1));
			double a = i * GOLDEN_ANGLE;
			out.add(new Offset(r * Math.cos(a), r * Math.sin(a)));
		}
		return out;
	}
}
