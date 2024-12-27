package be.zeldown.joid.lib.ui.core;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.util.concurrent.AtomicDouble;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.internal.font.InternalFont;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.opengl.context.Drawing;
import be.zeldown.joid.lib.opengl.modifier.GLCoords;
import be.zeldown.joid.lib.opengl.transform.GLTransformation;
import be.zeldown.joid.lib.ui.bridge.BridgeHandler;
import be.zeldown.joid.lib.ui.bridge.IUIBridge;
import be.zeldown.joid.lib.ui.core.data.UIData;
import be.zeldown.joid.lib.ui.core.data.UIDataObject;
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
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.list.IndexedConcurrentList;
import be.zeldown.joid.lib.utils.list.IndexedElement;
import be.zeldown.joid.lib.utils.signal.impl.primitive.DoubleSignal;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class UI implements IUI, IndexedElement {

	@NonNull private static final Color HOVER_COLOR = new Color(16, 0, 16, 180);
	@NonNull private static final Color HOVER_BORDER_COLOR = new Color(30, 55, 153, 180);

	@NonNull private final UIDataObject data;
	@NonNull private final Map<Integer[], Runnable> keybindMap;
	@NonNull private final IndexedConcurrentList<@NonNull Node> nodeList;

	private final DoubleSignal scaledWidth;
	private final DoubleSignal scaledHeight;

	private transient FileAlterationMonitor fileMonitor;
	private transient Map<Class<? extends UIStore>, UIStore> storeMap;
	private transient List<UIScheduledTask> scheduledTaskList;

	private transient Transition transition;
	private transient IUIBridge bridge;

	private boolean initialized;

	private double x;
	private double y;
	private double width;
	private double height;

	private double viewportWidth;
	private double viewportHeight;

	private double fps;
	private long fpsCounter;
	private long lastFpsUpdate;
	private long renderTime;

	private double renderPipelineLevel;
	private boolean onTop;

	private DevNode devNode;

	public UI() {
		if (this.getClass().isAnnotationPresent(UIData.class)) {
			this.data = new UIDataObject(this.getClass().getAnnotation(UIData.class));
		} else if (this.getClass().getSuperclass() != null && this.getClass().getSuperclass().isAnnotationPresent(UIData.class)) {
			this.data = new UIDataObject(this.getClass().getSuperclass().getAnnotation(UIData.class));
		} else {
			this.data = new UIDataObject();
		}

		this.nodeList = new IndexedConcurrentList<>();
		this.keybindMap = new HashMap<>();
		this.storeMap = new HashMap<>();
		this.scheduledTaskList = new ArrayList<>();

		if (this.data.popup().active() && this.data.popup().transition().isActive()) {
			this.transition = new PopTransition();

			if (!this.data.popup().transition().isIn()) {
				this.transition.getIn().disable();
			}

			if (!this.data.popup().transition().isOut()) {
				this.transition.getOut().disable();
			}
		}

		this.scaledWidth = new DoubleSignal(1920D);
		this.scaledHeight = new DoubleSignal(1080D);

		this.devNode = null;
	}

	/* [ Bridge Section ] */
	public final void load(final double screenWidth, final double screenHeight) {
		final long start = System.nanoTime();
		if (JOID.inst().isDevMode() && this.data.profiler() && !this.initialized) {
			System.out.println("##########################");
			System.out.println("Starting load...");
		}

		this.width  = screenWidth;
		this.height = screenHeight;

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
			this.nodeList.clear();
			this.scheduledTaskList.clear();

			this.init();
			this.initialized = true;

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
					this.devNode = DevNode.create(1625, 1007).visible(node -> node.getUi().isOnTop()).enabled(node -> node.getUi().isOnTop()).draggable(DraggableProperty.screen()).zindex(Integer.MAX_VALUE);
				}
			}

			if (JOID.inst().isDevMode() && this.data.profiler()) {
				final long end = System.nanoTime();
				System.out.println("Load completed in " + String.format("%.2f", (end - start) / 1000000F) + "ms");
				System.out.println("##########################");
			}
		}

		if (JOID.inst().isDevMode() && this.data.hotreload() && this.fileMonitor == null) {
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
					UI.this.load(UI.this.width, UI.this.height);
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

	public final boolean onMousePressed(final int clickType) {
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

	public final boolean onMouseDragged(final int clickType, final long deltaTime) {
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

	public final boolean onMouseReleased(final int clickType) {
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

		if (JOID.inst().isDevMode() && !context.isCancelled()) {
			if (keyCode == Keyboard.KEY_R && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || keyCode == Keyboard.KEY_F5) {
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

	public final void draw() {
		final long start = System.nanoTime();
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

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0D, this.viewportWidth, this.viewportHeight, 0D, 0D, 10000D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		final double translateX = this.data.anchorX() == Align.START ? 0 : (this.viewportWidth - 1920D) / (1920D / this.data.getAnchorPositionX());
		final double translateY = this.data.anchorY() == Align.START ? 0 : (this.viewportHeight - 1080D) / (1080D / this.data.getAnchorPositionY());
		GLTransformation.create().translate(GLCoords.create(translateX, translateY)).apply(() -> {
			this.renderPipelineLevel = 0;
			final AtomicDouble lastRenderPipelineLevel = new AtomicDouble(this.renderPipelineLevel);

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

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0D, this.width, this.height, 0D, 0D, 10000D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

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

		if (JOID.inst().isDevMode() && this.data.profiler()) {
			final float ms = this.renderTime / 1_000_000F;
			if (ms > 16.66F) {
				System.err.println("##########################");
				System.err.println("[!] Frame took " + String.format("%.2f", ms) + "ms to render");
				System.err.println("##########################");
			}
		}

		if (this.lastFpsUpdate == 0L) {
			this.fps = 30;
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

	/* [ Utility Section ] */
	/**
	 * Gets the X-coordinate of the mouse within the scaled viewport.
	 *
	 * @return The X-coordinate of the mouse.
	 */
	public final double getMouseX() {
		return this.getRelativeX(Mouse.getX() * (this.viewportWidth / this.width));
	}

	/**
	 * Gets the Y-coordinate of the mouse within the scaled viewport.
	 *
	 * @return The Y-coordinate of the mouse.
	 */
	public final double getMouseY() {
		return this.getRelativeY((this.height - Mouse.getY()) * (this.viewportHeight / this.height));
	}

	/**
	 * Converts a value from the source coordinate space to the scaled viewport coordinate space along the x-axis.
	 *
	 * @param value The value to convert.
	 * @return The converted value in the scaled viewport coordinate space.
	 */
	public final double getRelativeX(final double value) {
		return value - (this.data.anchorX() == Align.START ? 0 : (this.viewportWidth - 1920D) / (1920D / this.data.getAnchorPositionX()));
	}

	/**
	 * Converts a value from the source coordinate space to the scaled viewport coordinate space along the y-axis.
	 *
	 * @param value The value to convert.
	 * @return The converted value in the scaled viewport coordinate space.
	 */
	public final double getRelativeY(final double value) {
		return value - (this.data.anchorY() == Align.START ? 0 : (this.viewportHeight - 1080D) / (1080D / this.data.getAnchorPositionY()));
	}

	/**
	 * Converts a value from the absolute coordinate space to the scaled viewport coordinate space along the x-axis.
	 *
	 * @param value The value to convert.
	 * @return The converted value in the scaled viewport coordinate space.
	 */
	public final double getAbsoluteX(double value) {
		value += this.data.anchorX() == Align.START ? 0 : (this.viewportWidth - 1920D) / (1920D / this.data.getAnchorPositionX());
		value /= this.viewportWidth / this.width;
		return value;
	}

	/**
	 * Converts a value from the absolute coordinate space to the scaled viewport coordinate space along the y-axis.
	 *
	 * @param value The value to convert.
	 * @return The converted value in the scaled viewport coordinate space.
	 */
	public final double getAbsoluteY(double value) {
		value += this.data.anchorY() == Align.START ? 0 : (this.viewportHeight - 1080D) / (1080D / this.data.getAnchorPositionY());
		value /= this.viewportHeight / this.height;
		return value;
	}

	/**
	 * Converts a value from the absolute coordinate space to the scaled viewport coordinate space along the width-axis.
	 *
	 * @param value The value to convert.
	 * @return The converted value in the scaled viewport coordinate space.
	 */
	public final double getAbsoluteWidth(final double value) {
		return value / (this.viewportWidth / this.width);
	}


	/**
	 * Converts a value from the absolute coordinate space to the scaled viewport coordinate space along the height-axis.
	 *
	 * @param value The value to convert.
	 * @return The converted value in the scaled viewport coordinate space.
	 */
	public final double getAbsoluteHeight(final double value) {
		return value / (this.viewportHeight / this.height);
	}

	/**
	 * Enables stencil and sets the stencil mask based on the specified parameters.
	 *
	 * @param stencilX      The x-coordinate of the stencil region in local coordinates.
	 * @param stencilY      The y-coordinate of the stencil region in local coordinates.
	 * @param stencilWidth  The width of the stencil region in local coordinates.
	 * @param stencilHeight The finalHeight of the stencil region in local coordinates.
	 */
	public final void startStencil(final double stencilX, final double stencilY, final double stencilWidth, final double stencilHeight) {
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 255);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glColorMask(false, false, false, false);
		DrawUtils.SHAPE.drawRect(stencilX, stencilY, stencilWidth, stencilHeight, Color.RED);
		GL11.glColorMask(true, true, true, true);

		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 255);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
	}

	/**
	 * Disables stencil.
	 */
	public final void endStencil() {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
	}

	/**
	 * Draws the content of the specified drawing within a stencil region.
	 *
	 * @param stencilX      The x-coordinate of the stencil region in local coordinates.
	 * @param stencilY      The y-coordinate of the stencil region in local coordinates.
	 * @param stencilWidth  The width of the stencil region in local coordinates.
	 * @param stencilHeight The finalHeight of the stencil region in local coordinates.
	 * @param drawing       The drawing to be rendered within the stencil region. Must not be null.
	 */
	public final void stencil(final double stencilX, final double stencilY, final double stencilWidth, final double stencilHeight, final @NonNull Drawing drawing) {
		this.stencil(stencilX, stencilY, stencilWidth, stencilHeight, drawing, true);
	}

	/**
	 * Draws the content of the specified drawing within a stencil region, optionally enabling or disabling stencil testing.
	 *
	 * @param stencilX      The x-coordinate of the stencil region in local coordinates.
	 * @param stencilY      The y-coordinate of the stencil region in local coordinates.
	 * @param stencilWidth  The width of the stencil region in local coordinates.
	 * @param stencilHeight The finalHeight of the stencil region in local coordinates.
	 * @param drawing       The drawing to be rendered within the stencil region. Must not be null.
	 * @param enableStencil Flag indicating whether to enable stencil testing.
	 */
	public final void stencil(final double stencilX, final double stencilY, final double stencilWidth, final double stencilHeight, final @NonNull Drawing drawing, final boolean enableStencil) {
		if (enableStencil) {
			this.startStencil(stencilX, stencilY, stencilWidth, stencilHeight);
		}
		drawing.draw();
		if (enableStencil) {
			this.endStencil();
		}
	}

	/**
	 * Draws a hover tooltip with the specified list of text lines at the given coordinates and context.
	 *
	 * @param texts   The list of text lines to be displayed in the tooltip.
	 * @param x       The x-coordinate of the top-left corner of the tooltip.
	 * @param y       The y-coordinate of the top-left corner of the tooltip.
	 * @param context The drawing context providing necessary information for rendering.
	 * @throws NullPointerException if the provided list of texts or drawing context is null.
	 */
	public void drawHover(final List<@NonNull String> lines, double x, double y) {
		if (lines == null || lines.isEmpty()) {
			return;
		}

		x += 25;

		final TextInfo textInfo = TextInfo.create(InternalFont.MONTSERRAT_SEMI_BOLD, 24, Color.WHITE);
		final double fontHeight = textInfo.getHeight();

		final int offset = 3;
		final int padding = 15;

		double width = 0;
		for (final String text : lines) {
			if (text != null) {
				width = Math.max(width, textInfo.getWidth(text) + padding * 2);
			}
		}

		int height = (int) (fontHeight * lines.size() + padding * 2);
		y -= height;

		if (y < 0) {
			y = 0;
		}

		if (x + width > 1920) {
			x -= width + 50;

			if (x < 0) {
				y += height;

				final List<String> copiedLines = new ArrayList<>(lines);
				lines.clear();
				for (final String line : copiedLines) {
					lines.addAll(DrawUtils.TEXT.getLines(1920, Text.create(line, textInfo)));
				}

				width = 0;
				for (final String text : lines) {
					if (text != null) {
						width = Math.max(width, textInfo.getWidth(text) + padding * 2);
					}
				}

				height = (int) (fontHeight * lines.size() + padding * 2);
				y -= height + padding;

				if (y < 0) {
					y = 0;
				}

				x = 1920 / 2 - width / 2;
			}
		}

		DrawUtils.SHAPE.drawRect(x - offset, y, width + offset * 2, height, UI.HOVER_COLOR);
		DrawUtils.SHAPE.drawRect(x, y - offset, width, offset, UI.HOVER_COLOR);
		DrawUtils.SHAPE.drawRect(x, y + height, width, offset, UI.HOVER_COLOR);

		DrawUtils.SHAPE.drawRect(x, y + offset, offset, height - offset * 2, UI.HOVER_BORDER_COLOR);
		DrawUtils.SHAPE.drawRect(x + width - offset, y + offset, offset, height - offset * 2, UI.HOVER_BORDER_COLOR);
		DrawUtils.SHAPE.drawRect(x + offset, y, width - offset * 2, offset, UI.HOVER_BORDER_COLOR);
		DrawUtils.SHAPE.drawRect(x + offset, y + height - offset, width - offset * 2, offset, UI.HOVER_BORDER_COLOR);

		DrawUtils.SHAPE.drawRect(x + offset, y + offset, width - offset * 2, height - offset * 2, UI.HOVER_COLOR);

		for (int i = 0; i < lines.size(); i++) {
			final String text = lines.get(i);
			if (text != null) {
				DrawUtils.TEXT.drawText(x + padding, y + padding + i * fontHeight, Text.create(text, textInfo));
			}
		}
	}

	/**
	 * Bind the specified runnable to the specified keys.
	 *
	 * @param runnable
	 * @param keys
	 * @throws NullPointerException if the provided runnable or keys are null.
	 */
	public final void keybind(final @NonNull Runnable runnable, final @NonNull Integer... keys) {
		this.keybindMap.put(keys, runnable);
	}

	/**
	 * Reloads the UI.
	 */
	public final void reload() {
		if (this.devNode != null) {
			this.devNode.getReloadAnimator().sequence(100F, 1F).push(100F, 0F);
			this.devNode.getReloadAnimator().start();
		}

		this.initialized = false;
		this.load(this.width, this.height);
	}

	public final void updateScaledSize() {
		final double tempScaledWidth = this.viewportWidth;
		final double tempScaledHeight = this.viewportHeight;

		if (this.scaledWidth.getOrDefault() != tempScaledWidth) {
			this.scaledWidth.set(tempScaledWidth);
		}

		if (this.scaledHeight.getOrDefault() != tempScaledHeight) {
			this.scaledHeight.set(tempScaledHeight);
		}
	}

	/**
	 * Interpolates the specified value to the specified target value based on the specified speed.
	 *
	 * @param value
	 * @param target
	 * @param speed
	 * @param snapDiff
	 * @param snap
	 * @return The interpolated value.
	 */
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

	/**
	 * Adds the specified non-null nodes to the list of nodes.
	 * <p>
	 * The nodes are appended to the end of the existing list.
	 * </p>
	 *
	 * @param nodes The non-null nodes to be added to the list.
	 * @throws NullPointerException If any of the provided nodes is {@code null}.
	 */
	public final void add(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			node.load(this);
			this.nodeList.add(node);
		}
	}

	/**
	 * Sets the rendering pipeline level to the specified value.
	 *
	 * @param level The rendering pipeline level to be set.
	 */
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
	private final IUIBridge getBridge() {
		if (this.bridge != null) {
			return this.bridge;
		}

		return this.bridge = BridgeHandler.get(this);
	}

	/* [ Setter Section ] */
	public final @NonNull UI setTransition(final Transition transition) {
		this.transition = transition;
		return this;
	}

	/* [ Abstract Methods ] */
	@Override
	public int getIndex() {
		return this.data.zindex();
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

}