package dev.joid.lib.bridge.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.BridgeRegistry;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.thread.FrameThreadBridge;
import dev.joid.lib.bridge.thread.IThreadBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.KeyState;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;

public final class UIBridgeRegistry extends BridgeRegistry<IUIBridge> {

	private final List<Runnable>                   frameList;
	private final Map<MouseButton, List<UIBridge>> pressedMap;

	@Getter
	private final KeyState keyState;

	private int width;
	private int height;

	private UIBridgeRegistry() {
		super("UI");
		this.frameList = new CopyOnWriteArrayList<>();
		this.pressedMap = new EnumMap<>(MouseButton.class);
		this.keyState = KeyState.create();
	}

	public static @NonNull UIBridgeRegistry create() {
		return new UIBridgeRegistry();
	}

	public void frame() {
		final List<UIBridge> bridgeList = this.getUIBridgeList();
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final int width = window.getWidth();
		final int height = window.getHeight();
		if ((width != this.width || height != this.height) && width > 0 && height > 0) {
			this.width = width;
			this.height = height;
			render.screen(width, height);
			bridgeList.forEach(UIBridge::load);
		}

		this.fireFrame();
		bridgeList.forEach(UIBridge::update);
		render.beginFrame();
		try {
			if (!bridgeList.isEmpty()) {
				bridgeList.get(0).drawBackground();
			}

			bridgeList.forEach(UIBridge::draw);
		} finally {
			render.endFrame();
		}
	}

	public @NonNull UIBridgeRegistry onFrame(final @NonNull Runnable task) {
		this.frameList.add(task);
		return this;
	}

	public void fireFrame() {
		this.runThreadTasks();
		this.frameList.forEach(Runnable::run);
	}

	public boolean mouseMoved() {
		return this.dispatch(UIBridge::mouseMoved);
	}

	public boolean mousePressed(final @NonNull MouseButton button) {
		final List<UIBridge> pressedList = new ArrayList<>();
		this.pressedMap.put(button, pressedList);
		for (final UIBridge bridge : this.getInputList()) {
			pressedList.add(bridge);
			if (bridge.mousePressed(button)) {
				return true;
			}
		}
		return false;
	}

	public boolean mouseReleased(final @NonNull MouseButton button) {
		final List<UIBridge> pressedList = this.pressedMap.remove(button);
		if (pressedList == null) {
			return this.dispatch(bridge -> bridge.mouseReleased(button));
		}

		boolean consumed = false;
		for (final UIBridge bridge : pressedList) {
			consumed |= bridge.mouseReleased(button);
		}
		return consumed;
	}

	public boolean mouseScroll(final double notchesX, final double notchesY) {
		return this.dispatch(bridge -> bridge.mouseScroll(notchesX, notchesY));
	}

	public boolean charTyped(final int codepoint) {
		return this.dispatch(bridge -> bridge.charTyped(codepoint));
	}

	public boolean keyPressed(final @NonNull Key physicalKey, final @NonNull Key key) {
		this.keyState.press(physicalKey, key);
		return this.dispatch(bridge -> bridge.keyPressed(physicalKey, key));
	}

	public boolean keyReleased(final @NonNull Key physicalKey) {
		final Key key = this.keyState.release(physicalKey);
		boolean consumed = false;
		for (final UIBridge bridge : this.getInputList()) {
			consumed |= bridge.keyReleased(physicalKey, key);
		}
		return consumed;
	}

	public boolean isKeyConsumed(final @NonNull Key physicalKey) {
		final Key key = this.keyState.getKey(physicalKey);
		return this.dispatch(bridge -> bridge.isKeyConsumed(key));
	}

	public IUIBridge get(final @NonNull UI ui) {
		final IUIBridge holder = super.find(bridge -> bridge.isOpen(ui));
		return holder != null ? holder : super.find(bridge -> bridge.canHandle(ui));
	}

	public IUIBridge get(final @NonNull Class<? extends UI> clazz) {
		return super.find(bridge -> bridge.canHandle(clazz));
	}

	private void runThreadTasks() {
		for (final IThreadBridge bridge : BridgeHandler.THREAD.getBridgeList()) {
			if (bridge instanceof FrameThreadBridge) {
				((FrameThreadBridge) bridge).runTasks();
			}
		}
	}

	private boolean dispatch(final Predicate<UIBridge> event) {
		for (final UIBridge bridge : this.getInputList()) {
			if (event.test(bridge)) {
				return true;
			}
		}
		return false;
	}

	private List<UIBridge> getUIBridgeList() {
		final List<UIBridge> bridgeList = new ArrayList<>();
		for (final IUIBridge bridge : super.getBridgeList()) {
			if (bridge instanceof UIBridge) {
				bridgeList.add((UIBridge) bridge);
			}
		}
		return bridgeList;
	}

	private List<UIBridge> getInputList() {
		final List<UIBridge> inputList = this.getUIBridgeList();
		Collections.reverse(inputList);
		return inputList;
	}

}