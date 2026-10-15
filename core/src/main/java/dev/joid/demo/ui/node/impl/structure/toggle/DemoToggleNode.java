package dev.joid.demo.ui.node.impl.structure.toggle;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.toggle.ToggleNode;
import lombok.NonNull;

public class DemoToggleNode extends ToggleNode<String, String> {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.track(RectNode.create(0, 0, 0, 0).color(() -> Color.WHITE.withAlpha(super.isEnabled() ? 1F : 0.4F)));
		super.knob(RectNode.create(0, 0, 0, 0).color(() -> DemoToggleNode.INK.withAlpha(super.isEnabled() ? 1F : 0.4F)));
	}

	public static @NonNull DemoToggleNode create(final double x, final double y, final double width, final double height) {
		return new DemoToggleNode(x, y, width, height);
	}

}