package dev.joid.lib.input.mouse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MouseButton {

	LEFT(0),
	RIGHT(1),
	MIDDLE(2),

	BACK(3),
	FORWARD(4),

	OTHER(-1);

	private final int button;

	public static @NonNull MouseButton from(final int button) {
		for (final MouseButton value : MouseButton.values()) {
			if (value.button == button) {
				return value;
			}
		}
		return MouseButton.OTHER;
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