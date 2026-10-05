package be.zeldown.joid.demo;

import java.util.List;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.ui.IUIBridge;
import be.zeldown.joid.lib.bridge.ui.UIBridge;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.font.dto.TextInfo;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.align.Align;
import lombok.NonNull;

public class DemoUIBridge extends UIBridge {

	@Override
	public void close(final @NonNull UI ui) {
		this.remove(ui);
	}

	@Override
	public int getIndex() {
		return 0;
	}

	@Override
	public void add(final @NonNull UI ui) {
		super.getUiList().add(ui);
		ui.load(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
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
	public @NonNull IUIBridge getInstance() {
		return this;
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
	public boolean canHandle(final @NonNull UI ui) {
		return true;
	}

	@Override
	public boolean canHandle(final @NonNull Class<? extends UI> ui) {
		return true;
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

		DrawUtils.SHAPE.drawRoundedRect(x, y, width, height, Color.decode("#27272a"), 6F);
		DrawUtils.SHAPE.drawRoundedRect(x + 1D, y + 1D, width - 2D, height - 2D, Color.decode("#18181b"), 5F);

		double textY = y + paddingY;
		for (final String line : lines) {
			DrawUtils.TEXT.drawText(x + paddingX, textY, line, info, Align.START, Align.START);
			textY += lineHeight + lineGap;
		}
	}

}