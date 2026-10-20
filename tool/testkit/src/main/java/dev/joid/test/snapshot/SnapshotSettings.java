package dev.joid.test.snapshot;

import java.io.File;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotSettings {

	public static boolean isUpdate() {
		return Boolean.getBoolean("joid.snapshot.update");
	}

	public static boolean isRecord() {
		return Boolean.getBoolean("joid.snapshot.record");
	}

	public static @NonNull File getOutput() {
		return new File(System.getProperty("joid.snapshot.output", "build/snapshots/renders"));
	}

	public static @NonNull File getReferences() {
		return new File(System.getProperty("joid.snapshot.references", ".snapshots/references"));
	}

}