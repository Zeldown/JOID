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

	public void bind(final @NonNull Vector4f canvas) {
		GradientShader.bind(new Vector2f(this.direction.x, this.direction.y), new Vector2f(this.direction.z, this.direction.w), this.startColor, this.endColor, canvas);
	}

	public void unbind() {
		GradientShader.unbind();
	}

}