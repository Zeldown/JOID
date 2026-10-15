package dev.joid.showcase;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.control.slider.DoubleSliderNode;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public interface IShowKit {

	public void backdrop(final Node layer);

	public TextInfo hint();
	public TextInfo title();
	public TextInfo label();
	public TextInfo accent();
	public TextInfo display();

	public RectNode divider(final double x, final double y, final double width);
	public RectNode panel(final double x, final double y, final double width, final double height);
	public RectNode choice(final double x, final double y, final String label, final BooleanSupplier selected);
	public RectNode button(final double x, final double y, final double width, final Supplier<String> label, final boolean primary);

	public ToggleNode<Boolean, Boolean> toggle(final double x, final double y);
	public TextFieldNode field(final double x, final double y, final double width);
	public DoubleSliderNode slider(final double x, final double y, final double width);

}