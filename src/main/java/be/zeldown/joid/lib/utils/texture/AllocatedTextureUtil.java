package be.zeldown.joid.lib.utils.texture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class AllocatedTextureUtil {

	private static final int UPLOAD_BUFFER_SIZE = 2048 * 2048;
	private static final IntBuffer UPLOAD_DATA_BUFFER = ByteBuffer.allocateDirect(AllocatedTextureUtil.UPLOAD_BUFFER_SIZE << 2).order(ByteOrder.nativeOrder()).asIntBuffer();

	public static void allocateTexture(final int textureId, final int width, final int height) {
		GL11.glDeleteTextures(textureId);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (IntBuffer) null);
	}

	public static void uploadTexture(final int textureId, final int[] data, final int width, final int height) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		final int chunkSize = AllocatedTextureUtil.UPLOAD_BUFFER_SIZE / width;

		int remainingHeight;
		for (int offset = 0; offset < width * height; offset += width * remainingHeight) {
			final int row = offset / width;
			remainingHeight = Math.min(chunkSize, height - row);
			final int stride = width * remainingHeight;

			AllocatedTextureUtil.copyToBufferPos(data, offset, stride);
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, row, width, remainingHeight, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, AllocatedTextureUtil.UPLOAD_DATA_BUFFER);
		}
	}

	public static int[] getTextureSize(final int textureId) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		final int[] size = new int[2];
		size[0] = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
		size[1] = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		return size;
	}

	private static void copyToBufferPos(final int[] data, final int width, final int height) {
		final int[] copiedData = data;

		AllocatedTextureUtil.UPLOAD_DATA_BUFFER.clear();
		AllocatedTextureUtil.UPLOAD_DATA_BUFFER.put(copiedData, width, height);
		AllocatedTextureUtil.UPLOAD_DATA_BUFFER.position(0).limit(height);
	}

}