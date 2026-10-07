package dev.joid.lib.ui.core;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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
import dev.joid.lib.bridge.ui.IUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.context.Drawing;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.data.UIDataObject;
import dev.joid.lib.ui.core.data.debug.UIDataDebugObject;
import dev.joid.lib.ui.core.data.popup.UIDataPopupObject;
import dev.joid.lib.ui.core.hook.property.UIPropertyHook;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreHook;
import dev.joid.lib.ui.core.task.UIScheduledTask;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.impl.PopTransition;
import dev.joid.lib.ui.core.view.UIView;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.dev.DevNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.list.IndexedConcurrentList;
import dev.joid.lib.utils.list.IndexedElement;
import dev.joid.lib.utils.signal.SignalContext;
import dev.joid.lib.utils.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.utils.signal.replay.SignalReplay;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class UI implements IUI, IndexedElement {

	@NonNull private static final Color HOVER_COLOR = new Color(16, 0, 16, 180);
	@NonNull private static final Color HOVER_BORDER_COLOR = new Color(30, 55, 153, 180);

	@Getter
	private static UI current;

	@NonNull private final UIDataObject         data;
	@NonNull private final UIDataDebugObject    debug;
	@NonNull private final UIDataPopupObject    popup;

	@NonNull private final Map<Set<Key>, Runnable>              keybindMap;
	@NonNull private final Stack<StencilState>                  stencilStack;
	@NonNull private final IndexedConcurrentList<@NonNull Node> nodeList;

	private final UIView       view;
	private final DoubleSignal zoomLevel;
	private final DoubleSignal scaledWidth;
	private final DoubleSignal scaledHeight;

	private transient Transition                             transition;
	private transient FileAlterationMonitor                  fileMonitor;
	private transient List<UIScheduledTask>                  scheduledTaskList;
	private transient Map<Class<? extends UIStore>, UIStore> storeMap;

	private boolean closed;
	private boolean initialized;

	private double fps;
	private long   lastFrame;
	private long   fpsCounter;
	private long   renderTime;
	private double frameTime;
	private long   lastFpsUpdate;

	private double  mouseX;
	private double  mouseY;
	private boolean onTop;
	private double  renderPipelineLevel;

	private Node devNode;

	public UI() {
		this.data = UIDataObject.getOrDefault(this.getClass());
		this.debug = UIDataDebugObject.getOrDefault(this.getClass());
		this.popup = UIDataPopupObject.getOrDefault(this.getClass());

		this.stencilStack = new Stack<>();
		this.keybindMap = new HashMap<>();
		this.nodeList = new IndexedConcurrentList<>();
		this.storeMap = new HashMap<>();
		this.scheduledTaskList = new CopyOnWriteArrayList<>();

		if (this.popup.active() && this.popup.transition().isActive()) {
			this.transition = new PopTransition();

			if (!this.popup.transition().isIn()) {
				this.transition.getIn().disable();
			}

			if (!this.popup.transition().isOut()) {
				this.transition.getOut().disable();
			}
		}

		this.view = UIView.create(this.data.getAnchorPositionX(), this.data.getAnchorPositionY());
		this.zoomLevel = new DoubleSignal(1D);
		this.scaledWidth = new DoubleSignal(1920D);
		this.scaledHeight = new DoubleSignal(1080D);

		this.devNode = null;
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
					node.onDetach();
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

		if (JOID.inst().isDevMode() && this.debug.hotreload() && this.fileMonitor == null) {
			final File currentFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
			final FileAlterationObserver observer = new FileAlterationObserver(currentFile.getParentFile());
			observer.addListener(new FileAlterationListener() {

				@Override
				public void onFileChange(final @NonNull File file) {
					if (!file.getName().equals(currentFile.getName())) {
						return;
					}

					try {
						Thread.sleep(1000L);
					} catch (final Exception e) {}

					System.out.println("##########################");
					System.out.println("Detected file change: " + file.getName());
					System.out.println("Starting reload...");

					final long start = System.nanoTime();
					UIPropertyHook.save(UI.this);
					SignalReplay.clear();
					UI.this.initialized = false;
					UI.this.load(UI.this.view.getWidth(), UI.this.view.getHeight(), UI.this.view.getZoom());
					final long end = System.nanoTime();

					System.out.println("Reload completed in " + String.format("%.2f", (end - start) / 1000000F) + "ms");
					System.out.println("##########################");
				}

				@Override
				public void onStop(final @NonNull FileAlterationObserver observer) {}

				@Override
				public void onStart(final @NonNull FileAlterationObserver observer) {}

				@Override
				public void onFileDelete(final @NonNull File file) {}

				@Override
				public void onFileCreate(final @NonNull File file) {}

				@Override
				public void onDirectoryDelete(final @NonNull File file) {}

				@Override
				public void onDirectoryCreate(final @NonNull File file) {}

				@Override
				public void onDirectoryChange(final @NonNull File file) {}

			});

			this.fileMonitor = new FileAlterationMonitor(500);
			this.fileMonitor.setThreadFactory(ThreadUtils.daemonFactory("UI/" + this.getClass().getName() + "/monitor"));
			this.fileMonitor.addObserver(observer);
			try {
				this.fileMonitor.start();

				System.out.println("##########################");
				System.out.println("Hot-reload enabled on " + this.getClass().getSimpleName());
				System.out.println("##########################");
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public final boolean onMouseScroll(final int value) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		if (JOID.inst().isDevMode() && Key.LEFT_ALT.isDown() && value != 0) {
			this.zoom(this.view.getZoom() + value / (Key.LEFT_SHIFT.isDown() ? 1000D : 10000D));
			return true;
		}

		final InternalContext context = InternalContext.create();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> node.onMouseScroll(mx, my, value, context)));

		this.traced(() -> this.mouseScroll(mx, my, value, context));
		return context.isCancelled();
	}

	public final boolean onMousePressed(final @NonNull ClickType clickType) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final InternalContext context = InternalContext.create();

		this.untraced(() -> {
			this.nodeList.reversed().stream().filter(node -> node.getZindex() > 0).forEach(node -> node.onMousePressed(mx, my, clickType, context));
			this.nodeList.reversed().stream().filter(node -> node.getZindex() <= 0).forEach(node -> node.onMousePressed(mx, my, clickType, context));
		});

		this.traced(() -> this.mousePressed(mx, my, clickType, context));
		return context.isCancelled();
	}

	public final boolean onMouseReleased(final @NonNull ClickType clickType) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final InternalContext context = InternalContext.create();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> node.onMouseReleased(mx, my, clickType, context)));

		this.traced(() -> this.mouseReleased(mx, my, clickType, context));
		return context.isCancelled();
	}

	public final boolean onMouseDragged(final @NonNull ClickType clickType, final long deltaTime) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final InternalContext context = InternalContext.create();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> node.onMouseDragged(mx, my, clickType, deltaTime, context)));

		this.traced(() -> this.mouseDragged(mx, my, clickType, deltaTime, context));
		return context.isCancelled();
	}

	public final boolean onKeyPressed(final char c, final @NonNull Key key) {
		if (!this.initialized) {
			return false;
		}

		final InternalContext context = InternalContext.create();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> node.onKeyPressed(c, key, context)));

		if (!context.isCancelled()) {
			for (final Map.Entry<Set<Key>, Runnable> entry : this.keybindMap.entrySet()) {
				boolean match = entry.getKey().contains(key);
				for (final Key bindKey : entry.getKey()) {
					if (!bindKey.isDown()) {
						match = false;
						break;
					}
				}

				if (match) {
					this.traced(entry.getValue());
					context.cancel();
				}
			}
		}

		if (this.data.zoomable() && !context.isCancelled()) {
			if ((key == Key.NUMPAD_ADD || c == '+') && (UI.isCtrlKeyDown() || UI.isAltKeyDown())) {
				final double previous = this.view.getZoom();
				this.zoom(previous + 0.1D);
				if (this.view.getZoom() != previous) {
					context.cancel();
				}
			}

			if ((key == Key.NUMPAD_SUBTRACT || c == '-') && (UI.isCtrlKeyDown() || UI.isAltKeyDown())) {
				final double previous = this.view.getZoom();
				this.zoom(previous - 0.1D);
				if (this.view.getZoom() != previous) {
					context.cancel();
				}
			}
		}

		if (JOID.inst().isDevMode() && !context.isCancelled()) {
			if (key == Key.R && Key.LEFT_CONTROL.isDown() || key == Key.F5) {
				if (Key.LEFT_SHIFT.isDown()) {
					this.zoom(1D);
				}

				this.reload();
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

		this.traced(() -> this.keyPressed(c, key, context));
		return context.isCancelled();
	}

	public final void onUpdate() {
		this.untraced(() -> {
			this.nodeList.forEach(Node::onUpdate);
			this.update();
		});
	}

	public final boolean onClose() {
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
				this.properlyClose();
				this.getBridge().close(this);
			});
			return false;
		}

		this.properlyClose();
		return true;
	}

	@Override
	public int getIndex() {
		return (int) Math.floor(this.data.zlevel());
	}

	public final double getWidth() {
		return this.view.getWidth();
	}

	public final double getHeight() {
		return this.view.getHeight();
	}

	public final double getMouseX() {
		return this.view.toUiX(this.mouseX);
	}

	public final double getMouseY() {
		return this.view.toUiY(this.mouseY);
	}

	public final IUIBridge getBridge() {
		return BridgeHandler.UI.get(this);
	}

	public final void properlyClose() {
		this.nodeList.forEach(Node::onDetach);
		this.closed = true;

		if (this.fileMonitor != null) {
			ThreadUtils.daemonThread(() -> {
				try {
					final Field runningField = FileAlterationMonitor.class.getDeclaredField("running");
					runningField.setAccessible(true);
					final boolean running = runningField.getBoolean(this.fileMonitor);
					if (running) {
						this.fileMonitor.stop();
					}
					this.fileMonitor = null;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}, "UI/" + this.getClass().getName() + "/monitor-close").start();
		}

		UIStoreHook.saveAll();
		for (final Entry<Class<? extends UIStore>, UIStore> storeEntry : this.storeMap.entrySet()) {
			UIStoreHook.destroyStore(storeEntry.getValue());
		}
		this.storeMap.clear();

		UIPropertyHook.save(this);
	}

	public final void draw(final double mouseX, final double mouseY) {
		this.untraced(() -> this.drawFrame(mouseX, mouseY));
	}

	public void drawHover(final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY) {
		this.getBridge().drawHover(this, lines, mouseX, mouseY);
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
			render.stencilTest(false);
			render.clearStencil();
		} else {
			render.stencilFunction(StencilFunction.EQUAL, stencilValue, 0xFF);
			render.stencilOperation(StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP);
		}
	}

	public final void startMask(final double maskX, final double maskY, final double maskWidth, final double maskHeight) {
		final int stencilValue = this.stencilStack.size() + 1;
		this.stencilStack.push(new StencilState(stencilValue, maskX, maskY, maskWidth, maskHeight));
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (stencilValue == 1) {
			render.clearStencil();
			render.stencilTest(true);
		}

		render.stencilFunction(StencilFunction.EQUAL, stencilValue - 1, 0xFF);
		render.stencilOperation(StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.INCREMENT);

		render.colorMask(false);
		DrawUtils.SHAPE.drawRect(maskX, maskY, maskWidth, maskHeight, Color.RED);
		render.colorMask(true);

		render.stencilFunction(StencilFunction.EQUAL, stencilValue, 0xFF);
		render.stencilOperation(StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP);
	}

	public final void startMask(final @NonNull Resource resource, final double maskX, final double maskY, final double maskWidth, final double maskHeight) {
		final int stencilValue = this.stencilStack.size() + 1;
		this.stencilStack.push(new StencilState(stencilValue, maskX, maskY, maskWidth, maskHeight));
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (stencilValue == 1) {
			render.clearStencil();
			render.stencilTest(true);
		}

		render.stencilFunction(StencilFunction.EQUAL, stencilValue - 1, 0xFF);
		render.stencilOperation(StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.INCREMENT);

		render.pushState();
		try {
			render.colorMask(false);
			render.alphaTest(0.5F);
			DrawUtils.RESOURCE.drawResource(maskX, maskY, maskWidth, maskHeight, resource);
		} finally {
			render.popState();
		}

		render.stencilFunction(StencilFunction.EQUAL, stencilValue, 0xFF);
		render.stencilOperation(StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP);
	}

	public final void keybind(final @NonNull Runnable runnable, final @NonNull Key... keys) {
		this.keybindMap.put(new HashSet<>(Arrays.asList(keys)), runnable);
	}

	public final void reload() {
		if (this.devNode != null) {
			final DevNode devNode = (DevNode) this.devNode;
			devNode.getReloadAnimator().sequence(100F, 1F).push(100F, 0F);
			devNode.getReloadAnimator().start();
		}

		UIPropertyHook.save(this);
		SignalReplay.clear();
		this.initialized = false;
		this.load(this.view.getWidth(), this.view.getHeight(), this.view.getZoom());
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

	public final void setRenderPipelineLevel(final double level) {
		this.renderPipelineLevel = level;
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
		if (store.getData().context().isLocal()) {
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

	private void refreshView() {
		this.zoomLevel.set(this.view.getZoom());
		this.scaledWidth.set(this.view.getVisibleWidth());
		this.scaledHeight.set(this.view.getVisibleHeight());
	}

	private void drawFrame(final double mouseX, final double mouseY) {
		final long start = System.nanoTime();
		final IRenderBridge render = BridgeHandler.RENDER.get();

		final long frame = BridgeHandler.CLOCK.get().nanoTime();
		this.frameTime = this.lastFrame == 0L ? 1000D / 60D : (frame - this.lastFrame) / 1_000_000D;
		this.lastFrame = frame;

		this.mouseX = mouseX;
		this.mouseY = mouseY;
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
		final double interfaceScale = bridge == null ? 1D : bridge.getInterfaceScale(this);
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

		this.drawBackground(mx, my);

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

		render.alphaTest(0F);
		try {
			this.view.render(render, this.data.projection(), () -> {
				this.renderPipelineLevel = 0;
				final AtomicDouble lastRenderPipelineLevel = new AtomicDouble(this.renderPipelineLevel);

				this.nodeList
				.ordered()
				.stream()
				.filter(node -> node.getZindex() < 0)
				.forEach(node -> {
					render.translate(0, 0, this.renderPipelineLevel - lastRenderPipelineLevel.get());
					lastRenderPipelineLevel.set(this.renderPipelineLevel);
					node.render(mx, my);
				});

				render.translate(0, 0, this.renderPipelineLevel - lastRenderPipelineLevel.get());
				lastRenderPipelineLevel.set(this.renderPipelineLevel);
				this.preDraw(mx, my);

				this.nodeList
				.ordered()
				.stream()
				.filter(node -> node.getZindex() >= 0 && node.getZindex() < 100)
				.forEach(node -> {
					render.translate(0, 0, this.renderPipelineLevel - lastRenderPipelineLevel.get());
					lastRenderPipelineLevel.set(this.renderPipelineLevel);
					node.render(mx, my);
				});

				render.translate(0, 0, this.renderPipelineLevel - lastRenderPipelineLevel.get());
				lastRenderPipelineLevel.set(this.renderPipelineLevel);
				this.postDraw(mx, my);

				this.nodeList
				.ordered()
				.stream()
				.filter(node -> node.getZindex() >= 100)
				.forEach(node -> {
					render.translate(0, 0, this.renderPipelineLevel - lastRenderPipelineLevel.get());
					lastRenderPipelineLevel.set(this.renderPipelineLevel);
					node.render(mx, my);
				});

				if (this.onTop) {
					render.pushMatrix();
					render.pushState();
					try {
						render.depth(false, false);
						for (final Node node : this.nodeList.reversed()) {
							if (node.renderHover(mx, my)) {
								break;
							}
						}
					} finally {
						render.popState();
						render.popMatrix();
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

	@Getter
	private class StencilState {

		public final double x;
		public final double y;
		public final int value;
		public final double width;
		public final double height;

		public StencilState(final int value, final double x, final double y, final double width, final double height) {
			this.value = value;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

	}

}