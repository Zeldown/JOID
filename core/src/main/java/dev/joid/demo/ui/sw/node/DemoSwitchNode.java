package dev.joid.demo.ui.sw.node;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.impl.structure.sw.SwitchNode;
import dev.joid.lib.utils.align.Align;
import lombok.NonNull;

public class DemoSwitchNode extends SwitchNode {

	protected DemoSwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoSwitchNode create(final double x, final double y, final double width, final double height) {
		return new DemoSwitchNode(x, y, width, height);
	}

	@Override
	public void init(final @NonNull UI ui) {
		RectNode
		.create(0, 0, super.getWidth(), super.getHeight())
		.color(Color.BLACK)
		.attach(this);

		final double stateWidth = super.getWidth() / super.getStateList().size();
		FlexNode
		.horizontal(0, 0, super.getHeight())
		.body(flex -> {
			for (final String state : super.getStateList().getOrDefault()) {
				RectNode
				.create(0, 0, stateWidth, super.getHeight())
				.color(super.getState().equals(state) ? Color.BLUE : Color.RED)
				.body(rect -> {
					TextNode
					.create(0, 0, rect.getWidth(), rect.getHeight())
					.text(Text.create(state, TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE).shadow(Color.BLACK), Align.CENTER, Align.CENTER))
					.attach(rect);
				})
				.onClick((node, mouseX, mouseY, clickType) -> {
					super.index(state);
				})
				.attach(flex);
			}
		}).attach(this);
	}

}