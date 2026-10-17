package dev.joid.lib.input.key;

import java.util.EnumMap;
import java.util.Map;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.window.IWindowBridge;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyState {

	private final Map<Key, Key> heldMap = new EnumMap<>(Key.class);

	public static @NonNull KeyState create() {
		return new KeyState();
	}

	public void press(final @NonNull Key physicalKey, final @NonNull Key key) {
		this.heldMap.put(physicalKey, key);
	}

	public @NonNull Key release(final @NonNull Key physicalKey) {
		final Key key = this.heldMap.remove(physicalKey);
		return key != null ? key : physicalKey;
	}

	public boolean isDown(final @NonNull Key key) {
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		return KeyLayout.isLayoutKey(key) ? this.heldMap.entrySet().stream().anyMatch(entry -> entry.getValue() == key && window.isKeyDown(entry.getKey())) : window.isKeyDown(key);
	}

	public @NonNull Key getKey(final @NonNull Key physicalKey) {
		return this.heldMap.getOrDefault(physicalKey, physicalKey);
	}

}