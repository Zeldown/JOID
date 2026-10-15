package dev.joid.demo.ui.node.impl.structure.sw;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.sw.SwitchNode;
import lombok.NonNull;

public class DemoSwitchNode extends SwitchNode {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoSwitchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.option((state, index) -> RectNode
		.create(0, 0, 0, 0)
		.color(() -> (state.equals(super.getValue()) ? DemoSwitchNode.INK : Color.WHITE).withAlpha(super.isEnabled() ? 1F : 0.4F))
		.label(() -> Text.create(state, TextInfo.create(DemoFont.MONTSERRAT, 20, state.equals(super.getValue()) ? Color.WHITE : DemoSwitchNode.INK))));
	}

	public static @NonNull DemoSwitchNode create(final double x, final double y, final double width, final double height) {
		return new DemoSwitchNode(x, y, width, height);
	}

}