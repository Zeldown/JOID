package dev.joid.showcase;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.DoubleSliderNode;

public interface IShowKit {

	public void backdrop(final Node layer);

	public TextInfo display();
	public TextInfo accent();
	public TextInfo title();
	public TextInfo label();
	public TextInfo hint();

	public RectNode panel(final double x, final double y, final double width, final double height);
	public RectNode divider(final double x, final double y, final double width);
	public RectNode choice(final double x, final double y, final String label, final BooleanSupplier selected);
	public RectNode button(final double x, final double y, final double width, final Supplier<String> label, final boolean primary);

	public CheckboxNode toggle(final double x, final double y);
	public DoubleSliderNode slider(final double x, final double y, final double width);
	public TextFieldNode field(final double x, final double y, final double width);

}