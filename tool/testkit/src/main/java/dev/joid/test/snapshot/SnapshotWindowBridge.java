package dev.joid.test.snapshot;

import java.util.EnumSet;
import java.util.Set;

import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.input.key.Key;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class SnapshotWindowBridge implements IWindowBridge {

	private final Set<Key> keys = EnumSet.noneOf(Key.class);

	private int    width;
	private int    height;
	private double mouseX;
	private double mouseY;
	private Cursor cursor    = Cursor.DEFAULT;
	private String clipboard = "";

	@Override
	public boolean isMouseGrabbed() {
		return false;
	}

	@Override
	public boolean isKeyDown(final @NonNull Key physicalKey) {
		return this.keys.contains(physicalKey);
	}

}