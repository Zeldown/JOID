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

	public static @NonNull GLTransformation create() {
	    return new GLTransformation();
	}

	public @NonNull GLTransformation translate(final @NonNull GLCoords coords) {
	    this.operations.add(new GLTOTranslating(coords));
	    return this;
	}

	public @NonNull GLTransformation rotate(final double angle, final @NonNull GLRotation rotation, final @NonNull GLCoords pivot) {
	    this.operations.add(new GLTORotating(angle, rotation, pivot));
	    return this;
	}

	public @NonNull GLTransformation scale(final @NonNull GLScale scale, final @NonNull GLCoords pivot) {
	    this.operations.add(new GLTOScaling(scale, pivot));
	    return this;
	}

	public void apply(final @NonNull Drawing drawing) {
	    GLContext.matrix(() -> {
	        for (final GLTO operation : this.operations) {
	            operation.transform();
	        }

	        drawing.draw();
	    });
	}

}