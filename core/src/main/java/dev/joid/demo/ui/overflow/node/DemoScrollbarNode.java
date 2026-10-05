package dev.joid.demo.ui.overflow.node;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.structure.scrollbar.ScrollbarNode;
import dev.joid.lib.utils.box.BoundingBox;
import lombok.NonNull;

public class DemoScrollbarNode extends ScrollbarNode {

	private DemoScrollbarNode(final double x, final double y, final double width, final double height, final @NonNull BoundingBox scroll) {
		super(x, y, width, height, scroll);
	}

	public static final @NonNull DemoScrollbarNode create(final double x, final double y, final double width,final double height, final @NonNull BoundingBox scroll) {
		return new DemoScrollbarNode(x, y, width, height, scroll);
	}

	@Override
	public void drawScrollbar(final double mouseX, final double mouseY) {
		final BoundingBox scroll = super.getScroll();
		DrawUtils.SHAPE.drawRect(scroll.getMinX(), scroll.getMinY(), scroll.getWidth(), scroll.getHeight(), Color.WHITE);
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.BLUE);
	}

}