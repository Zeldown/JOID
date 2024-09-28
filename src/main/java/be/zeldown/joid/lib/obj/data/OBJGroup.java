package be.zeldown.joid.lib.obj.data;

import java.util.ArrayList;
import java.util.List;

import be.zeldown.joid.lib.tessellator.T9R;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class OBJGroup {

	private String        name;
	private int           glDrawingMode;
	private List<OBJFace> faces;

	public OBJGroup() {
		this("");
	}

	public OBJGroup(final @NonNull String name) {
		this(name, -1);
	}

	public OBJGroup(final @NonNull String name, final int glDrawingMode) {
		this.name          = name;
		this.glDrawingMode = glDrawingMode;
		this.faces         = new ArrayList<>();
	}

	public void render() {
		if (this.faces.size() > 0) {
			final T9R tessellator = T9R.inst().copy();
			tessellator.start(this.glDrawingMode);
			for (final OBJFace face : this.faces) {
				face.render(tessellator);
			}
			tessellator.draw();
		}
	}

}