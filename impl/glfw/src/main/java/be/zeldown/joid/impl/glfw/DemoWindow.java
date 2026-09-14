package be.zeldown.joid.impl.glfw;

import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemoChoice;
import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.bridge.ui.IUIBridge;
import be.zeldown.joid.lib.bridge.ui.UIBridge;
import be.zeldown.joid.lib.bridge.window.IWindowBridge;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.key.Key;
import lombok.Getter;
import lombok.NonNull;

public abstract class DemoWindow extends UIBridge {

	@Getter private final long window;

	private ClickType clickType = null;
	private long lastMouseEvent = 0L;
	private Key pendingKey = null;

	protected DemoWindow() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		this.configureWindow();
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

		this.window = GLFW.glfwCreateWindow(1920, 1080, "JOID - Demo", 0L, 0L);
		if (this.window == 0L) {
			throw new IllegalStateException("Unable to create the GLFW window");
		}

		this.registerBackend(this.window);

		this.registerCallbacks();
		this.identity();
	}

	public void run() {
		this.init();
		this.loop();
	}

	public void init() {
		JOID.open(new UIDemoChoice());
		super.load();
	}

	public void loop() {
		while (!GLFW.glfwWindowShouldClose(this.window)) {
			if (GLFW.glfwGetWindowAttrib(this.window, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE) {
				GLFW.glfwWaitEvents();
				continue;
			}

			GLFW.glfwPollEvents();
			this.flushPendingKey();

			super.update();
			this.beginFrame();
			this.render();
			this.endFrame();
		}

		GLFW.glfwDestroyWindow(this.window);
		GLFW.glfwTerminate();
		System.exit(0);
	}

	protected abstract void configureWindow();

	protected abstract void registerBackend(final long window);

	protected abstract void beginFrame();

	protected abstract void endFrame();

	private void identity() {
		final IWindowBridge windowBridge = BridgeHandler.WINDOW.get();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.ortho(0D, windowBridge.getWidth(), windowBridge.getHeight(), 0D, 0D, 10000D);
		render.viewport(0, 0, windowBridge.getWidth(), windowBridge.getHeight());
	}

	private void render() {
		final IWindowBridge windowBridge = BridgeHandler.WINDOW.get();
		BridgeHandler.RENDER.get().clear(0F, 0F, 0F, 0F);
		DrawUtils.SHAPE.drawRect(0, 0, windowBridge.getWidth(), windowBridge.getHeight(), new Color(50, 50, 50));
		super.draw();
	}

	private void registerCallbacks() {
		GLFW.glfwSetCharCallback(this.window, (handle, codepoint) -> this.onCharacter(codepoint));
		GLFW.glfwSetCursorPosCallback(this.window, (handle, x, y) -> this.onCursorMove());
		GLFW.glfwSetScrollCallback(this.window, (handle, x, y) -> super.mouseScroll((int) (y * 120D)));
		GLFW.glfwSetKeyCallback(this.window, (handle, key, scancode, action, mods) -> this.onKey(key, action, mods));
		GLFW.glfwSetFramebufferSizeCallback(this.window, (handle, width, height) -> this.onResize());
		GLFW.glfwSetMouseButtonCallback(this.window, (handle, button, action, mods) -> this.onMouseButton(button, action));
	}

	private void onKey(final int code, final int action, final int mods) {
		if (action == GLFW.GLFW_RELEASE) {
			return;
		}

		this.flushPendingKey();
		final Key key = WindowBridge.getKey(code);
		if (DemoWindow.isTextKey(code) && (mods & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_ALT)) == 0) {
			this.pendingKey = key;
			return;
		}

		super.keyTyped((char) 0, key);
	}

	private void onCharacter(final int codepoint) {
		final Key key = this.pendingKey == null ? Key.UNKNOWN : this.pendingKey;
		this.pendingKey = null;
		super.keyTyped((char) codepoint, key);
	}

	private void flushPendingKey() {
		if (this.pendingKey == null) {
			return;
		}

		final Key key = this.pendingKey;
		this.pendingKey = null;
		super.keyTyped((char) 0, key);
	}

	private void onMouseButton(final int button, final int action) {
		if (action == GLFW.GLFW_PRESS) {
			this.clickType = ClickType.from(button);
			this.lastMouseEvent = System.currentTimeMillis();
			super.mousePressed(this.clickType);
		} else if (this.clickType != null) {
			super.mouseReleased(this.clickType);
			this.clickType = null;
		}
	}

	private void onCursorMove() {
		if (this.clickType != null && this.lastMouseEvent > 0L) {
			super.mouseDragged(this.clickType, System.currentTimeMillis() - this.lastMouseEvent);
		}
	}

	private void onResize() {
		if (BridgeHandler.WINDOW.get().getWidth() == 0 || BridgeHandler.WINDOW.get().getHeight() == 0) {
			return;
		}

		this.identity();
		super.load();
	}

	private static boolean isTextKey(final int code) {
		return code >= GLFW.GLFW_KEY_SPACE && code <= GLFW.GLFW_KEY_GRAVE_ACCENT || code >= GLFW.GLFW_KEY_KP_0 && code <= GLFW.GLFW_KEY_KP_ADD;
	}

	@Override
	public void drawHover(final @NonNull UI ui, final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY) {
		if (lines.isEmpty() || DemoFont.MONTSERRAT == null) {
			return;
		}

		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE);

		final double paddingX = 10D;
		final double paddingY = 6D;
		final double lineGap  = 2D;
		final double lineHeight = info.getHeight();

		double width = 0D;
		for (final String line : lines) {
			width = Math.max(width, info.getWidth(line));
		}
		width += paddingX * 2D;
		final double height = paddingY * 2D + lines.size() * lineHeight + Math.max(0, lines.size() - 1) * lineGap;

		double x = mouseX + 14D;
		double y = mouseY + 14D;

		if (x + width > ui.getWidth() - 4D) {
			x = mouseX - width - 14D;
		}
		if (y + height > ui.getHeight() - 4D) {
			y = mouseY - height - 14D;
		}
		if (x < 4D) {
			x = 4D;
		}
		if (y < 4D) {
			y = 4D;
		}

		DrawUtils.SHAPE.drawRoundedRect(x, y, width, height, Color.decode("#27272a"), 6F);
		DrawUtils.SHAPE.drawRoundedRect(x + 1D, y + 1D, width - 2D, height - 2D, Color.decode("#18181b"), 5F);

		double textY = y + paddingY;
		for (final String line : lines) {
			DrawUtils.TEXT.drawText(x + paddingX, textY, line, info, Align.START, Align.START);
			textY += lineHeight + lineGap;
		}
	}

	@Override
	public void open(final @NonNull UI ui) {
		if (!ui.getPopup().active()) {
			for (final UI currentUi : super.getUiList()) {
				final boolean result = currentUi.onClose();
				if (currentUi.getTransition() != null && currentUi.getTransition().getOut() != null && currentUi.getTransition().getOut().isRunning()) {
					currentUi.getTransition().getOut().getAnimator().setCallback(tween -> {
						JOID.open(ui);
					});
					return;
				}

				if (!result) {
					return;
				}

				this.close(currentUi);
			}
		}

		this.add(ui);
	}

	@Override
	public void close(final @NonNull UI ui) {
		this.remove(ui);
	}

	@Override
	public void add(final @NonNull UI ui) {
		super.getUiList().add(ui);
		ui.load(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
	}

	@Override
	public void remove(final @NonNull UI ui) {
		super.getUiList().remove(ui);
	}

	@Override
	public boolean isOnTop(final @NonNull UI ui) {
		if (this.getUiList().isEmpty()) {
			return false;
		}
		return this.getUiList().ordered().getLast() == ui && ui.getData().active() && ui.getData().visible();
	}

	@Override
	public boolean canHandle(final @NonNull Class<? extends UI> ui) {
		return true;
	}

	@Override
	public boolean canHandle(final @NonNull UI ui) {
		return true;
	}

	@Override
	public int getIndex() {
		return 0;
	}

	@Override
	public @NonNull IUIBridge getInstance() {
		return this;
	}

}