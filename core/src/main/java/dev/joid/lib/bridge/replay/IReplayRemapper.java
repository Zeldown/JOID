package dev.joid.lib.bridge.replay;

import dev.joid.lib.bridge.IBridge;
import lombok.NonNull;

public interface IReplayRemapper extends IBridge {

	public default @NonNull String mapClass(final @NonNull String name) {
		return name;
	}

	public default @NonNull String mapField(final @NonNull String owner, final @NonNull String name, final @NonNull String descriptor) {
		return name;
	}

	public default @NonNull String mapMethod(final @NonNull String owner, final @NonNull String name, final @NonNull String descriptor) {
		return name;
	}

}