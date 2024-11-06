package be.zeldown.joid.lib.color;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import be.zeldown.joid.lib.shader.impl.GradientShader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class ColorGradient {

	private final Color    startColor;
	private final Color    endColor;
	private final Vector4f direction;

	public void use(final @NonNull Vector4f canvas, final @NonNull Runnable runnable) {
		GradientShader.use(new Vector2f(this.direction.x, this.direction.y), new Vector2f(this.direction.z, this.direction.w), this.startColor, this.endColor, canvas, runnable);
	}

}