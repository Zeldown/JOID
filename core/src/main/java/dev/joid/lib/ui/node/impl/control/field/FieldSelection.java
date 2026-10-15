package dev.joid.lib.ui.node.impl.control.field;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FieldSelection {

	private final int start;
	private final int end;

	public static @NonNull FieldSelection create(final int start, final int end) {
		return new FieldSelection(Math.min(start, end), Math.max(start, end));
	}

	public boolean isEmpty() {
		return this.start == this.end;
	}

}