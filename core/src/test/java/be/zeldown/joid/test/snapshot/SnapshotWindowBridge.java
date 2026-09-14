package be.zeldown.joid.test.snapshot;

import be.zeldown.joid.lib.bridge.window.IWindowBridge;
import be.zeldown.joid.lib.utils.key.Key;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public final class SnapshotWindowBridge implements IWindowBridge {

	private final int width;
	private final int height;

	private double mouseX;
	private double mouseY;
	private String clipboard = "";

	@Override
	public boolean isMouseGrabbed() {
		return false;
	}

	@Override
	public boolean isKeyDown(final @NonNull Key key) {
		return false;
	}

}