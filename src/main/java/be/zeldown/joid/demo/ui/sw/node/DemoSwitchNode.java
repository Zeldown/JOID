package be.zeldown.joid.demo.ui.sw.node;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;
import be.zeldown.joid.lib.ui.node.impl.structure.sw.SwitchNode;
import be.zeldown.joid.lib.utils.align.Align;
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