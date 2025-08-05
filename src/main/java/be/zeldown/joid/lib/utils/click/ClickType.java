package be.zeldown.joid.lib.utils.click;

import lombok.NonNull;

public enum ClickType {

	LEFT,
	RIGHT,
	MIDDLE;

	public static @NonNull ClickType from(final int button) {
		switch (button) {
		case 0:
			return LEFT;
		case 1:
			return RIGHT;
		case 2:
			return MIDDLE;
		default:
			throw new IllegalArgumentException("Invalid mouse button: " + button);
		}
	}

	public int getButton() {
		switch (this) {
		case LEFT:
			return 0;
		case RIGHT:
			return 1;
		case MIDDLE:
			return 2;
		default:
			throw new IllegalStateException("Unexpected value: " + this);
		}
	}

	public boolean isLeft() {
		return this == LEFT;
	}

	public boolean isRight() {
		return this == RIGHT;
	}

	public boolean isMiddle() {
		return this == MIDDLE;
	}

}