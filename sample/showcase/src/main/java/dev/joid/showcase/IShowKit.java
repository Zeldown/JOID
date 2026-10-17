package dev.joid.showcase;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.control.slider.DoubleSliderNode;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public interface IShowKit {

	public void backdrop(final @NonNull Node layer);

	public @NonNull TextInfo hint();
	public @NonNull TextInfo title();
	public @NonNull TextInfo label();
	public @NonNull TextInfo accent();
	public @NonNull TextInfo display();

	public @NonNull RectNode divider(final double x, final double y, final double width);
	public @NonNull RectNode panel(final double x, final double y, final double width, final double height);
	public @NonNull RectNode choice(final double x, final double y, final @NonNull String label, final @NonNull BooleanSupplier selected);
	public @NonNull RectNode button(final double x, final double y, final double width, final @NonNull Supplier<String> label, final boolean primary);

	public @NonNull ToggleNode<Boolean, Boolean> toggle(final double x, final double y);
	public @NonNull TextFieldNode field(final double x, final double y, final double width);
	public @NonNull DoubleSliderNode slider(final double x, final double y, final double width);

	public static double wave(final double phase) {
		return Math.sin((BridgeHandler.CLOCK.get().currentTimeMillis() / 9000D + phase) * Math.PI * 2D);
	}

}