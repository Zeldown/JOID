package dev.joid.lib.obj.data;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class ObjGroup {

	private String        name;
	private DrawMode      drawMode;
	private List<ObjFace> faces;

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

}