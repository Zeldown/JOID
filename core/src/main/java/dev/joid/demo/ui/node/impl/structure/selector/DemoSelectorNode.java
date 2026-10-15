package dev.joid.demo.ui.node.impl.structure.selector;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode;
import dev.joid.lib.utils.align.Align;
import lombok.NonNull;

public class DemoSelectorNode extends SelectorNode<String> {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(238, 238, 238).lock();

	protected DemoSelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.field(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> {
			if (super.isActive()) {
				DrawUtils.SHAPE.drawRect(-2D, -2D, node.getWidth() + 4D, node.getHeight() + 4D, DemoSelectorNode.INK);
			}
		}));
		super.option(value -> RectNode
		.create(0, 0, width, height)
		.color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F))
		.hoveredColor(DemoSelectorNode.PLACEHOLDER)
		.body(rect -> {
			TextNode
			.create(16, rect.dh(2))
			.text(() -> Text.create(value, TextInfo.create(DemoFont.MONTSERRAT, 22, DemoSelectorNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F))).alignX(Align.START).alignY(Align.CENTER))
			.anchorY(Align.CENTER)
			.attach(rect);
		}));
	}

	public static @NonNull DemoSelectorNode create(final double x, final double y, final double width, final double height) {
		return new DemoSelectorNode(x, y, width, height);
	}

}