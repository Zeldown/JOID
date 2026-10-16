package dev.joid.demo.ui.node.impl.control.choice;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.control.choice.SwitchNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class DemoSwitchNode<V> extends SwitchNode<V> {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoSwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.option((value, index) -> RectNode
		.create(0, 0, 0, 0)
		.color(() -> (value.equals(super.getValue()) ? DemoSwitchNode.INK : Color.WHITE).withAlpha(super.isEnabled() ? 1F : 0.4F))
		.label(() -> Text.create(value, TextInfo.create(DemoFont.MONTSERRAT, 20, value.equals(super.getValue()) ? Color.WHITE : DemoSwitchNode.INK))));
	}

	public static <V> @NonNull DemoSwitchNode<V> create(final double x, final double y, final double width, final double height) {
		return new DemoSwitchNode<>(x, y, width, height);
	}

}