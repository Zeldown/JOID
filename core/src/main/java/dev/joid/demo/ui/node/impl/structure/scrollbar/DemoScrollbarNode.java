package dev.joid.demo.ui.node.impl.structure.scrollbar;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.scrollbar.ScrollbarNode;
import lombok.NonNull;

public class DemoScrollbarNode extends ScrollbarNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoScrollbarNode(final double x, final double y, final double width, final double height, final double thumb) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(Color.WHITE));
		super.thumb(RectNode.create(0, 0, width > height ? thumb : 0, width > height ? 0 : thumb).color(DemoScrollbarNode.INK));
	}

	public static @NonNull DemoScrollbarNode create(final double x, final double y, final double width, final double height, final double thumb) {
		return new DemoScrollbarNode(x, y, width, height, thumb);
	}

}