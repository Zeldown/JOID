package be.zeldown.joid.lib.opengl.transform;

import java.util.LinkedList;
import java.util.List;

import be.zeldown.joid.lib.opengl.context.Drawing;
import be.zeldown.joid.lib.opengl.modifier.GLRotation;
import be.zeldown.joid.lib.opengl.modifier.GLScale;
import be.zeldown.joid.lib.opengl.modifier.GLVector;
import be.zeldown.joid.lib.opengl.transform.glto.GLTO;
import be.zeldown.joid.lib.opengl.transform.glto.GLTORotating;
import be.zeldown.joid.lib.opengl.transform.glto.GLTOScaling;
import be.zeldown.joid.lib.opengl.transform.glto.GLTOTranslating;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class GLTransformation {

	@NonNull private final List<@NonNull GLTO> operations;

	private GLTransformation() {
		this.operations = new LinkedList<>();
	}

	/**
	 * Creates a new GLTransformation instance with no initial transformations.
	 *
	 * @return A new GLTransformation instance.
	 */
	public static @NonNull GLTransformation create() {
		return new GLTransformation();
	}

	/**
	 * Creates a new GLTransformation instance with a single transformation
	 * operation.
	 *
	 * @param operation The transformation operation to add. Must not be null.
	 * @return A new GLTransformation instance containing the specified operation.
	 * @throws NullPointerException if operation is null.
	 */
	public static @NonNull GLTransformation create(final @NonNull GLTO operation) {
		return new GLTransformation().add(operation);
	}

	/**
	 * Adds a transformation operation to the transformation list.
	 *
	 * @param operation The transformation operation to add. Must not be null.
	 * @return The modified GLTransformation instance.
	 * @throws NullPointerException if operation is null.
	 */
	public @NonNull GLTransformation add(final @NonNull GLTO operation) {
		this.operations.add(operation);
		return this;
	}

	/**
	 * Translates the transformation using the specified coordinates.
	 *
	 * @param coords The translation coordinates. Must not be null.
	 * @return The modified GLTransformation instance.
	 * @throws NullPointerException if coords is null.
	 */
	public @NonNull GLTransformation translate(final @NonNull GLVector vector) {
		this.operations.add(new GLTOTranslating(vector));
		return this;
	}

	/**
	 * Rotates the transformation by the specified angle around the given rotation axis with the specified pivot point.
	 *
	 * @param angle    The rotation angle in degrees.
	 * @param rotation The rotation angles around the yaw, pitch, and roll axes. Must not be null.
	 * @param pivot    The pivot point for the rotation. Must not be null.
	 * @return The modified GLTransformation instance.
	 * @throws NullPointerException if rotation or pivot is null.
	 */
	public @NonNull GLTransformation rotate(final double angle, final @NonNull GLRotation rotation, final @NonNull GLVector pivot) {
		this.operations.add(new GLTORotating(angle, rotation, pivot));
		return this;
	}

	/**
	 * Scales the transformation using the specified scale factors and pivot point.
	 *
	 * @param scale The scale factors along the x, y, and z axes. Must not be null.
	 * @param pivot The pivot point for the scaling. Must not be null.
	 * @return The modified GLTransformation instance.
	 * @throws NullPointerException if scale or pivot is null.
	 */
	public @NonNull GLTransformation scale(final @NonNull GLScale scale, final @NonNull GLVector pivot) {
		this.operations.add(new GLTOScaling(scale, pivot));
		return this;
	}

	/**
	 * Applies the accumulated transformations to the specified drawing.
	 *
	 * @param drawing The drawing to which the transformations will be applied. Must not be null.
	 * @throws NullPointerException if drawing is null.
	 */
	public void apply(final @NonNull Drawing drawing) {
		this.operations.forEach(GLTO::transform);
		drawing.draw();
		this.operations.forEach(GLTO::reset);
	}

	/**
	 * Applies the accumulated transformations
	 */
	public void apply() {
		this.operations.forEach(GLTO::transform);
	}

	/**
	 * Resets all transformations applied by this GLTransformation instance.
	 */
	public void reset() {
		this.operations.forEach(GLTO::reset);
	}

	/**
	 * Clears all transformation operations from this GLTransformation instance.
	 */
	public void clear() {
		this.operations.clear();
	}

}