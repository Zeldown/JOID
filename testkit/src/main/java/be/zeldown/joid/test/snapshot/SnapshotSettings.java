package be.zeldown.joid.test.snapshot;

import java.io.File;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SnapshotSettings {

	public static boolean isUpdate() {
		return Boolean.getBoolean("joid.snapshot.update");
	}

	public static @NonNull File getCache() {
		return new File(System.getProperty("joid.snapshot.cache", ".snapshots/cache"));
	}

	public static @NonNull File getOutput() {
		return new File(System.getProperty("joid.snapshot.output", "build/snapshots/renders"));
	}

	public static @NonNull File getReferences() {
		return new File(System.getProperty("joid.snapshot.references", ".snapshots/references"));
	}

}