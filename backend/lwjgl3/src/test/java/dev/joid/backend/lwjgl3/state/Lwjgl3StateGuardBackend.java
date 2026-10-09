package dev.joid.backend.lwjgl3.state;

import java.nio.ByteBuffer;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL21C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;

import dev.joid.backend.lwjgl3.binding.Lwjgl3GlBinding;
import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlProfile;
import dev.joid.base.opengl.snapshot.GlStateSnapshot;
import dev.joid.test.contract.IStateGuardBackend;
import dev.joid.test.contract.StateTrap;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class Lwjgl3StateGuardBackend implements IStateGuardBackend {

	private final Lwjgl3SnapshotBackend backend = new Lwjgl3SnapshotBackend();

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
		GL11C.glEnable(GL11C.GL_SCISSOR_TEST);
		GL11C.glScissor(x, y, width, height);
		GL11C.glClearColor((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, (color >>> 24) / 255F);
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT);
		GL11C.glDisable(GL11C.GL_SCISSOR_TEST);
	}

	@Override
	public void drawExternal() {
		GL11C.glEnable(GL11C.GL_BLEND);
		GL11C.glBlendFunc(GL11C.GL_ONE, GL11C.GL_ONE);
		GL11C.glEnable(GL11C.GL_SCISSOR_TEST);
		GL11C.glScissor(2, 2, 4, 4);
		GL11C.glViewport(3, 3, 10, 10);
		GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, Lwjgl3StateGuardBackend.texture(GL11C.GL_NEAREST, GL12C.GL_CLAMP_TO_EDGE));
		GL13C.glActiveTexture(GL13C.GL_TEXTURE1);
		if (this.isCompatibility()) {
			GL11.glColor4f(0.3F, 0.6F, 0.9F, 1F);
		}
	}

	@Override
	public void inject(final @NonNull StateTrap trap) {
		switch (trap) {
		case BLEND:
			GL11C.glEnable(GL11C.GL_BLEND);
			GL14C.glBlendFuncSeparate(GL11C.GL_ONE_MINUS_DST_COLOR, GL11C.GL_SRC_ALPHA_SATURATE, GL11C.GL_ZERO, GL11C.GL_DST_ALPHA);
			GL20C.glBlendEquationSeparate(GL14C.GL_FUNC_REVERSE_SUBTRACT, GL14C.GL_MAX);
			GL14C.glBlendColor(0.3F, 0.1F, 0.9F, 0.2F);
			GL11C.glColorMask(true, false, true, false);
			GL11C.glDepthMask(false);
			GL11C.glClearColor(1F, 0F, 1F, 1F);
			GL11C.glViewport(1, 2, 30, 40);
			break;
		case SCISSOR:
			GL11C.glEnable(GL11C.GL_SCISSOR_TEST);
			GL11C.glScissor(0, 0, 8, 8);
			break;
		case LOGIC_OP:
			GL11C.glEnable(GL11C.GL_COLOR_LOGIC_OP);
			GL11C.glLogicOp(GL11C.GL_COPY_INVERTED);
			break;
		case FRONT_FACE:
			GL11C.glFrontFace(GL11C.GL_CW);
			GL11C.glCullFace(GL11C.GL_FRONT);
			break;
		case ALPHA_TEST:
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.5F);
			break;
		case PIXEL_STORE:
			GL11C.glPixelStorei(GL11C.GL_UNPACK_ALIGNMENT, 1);
			GL11C.glPixelStorei(GL11C.GL_UNPACK_ROW_LENGTH, 7);
			GL11C.glPixelStorei(GL11C.GL_UNPACK_SKIP_ROWS, 2);
			GL11C.glPixelStorei(GL11C.GL_UNPACK_SKIP_PIXELS, 1);
			GL11C.glPixelStorei(GL11C.GL_PACK_ALIGNMENT, 1);
			GL11C.glPixelStorei(GL11C.GL_PACK_ROW_LENGTH, 5);
			GL11C.glPixelStorei(GL11C.GL_PACK_SKIP_PIXELS, 3);
			break;
		case FRAMEBUFFER:
			GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, GL30C.glGenFramebuffers());
			GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, GL30C.GL_COLOR_ATTACHMENT0, GL11C.GL_TEXTURE_2D, Lwjgl3StateGuardBackend.texture(GL11C.GL_NEAREST, GL12C.GL_CLAMP_TO_EDGE), 0);
			GL30C.glBindRenderbuffer(GL30C.GL_RENDERBUFFER, GL30C.glGenRenderbuffers());
			break;
		case PIXEL_BUFFER:
			GL15C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, GL15C.glGenBuffers());
			GL15C.glBufferData(GL21C.GL_PIXEL_PACK_BUFFER, 65536L, GL15C.GL_STREAM_READ);
			GL15C.glBindBuffer(GL21C.GL_PIXEL_UNPACK_BUFFER, GL15C.glGenBuffers());
			GL15C.glBufferData(GL21C.GL_PIXEL_UNPACK_BUFFER, 65536L, GL15C.GL_STREAM_DRAW);
			break;
		case POLYGON_MODE:
			GL11C.glPolygonMode(GL11C.GL_FRONT_AND_BACK, GL11C.GL_LINE);
			break;
		case STENCIL_MASK:
			GL11C.glStencilMask(0);
			GL11C.glClearStencil(5);
			break;
		case VERTEX_ARRAY:
			GL30C.glBindVertexArray(GL30C.glGenVertexArrays());
			GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, Lwjgl3StateGuardBackend.buffer());
			for (int index = 0; index < 4; index++) {
				GL20C.glVertexAttribPointer(index, 2, GL11C.GL_FLOAT, false, 8, 0L);
				GL20C.glEnableVertexAttribArray(index);
			}
			GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, Lwjgl3StateGuardBackend.buffer());
			GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, Lwjgl3StateGuardBackend.buffer());
			break;
		case CLIENT_ARRAYS:
			if (this.getCapabilities().hasVertexArrays()) {
				GL30C.glBindVertexArray(0);
			}
			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
			GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, Lwjgl3StateGuardBackend.buffer());
			for (int index = 1; index < 4; index++) {
				GL20C.glVertexAttribPointer(index, 2, GL11C.GL_FLOAT, false, 8, 0L);
				GL20C.glEnableVertexAttribArray(index);
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
			GL11C.glDepthFunc(GL11C.GL_GREATER);
			GL11C.glClearDepth(0.25D);
			break;
		case SAMPLER_OBJECTS:
			for (int unit = 0; unit < 4; unit++) {
				final int sampler = GL33C.glGenSamplers();
				GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
				GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
				GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_WRAP_S, GL14C.GL_MIRRORED_REPEAT);
				GL33C.glBindSampler(unit, sampler);
			}
			break;
		case TEXTURE_PARAMETERS:
			for (int unit = 0; unit < 4; unit++) {
				GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + unit);
				GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, Lwjgl3StateGuardBackend.texture(GL11C.GL_LINEAR, GL14C.GL_MIRRORED_REPEAT));
			}
			GL13C.glActiveTexture(GL13C.GL_TEXTURE2);
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
		return GlStateSnapshot.read(Lwjgl3GlBinding.inst(), this.getCapabilities());
	}

	private boolean isCompatibility() {
		return this.getCapabilities().getProfile() == GlProfile.COMPATIBILITY;
	}

	private GlCapabilities getCapabilities() {
		return this.backend.getBridge().getCapabilities();
	}

	private static int buffer() {
		final int buffer = GL15C.glGenBuffers();
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, buffer);
		GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, new float[] {-1F, -1F, 1F, -1F, 1F, 1F}, GL15C.GL_STATIC_DRAW);
		return buffer;
	}

	private static int texture(final int filter, final int wrap) {
		final int texture = GL11C.glGenTextures();
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, texture);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, filter);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, filter);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, wrap);
		GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, wrap);
		GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, 2, 2, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		return texture;
	}

}