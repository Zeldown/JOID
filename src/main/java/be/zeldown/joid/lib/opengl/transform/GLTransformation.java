package be.zeldown.joid.lib.opengl.transform;

import java.util.LinkedList;
import java.util.List;

import be.zeldown.joid.lib.opengl.context.Drawing;
import be.zeldown.joid.lib.opengl.context.GLContext;
import be.zeldown.joid.lib.opengl.modifier.GLCoords;
import be.zeldown.joid.lib.opengl.modifier.GLRotation;
import be.zeldown.joid.lib.opengl.modifier.GLScale;
import be.zeldown.joid.lib.opengl.transform.glto.GLTO;
import be.zeldown.joid.lib.opengl.transform.glto.GLTORotating;
import be.zeldown.joid.lib.opengl.transform.glto.GLTOScaling;
import be.zeldown.joid.lib.opengl.transform.glto.GLTOTranslating;
import lombok.NonNull;

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
	 * Translates the transformation using the specified coordinates.
	 *
	 * @param coords The translation coordinates. Must not be null.
	 * @return The modified GLTransformation instance.
	 * @throws NullPointerException if coords is null.
	 */
	public @NonNull GLTransformation translate(final @NonNull GLCoords coords) {
	    this.operations.add(new GLTOTranslating(coords));
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
	public @NonNull GLTransformation rotate(final double angle, final @NonNull GLRotation rotation, final @NonNull GLCoords pivot) {
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
	public @NonNull GLTransformation scale(final @NonNull GLScale scale, final @NonNull GLCoords pivot) {
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
	    GLContext.matrix(() -> {
	        for (final GLTO operation : this.operations) {
	            operation.transform();
	        }

	        drawing.draw();
	    });
	}

}