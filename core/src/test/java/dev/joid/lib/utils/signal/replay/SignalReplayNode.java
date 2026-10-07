package dev.joid.lib.utils.signal.replay;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class SignalReplayNode extends Node {

	private Supplier<String>  text;
	private Supplier<Color>   color;
	private Supplier<Boolean> shown;
	private Supplier<Double>  sizeX;
	private Supplier<Double>  sizeY;

	private Supplier<SignalReplayCaption> caption;

	protected SignalReplayNode(final double x, final double y) {
		super(x, y);
	}

	public static @NonNull SignalReplayNode create() {
		return new SignalReplayNode(0D, 0D);
	}

	public static @NonNull SignalReplayNode titled(final String title) {
		return SignalReplayNode.create().text(title);
	}

	public final <T extends SignalReplayNode> @NonNull T text(final String text) {
		return this.text(Signal.from(text));
	}

	public final <T extends SignalReplayNode> @NonNull T text(final @NonNull Supplier<String> text) {
		this.text = text;
		return (T) this;
	}

	public final <T extends SignalReplayNode> @NonNull T prefixed(final String text) {
		return this.text("> " + text);
	}

	public final <T extends SignalReplayNode> @NonNull T color(final Color color) {
		return this.color(Signal.from(color));
	}

	public final <T extends SignalReplayNode> @NonNull T color(final @NonNull Supplier<Color> color) {
		this.color = color;
		return (T) this;
	}

	public final <T extends SignalReplayNode> @NonNull T caption(final SignalReplayCaption caption) {
		return this.caption(Signal.from(caption));
	}

	public final <T extends SignalReplayNode> @NonNull T caption(final @NonNull Supplier<SignalReplayCaption> caption) {
		this.caption = caption;
		return (T) this;
	}

	public final <T extends SignalReplayNode> @NonNull T shown(final boolean shown) {
		this.shown = Signal.from(shown);
		return (T) this;
	}

	public final <T extends SignalReplayNode> @NonNull T extent(final double sizeX, final double sizeY) {
		this.sizeY = Signal.from(sizeY);
		this.sizeX = Signal.from(sizeX);
		return (T) this;
	}

}