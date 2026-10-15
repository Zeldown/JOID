package dev.joid.showcase;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.control.scrollbar.ScrollbarNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;

public class ShowScrollbarNode extends ScrollbarNode {

	protected ShowScrollbarNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedRect(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE.withAlpha(0.06F), (float) node.getWidth() / 2F)));
		super.thumb(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawShadow(0D, 0D, node.getWidth(), node.getHeight(), ShowUI.VIOLET.withAlpha(0.6F), (float) node.getWidth() / 2F, 12F);
			DrawUtils.SHAPE.drawRoundedRect(0D, 0D, node.getWidth(), node.getHeight(), ShowUI.vertical(ShowUI.FUCHSIA, ShowUI.VIOLET), (float) node.getWidth() / 2F);
		}));
	}

	public static ShowScrollbarNode create(final double x, final double y, final double width, final double height) {
		return new ShowScrollbarNode(x, y, width, height);
	}

}