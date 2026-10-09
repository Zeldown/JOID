package dev.joid.lib.font;

import java.util.function.DoubleSupplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FontScale {

	private static final ThreadLocal<DoubleSupplier> SCALE = new ThreadLocal<>();

	public static void run(final @NonNull DoubleSupplier scale, final @NonNull Runnable runnable) {
		final DoubleSupplier previous = FontScale.SCALE.get();
		FontScale.SCALE.set(scale);
		try {
			runnable.run();
		} finally {
			FontScale.SCALE.set(previous);
		}
	}

	public static double getScale() {
		final DoubleSupplier scale = FontScale.SCALE.get();
		if (scale != null) {
			return scale.getAsDouble();
		}

		final IRenderBridge render = BridgeHandler.RENDER.find(bridge -> true);
		if (render == null) {
			return 1D;
		}

		final PixelGrid grid = render.getPixelGrid();
		return Math.min(grid.getScaleX(), grid.getScaleY());
	}

}