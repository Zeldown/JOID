package dev.joid.lib.bridge.window;

import java.util.EnumSet;
import java.util.Set;

import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.key.KeyLayout;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class RecordingWindowBridge implements IWindowBridge {

	private final Set<Key> keys = EnumSet.noneOf(Key.class);

	private int       width;
	private int       height;
	private double    mouseX;
	private double    mouseY;
	private KeyLayout layout;
	private String    clipboard = "";

	@Override
	public boolean isMouseGrabbed() {
		return false;
	}

	@Override
	public boolean isKeyDown(final @NonNull Key key) {
		return this.layout == null ? this.keys.contains(key) : this.layout.isDown(key, this.keys::contains);
	}

	@Override
	public boolean isPhysicalKeyDown(final @NonNull Key key) {
		return this.keys.contains(key);
	}

}