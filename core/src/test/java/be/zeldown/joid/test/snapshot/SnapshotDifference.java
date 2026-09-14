package be.zeldown.joid.test.snapshot;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotDifference {

	private final int           pixels;
	private final int           maximum;
	private final SnapshotImage image;

	public static @NonNull SnapshotDifference create(final int pixels, final int maximum, final @NonNull SnapshotImage image) {
		return new SnapshotDifference(pixels, maximum, image);
	}

}