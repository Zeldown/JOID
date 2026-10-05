package be.zeldown.joid.lib.bridge.render.matrix;

import java.util.ArrayDeque;
import java.util.Deque;

import lombok.Getter;

public final class MatrixStack {

	private final Deque<float[]> stack;

	@Getter
	private float[] matrix;

	public MatrixStack() {
		this.stack  = new ArrayDeque<>();
		this.matrix = MatrixStack.identityMatrix();
	}

	public void pop() {
		this.matrix = this.stack.pop();
	}

	public void push() {
		this.stack.push(this.matrix.clone());
	}

	public void identity() {
		this.matrix = MatrixStack.identityMatrix();
	}

	public float[] getNormalMatrix() {
		final float[] m = this.matrix;
		final float c00 = m[5] * m[10] - m[9] * m[6];
		final float c01 = m[9] * m[2] - m[1] * m[10];
		final float c02 = m[1] * m[6] - m[5] * m[2];
		final float c10 = m[8] * m[6] - m[4] * m[10];
		final float c11 = m[0] * m[10] - m[8] * m[2];
		final float c12 = m[4] * m[2] - m[0] * m[6];
		final float c20 = m[4] * m[9] - m[8] * m[5];
		final float c21 = m[8] * m[1] - m[0] * m[9];
		final float c22 = m[0] * m[5] - m[4] * m[1];
		final float determinant = m[0] * c00 + m[4] * c01 + m[8] * c02;
		final float inverse = determinant == 0F ? 0F : 1F / determinant;
		return new float[] {
				c00 * inverse, c10 * inverse, c20 * inverse,
				c01 * inverse, c11 * inverse, c21 * inverse,
				c02 * inverse, c12 * inverse, c22 * inverse
		};
	}

	public void multiply(final float[] other) {
		final float[] result = new float[16];
		for (int column = 0; column < 4; column++) {
			for (int row = 0; row < 4; row++) {
				float value = 0F;
				for (int k = 0; k < 4; k++) {
					value += this.matrix[k * 4 + row] * other[column * 4 + k];
				}
				result[column * 4 + row] = value;
			}
		}
		this.matrix = result;
	}

	public void scale(final double x, final double y, final double z) {
		final float[] m = this.matrix;
		for (int i = 0; i < 4; i++) {
			m[i] *= x;
			m[4 + i] *= y;
			m[8 + i] *= z;
		}
	}

	public void translate(final double x, final double y, final double z) {
		final float[] m = this.matrix;
		m[12] = (float) (m[0] * x + m[4] * y + m[8] * z + m[12]);
		m[13] = (float) (m[1] * x + m[5] * y + m[9] * z + m[13]);
		m[14] = (float) (m[2] * x + m[6] * y + m[10] * z + m[14]);
		m[15] = (float) (m[3] * x + m[7] * y + m[11] * z + m[15]);
	}

	public void rotate(final double angle, final double x, final double y, final double z) {
		final double length = Math.sqrt(x * x + y * y + z * z);
		if (length == 0D) {
			return;
		}

		final double nx = x / length;
		final double ny = y / length;
		final double nz = z / length;
		final double radians = Math.toRadians(angle);
		final double c = Math.cos(radians);
		final double s = Math.sin(radians);
		final double t = 1D - c;

		this.multiply(new float[] {
				(float) (t * nx * nx + c), (float) (t * nx * ny + s * nz), (float) (t * nx * nz - s * ny), 0F,
				(float) (t * nx * ny - s * nz), (float) (t * ny * ny + c), (float) (t * ny * nz + s * nx), 0F,
				(float) (t * nx * nz + s * ny), (float) (t * ny * nz - s * nx), (float) (t * nz * nz + c), 0F,
				0F, 0F, 0F, 1F
		});
	}

	public void ortho(final double left, final double right, final double bottom, final double top, final double near, final double far) {
		this.matrix = new float[] {
				(float) (2D / (right - left)), 0F, 0F, 0F,
				0F, (float) (2D / (top - bottom)), 0F, 0F,
				0F, 0F, (float) (-2D / (far - near)), 0F,
				(float) (-(right + left) / (right - left)), (float) (-(top + bottom) / (top - bottom)), (float) (-(far + near) / (far - near)), 1F
		};
	}

	private static float[] identityMatrix() {
		return new float[] {
				1F, 0F, 0F, 0F,
				0F, 1F, 0F, 0F,
				0F, 0F, 1F, 0F,
				0F, 0F, 0F, 1F
		};
	}

}