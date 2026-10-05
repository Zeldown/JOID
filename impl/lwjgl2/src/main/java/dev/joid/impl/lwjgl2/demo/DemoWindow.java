package dev.joid.impl.lwjgl2.demo;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import dev.joid.demo.DemoUIBridge;
import dev.joid.demo.ui.UIDemoChoice;
import dev.joid.impl.lwjgl2.Backend;
import dev.joid.impl.lwjgl2.window.WindowBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.utils.click.ClickType;

public class DemoWindow extends DemoUIBridge {

	private ClickType clickType = null;
	private long lastMouseEvent = 0L;

	public DemoWindow() throws LWJGLException {
		Display.setDisplayMode(new DisplayMode(1920, 1080));
		Display.setResizable(true);
		Display.setTitle("JOID - Demo (LWJGL 2)");
		Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));

		this.identity();
	}

	public void init() {
		JOID.open(new UIDemoChoice());
		super.load();
	}

	public static void main(final String[] args) throws LWJGLException {
		Backend.register();
		final DemoWindow window = new DemoWindow();
		BridgeHandler.UI.register(window);
		JOID.inst().setDevMode(true).setDemoMode(true).load();
		window.run();
	}

	public void run() {
		this.init();
		this.loop();
	}

	public void loop() {
		while (!Display.isCloseRequested()) {
			if (!Display.isVisible()) {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				Display.update();
				continue;
			}

			while (Mouse.next()) {
				final int scroll = Mouse.getEventDWheel();
				final int button = Mouse.getEventButton();
				final boolean state = Mouse.getEventButtonState();

				if (state && button != -1) {
					this.clickType = ClickType.from(button);
					this.lastMouseEvent = System.currentTimeMillis();
					super.mousePressed(this.clickType);
				} else if (this.clickType != null && !state && button != -1) {
					super.mouseReleased(this.clickType);
					this.clickType = null;
				} else if (this.clickType != null && this.lastMouseEvent > 0L && button == -1) {
					super.mouseDragged(this.clickType, System.currentTimeMillis() - this.lastMouseEvent);
				}

				if (scroll != 0) {
					super.mouseScroll(scroll);
				}
			}

			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					super.keyTyped(Keyboard.getEventCharacter(), WindowBridge.getKey(Keyboard.getEventKey()));
				}
			}

			super.update();
			this.render();
			Display.update();

			if (Display.wasResized()) {
				this.identity();
				super.load();
			}
		}

		Display.destroy();
		System.exit(0);
	}

	private void identity() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.ortho(0D, Display.getWidth(), Display.getHeight(), 0D, 0D, 10000D);
		render.viewport(0, 0, Display.getWidth(), Display.getHeight());
	}

	private void render() {
		BridgeHandler.RENDER.get().clear(0F, 0F, 0F, 0F);
		DrawUtils.SHAPE.drawRect(0, 0, Display.getWidth(), Display.getHeight(), new Color(50, 50, 50));
		super.draw();
	}

}