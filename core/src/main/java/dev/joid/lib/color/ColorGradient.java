package dev.joid.lib.color;

import dev.joid.lib.shader.impl.GradientShader;
import dev.joid.lib.utils.geom.BoundingBox;
import dev.joid.lib.utils.geom.Point;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColorGradient {

	private final Color startColor;
	private final Color endColor;
	private final Point startPoint;
	private final Point endPoint;

	public static @NonNull ColorGradient of(final @NonNull Color startColor, final @NonNull Color endColor, final @NonNull Point startPoint, final @NonNull Point endPoint) {
		return new ColorGradient(startColor, endColor, startPoint, endPoint);
	}

	public void use(final @NonNull Runnable runnable, final @NonNull BoundingBox canvas) {
		this.use(false, runnable, canvas);
	}

	public void use(final boolean hasTexture, final @NonNull Runnable runnable, final @NonNull BoundingBox canvas) {
		GradientShader.use(this.startPoint, this.endPoint, this.startColor, this.endColor, hasTexture, runnable, canvas);
	}

}