package be.zeldown.joid.demo;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import be.zeldown.joid.demo.ui.UIDemoChoice;
import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.ui.bridge.UIBridge;
import be.zeldown.joid.lib.ui.core.UI;
import lombok.NonNull;

public class DemoWindow extends UIBridge {

	private final DisplayMode displayMode;

	private int clickType = -1;
	private long lastMouseEvent = 0L;

	public DemoWindow() throws LWJGLException {
		this.displayMode = new DisplayMode(1920, 1080);
		Display.setDisplayMode(this.displayMode);
		Display.setResizable(true);
		Display.setTitle("JOID - Demo");
		Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));

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

	private void identity() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0D, Display.getWidth(), Display.getHeight(), 0D, 0D, 10000D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		GL11.glClearColor(0F, 0F, 0F, 0F);
	}

	private void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		DrawUtils.SHAPE.drawRect(0, 0, Display.getWidth(), Display.getHeight(), new Color(50, 50, 50));
		super.draw();
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
				final int clickType = Mouse.getEventButton();
				final int scroll = Mouse.getEventDWheel();

				if (Mouse.getEventButtonState()) {
					this.clickType = clickType;
					this.lastMouseEvent = System.currentTimeMillis();
					super.mousePressed(clickType);
				} else if (clickType != -1) {
					this.clickType = -1;
					super.mouseReleased(clickType);
				} else if (this.clickType != -1 && this.lastMouseEvent > 0L) {
					super.mouseDragged(this.clickType, System.currentTimeMillis() - this.lastMouseEvent);
				}

				if (scroll != 0) {
					super.mouseScroll(scroll);
				}
			}

			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					super.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
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

	@Override
	public void open(final @NonNull UI ui) {
		this.add(ui);
	}

	@Override
	public void close(final @NonNull UI ui) {
		this.remove(ui);
	}

	@Override
	public void add(final @NonNull UI ui) {
		super.getUiList().add(ui);
		ui.load(Display.getWidth(), Display.getHeight());
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

}