package be.zeldown.joid.lib.opengl.modifier;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class GLCoords {

	private double x;
	private double y;
	private double z;

	private GLCoords(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static @NonNull GLCoords create() {
		return new GLCoords(0D, 0D, 0D);
	}

	public static @NonNull GLCoords create(final double x, final double y, final double z) {
		return new GLCoords(x, y, z);
	}

	public static @NonNull GLCoords create(final double x, final double y) {
		return new GLCoords(x, y, 0D);
	}

	public static @NonNull GLCoords X(final double x) {
		return new GLCoords(x, 0D, 0D);
	}

	public static @NonNull GLCoords Y(final double y) {
		return new GLCoords(0D, y, 0D);
	}

	public static @NonNull GLCoords Z(final double z) {
		return new GLCoords(0D, 0D, z);
	}

	public @NonNull GLCoords x(final double x) {
		this.x = x;
		return this;
	}

	public @NonNull GLCoords y(final double y) {
		this.y = y;
		return this;
	}

	public @NonNull GLCoords z(final double z) {
		this.z = z;
		return this;
	}

	public @NonNull GLCoords add(final double x, final double y, final double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

}