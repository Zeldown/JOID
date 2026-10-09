package dev.joid.test.snapshot;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.utils.cursor.Cursor;
import dev.joid.lib.utils.key.Key;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class SnapshotWindowBridge implements IWindowBridge {

	private final Set<Key>     keys    = EnumSet.noneOf(Key.class);
	private final List<Cursor> cursors = new ArrayList<>();

	private int    width;
	private int    height;
	private double mouseX;
	private double mouseY;
	private String clipboard = "";

	public @NonNull Cursor getCursor() {
		return this.cursors.isEmpty() ? Cursor.DEFAULT : this.cursors.get(this.cursors.size() - 1);
	}

	@Override
	public void setCursor(final @NonNull Cursor cursor) {
		this.cursors.add(cursor);
	}

	@Override
	public boolean isMouseGrabbed() {
		return false;
	}

	@Override
	public boolean isKeyDown(final @NonNull Key key) {
		return this.keys.contains(key);
	}

}