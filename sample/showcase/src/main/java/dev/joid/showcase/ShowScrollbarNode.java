package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.scrollbar.ScrollbarNode;
import dev.joid.lib.utils.box.BoundingBox;

public class ShowScrollbarNode extends ScrollbarNode {

	protected ShowScrollbarNode(final double x, final double y, final double width, final double height, final BoundingBox scroll) {
		super(x, y, width, height, scroll);
	}

	public static ShowScrollbarNode create(final double x, final double y, final double width, final double height, final BoundingBox scroll) {
		return new ShowScrollbarNode(x, y, width, height, scroll);
	}

	@Override
	public void drawScrollbar(final double mouseX, final double mouseY) {
		final BoundingBox scroll = super.getScroll();
		DrawUtils.SHAPE.drawRoundedRect(scroll.getMinX(), scroll.getMinY(), scroll.getWidth(), scroll.getHeight(), Color.WHITE.copyAlpha(0.06F), (float) scroll.getWidth() / 2F);
		DrawUtils.SHAPE.drawShadow(super.getX(), super.getY(), super.getWidth(), super.getHeight(), ShowUI.VIOLET.copyAlpha(0.6F), (float) super.getWidth() / 2F, 12F);
		DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), ShowUI.vertical(ShowUI.FUCHSIA, ShowUI.VIOLET), (float) super.getWidth() / 2F);
	}

}