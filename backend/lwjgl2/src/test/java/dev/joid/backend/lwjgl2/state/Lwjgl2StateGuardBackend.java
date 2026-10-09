package dev.joid.backend.lwjgl2.state;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import dev.joid.backend.lwjgl2.binding.Lwjgl2GlBinding;
import dev.joid.backend.lwjgl2.snapshot.Lwjgl2SnapshotBackend;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlProfile;
import dev.joid.base.opengl.snapshot.GlStateSnapshot;
import dev.joid.test.contract.IStateGuardBackend;
import dev.joid.test.contract.StateTrap;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class Lwjgl2StateGuardBackend implements IStateGuardBackend {

	private final Lwjgl2SnapshotBackend backend = new Lwjgl2SnapshotBackend();

	@Override
	public void destroy() {
		this.backend.destroy();
	}

	@Override
	public void create(final int width, final int height) {
		this.backend.create(width, height);
	}

	@Override
	public void present() {
		this.backend.present();
	}

	@Override
	public @NonNull SnapshotImage capture(final int width, final int height) {
		return this.backend.capture(width, height);
	}

	@Override
	public @NonNull String getRenderer() {
		return this.backend.getRenderer();
	}

	@Override
	public void fill(final int x, final int y, final int width, final int height, final int color) {
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x, y, width, height);
		GL11.glClearColor((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, (color >>> 24) / 255F);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	@Override
	public void drawExternal() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(2, 2, 4, 4);
		GL11.glViewport(3, 3, 10, 10);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Lwjgl2StateGuardBackend.texture(GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE));
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		if (this.isCompatibility()) {
			GL11.glColor4f(0.3F, 0.6F, 0.9F, 1F);
		}
	}

	@Override
	public void inject(final @NonNull StateTrap trap) {
		switch (trap) {
		case BLEND:
			GL11.glEnable(GL11.GL_BLEND);
			GL14.glBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_SRC_ALPHA_SATURATE, GL11.GL_ZERO, GL11.GL_DST_ALPHA);
			GL20.glBlendEquationSeparate(GL14.GL_FUNC_REVERSE_SUBTRACT, GL14.GL_MAX);
			GL14.glBlendColor(0.3F, 0.1F, 0.9F, 0.2F);
			GL11.glColorMask(true, false, true, false);
			GL11.glDepthMask(false);
			GL11.glClearColor(1F, 0F, 1F, 1F);
			GL11.glViewport(1, 2, 30, 40);
			break;
		case SCISSOR:
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			GL11.glScissor(0, 0, 8, 8);
			break;
		case LOGIC_OP:
			GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
			GL11.glLogicOp(GL11.GL_COPY_INVERTED);
			break;
		case FRONT_FACE:
			GL11.glFrontFace(GL11.GL_CW);
			GL11.glCullFace(GL11.GL_FRONT);
			break;
		case ALPHA_TEST:
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.5F);
			break;
		case PIXEL_STORE:
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
			GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 7);
			GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 2);
			GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 1);
			GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
			GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, 5);
			GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, 3);
			break;
		case FRAMEBUFFER:
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, GL30.glGenFramebuffers());
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, Lwjgl2StateGuardBackend.texture(GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE), 0);
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, GL30.glGenRenderbuffers());
			break;
		case PIXEL_BUFFER:
			GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, GL15.glGenBuffers());
			GL15.glBufferData(GL21.GL_PIXEL_PACK_BUFFER, 65536L, GL15.GL_STREAM_READ);
			GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, GL15.glGenBuffers());
			GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, 65536L, GL15.GL_STREAM_DRAW);
			break;
		case POLYGON_MODE:
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			break;
		case STENCIL_MASK:
			GL11.glStencilMask(0);
			GL11.glClearStencil(5);
			break;
		case VERTEX_ARRAY:
			GL30.glBindVertexArray(GL30.glGenVertexArrays());
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, Lwjgl2StateGuardBackend.buffer());
			for (int index = 0; index < 4; index++) {
				GL20.glVertexAttribPointer(index, 2, GL11.GL_FLOAT, false, 8, 0L);
				GL20.glEnableVertexAttribArray(index);
			}
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, Lwjgl2StateGuardBackend.buffer());
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, Lwjgl2StateGuardBackend.buffer());
			break;
		case CLIENT_ARRAYS:
			if (this.getCapabilities().hasVertexArrays()) {
				GL30.glBindVertexArray(0);
			}
			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, Lwjgl2StateGuardBackend.buffer());
			for (int index = 1; index < 4; index++) {
				GL20.glVertexAttribPointer(index, 2, GL11.GL_FLOAT, false, 8, 0L);
				GL20.glEnableVertexAttribArray(index);
			}
			break;
		case COLOR_MATERIAL:
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_LIGHT0);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glColor4f(1F, 0.2F, 0.2F, 0.4F);
			GL11.glNormal3f(0F, 0F, -1F);
			break;
		case DEPTH_FUNCTION:
			GL11.glDepthFunc(GL11.GL_GREATER);
			GL11.glClearDepth(0.25D);
			break;
		case SAMPLER_OBJECTS:
			for (int unit = 0; unit < 4; unit++) {
				final int sampler = GL33.glGenSamplers();
				GL33.glSamplerParameteri(sampler, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL33.glSamplerParameteri(sampler, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL33.glSamplerParameteri(sampler, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
				GL33.glBindSampler(unit, sampler);
			}
			break;
		case TEXTURE_PARAMETERS:
			for (int unit = 0; unit < 4; unit++) {
				GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, Lwjgl2StateGuardBackend.texture(GL11.GL_LINEAR, GL14.GL_MIRRORED_REPEAT));
			}
			GL13.glActiveTexture(GL13.GL_TEXTURE2);
			break;
		default:
			for (final StateTrap other : StateTrap.values()) {
				if (other != StateTrap.EVERYTHING && this.supports(other)) {
					this.inject(other);
				}
			}
			break;
		}
	}

	@Override
	public boolean supports(final @NonNull StateTrap trap) {
		switch (trap) {
		case ALPHA_TEST:
		case CLIENT_ARRAYS:
		case COLOR_MATERIAL:
			return this.isCompatibility();
		case PIXEL_BUFFER:
			return this.getCapabilities().hasPixelBuffers();
		case VERTEX_ARRAY:
			return this.getCapabilities().hasVertexArrays();
		case SAMPLER_OBJECTS:
			return this.getCapabilities().hasSamplerObjects();
		default:
			return true;
		}
	}

	@Override
	public @NonNull Map<@NonNull String, @NonNull String> readState() {
		return GlStateSnapshot.read(Lwjgl2GlBinding.inst(), this.getCapabilities());
	}

	private boolean isCompatibility() {
		return this.getCapabilities().getProfile() == GlProfile.COMPATIBILITY;
	}

	private GlCapabilities getCapabilities() {
		return this.backend.getBridge().getCapabilities();
	}

	private static int buffer() {
		final int buffer = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (FloatBuffer) BufferUtils.createFloatBuffer(6).put(new float[] {-1F, -1F, 1F, -1F, 1F, 1F}).flip(), GL15.GL_STATIC_DRAW);
		return buffer;
	}

	private static int texture(final int filter, final int wrap) {
		final int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrap);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrap);
		if (GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING) != 0) {
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L);
		} else {
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		}
		return texture;
	}

}