package be.zeldown.joid.lib.bridge.render.vertex;

import java.nio.ByteBuffer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class VertexBuffer {

	public static final int STRIDE          = 32;
	public static final int POSITION_OFFSET = 0;
	public static final int TEXTURE_OFFSET  = 12;
	public static final int COLOR_OFFSET    = 20;
	public static final int NORMAL_OFFSET   = 24;

	private final ByteBuffer buffer;
	private final int        count;
	private final boolean    texture;
	private final boolean    color;
	private final boolean    normal;

	public static @NonNull VertexBuffer create(final @NonNull ByteBuffer buffer, final int count, final boolean texture, final boolean color, final boolean normal) {
		return new VertexBuffer(buffer, count, texture, color, normal);
	}

}