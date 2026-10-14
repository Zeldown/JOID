package dev.joid.lib.obj.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public final class ObjGroup {

	@Setter private String   name;
	@Setter private DrawMode drawMode;

	private final List<ObjFace> faces;

	public ObjGroup() {
		this("");
	}

	public ObjGroup(final @NonNull String name) {
		this(name, null);
	}

	public ObjGroup(final @NonNull String name, final DrawMode drawMode) {
		this.name     = name;
		this.drawMode = drawMode;
		this.faces    = new ArrayList<>();
	}

	public void addFace(final @NonNull ObjFace face) {
		this.faces.add(face);
	}

	public void render() {
		if (this.faces.size() > 0) {
			final Tessellator tessellator = Tessellator.inst().copy();
			tessellator.start(this.drawMode);
			for (final ObjFace face : this.faces) {
				face.render(tessellator);
			}
			tessellator.draw();
		}
	}

	public @NonNull List<ObjFace> getFaces() {
		return Collections.unmodifiableList(this.faces);
	}

}