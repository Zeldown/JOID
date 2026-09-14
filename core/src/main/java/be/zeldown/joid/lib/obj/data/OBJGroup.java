package be.zeldown.joid.lib.obj.data;

import java.util.ArrayList;
import java.util.List;

import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.render.tessellator.Tessellator;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class OBJGroup {

	private String        name;
	private DrawMode      drawMode;
	private List<OBJFace> faces;

	public OBJGroup() {
		this("");
	}

	public OBJGroup(final @NonNull String name) {
		this(name, null);
	}

	public OBJGroup(final @NonNull String name, final DrawMode drawMode) {
		this.name     = name;
		this.drawMode = drawMode;
		this.faces    = new ArrayList<>();
	}

	public void render() {
		if (this.faces.size() > 0) {
			final Tessellator tessellator = Tessellator.inst().copy();
			tessellator.start(this.drawMode);
			for (final OBJFace face : this.faces) {
				face.render(tessellator);
			}
			tessellator.draw();
		}
	}

}