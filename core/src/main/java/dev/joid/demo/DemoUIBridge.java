package dev.joid.demo;

import java.util.List;

import dev.joid.demo.ui.UIDemoChoice;
import dev.joid.internal.JOID;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.ui.StackUIBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.converter.TextConverter;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.align.Align;
import lombok.NonNull;

public class DemoUIBridge extends StackUIBridge {

	private static final Color BACKGROUND   = new Color(50, 50, 50);
	private static final Color HOVER        = Color.decode("#18181b");
	private static final Color HOVER_BORDER = Color.decode("#27272a");

	public void start() {
		JOID.open(new UIDemoChoice());
		super.load();
	}

	public void resize(final int width, final int height) {
		BridgeHandler.RENDER.get().screen(width, height);
		super.load();
	}

	public void frame() {
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		super.update();
		render.beginFrame();
		try {
			render.clearColor(0F, 0F, 0F, 0F);
			DrawUtils.SHAPE.drawRect(0, 0, window.getWidth(), window.getHeight(), DemoUIBridge.BACKGROUND);
			super.draw();
		} finally {
			render.endFrame();
		}
	}


	@Override
	public void drawHover(final @NonNull UI ui, final @NonNull Object content, final double mouseX, final double mouseY) {
		final List<String> lines = TextConverter.convertLines(content);
		final TextInfo info = this.getHoverInfo();
		if (lines.isEmpty() || info == null) {
			return;
		}

		final double paddingX = 10D;
		final double paddingY = 6D;
		final double lineGap = 2D;
		final double lineHeight = info.getHeight();

		double width = 0D;
		for (final String line : lines) {
			width = Math.max(width, info.getWidth(line));
		}
		width += paddingX * 2D;
		final double height = paddingY * 2D + lines.size() * lineHeight + Math.max(0, lines.size() - 1) * lineGap;

		double x = mouseX + 14D;
		double y = mouseY + 14D;

		final double left = ui.getView().toUiX(0D) + 4D;
		final double top = ui.getView().toUiY(0D) + 4D;
		if (x + width > ui.getView().toUiX(ui.getWidth()) - 4D) {
			x = mouseX - width - 14D;
		}
		if (y + height > ui.getView().toUiY(ui.getHeight()) - 4D) {
			y = mouseY - height - 14D;
		}
		if (x < left) {
			x = left;
		}
		if (y < top) {
			y = top;
		}

		DrawUtils.SHAPE.drawRoundedRect(x, y, width, height, DemoUIBridge.HOVER_BORDER, 6F);
		DrawUtils.SHAPE.drawRoundedRect(x + 1D, y + 1D, width - 2D, height - 2D, DemoUIBridge.HOVER, 5F);

		double textY = y + paddingY;
		for (final String line : lines) {
			DrawUtils.TEXT.drawText(x + paddingX, textY, line, info, Align.START, Align.START);
			textY += lineHeight + lineGap;
		}
	}

	protected TextInfo getHoverInfo() {
		return DevFont.MONTSERRAT != null ? TextInfo.create(DevFont.MONTSERRAT, 20, Color.WHITE) : null;
	}

}