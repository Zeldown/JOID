package dev.joid.test.snapshot;

import java.io.File;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotEntry {

	private final String         name;
	private final SnapshotStatus status;
	private final int            pixels;
	private final File           render;
	private final File           reference;

	public static @NonNull SnapshotEntry of(final @NonNull String name, final @NonNull SnapshotStatus status, final int pixels, final @NonNull File render, final @NonNull File reference) {
		return new SnapshotEntry(name, status, pixels, render, reference);
	}

	public static @NonNull SnapshotEntry compare(final @NonNull String name, final @NonNull SnapshotImage actual, final @NonNull File render, final @NonNull File reference, final int tolerance, final @NonNull String against, final @NonNull List<String> failures) {
		final SnapshotImage expected = SnapshotImage.read(reference);
		final SnapshotDifference difference = actual.compare(expected, tolerance);
		if (difference.getPixels() == 0) {
			return SnapshotEntry.of(name, SnapshotStatus.IDENTICAL, 0, render, reference);
		}

		if (actual.getWidth() != expected.getWidth() || actual.getHeight() != expected.getHeight()) {
			failures.add(name + ": " + actual.getWidth() + "x" + actual.getHeight() + " instead of the " + expected.getWidth() + "x" + expected.getHeight() + " of " + against);
		} else {
			failures.add(name + ": " + difference.getPixels() + " pixels differ from " + against + ", maximum channel delta " + difference.getMaximum());
		}
		return SnapshotEntry.of(name, SnapshotStatus.DIFFERENT, difference.getPixels(), render, reference);
	}

}