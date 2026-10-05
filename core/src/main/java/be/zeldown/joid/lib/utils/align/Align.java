package be.zeldown.joid.lib.utils.align;

import lombok.NonNull;

public enum Align {

	START,
	CENTER,
	END;

	public boolean is(final @NonNull Align align) {
		return this == align;
	}

	public boolean isStart() {
		return this == Align.START;
	}

	public boolean isLeft() {
		return this == Align.START;
	}

	public boolean isCenter() {
		return this == Align.CENTER;
	}

	public boolean isEnd() {
		return this == Align.END;
	}

	public boolean isRight() {
		return this == Align.END;
	}

}