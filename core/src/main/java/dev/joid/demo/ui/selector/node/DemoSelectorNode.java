package dev.joid.demo.ui.selector.node;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode;
import dev.joid.lib.utils.align.Align;
import lombok.NonNull;

public class DemoSelectorNode extends SelectorNode<String> {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(238, 238, 238);

	protected DemoSelectorNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoSelectorNode create(final double x, final double y, final double width, final double height) {
		return new DemoSelectorNode(x, y, width, height);
	}

	@Override
	protected @NonNull Node option(final @NonNull String value) {
		return RectNode
		.create(0, 0, super.getDefaultWidth(), super.getDefaultHeight())
		.color(() -> Color.WHITE.copyAlpha(super.isEnabled() ? 1F : 0.4F))
		.hoveredColor(DemoSelectorNode.PLACEHOLDER)
		.body(rect -> {
			TextNode
			.create(16, rect.dh(2))
			.text(() -> Text.create(value, TextInfo.create(DemoFont.MONTSERRAT, 22, DemoSelectorNode.INK.copyAlpha(super.isEnabled() ? 1F : 0.4F)), Align.START, Align.CENTER))
			.anchorY(Align.CENTER)
			.attach(rect);
		});
	}

	@Override
	public void drawBackground(final double mouseX, final double mouseY) {
		if (super.isActive()) {
			DrawUtils.SHAPE.drawRect(super.getX() - 2D, super.getY() - (super.getDirection().isDown() ? 2D : super.getHeight() * (super.getOptionMap().size() - 1) + 2D), super.getWidth() + 4D, super.getDefaultHeight() * super.getOptionMap().size() + 4D, DemoSelectorNode.INK);
		}
	}

}