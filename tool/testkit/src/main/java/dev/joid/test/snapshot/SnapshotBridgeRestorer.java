package dev.joid.test.snapshot;

import java.util.Arrays;
import java.util.List;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.BridgeRegistry;
import dev.joid.lib.bridge.IBridge;

public final class SnapshotBridgeRestorer {

	private final List<Runnable> restorers;

	public SnapshotBridgeRestorer() {
		this.restorers = Arrays.asList(SnapshotBridgeRestorer.restorer(BridgeHandler.UI), SnapshotBridgeRestorer.restorer(BridgeHandler.AUDIO), SnapshotBridgeRestorer.restorer(BridgeHandler.CLOCK), SnapshotBridgeRestorer.restorer(BridgeHandler.WINDOW), SnapshotBridgeRestorer.restorer(BridgeHandler.RENDER), SnapshotBridgeRestorer.restorer(BridgeHandler.THREAD), SnapshotBridgeRestorer.restorer(BridgeHandler.SIGNAL_REPLAY));
	}

	public void restore() {
		this.restorers.forEach(Runnable::run);
	}

	private static <T extends IBridge> Runnable restorer(final BridgeRegistry<T> registry) {
		final List<T> before = registry.getBridgeList();
		return () -> {
			for (final T bridge : registry.getBridgeList()) {
				if (!before.contains(bridge)) {
					registry.unregister(bridge);
				}
			}
		};
	}

}