package be.zeldown.joid.test.snapshot;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotDifference {

	private final int pixels;
	private final int maximum;

	public static @NonNull SnapshotDifference create(final int pixels, final int maximum) {
		return new SnapshotDifference(pixels, maximum);
	}

}