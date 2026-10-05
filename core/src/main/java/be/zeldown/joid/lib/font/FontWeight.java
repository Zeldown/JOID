package be.zeldown.joid.lib.font;

import lombok.Getter;
import lombok.NonNull;

public enum FontWeight {

	THIN(100),
	EXTRA_LIGHT(200),
	LIGHT(300),
	REGULAR(400),
	MEDIUM(500),
	SEMI_BOLD(600),
	BOLD(700),
	EXTRA_BOLD(800),
	BLACK(900);

	@Getter
	private final int value;

	private FontWeight(final int value) {
		this.value = value;
	}

	public static @NonNull FontWeight of(final int value) {
		final FontWeight[] weights = FontWeight.values();
		return weights[Math.max(0, Math.min(weights.length - 1, Math.round(value / 100F) - 1))];
	}

}