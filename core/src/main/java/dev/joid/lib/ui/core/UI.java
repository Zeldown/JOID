package dev.joid.lib.ui.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import com.google.common.util.concurrent.AtomicDouble;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.state.StencilFunction;
import dev.joid.lib.bridge.render.state.StencilOperation;
import dev.joid.lib.bridge.render.state.StencilState;
import dev.joid.lib.bridge.ui.IUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.resolver.KeyResolver;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.render.Drawing;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.SignalContext;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.replay.SignalReplay;
import dev.joid.lib.ui.core.data.UIDataObject;
import dev.joid.lib.ui.core.data.debug.UIDataDebugObject;
import dev.joid.lib.ui.core.data.overlay.UIDataOverlayObject;
import dev.joid.lib.ui.core.data.popup.UIDataPopupObject;
import dev.joid.lib.ui.core.data.scale.UIDataScaleObject;
import dev.joid.lib.ui.core.hook.property.UIPropertyHook;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreHook;
import dev.joid.lib.ui.core.task.UIScheduledTask;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.impl.PopTransition;
import dev.joid.lib.ui.core.view.UIView;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.impl.dev.DevNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.utils.list.IndexedConcurrentList;
import dev.joid.lib.utils.list.IndexedElement;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class UI implements IUI, IndexedElement {

	@NonNull private static final Color HOVER_COLOR = new Color(16, 0, 16, 180);
	@NonNull private static final Color HOVER_BORDER_COLOR = new Color(30, 55, 153, 180);

	@Getter
	private static UI current;

	private static boolean monitorWarned;

	@NonNull private final Stack<MaskRegion>                    stencilStack;
	@NonNull private final Map<Set<Object>, Runnable>           keybindMap;
	@NonNull private final IndexedConcurrentList<@NonNull Node> nodeList;

	private final UIView       view;
	private final DoubleSignal zoomLevel;
	private final DoubleSignal scaledWidth;
	private final DoubleSignal scaledHeight;

	@NonNull private final UIDataObject        data;
	@NonNull private final UIDataDebugObject   debug;
	@NonNull private final UIDataPopupObject   popup;
	@NonNull private final UIDataScaleObject   scale;
	@NonNull private final UIDataOverlayObject overlay;

	@NonNull private UIDataObject        annotatedData;
	@NonNull private UIDataDebugObject   annotatedDebug;
	@NonNull private UIDataPopupObject   annotatedPopup;
	@NonNull private UIDataScaleObject   annotatedScale;
	@NonNull private UIDataPopupObject   transitionPopup;
	@NonNull private UIDataOverlayObject annotatedOverlay;

	private transient Transition                             transition;
	private transient FileAlterationMonitor                  fileMonitor;
	private transient List<UIScheduledTask>                  scheduledTaskList;
	private transient Map<Class<? extends UIStore>, UIStore> storeMap;

	private boolean closed;
	private boolean monitored;
	private boolean initialized;
	private volatile boolean reloadPending;

	private double fps;
	private long   lastFrame;
	private long   fpsCounter;
	private long   renderTime;
	private long   frameCount;
	private double frameTime;
	private long   lastFpsUpdate;

	private double  mouseX;
	private double  mouseY;
	private boolean onTop;
	private double  depthLevel;

	private Node devNode;

	private List<Node> hoveredPath;
	private double     lastHoveredX;
	private double     lastHoveredY;
	private long       lastHoveredFrame;

	public UI() {
		this.annotatedData    = UIDataObject.getOrDefault(this.getClass());
		this.annotatedDebug   = UIDataDebugObject.getOrDefault(this.getClass());
		this.annotatedPopup   = UIDataPopupObject.getOrDefault(this.getClass());
		this.annotatedScale   = UIDataScaleObject.getOrDefault(this.getClass());
		this.annotatedOverlay = UIDataOverlayObject.getOrDefault(this.getClass());
		this.data             = new UIDataObject(this.annotatedData);
		this.debug            = new UIDataDebugObject(this.annotatedDebug);
		this.popup            = new UIDataPopupObject(this.annotatedPopup);
		this.scale            = new UIDataScaleObject(this.annotatedScale);
		this.overlay          = new UIDataOverlayObject(this.annotatedOverlay);
		this.transitionPopup  = new UIDataPopupObject(this.popup);

		if (this.popup.active() && this.overlay.active()) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " cannot be a popup and an overlay at the same time");
		}

		this.stencilStack = new Stack<>();
		this.keybindMap = new HashMap<>();
		this.nodeList = new IndexedConcurrentList<>();
		this.storeMap = new HashMap<>();
		this.scheduledTaskList = new CopyOnWriteArrayList<>();

		this.transition = this.createPopupTransition();

		this.view = UIView.create(this.data.getAnchorPositionX(), this.data.getAnchorPositionY());
		this.zoomLevel = new DoubleSignal(1D);
		this.scaledWidth = new DoubleSignal(1920D);
		this.scaledHeight = new DoubleSignal(1080D);

		this.devNode = null;
		this.lastHoveredFrame = -1L;
		this.hoveredPath = Collections.emptyList();
	}

	public final void load(final double finalWidth, final double finalHeight) {
		this.load(finalWidth, finalHeight, 1D);
	}

	public final void load(final double finalWidth, final double finalHeight, final double zoomLevel) {
		final long start = System.nanoTime();
		if (JOID.inst().isDevMode() && this.debug.profiler() && !this.initialized) {
			System.out.println("##########################");
			System.out.println("Starting load...");
		}

		this.view.resize(finalWidth, finalHeight).zoom(zoomLevel);
		this.refreshView();

		if (!this.initialized) {
			UIPropertyHook.load(this);

			final boolean devNodeEnabled = this.nodeList.contains(this.devNode);
			this.keybindMap.clear();
			for (final Node node : this.nodeList) {
				if (node != this.devNode) {
					node.fireDetach();
				}
			}
			this.nodeList.clear();
			this.scheduledTaskList.clear();

			UI.current = this;
			SignalReplay.reset();
			SignalReplay.enter(this);
			try {
				this.init();
			} finally {
				SignalReplay.exit();
			}
			this.initialized = true;
			UI.current = null;

			if (this.transition != null) {
				if (this.transition.getIn() != null && this.transition.getIn().isEnabled()) {
					this.transition.getIn().init(this);
					this.transition.getIn().start();
				}

				if (this.transition.getOut() != null && this.transition.getOut().isEnabled()) {
					this.transition.getOut().init(this);
				}
			}

			if (JOID.inst().isDevMode()) {
				if (devNodeEnabled) {
					this.devNode.attach(this);
				} else {
					this.devNode = DevNode.create(1625, 1007).visible(node -> node.getUi().isOnTop()).enabled(node -> node.getUi().isOnTop()).draggable(DraggableProperty.screen()).zindex(Integer.MAX_VALUE).zlevel(999);
				}
			}

			if (JOID.inst().isDevMode() && this.debug.profiler()) {
				final long end = System.nanoTime();
				System.out.println("Load completed in " + String.format("%.2f", (end - start) / 1000000F) + "ms");
				System.out.println("##########################");
			}
		} else if (this.closed) {
			this.nodeList.forEach(node -> node.load(this));
		}

		this.closed = false;
		this.refreshMonitor();
	}

	public final boolean fireMouseScroll(final double notchesX, final double notchesY) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		if (JOID.inst().isDevMode() && Key.LEFT_ALT.isDown() && notchesY != 0D) {
			this.zoom(this.view.getZoom() + notchesY * (Key.LEFT_SHIFT.isDown() ? 0.12D : 0.012D));
			return true;
		}

		final DispatchContext context = DispatchContext.create();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> node.fireMouseScroll(mx, my, notchesX, notchesY, context)));

		this.traced(() -> this.mouseScroll(mx, my, notchesX, notchesY, context));
		return context.isCancelled();
	}

	public final boolean fireMousePressed(final @NonNull MouseButton button) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final DispatchContext context = DispatchContext.create();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> node.fireMousePressed(mx, my, button, context)));

		this.traced(() -> this.mousePressed(mx, my, button, context));
		return context.isCancelled();
	}

	public final boolean fireMouseReleased(final @NonNull MouseButton button) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final DispatchContext context = DispatchContext.create();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> node.fireMouseReleased(mx, my, button, context)));

		this.traced(() -> this.mouseReleased(mx, my, button, context));
		return context.isCancelled();
	}

	public final boolean fireMouseDragged(final @NonNull MouseButton button, final long deltaTime) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final DispatchContext context = DispatchContext.create();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> node.fireMouseDragged(mx, my, button, deltaTime, context)));

		this.traced(() -> this.mouseDragged(mx, my, button, deltaTime, context));
		return context.isCancelled();
	}

	public final boolean fireCharTyped(final int codepoint) {
		if (!this.initialized) {
			return false;
		}

		final DispatchContext context = DispatchContext.create();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> node.fireCharTyped(codepoint, context)));

		this.traced(() -> this.charTyped(codepoint, context));
		return context.isCancelled();
	}

	public final boolean fireKeyPressed(final @NonNull Key key) {
		if (!this.initialized) {
			return false;
		}

		final DispatchContext context = DispatchContext.create();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> node.fireKeyPressed(key, context)));

		if (!context.isCancelled()) {
			for (final Map.Entry<Set<Object>, Runnable> entry : this.keybindMap.entrySet()) {
				if (UI.isPressed(entry.getKey(), key)) {
					this.traced(entry.getValue());
					context.cancel();
				}
			}
		}

		if (this.data.zoomable() && !context.isCancelled()) {
			if ((key == Key.NUMPAD_ADD || key == Key.EQUAL) && (UI.isCtrlKeyDown() || UI.isAltKeyDown())) {
				final double previous = this.view.getZoom();
				this.zoom(previous + 0.1D);
				if (this.view.getZoom() != previous) {
					context.cancel();
				}
			}

			if ((key == Key.NUMPAD_SUBTRACT || key == Key.MINUS) && (UI.isCtrlKeyDown() || UI.isAltKeyDown())) {
				final double previous = this.view.getZoom();
				this.zoom(previous - 0.1D);
				if (this.view.getZoom() != previous) {
					context.cancel();
				}
			}
		}

		if (JOID.inst().isDevMode() && !context.isCancelled()) {
			if (key == Key.R && Key.LEFT_CONTROL.isDown() || key == Key.F5) {
				if (!Key.LEFT_SHIFT.isDown()) {
					this.reload();
				} else {
					try {
						this.renew();
					} catch (final IllegalStateException exception) {
						System.err.println("[JOID] " + exception.getMessage());
					}
				}
				context.cancel();
			} else if (key == Key.F3 && this.devNode != null) {
				final boolean enabled = this.nodeList.contains(this.devNode);
				if (enabled) {
					this.nodeList.remove(this.devNode);
				} else {
					this.devNode.attach(this);
				}
				context.cancel();
			}
		}

		this.traced(() -> this.keyPressed(key, context));
		return context.isCancelled();
	}

	public final void fireUpdate() {
		this.untraced(() -> {
			this.nodeList.forEach(Node::fireUpdate);
			this.update();
		});
	}

	public final boolean fireClose() {
		if (this.transition != null && this.transition.getOut() != null && this.transition.getOut().isRunning()) {
			return false;
		}

		final boolean result = this.close();
		if (!result) {
			return result;
		}

		if (this.transition != null && this.transition.getOut() != null && this.transition.getOut().isEnabled()) {
			this.transition.getOut().start();
			this.transition.getOut().getAnimator().setCallback(tween -> {
				this.dispose();
				this.getBridge().close(this);
			});
			return false;
		}

		this.dispose();
		return true;
	}

	@Override
	public int getIndex() {
		return this.data.zindex();
	}

	public final double getWidth() {
		return this.view.getWidth();
	}

	public final double getHeight() {
		return this.view.getHeight();
	}

	public final double getViewX() {
		return this.view.toUiX(0D);
	}

	public final double getViewY() {
		return this.view.toUiY(0D);
	}

	public final double getViewWidth() {
		return this.view.getVisibleWidth();
	}

	public final double getViewHeight() {
		return this.view.getVisibleHeight();
	}

	public final double getMouseX() {
		return this.view.toUiX(this.mouseX);
	}

	public final double getMouseY() {
		return this.view.toUiY(this.mouseY);
	}

	public final Node getHoveredNode() {
		final List<Node> path = this.getHoveredPath();
		return path.isEmpty() ? null : path.get(0);
	}

	public final @NonNull List<@NonNull Node> getHoveredPath() {
		final double mx = this.getMouseX();
		final double my = this.getMouseY();
		if (this.lastHoveredFrame != this.frameCount || this.lastHoveredX != mx || this.lastHoveredY != my) {
			final List<Node> path = new ArrayList<>();
			for (Node node = this.onTop ? this.getNodeAt(mx, my) : null; node != null; node = node.getParent()) {
				path.add(node);
			}

			this.hoveredPath = Collections.unmodifiableList(path);
			this.lastHoveredFrame = this.frameCount;
			this.lastHoveredX = mx;
			this.lastHoveredY = my;
		}
		return this.hoveredPath;
	}

	public final Node getNodeAt(final double x, final double y) {
		for (final Node node : this.getNodeListAt(x, y)) {
			if (node.isInteractive()) {
				return node;
			}
		}
		return null;
	}

	public final @NonNull List<@NonNull Node> getNodeListAt(final double x, final double y) {
		final List<Node> nodeList = new ArrayList<>();
		for (final Node node : this.nodeList.reversed()) {
			nodeList.addAll(node.getNodeListAt(x, y));
		}
		return nodeList;
	}

	public final IUIBridge getBridge() {
		return BridgeHandler.UI.get(this);
	}

	public final void dispose() {
		this.nodeList.forEach(Node::fireDetach);
		this.closed = true;

		this.stopMonitor();

		UIStoreHook.saveAll();
		for (final Entry<Class<? extends UIStore>, UIStore> storeEntry : this.storeMap.entrySet()) {
			UIStoreHook.destroyStore(storeEntry.getValue());
		}
		this.storeMap.clear();

		UIPropertyHook.save(this);
	}

	public final void draw(final double mouseX, final double mouseY) {
		if (this.reloadPending) {
			this.reloadPending = false;
			System.out.println("Starting reload...");

			final long start = System.nanoTime();
			this.reload();
			final long end = System.nanoTime();

			System.out.println("Reload completed in " + String.format("%.2f", (end - start) / 1000000F) + "ms");
			System.out.println("##########################");
		}

		this.untraced(() -> this.drawFrame(mouseX, mouseY));
	}

	public void drawHover(final @NonNull Object content, final double mouseX, final double mouseY) {
		this.getBridge().drawHover(this, content, mouseX, mouseY);
	}

	public final void mask(final double maskX, final double maskY, final double maskWidth, final double maskHeiht, final @NonNull Drawing drawing) {
		this.mask(maskX, maskY, maskWidth, maskHeiht, drawing, true);
	}

	public final void mask(final double maskX, final double maskY, final double maskWidth, final double maskHeight, final @NonNull Drawing drawing, final boolean enabled) {
		if (enabled) {
			this.startMask(maskX, maskY, maskWidth, maskHeight);
			try {
				drawing.draw();
			} finally {
				this.stopMask();
			}
		} else {
			drawing.draw();
		}
	}

	public final void mask(final @NonNull Resource resource, final double maskX, final double maskY, final double maskWidth, final double maskHeight, final @NonNull Drawing drawing) {
		this.mask(resource, maskX, maskY, maskWidth, maskHeight, drawing, true);
	}

	public final void mask(final @NonNull Resource resource, final double maskX, final double maskY, final double maskWidth, final double maskHeight, final @NonNull Drawing drawing, final boolean enabled) {
		if (enabled) {
			this.startMask(resource, maskX, maskY, maskWidth, maskHeight);
			try {
				drawing.draw();
			} finally {
				this.stopMask();
			}
		} else {
			drawing.draw();
		}
	}

	public final void stopMask() {
		this.stencilStack.pop();
		final int stencilValue = this.stencilStack.size();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (stencilValue == 0) {
			render.getState().stencil(StencilState.DISABLED);
			render.clearStencil();
		} else {
			render.getState().stencil(StencilState.create(StencilFunction.EQUAL, stencilValue, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP));
		}
	}

	public final void startMask(final double maskX, final double maskY, final double maskWidth, final double maskHeight) {
		final int stencilValue = this.stencilStack.size() + 1;
		this.stencilStack.push(new MaskRegion(stencilValue, maskX, maskY, maskWidth, maskHeight));
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (stencilValue == 1) {
			render.clearStencil();
		}

		render.getState().stencil(StencilState.create(StencilFunction.EQUAL, stencilValue - 1, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.INCREMENT));

		render.getState().colorWrite(false);
		DrawUtils.SHAPE.drawRect(maskX, maskY, maskWidth, maskHeight, Color.RED);
		render.getState().colorWrite(true);

		render.getState().stencil(StencilState.create(StencilFunction.EQUAL, stencilValue, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP));
	}

	public final void startMask(final @NonNull Resource resource, final double maskX, final double maskY, final double maskWidth, final double maskHeight) {
		final int stencilValue = this.stencilStack.size() + 1;
		this.stencilStack.push(new MaskRegion(stencilValue, maskX, maskY, maskWidth, maskHeight));
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (stencilValue == 1) {
			render.clearStencil();
		}

		render.getState().stencil(StencilState.create(StencilFunction.EQUAL, stencilValue - 1, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.INCREMENT));

		render.pushState();
		try {
			render.getState().colorWrite(false).alphaCutoff(0.5F);
			DrawUtils.RESOURCE.drawResource(maskX, maskY, maskWidth, maskHeight, resource);
		} finally {
			render.popState();
		}

		render.getState().stencil(StencilState.create(StencilFunction.EQUAL, stencilValue, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP));
	}

	public final void keybind(final @NonNull Runnable runnable, final @NonNull Object @NonNull... bindings) {
		for (final Object binding : bindings) {
			if (!KeyResolver.supports(binding)) {
				throw new IllegalArgumentException("No key resolver found for a binding of type " + binding.getClass().getName());
			}
		}

		this.keybindMap.put(new HashSet<>(Arrays.asList(bindings)), runnable);
	}

	public final void reload() {
		if (this.devNode != null) {
			final DevNode devNode = (DevNode) this.devNode;
			devNode.getReloadAnimator().sequence(100F, 1F).push(100F, 0F);
			devNode.getReloadAnimator().start();
		}

		UIPropertyHook.save(this);
		SignalReplay.clear();

		this.readData();
		this.refreshTransition();

		this.initialized = false;
		this.load(this.view.getWidth(), this.view.getHeight(), this.view.getZoom());
	}

	public final @NonNull UI renew() {
		final IUIBridge bridge = BridgeHandler.UI.get(this);
		if (bridge == null || !bridge.isOpen(this)) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " is not open, only an open UI can be renewed");
		}

		final UI renewed;
		try {
			final Constructor<? extends UI> constructor = this.getClass().getDeclaredConstructor();
			constructor.setAccessible(true);
			renewed = constructor.newInstance();
		} catch (final NoSuchMethodException exception) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " has no constructor without argument, it cannot be renewed: use Ctrl + R to reload it instead", exception);
		} catch (final ReflectiveOperationException | RuntimeException exception) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " cannot be renewed: its constructor without argument failed", exception);
		}

		this.dispose();
		bridge.remove(this);
		bridge.add(renewed);
		return renewed;
	}

	public final double lerpByFramerate(final double value, final double target, final double speed, final double snapDiff, final boolean snap) {
		final double diff = target - value;
		final double absDiff = Math.abs(diff);

		final double offset = Math.min(absDiff, speed * this.frameTime / (1000D / 60D) * absDiff / 3D);

		if (absDiff > snapDiff) {
			return value + (diff > 0 ? offset : -offset);
		}

		return snap ? target : value;
	}

	public final void schedule(final @NonNull Runnable runnable) {
		this.schedule(runnable, 0L, -1L);
	}

	public final void schedule(final @NonNull Runnable runnable, final long delay) {
		this.schedule(runnable, delay, -1L);
	}

	public final void schedule(final @NonNull Runnable runnable, final long delay, final long period) {
		this.scheduledTaskList.add(new UIScheduledTask(runnable, delay, period));
	}

	public final void add(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			node.load(this);
			this.nodeList.add(node);
		}
	}

	public final void setDepthLevel(final double level) {
		this.depthLevel = level;
	}

	public final @NonNull UI setTransition(final Transition transition) {
		this.transition = transition;
		return this;
	}

	@SuppressWarnings("unchecked")
	public final <T extends UIStore> @NonNull T useStore(final @NonNull Class<T> clazz, final Object... args) {
		if (this.storeMap.containsKey(clazz)) {
			return (T) this.storeMap.get(clazz);
		}

		final T store = UIStoreHook.useStore(clazz, args);
		if (store.getData().scope().isLocal()) {
			this.storeMap.put(clazz, store);
		}

		return store;
	}

	public static boolean isAltKeyDown() {
		return Key.LEFT_ALT.isDown() || Key.RIGHT_ALT.isDown();
	}

	public static boolean isCtrlKeyDown() {
		return Key.LEFT_CONTROL.isDown() || Key.RIGHT_CONTROL.isDown();
	}

	public static boolean isShiftKeyDown() {
		return Key.LEFT_SHIFT.isDown() || Key.RIGHT_SHIFT.isDown();
	}

	public final void zoom(final double zoom) {
		this.view.zoom(zoom);
		this.refreshView();
	}

	private void readData() {
		final UIDataObject data = UIDataObject.getOrDefault(this.getClass());
		final UIDataDebugObject debug = UIDataDebugObject.getOrDefault(this.getClass());
		final UIDataPopupObject popup = UIDataPopupObject.getOrDefault(this.getClass());
		final UIDataScaleObject scale = UIDataScaleObject.getOrDefault(this.getClass());
		final UIDataOverlayObject overlay = UIDataOverlayObject.getOrDefault(this.getClass());
		this.data.update(this.annotatedData, data);
		this.debug.update(this.annotatedDebug, debug);
		this.popup.update(this.annotatedPopup, popup);
		this.scale.update(this.annotatedScale, scale);
		this.overlay.update(this.annotatedOverlay, overlay);
		this.annotatedData    = data;
		this.annotatedDebug   = debug;
		this.annotatedPopup   = popup;
		this.annotatedScale   = scale;
		this.annotatedOverlay = overlay;
	}

	private void refreshView() {
		this.zoomLevel.set(this.view.getZoom());
		this.scaledWidth.set(this.view.getVisibleWidth());
		this.scaledHeight.set(this.view.getVisibleHeight());
	}

	private void refreshTransition() {
		if (this.popup.active() == this.transitionPopup.active() && this.popup.transition() == this.transitionPopup.transition()) {
			return;
		}

		this.transitionPopup = new UIDataPopupObject(this.popup);
		this.transition = this.createPopupTransition();
	}

	private void refreshMonitor() {
		this.monitored = this.debug.hotreload();
		if (!this.monitored) {
			this.stopMonitor();
		}

		if (JOID.inst().isDevMode() && this.debug.hotreload() && this.fileMonitor == null) {
			final File location = UI.locate(this.getClass());
			if (location == null) {
				if (!UI.monitorWarned) {
					UI.monitorWarned = true;
					System.err.println("[JOID] Hot reload is off for " + this.getClass().getSimpleName() + ": its classes are not loaded from a folder or a jar file");
				}
				return;
			}

			try {
				final FileAlterationObserver observer = new FileAlterationObserver(location.isDirectory() ? location : location.getParentFile());
				observer.addListener(new FileAlterationListener() {

					private File change;

					@Override
					public void onFileChange(final @NonNull File file) {
						this.track(file);
					}

					@Override
					public void onStop(final @NonNull FileAlterationObserver observer) {
						if (this.change == null) {
							return;
						}

						try {
							Thread.sleep(1000L);
						} catch (final Exception e) {}

						System.out.println("##########################");
						System.out.println("Detected file change: " + this.change.getName());
						this.change = null;
						UI.this.reloadPending = true;
					}

					@Override
					public void onStart(final @NonNull FileAlterationObserver observer) {}

					@Override
					public void onFileDelete(final @NonNull File file) {}

					@Override
					public void onFileCreate(final @NonNull File file) {
						this.track(file);
					}

					@Override
					public void onDirectoryDelete(final @NonNull File file) {}

					@Override
					public void onDirectoryCreate(final @NonNull File file) {}

					@Override
					public void onDirectoryChange(final @NonNull File file) {}

					private void track(final File file) {
						if (location.isDirectory() ? file.getName().endsWith(".class") : file.equals(location)) {
							this.change = file;
						}
					}

				});

				this.fileMonitor = new FileAlterationMonitor(500);
				this.fileMonitor.setThreadFactory(ThreadUtils.daemonFactory("UI/" + this.getClass().getName() + "/monitor"));
				this.fileMonitor.addObserver(observer);
				this.fileMonitor.start();

				System.out.println("##########################");
				System.out.println("Hot-reload enabled on " + this.getClass().getSimpleName());
				System.out.println("##########################");
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void stopMonitor() {
		final FileAlterationMonitor monitor = this.fileMonitor;
		if (monitor == null) {
			return;
		}

		this.fileMonitor = null;
		ThreadUtils.daemonThread(() -> {
			try {
				final Field runningField = FileAlterationMonitor.class.getDeclaredField("running");
				runningField.setAccessible(true);
				final boolean running = runningField.getBoolean(monitor);
				if (running) {
					monitor.stop();
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}, "UI/" + this.getClass().getName() + "/monitor-close").start();
	}

	private Transition createPopupTransition() {
		if (!this.popup.active() || !this.popup.transition().isActive()) {
			return null;
		}

		final Transition transition = new PopTransition();
		if (!this.popup.transition().isIn()) {
			transition.getIn().disable();
		}

		if (!this.popup.transition().isOut()) {
			transition.getOut().disable();
		}
		return transition;
	}

	private void drawFrame(final double mouseX, final double mouseY) {
		final long start = System.nanoTime();
		final IRenderBridge render = BridgeHandler.RENDER.get();

		final long frame = BridgeHandler.CLOCK.get().nanoTime();
		this.frameTime = this.lastFrame == 0L ? 1000D / 60D : (frame - this.lastFrame) / 1_000_000D;
		this.lastFrame = frame;
		this.frameCount++;

		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.view.anchorX(this.data.getAnchorPositionX()).anchorY(this.data.getAnchorPositionY());
		this.refreshTransition();
		if (this.monitored != this.debug.hotreload()) {
			this.refreshMonitor();
		}
		this.onTop = this.getBridge() != null && this.getBridge().isOnTop(this);

		final List<UIScheduledTask> toRemove = new ArrayList<>();
		for (final UIScheduledTask task : this.scheduledTaskList) {
			if (!task.shouldRun()) {
				continue;
			}

			SignalReplay.enter(this);
			try {
				if (!task.execute()) {
					toRemove.add(task);
				}
			} finally {
				SignalReplay.exit();
			}
		}
		this.scheduledTaskList.removeAll(toRemove);

		final IUIBridge bridge = this.getBridge();
		final double interfaceScale = bridge == null || !this.scale.active() ? 1D : this.scale.apply(bridge.getInterfaceScale(this));
		if (interfaceScale != this.view.getInterfaceScale()) {
			this.view.interfaceScale(interfaceScale);
			this.refreshView();
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		if (this.data.background()) {
			float opacity = this.data.getBackgroundColor().getAlpha() / 255F;
			if (this.transition != null) {
				if (this.transition.getIn() != null && this.transition.getIn().isRunning()) {
					opacity = this.transition.getIn().getAnimator().getValue() * (this.data.getBackgroundColor().getAlpha() / 255F);
				}

				if (this.transition.getOut() != null && this.transition.getOut().isRunning()) {
					opacity = this.transition.getOut().getAnimator().getValue() * (this.data.getBackgroundColor().getAlpha() / 255F);
				}
			}
			DrawUtils.SHAPE.drawRect(0, 0, this.view.getWidth(), this.view.getHeight(), this.data.getBackgroundColor().copyAlpha(opacity));
		}

		final boolean transitionIn = this.transition != null && this.transition.getIn() != null && this.transition.getIn().isRunning();
		final boolean transitionOut = this.transition != null && this.transition.getOut() != null && this.transition.getOut().isRunning();
		if (transitionIn) {
			this.transition.getIn().update();
			this.transition.getIn().pre(this, mx, my);
		}

		if (transitionOut) {
			this.transition.getOut().update();
			this.transition.getOut().pre(this, mx, my);
		}

		render.getState().alphaCutoff(0F);
		try {
			this.view.render(render, this.data.projection(), () -> {
				this.depthLevel = 0;
				final AtomicDouble lastDepthLevel = new AtomicDouble(this.depthLevel);

				this.nodeList.ordered().forEach(node -> {
					render.getModelView().translate(0, 0, this.depthLevel - lastDepthLevel.get());
					lastDepthLevel.set(this.depthLevel);
					node.render(mx, my);
				});

				Node hovered = this.getHoveredNode();
				if (hovered != null) {
					render.getModelView().push();
					render.pushState();
					try {
						render.getState().depthTest(false).depthWrite(false);
						while (hovered != null && !hovered.renderHover(mx, my)) {
							hovered = hovered.getParent();
						}
					} finally {
						render.popState();
						render.getModelView().pop();
					}
				}
			});
		} finally {
			if (transitionIn) {
				this.transition.getIn().post(this, mx, my);
			}

			if (transitionOut) {
				this.transition.getOut().post(this, mx, my);
			}
		}

		final long end = System.nanoTime();
		this.renderTime = end - start;

		if (JOID.inst().isDevMode() && this.debug.profiler()) {
			final float ms = this.renderTime / 1_000_000F;
			if (ms > 16.66F) {
				System.err.println("##########################");
				System.err.println("[!] Frame took " + String.format("%.2f", ms) + "ms to render");
				System.err.println("##########################");
			}
		}

		if (this.lastFpsUpdate == 0L) {
			this.fps = 0;
			this.fpsCounter = 0;
			this.lastFpsUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();
		} else if (this.frameTime > 0D) {
			this.fpsCounter++;
			final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
			if (now - this.lastFpsUpdate >= 1000L) {
				this.fps = this.fpsCounter / ((now - this.lastFpsUpdate) / 1000D);
				this.fpsCounter = 0;
				this.lastFpsUpdate = now;
			}
		}
	}

	private void traced(final Runnable runnable) {
		SignalReplay.enter(this);
		try {
			runnable.run();
		} finally {
			SignalReplay.exit();
		}
	}

	private void untraced(final Runnable runnable) {
		final boolean tracing = SignalContext.current().tracing(false);
		try {
			runnable.run();
		} finally {
			SignalContext.current().tracing(tracing);
		}
	}

	private static boolean isPressed(final Set<Object> bindings, final Key key) {
		boolean pressed = false;
		for (final Object binding : bindings) {
			final Key bound = KeyResolver.resolve(binding);
			if (bound == null || !bound.isDown()) {
				return false;
			}

			pressed |= bound == key;
		}
		return pressed;
	}

	private static File locate(final Class<?> type) {
		final ProtectionDomain domain = type.getProtectionDomain();
		final CodeSource source = domain != null ? domain.getCodeSource() : null;
		final URL location = source != null ? source.getLocation() : null;
		if (location == null || !"file".equals(location.getProtocol())) {
			return null;
		}

		try {
			return new File(location.toURI());
		} catch (final URISyntaxException | IllegalArgumentException exception) {
			return null;
		}
	}

	@Getter
	private class MaskRegion {

		public final double x;
		public final double y;
		public final int value;
		public final double width;
		public final double height;

		public MaskRegion(final int value, final double x, final double y, final double width, final double height) {
			this.value = value;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

	}

}