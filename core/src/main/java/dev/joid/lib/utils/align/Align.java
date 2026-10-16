package dev.joid.lib.utils.align;

public enum Align {

	START,
	CENTER,
	END;

	public boolean isEnd() {
		return this == Align.END;
	}

	public boolean isStart() {
		return this == Align.START;
	}

	public boolean isCenter() {
		return this == Align.CENTER;
	}
}