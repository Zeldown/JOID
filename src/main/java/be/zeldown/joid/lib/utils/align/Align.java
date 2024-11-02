package be.zeldown.joid.lib.utils.align;

import lombok.NonNull;

public enum Align {

	START,
	CENTER,
	END;

	/* [ Utils Section ] */
	public boolean is(final @NonNull Align align) {
		return this == align;
	}

	public boolean isStart() {
		return this == START;
	}

	public boolean isLeft() {
		return this == START;
	}

	public boolean isCenter() {
		return this == CENTER;
	}

	public boolean isEnd() {
		return this == END;
	}

	public boolean isRight() {
		return this == END;
	}

}