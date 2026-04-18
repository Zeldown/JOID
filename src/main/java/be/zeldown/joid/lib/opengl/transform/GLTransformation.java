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

	public static @NonNull GLTransformation create() {
		return new GLTransformation();
	}

	public static @NonNull GLTransformation create(final @NonNull GLTO operation) {
		return new GLTransformation().add(operation);
	}

	public @NonNull GLTransformation add(final @NonNull GLTO operation) {
		this.operations.add(operation);
		return this;
	}

	public @NonNull GLTransformation translate(final @NonNull GLVector vector) {
		this.operations.add(new GLTOTranslating(vector));
		return this;
	}

	public @NonNull GLTransformation rotate(final double angle, final @NonNull GLRotation rotation, final @NonNull GLVector pivot) {
		this.operations.add(new GLTORotating(angle, rotation, pivot));
		return this;
	}

	public @NonNull GLTransformation scale(final @NonNull GLScale scale, final @NonNull GLVector pivot) {
		this.operations.add(new GLTOScaling(scale, pivot));
		return this;
	}

	public void apply(final @NonNull Drawing drawing) {
		this.operations.forEach(GLTO::transform);
		drawing.draw();
		this.operations.forEach(GLTO::reset);
	}

	public void apply() {
		this.operations.forEach(GLTO::transform);
	}

	public void reset() {
		this.operations.forEach(GLTO::reset);
	}

	public void clear() {
		this.operations.clear();
	}

}