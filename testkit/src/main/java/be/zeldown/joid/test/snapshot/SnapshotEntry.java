package be.zeldown.joid.test.snapshot;

import java.io.File;

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

	public static @NonNull SnapshotEntry create(final @NonNull String name, final @NonNull SnapshotStatus status, final int pixels, final @NonNull File reference, final @NonNull File render) {
		return new SnapshotEntry(name, status, pixels, render, reference);
	}

}