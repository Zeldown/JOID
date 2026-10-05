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
			return ClickType.LEFT;
		case 1:
			return ClickType.RIGHT;
		case 2:
			return ClickType.MIDDLE;
		case 3:
			return ClickType.BACK;
		case 4:
			return ClickType.FORWARD;
		default:
			return ClickType.OTHER;
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
		return this == ClickType.LEFT;
	}

	public boolean isRight() {
		return this == ClickType.RIGHT;
	}

	public boolean isMiddle() {
		return this == ClickType.MIDDLE;
	}

	public boolean isBack() {
		return this == ClickType.BACK;
	}

	public boolean isForward() {
		return this == ClickType.FORWARD;
	}

	public boolean isOther() {
		return this == ClickType.OTHER;
	}

}