package be.zeldown.joid.lib.ui.core;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.util.concurrent.AtomicDouble;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.opengl.context.Drawing;
import be.zeldown.joid.lib.opengl.modifier.GLVector;
import be.zeldown.joid.lib.opengl.transform.GLTransformation;
import be.zeldown.joid.lib.ui.bridge.BridgeHandler;
import be.zeldown.joid.lib.ui.bridge.IUIBridge;
import be.zeldown.joid.lib.ui.core.data.UIDataObject;
import be.zeldown.joid.lib.ui.core.data.debug.UIDataDebugObject;
import be.zeldown.joid.lib.ui.core.data.popup.UIDataPopupObject;
import be.zeldown.joid.lib.ui.core.hook.property.UIPropertyHook;
import be.zeldown.joid.lib.ui.core.hook.store.UIStore;
import be.zeldown.joid.lib.ui.core.hook.store.UIStoreHook;
import be.zeldown.joid.lib.ui.core.task.UIScheduledTask;
import be.zeldown.joid.lib.ui.core.transition.Transition;
import be.zeldown.joid.lib.ui.core.transition.impl.PopTransition;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.impl.dev.DevNode;
import be.zeldown.joid.lib.ui.node.property.draggable.DraggableProperty;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.list.IndexedConcurrentList;
import be.zeldown.joid.lib.utils.list.IndexedElement;
import be.zeldown.joid.lib.utils.signal.impl.primitive.DoubleSignal;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class UI implements IUI, IndexedElement {

	@Getter private static UI current;

	@NonNull private static final Color HOVER_COLOR = new Color(16, 0, 16, 180);
	@NonNull private static final Color HOVER_BORDER_COLOR = new Color(30, 55, 153, 180);

	@NonNull private final UIDataObject         data;
	@NonNull private final UIDataDebugObject    debug;
	@NonNull private final UIDataPopupObject    popup;

	@NonNull private final Stack<StencilState>                  stencilStack;
	@NonNull private final Map<Integer[], Runnable>             keybindMap;
	@NonNull private final IndexedConcurrentList<@NonNull Node> nodeList;

	private transient Transition                             transition;
	private transient FileAlterationMonitor                  fileMonitor;
	private transient List<UIScheduledTask>                  scheduledTaskList;
	private transient Map<Class<? extends UIStore>, UIStore> storeMap;

	private final DoubleSignal zoomLevel;
	private final DoubleSignal scaledWidth;
	private final DoubleSignal scaledHeight;

	private boolean initialized;

	private double x;
	private double y;
	private double width;
	private double height;

	private double viewportWidth;
	private double viewportHeight;

	private double fps;
	private long   fpsCounter;
	private long   lastFpsUpdate;
	private long   renderTime;

	private double  mouseX;
	private double  mouseY;
	private double  renderPipelineLevel;
	private boolean onTop;

	private DevNode devNode;

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

		this.zoomLevel = new DoubleSignal(1D);
		this.scaledWidth = new DoubleSignal(1920D);
		this.scaledHeight = new DoubleSignal(1080D);

		this.devNode = null;
	}

	/* [ Bridge Section ] */
	public final void load(final double finalWidth, final double finalHeight) {
		this.load(finalWidth, finalHeight, 1D);
	}

	public final void load(final double finalWidth, final double finalHeight, final double zoomLevel) {
		final long start = System.nanoTime();
		if (JOID.inst().isDevMode() && this.debug.profiler() && !this.initialized) {
			System.out.println("##########################");
			System.out.println("Starting load...");
		}

		this.width  = finalWidth;
		this.height = finalHeight;
		this.zoomLevel.set(zoomLevel);

		final double baseRatio = 16D / 9D;
		final double ratio = this.width / this.height;

		if (Math.abs(ratio - baseRatio) > 0.2D) {
			if (ratio < baseRatio || Math.abs(ratio - baseRatio) < 0.005D) {
				this.x = 0;
				this.y = (this.height - this.width / baseRatio) / 2;
			} else {
				this.x = (this.width - this.height * baseRatio) / 2;
				this.y = 0;
			}
		} else {
			this.x = 0;
			this.y = 0;
		}

		this.viewportWidth  = 1920D * (this.width / (this.width - this.x * 2D));
		this.viewportHeight = 1080D * (this.height / (this.height - this.y * 2D));

		this.updateScaledSize();

		if (!this.initialized) {
			UIPropertyHook.load(this);

			final boolean devNodeEnabled = this.nodeList.contains(this.devNode);
			this.keybindMap.clear();
			this.nodeList.clear();
			this.scheduledTaskList.clear();

			UI.current = this;
			this.init();
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
		}

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
					UI.this.initialized = false;
					UI.this.load(UI.this.width, UI.this.height, UI.this.zoomLevel.getOrDefault());
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

	public final boolean onMousePressed(final @NonNull ClickType clickType) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final InternalContext context = InternalContext.create();

		this.nodeList.reversed().stream().filter(node -> node.getZindex() > 0).forEach(node -> node.onMousePressed(mx, my, clickType, context));
		this.nodeList.reversed().stream().filter(node -> node.getZindex() <= 0).forEach(node -> node.onMousePressed(mx, my, clickType, context));

		this.mousePressed(mx, my, clickType, context);
		return context.isCancelled();
	}

	public final boolean onMouseDragged(final @NonNull ClickType clickType, final long deltaTime) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final InternalContext context = InternalContext.create();
		this.nodeList.reversed().forEach(node -> node.onMouseDragged(mx, my, clickType, deltaTime, context));

		this.mouseDragged(mx, my, clickType, deltaTime, context);
		return context.isCancelled();
	}

	public final boolean onMouseReleased(final @NonNull ClickType clickType) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final InternalContext context = InternalContext.create();
		this.nodeList.reversed().forEach(node -> node.onMouseReleased(mx, my, clickType, context));

		this.mouseReleased(mx, my, clickType, context);
		return context.isCancelled();
	}

	public final boolean onMouseScroll(final int value) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		if (JOID.inst().isDevMode() && Keyboard.isKeyDown(Keyboard.KEY_LMENU) && value != 0) {
			this.zoomLevel.add(value / (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 1000D : 10000D));
			this.updateScaledSize();
			return true;
		}

		final InternalContext context = InternalContext.create();
		this.nodeList.reversed().forEach(node -> node.onMouseScroll(mx, my, value, context));

		this.mouseScroll(mx, my, value, context);
		return context.isCancelled();
	}

	public final boolean onKeyPressed(final char c, final int keyCode) {
		if (!this.initialized) {
			return false;
		}

		final InternalContext context = InternalContext.create();
		this.nodeList.reversed().forEach(node -> node.onKeyPressed(c, keyCode, context));

		if (!context.isCancelled()) {
			for (final Map.Entry<Integer[], Runnable> entry : this.keybindMap.entrySet()) {
				boolean match = true;
				for (final int key : entry.getKey()) {
					if (!Keyboard.isKeyDown(key)) {
						match = false;
						break;
					}
				}

				if (match) {
					entry.getValue().run();
					context.cancel();
				}
			}
		}

		if (this.data.zoomable() && !context.isCancelled()) {
			if ((keyCode == Keyboard.KEY_ADD || c == '+') && (UI.isCtrlKeyDown() || UI.isAltKeyDown())) {
				double tempZoomLevel = this.zoomLevel.getOrDefault();
				tempZoomLevel += 0.1D;
				tempZoomLevel = Math.min(1D, tempZoomLevel);
				if (tempZoomLevel != this.zoomLevel.getOrDefault()) {
					this.zoomLevel.set(tempZoomLevel);
					this.updateScaledSize();
					context.cancel();
				}
			}

			if ((keyCode == Keyboard.KEY_SUBTRACT || c == '-') && (UI.isCtrlKeyDown() || UI.isAltKeyDown())) {
				double tempZoomLevel = this.zoomLevel.getOrDefault();
				tempZoomLevel -= 0.1D;
				tempZoomLevel = Math.max(0.1D, tempZoomLevel);
				if (tempZoomLevel != this.zoomLevel.getOrDefault()) {
					this.zoomLevel.set(tempZoomLevel);
					this.updateScaledSize();
					context.cancel();
				}
			}
		}

		if (JOID.inst().isDevMode() && !context.isCancelled()) {
			if (keyCode == Keyboard.KEY_R && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || keyCode == Keyboard.KEY_F5) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					double tempZoomLevel = this.zoomLevel.getOrDefault();
					tempZoomLevel = 1D;
					if (tempZoomLevel != this.zoomLevel.getOrDefault()) {
						this.zoomLevel.set(tempZoomLevel);
						this.updateScaledSize();
					}
				}

				this.reload();
				context.cancel();
			} else if (keyCode == Keyboard.KEY_F3 && this.devNode != null) {
				final boolean enabled = this.nodeList.contains(this.devNode);
				if (enabled) {
					this.nodeList.remove(this.devNode);
				} else {
					this.devNode.attach(this);
				}
				context.cancel();
			}
		}

		this.keyPressed(c, keyCode, context);
		return context.isCancelled();
	}

	public final void onUpdate() {
		this.nodeList.forEach(Node::onUpdate);
		this.update();
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

	public final void properlyClose() {
		if (this.fileMonitor != null) {
			new Thread(() -> {
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

		UIPropertyHook.save(this);
	}

	public final void draw(final double mouseX, final double mouseY) {
		final long start = System.nanoTime();

		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.onTop = this.getBridge() != null && this.getBridge().isOnTop(this);

		final List<UIScheduledTask> toRemove = new ArrayList<>();
		for (final UIScheduledTask task : this.scheduledTaskList) {
			if (!task.shouldRun()) {
				continue;
			}

			if (!task.execute()) {
				toRemove.add(task);
			}
		}
		this.scheduledTaskList.removeAll(toRemove);

		double tempZoomLevel = this.zoomLevel.getOrDefault();
		tempZoomLevel = Math.min(1D, tempZoomLevel);
		tempZoomLevel = Math.max(0.1D, tempZoomLevel);
		if (tempZoomLevel != this.zoomLevel.getOrDefault()) {
			this.zoomLevel.set(tempZoomLevel);
			this.updateScaledSize();
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
			DrawUtils.SHAPE.drawRect(0, 0, this.width, this.height, this.data.getBackgroundColor().copyAlpha(opacity));
		}

		this.drawBackground(mx, my);

		if (this.transition != null) {
			if (this.transition.getIn() != null && this.transition.getIn().isRunning()) {
				this.transition.getIn().update();
				this.transition.getIn().pre(this, mx, my);
			}

			if (this.transition.getOut() != null && this.transition.getOut().isRunning()) {
				this.transition.getOut().update();
				this.transition.getOut().pre(this, mx, my);
			}
		}

		GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
		if (this.data.projection()) {
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0D, this.viewportWidth, this.viewportHeight, 0D, 0D, 10000D);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
		}

		final double translateX = this.data.anchorX() == Align.START ? 0 : (this.viewportWidth - 1920D) / (1920D / this.data.getAnchorPositionX());
		final double translateY = this.data.anchorY() == Align.START ? 0 : (this.viewportHeight - 1080D) / (1080D / this.data.getAnchorPositionY());
		GLTransformation.create().translate(GLVector.create(translateX, translateY)).apply(() -> {
			this.renderPipelineLevel = 0;
			final AtomicDouble lastRenderPipelineLevel = new AtomicDouble(this.renderPipelineLevel);

			if (this.zoomLevel.getOrDefault() != 1D) {
				GL11.glTranslated(this.data.getAnchorPositionX(), this.data.getAnchorPositionY(), 0D);
				GL11.glScaled(this.zoomLevel.getOrDefault(), this.zoomLevel.getOrDefault(), 1D);
				GL11.glTranslated(-this.data.getAnchorPositionX(), -this.data.getAnchorPositionY(), 0D);
			}

			this.nodeList
			.ordered()
			.stream()
			.filter(node -> node.getZindex() < 0)
			.forEach(node -> {
				GL11.glTranslated(0, 0, this.renderPipelineLevel - lastRenderPipelineLevel.get());
				lastRenderPipelineLevel.set(this.renderPipelineLevel);
				node.render(mx, my);
			});

			GL11.glTranslated(0, 0, this.renderPipelineLevel - lastRenderPipelineLevel.get());
			lastRenderPipelineLevel.set(this.renderPipelineLevel);
			this.preDraw(mx, my);

			this.nodeList
			.ordered()
			.stream()
			.filter(node -> node.getZindex() >= 0 && node.getZindex() < 100)
			.forEach(node -> {
				GL11.glTranslated(0, 0, this.renderPipelineLevel - lastRenderPipelineLevel.get());
				lastRenderPipelineLevel.set(this.renderPipelineLevel);
				node.render(mx, my);
			});

			GL11.glTranslated(0, 0, this.renderPipelineLevel - lastRenderPipelineLevel.get());
			lastRenderPipelineLevel.set(this.renderPipelineLevel);
			this.postDraw(mx, my);

			this.nodeList
			.ordered()
			.stream()
			.filter(node -> node.getZindex() >= 100)
			.forEach(node -> {
				GL11.glTranslated(0, 0, this.renderPipelineLevel - lastRenderPipelineLevel.get());
				lastRenderPipelineLevel.set(this.renderPipelineLevel);
				node.render(mx, my);
			});

			if (this.onTop) {
				GL11.glPushMatrix();
				GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glDepthFunc(GL11.GL_ALWAYS);
				GL11.glDepthMask(true);
				GL11.glDepthMask(false);
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				GL11.glClearDepth(10000D);
				for (final Node node : this.nodeList.reversed()) {
					if (node.renderHover(mx, my)) {
						break;
					}
				}
				GL11.glPopAttrib();
				GL11.glPopMatrix();
			}
		});

		if (this.transition != null) {
			if (this.transition.getIn() != null && this.transition.getIn().isRunning()) {
				this.transition.getIn().post(this, mx, my);
			}

			if (this.transition.getOut() != null && this.transition.getOut().isRunning()) {
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
			this.lastFpsUpdate = System.currentTimeMillis();
		} else {
			this.fpsCounter++;
			final long now = System.currentTimeMillis();
			if (now - this.lastFpsUpdate >= 1000L) {
				this.fps = this.fpsCounter / ((now - this.lastFpsUpdate) / 1000D);
				this.fpsCounter = 0;
				this.lastFpsUpdate = now;
			}
		}
	}

	public void drawHover(final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY) {
		this.getBridge().drawHover(this, lines, mouseX, mouseY);
	}

	/* [ Utility Section ] */
	public final double getMouseX() {
		return this.getRelativeX(this.mouseX * (this.viewportWidth / this.width));
	}

	public final double getMouseY() {
		return this.getRelativeY((this.height - this.mouseY) * (this.viewportHeight / this.height));
	}

	public final double getRelativeX(double value) {
		value -= this.data.anchorX() == Align.START ? 0 : (this.viewportWidth - 1920D) / (1920D / this.data.getAnchorPositionX());

		if (this.zoomLevel.getOrDefault() != 1D) {
			value -= this.data.getAnchorPositionX() * 2D * (1D - this.zoomLevel.getOrDefault()) / 2D;
			value *= 1D / this.zoomLevel.getOrDefault();
		}

		return value;
	}

	public final double getRelativeY(double value) {
		value -= this.data.anchorY() == Align.START ? 0 : (this.viewportHeight - 1080D) / (1080D / this.data.getAnchorPositionY());

		if (this.zoomLevel.getOrDefault() != 1D) {
			value -= this.data.getAnchorPositionY() * 2D * (1D - this.zoomLevel.getOrDefault()) / 2D;
			value *= 1D / this.zoomLevel.getOrDefault();
		}

		return value;
	}

	public final double getAbsoluteX(double value) {
		if (this.zoomLevel.getOrDefault() != 1D) {
			value /= 1D / this.zoomLevel.getOrDefault();
			value += this.data.getAnchorPositionX() * 2D * (1D - this.zoomLevel.getOrDefault()) / 2D;
		}

		value += this.data.anchorX() == Align.START ? 0 : (this.viewportWidth - 1920D) / (1920D / this.data.getAnchorPositionX());
		value /= this.viewportWidth / this.width;

		return value;
	}

	public final double getAbsoluteY(double value) {
		if (this.zoomLevel.getOrDefault() != 1D) {
			value /= 1D / this.zoomLevel.getOrDefault();
			value += this.data.getAnchorPositionY() * 2D * (1D - this.zoomLevel.getOrDefault()) / 2D;
		}

		value += this.data.anchorY() == Align.START ? 0 : (this.viewportHeight - 1080D) / (1080D / this.data.getAnchorPositionY());
		value /= this.viewportHeight / this.height;

		return value;
	}

	public final double getAbsoluteWidth(final double value) {
		return value * this.zoomLevel.getOrDefault() / (this.viewportWidth / this.width);
	}

	public final double getAbsoluteHeight(final double value) {
		return value * this.zoomLevel.getOrDefault() / (this.viewportHeight / this.height);
	}

	public final void mask(final double maskX, final double maskY, final double maskWidth, final double maskHeiht, final @NonNull Drawing drawing) {
		this.mask(maskX, maskY, maskWidth, maskHeiht, drawing, true);
	}

	public final void mask(final double maskX, final double maskY, final double maskWidth, final double maskHeight, final @NonNull Drawing drawing, final boolean enabled) {
		if (enabled) {
			this.startMask(maskX, maskY, maskWidth, maskHeight);
			drawing.draw();
			this.stopMask();
		} else {
			drawing.draw();
		}
	}

	public final void startMask(final double maskX, final double maskY, final double maskWidth, final double maskHeight) {
		final int stencilValue = this.stencilStack.size() + 1;
		this.stencilStack.push(new StencilState(stencilValue, maskX, maskY, maskWidth, maskHeight));
		if (stencilValue == 1) {
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
			GL11.glEnable(GL11.GL_STENCIL_TEST);
		}

		GL11.glStencilFunc(GL11.GL_EQUAL, stencilValue - 1, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);

		GL11.glColorMask(false, false, false, false);
		DrawUtils.SHAPE.drawRect(maskX, maskY, maskWidth, maskHeight, Color.RED);
		GL11.glColorMask(true, true, true, true);

		GL11.glStencilFunc(GL11.GL_EQUAL, stencilValue, 0xFF);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
	}

	public final void stopMask() {
		this.stencilStack.pop();
		final int stencilValue = this.stencilStack.size();
		if (stencilValue == 0) {
			GL11.glDisable(GL11.GL_STENCIL_TEST);
			GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		} else {
			GL11.glStencilFunc(GL11.GL_EQUAL, stencilValue, 0xFF);
			GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		}
	}

	public final void keybind(final @NonNull Runnable runnable, final @NonNull Integer... keys) {
		this.keybindMap.put(keys, runnable);
	}

	public final void reload() {
		if (this.devNode != null) {
			this.devNode.getReloadAnimator().sequence(100F, 1F).push(100F, 0F);
			this.devNode.getReloadAnimator().start();
		}

		this.initialized = false;
		this.load(this.width, this.height, this.zoomLevel.getOrDefault());
	}

	public final void updateScaledSize() {
		final double tempScaledWidth = this.viewportWidth / this.zoomLevel.getOrDefault();
		final double tempScaledHeight = this.viewportHeight / this.zoomLevel.getOrDefault();

		if (this.scaledWidth.getOrDefault() != tempScaledWidth) {
			this.scaledWidth.set(tempScaledWidth);
		}

		if (this.scaledHeight.getOrDefault() != tempScaledHeight) {
			this.scaledHeight.set(tempScaledHeight);
		}
	}

	public final double lerpByFramerate(double value, final double target, final double speed, final double snapDiff, final boolean snap) {
		final double diff = target - value;
		final double absDiff = Math.abs(diff);

		final double offset = speed / ((this.fps == 0D ? 1D : this.fps) / 60D) * absDiff / 3D;

		if (absDiff > snapDiff) {
			value += diff > 0 ? offset : -offset;
		} else if (snap){
			value = target;
		}

		return value;
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

	/* [ Hook Section ] */
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

	/* [ Getter Section ] */
	public final IUIBridge getBridge() {
		return BridgeHandler.get(this);
	}

	/* [ Setter Section ] */
	public final @NonNull UI setTransition(final Transition transition) {
		this.transition = transition;
		return this;
	}

	/* [ Abstract Methods ] */
	@Override
	public int getIndex() {
		return 0;
	}

	/* [ Static Utils ] */
	public static boolean isCtrlKeyDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
	}

	public static boolean isShiftKeyDown()  {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}

	public static boolean isAltKeyDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
	}

	/* [ DTO Section ] */
	@Getter
	private class StencilState {

		public final int value;
		public final double x;
		public final double y;
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