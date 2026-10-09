package dev.joid.lib.input.mouse;

import lombok.NonNull;

public enum MouseButton {

	LEFT,
	RIGHT,
	MIDDLE,

	BACK,
	FORWARD,

	OTHER;

	public static @NonNull MouseButton from(final int button) {
		switch (button) {
		case 0:
			return MouseButton.LEFT;
		case 1:
			return MouseButton.RIGHT;
		case 2:
			return MouseButton.MIDDLE;
		case 3:
			return MouseButton.BACK;
		case 4:
			return MouseButton.FORWARD;
		default:
			return MouseButton.OTHER;
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
		case BACK:
			return 3;
		case FORWARD:
			return 4;
		default:
			return -1;
		}
	}

	public boolean isBack() {
		return this == MouseButton.BACK;
	}

	public boolean isLeft() {
		return this == MouseButton.LEFT;
	}

	public boolean isOther() {
		return this == MouseButton.OTHER;
	}

	public boolean isRight() {
		return this == MouseButton.RIGHT;
	}

	public boolean isMiddle() {
		return this == MouseButton.MIDDLE;
	}

	public boolean isForward() {
		return this == MouseButton.FORWARD;
	}

}