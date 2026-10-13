package dev.joid.lib.color;

import dev.joid.lib.shader.impl.GradientShader;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.point.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class ColorGradient {

	private final Color startColor;
	private final Color endColor;
	private final Point startPoint;
	private final Point endPoint;

	public void use(final @NonNull Runnable runnable, final @NonNull BoundingBox canvas) {
		this.use(false, runnable, canvas);
	}

	public void use(final boolean hasTexture, final @NonNull Runnable runnable, final @NonNull BoundingBox canvas) {
		GradientShader.use(this.startPoint, this.endPoint, this.startColor, this.endColor, hasTexture, runnable, canvas);
	}

}