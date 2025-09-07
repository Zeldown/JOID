package be.zeldown.joid.lib.utils.click;

import lombok.NonNull;

public enum ClickType {

	LEFT,
	RIGHT,
	MIDDLE,

	BACK,
	FORWARD,

	OTHER;

	public static @NonNull ClickType from(final int button) {
		switch (button) {
		case 0:
			return LEFT;
		case 1:
			return RIGHT;
		case 2:
			return MIDDLE;
		case 3:
			return BACK;
		case 4:
			return FORWARD;
		default:
			return OTHER;
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

	public boolean isLeft() {
		return this == LEFT;
	}

	public boolean isRight() {
		return this == RIGHT;
	}

	public boolean isMiddle() {
		return this == MIDDLE;
	}

	public boolean isBack() {
		return this == BACK;
	}

	public boolean isForward() {
		return this == FORWARD;
	}

	public boolean isOther() {
		return this == OTHER;
	}

}