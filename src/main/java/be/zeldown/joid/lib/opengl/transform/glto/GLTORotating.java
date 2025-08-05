package be.zeldown.joid.lib.opengl.transform.glto;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.opengl.modifier.GLVector;
import be.zeldown.joid.lib.opengl.modifier.GLRotation;
import lombok.NonNull;

public class GLTORotating implements GLTO {

	private final Supplier<Double> angleSupplier;
	private final GLRotation       rotation;
	private final GLVector         pivot;

	public GLTORotating(final double angle, final @NonNull GLRotation rotation, final @NonNull GLVector pivot) {
		this(() -> angle, rotation, pivot);
	}

	public GLTORotating(final @NonNull Supplier<Double> angleSupplier, final @NonNull GLRotation rotation, final @NonNull GLVector pivot) {
		this.angleSupplier = angleSupplier;
		this.rotation = rotation;
		this.pivot = pivot;
	}

	@Override
	public void transform() {
		final double x = this.rotation.getRawX();
		final double y = this.rotation.getRawY();
		final double z = this.rotation.getRawZ();
		final double pivotX = this.pivot.getX();
		final double pivotY = this.pivot.getY();
		final double pivotZ = this.pivot.getZ();
		GL11.glTranslated(pivotX, pivotY, pivotZ);
		GL11.glRotated(this.angleSupplier.get(), x, y, z);
		GL11.glTranslated(-pivotX, -pivotY, -pivotZ);
	}

	@Override
	public void reset() {
		final double x = this.rotation.getRawX();
		final double y = this.rotation.getRawY();
		final double z = this.rotation.getRawZ();
		final double pivotX = this.pivot.getX();
		final double pivotY = this.pivot.getY();
		final double pivotZ = this.pivot.getZ();
		GL11.glTranslated(pivotX, pivotY, pivotZ);
		GL11.glRotated(-this.angleSupplier.get(), x, y, z);
		GL11.glTranslated(-pivotX, -pivotY, -pivotZ);
	}

}