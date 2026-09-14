package be.zeldown.joid.test.snapshot;

import java.util.ArrayList;
import java.util.List;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.ui.IUIBridge;
import be.zeldown.joid.lib.bridge.ui.UIBridge;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.align.Align;
import lombok.NonNull;

public final class SnapshotUIBridge extends UIBridge {

	public void closeAll() {
		for (final UI ui : new ArrayList<>(super.getUiList().ordered())) {
			ui.properlyClose();
			super.getUiList().remove(ui);
		}
	}

	@Override
	public void drawHover(final @NonNull UI ui, final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY) {
		if (lines.isEmpty() || DemoFont.MONTSERRAT == null) {
			return;
		}

		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE);
		double width = 0D;
		for (final String line : lines) {
			width = Math.max(width, info.getWidth(line));
		}

		width += 20D;
		final double height = 12D + lines.size() * info.getHeight() + Math.max(0, lines.size() - 1) * 2D;
		final double x = mouseX + 14D;
		final double y = mouseY + 14D;
		DrawUtils.SHAPE.drawRoundedRect(x, y, width, height, Color.decode("#27272a"), 6F);
		DrawUtils.SHAPE.drawRoundedRect(x + 1D, y + 1D, width - 2D, height - 2D, Color.decode("#18181b"), 5F);

		double textY = y + 6D;
		for (final String line : lines) {
			DrawUtils.TEXT.drawText(x + 10D, textY, line, info, Align.START, Align.START);
			textY += info.getHeight() + 2D;
		}
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
		ui.load(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
	}

	@Override
	public void remove(final @NonNull UI ui) {
		super.getUiList().remove(ui);
	}

	@Override
	public boolean isOnTop(final @NonNull UI ui) {
		return !super.getUiList().isEmpty() && super.getUiList().ordered().getLast() == ui;
	}

	@Override
	public boolean canHandle(final @NonNull Class<? extends UI> clazz) {
		return true;
	}

	@Override
	public boolean canHandle(final @NonNull UI ui) {
		return true;
	}

	@Override
	public @NonNull IUIBridge getInstance() {
		return this;
	}

}