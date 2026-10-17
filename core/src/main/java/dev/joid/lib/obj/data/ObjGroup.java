package dev.joid.lib.obj.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjGroup {

	private final String        name;
	private final List<ObjFace> faces = new ArrayList<>();

	public static @NonNull ObjGroup create(final @NonNull String name) {
		return new ObjGroup(name);
	}

	public @NonNull ObjGroup face(final @NonNull ObjFace face) {
		this.faces.add(face);
		return this;
	}

	public void render() {
		if (this.faces.size() > 0) {
			final Tessellator tessellator = Tessellator.inst();
			tessellator.start(DrawMode.TRIANGLES);
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