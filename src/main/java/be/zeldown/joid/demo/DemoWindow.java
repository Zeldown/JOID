package be.zeldown.joid.demo;

import java.util.List;

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
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.bridge.IUIBridge;
import be.zeldown.joid.lib.ui.bridge.UIBridge;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.click.ClickType;
import lombok.NonNull;

public class DemoWindow extends UIBridge {

	private final DisplayMode displayMode;

	private ClickType clickType = null;
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