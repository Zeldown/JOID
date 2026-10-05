package be.zeldown.joid.lib.bridge.render.state;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlendState {

	public static final BlendState NORMAL        = BlendState.create(Equation.ADD, Factor.SRC_ALPHA, Factor.ONE_MINUS_SRC_ALPHA, Factor.ONE, Factor.ONE_MINUS_SRC_ALPHA);
	public static final BlendState DISABLED      = new BlendState(false, Equation.ADD, Factor.ONE, Factor.ZERO, Factor.ONE, Factor.ZERO);
	public static final BlendState PREMULTIPLIED = BlendState.create(Equation.ADD, Factor.ONE, Factor.ONE_MINUS_SRC_ALPHA);

	private final boolean  enabled;
	private final Equation equation;
	private final Factor   sourceColor;
	private final Factor   destinationColor;
	private final Factor   sourceAlpha;
	private final Factor   destinationAlpha;

	public static @NonNull BlendState create(final @NonNull Equation equation, final @NonNull Factor source, final @NonNull Factor destination) {
		return new BlendState(true, equation, source, destination, source, destination);
	}

	public static @NonNull BlendState create(final @NonNull Equation equation, final @NonNull Factor sourceColor, final @NonNull Factor destinationColor, final @NonNull Factor sourceAlpha, final @NonNull Factor destinationAlpha) {
		return new BlendState(true, equation, sourceColor, destinationColor, sourceAlpha, destinationAlpha);
	}

	public enum Equation {

		ADD,
		SUBTRACT,
		REVERSE_SUBTRACT,
		MIN,
		MAX;

	}

	public enum Factor {

		ZERO,
		ONE,
		SRC_COLOR,
		ONE_MINUS_SRC_COLOR,
		DST_COLOR,
		ONE_MINUS_DST_COLOR,
		SRC_ALPHA,
		ONE_MINUS_SRC_ALPHA,
		DST_ALPHA,
		ONE_MINUS_DST_ALPHA;

	}

}