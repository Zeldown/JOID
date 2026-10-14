package dev.joid.lib.bridge.ui;

import java.util.List;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.converter.TextConverter;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

public class WindowUIBridge extends StackUIBridge {

	@Getter private Color    background;
	@Getter private Color    hoverColor;
	@Getter private TextInfo hoverInfo;
	@Getter private Color    hoverBorderColor;

	protected WindowUIBridge() {
		this.background       = Color.BLACK;
		this.hoverColor       = Color.decode("#18181b");
		this.hoverBorderColor = Color.decode("#27272a");
	}

	public static @NonNull WindowUIBridge create() {
		return new WindowUIBridge();
	}

	public final <T extends WindowUIBridge> @NonNull T background(final @NonNull Color background) {
		this.background = background;
		return (T) this;
	}

	public final <T extends WindowUIBridge> @NonNull T hoverColor(final @NonNull Color hoverColor) {
		this.hoverColor = hoverColor;
		return (T) this;
	}

	public final <T extends WindowUIBridge> @NonNull T hoverInfo(final @NonNull TextInfo hoverInfo) {
		this.hoverInfo = hoverInfo;
		return (T) this;
	}

	public final <T extends WindowUIBridge> @NonNull T hoverBorderColor(final @NonNull Color hoverBorderColor) {
		this.hoverBorderColor = hoverBorderColor;
		return (T) this;
	}

	@Override
	public void drawHover(final @NonNull UI ui, final @NonNull Object content, final double mouseX, final double mouseY) {
		final List<String> lines = TextConverter.convertLines(content);
		final TextInfo info = this.getHoverInfo();
		if (info == null) {
			JOIDLog.devWarnOnce("WindowUIBridge.hoverInfo." + System.identityHashCode(this), () -> this.getClass().getSimpleName() + " draws no tooltip, set its hoverInfo(TextInfo) to draw them");
			return;
		}

		if (lines.isEmpty()) {
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
		if (x + width > ui.getView().toUiX(ui.getWindowWidth()) - 4D) {
			x = mouseX - width - 14D;
		}
		if (y + height > ui.getView().toUiY(ui.getWindowHeight()) - 4D) {
			y = mouseY - height - 14D;
		}
		if (x < left) {
			x = left;
		}
		if (y < top) {
			y = top;
		}

		DrawUtils.SHAPE.drawRoundedRect(x, y, width, height, this.hoverBorderColor, 6F);
		DrawUtils.SHAPE.drawRoundedRect(x + 1D, y + 1D, width - 2D, height - 2D, this.hoverColor, 5F);

		double textY = y + paddingY;
		for (final String line : lines) {
			DrawUtils.TEXT.drawText(x + paddingX, textY, line, info, Align.START, Align.START);
			textY += lineHeight + lineGap;
		}
	}

	@Override
	protected void drawBackground() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.clearColor(this.background.getRedF(), this.background.getGreenF(), this.background.getBlueF(), this.background.getAlphaF());
		render.clearDepth();
		render.clearStencil();
	}

}