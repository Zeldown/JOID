package dev.joid.base.opengl.render.host;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.binding.IGlBufferBinding;
import dev.joid.base.opengl.binding.IGlFrameBufferBinding;
import dev.joid.base.opengl.binding.IGlStateBinding;
import dev.joid.base.opengl.binding.IGlTextureBinding;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlFrameBufferFamily;
import dev.joid.base.opengl.capability.GlProfile;
import lombok.NonNull;

public final class GlStateJournal {

	private final List<Entry>           entryList;
	private final Set<Integer>          textureSet;
	private final int[]                 unitTextures;
	private final IGlBinding            binding;
	private final Set<Integer>          vertexArraySet;
	private final GlCapabilities        capabilities;
	private final IGlFrameBufferBinding frameBufferBinding;

	private int     size;
	private int     activeUnit;
	private int     vertexArray;
	private boolean recording;

	private GlStateJournal(final IGlBinding binding, final GlCapabilities capabilities, final GlFrameBufferFamily family) {
		this.binding            = binding;
		this.capabilities       = capabilities;
		this.frameBufferBinding = binding.getFrameBufferBinding(family);
		this.entryList          = new ArrayList<>();
		this.textureSet         = new HashSet<>();
		this.vertexArraySet     = new HashSet<>();
		this.unitTextures       = new int[32];
	}

	public static @NonNull GlStateJournal create(final @NonNull IGlBinding binding, final @NonNull GlCapabilities capabilities, final @NonNull GlFrameBufferFamily family) {
		return new GlStateJournal(binding, capabilities, family);
	}

	public void start() {
		this.size        = 0;
		this.activeUnit  = -1;
		this.vertexArray = -1;
		this.recording   = true;
		Arrays.fill(this.unitTextures, -1);
	}

	public void restore() {
		this.recording = false;
		for (int i = 1; i < this.size; i++) {
			final Entry entry = this.entryList.get(i);
			int j = i - 1;
			while (j >= 0 && this.entryList.get(j).key.getRank() > entry.key.getRank()) {
				this.entryList.set(j + 1, this.entryList.get(j));
				j--;
			}
			this.entryList.set(j + 1, entry);
		}

		for (int i = 0; i < this.size; i++) {
			this.restore(this.entryList.get(i));
		}
		this.size = 0;
	}

	public void ownTexture(final int texture) {
		this.textureSet.add(texture);
	}

	public void releaseTexture(final int texture) {
		this.textureSet.remove(texture);
	}

	public void ownVertexArray(final int array) {
		this.vertexArraySet.add(array);
	}

	public void trackTexture(final int texture) {
		final int unit = this.getActiveUnit();
		if (unit < this.unitTextures.length) {
			this.unitTextures[unit] = texture;
		}
	}

	public void trackVertexArray(final int array) {
		this.vertexArray = array;
	}

	public void trackActiveTexture(final int unit) {
		this.activeUnit = unit - GlConstants.TEXTURE0;
	}

	public void touch(final @NonNull GlStateKey key) {
		this.touch(key, 0, 0);
	}

	public void touch(final @NonNull GlStateKey key, final int container) {
		this.touch(key, container, 0);
	}

	public void touchTexture() {
		if (this.recording) {
			this.touch(GlStateKey.ACTIVE_TEXTURE);
			this.touch(GlStateKey.TEXTURE, this.getActiveUnit());
		}
	}

	public void touchReadBuffer() {
		if (this.recording) {
			this.touchFrameBuffer(GlConstants.READ_FRAMEBUFFER);
			this.touch(GlStateKey.READ_BUFFER, this.binding.getInteger(this.capabilities.hasFrameBufferBlit() ? GlConstants.READ_FRAMEBUFFER_BINDING : GlConstants.DRAW_FRAMEBUFFER_BINDING));
		}
	}

	public void touchSampler(final int unit) {
		if (this.recording) {
			this.touch(GlStateKey.ACTIVE_TEXTURE);
			this.touch(GlStateKey.SAMPLER, unit);
		}
	}

	public void touchFrameBuffer(final int target) {
		if (target != GlConstants.READ_FRAMEBUFFER) {
			this.touch(GlStateKey.FRAMEBUFFER, GlConstants.DRAW_FRAMEBUFFER);
		}
		if (target != GlConstants.DRAW_FRAMEBUFFER && this.capabilities.hasFrameBufferBlit()) {
			this.touch(GlStateKey.FRAMEBUFFER, GlConstants.READ_FRAMEBUFFER);
		}
	}

	public void touchTextureParameter(final int name) {
		if (!this.recording) {
			return;
		}

		final int unit = this.getActiveUnit();
		final int texture = unit < this.unitTextures.length && this.unitTextures[unit] >= 0 ? this.unitTextures[unit] : this.binding.getInteger(GlConstants.TEXTURE_BINDING_2D);
		if (!this.textureSet.contains(texture)) {
			this.touchTexture();
			this.touch(GlStateKey.TEXTURE_PARAMETER, texture, name);
		}
	}

	public void touchVertexAttribute(final int index) {
		if (!this.recording) {
			return;
		}

		final int array = this.getVertexArray();
		if (!this.vertexArraySet.contains(array)) {
			if (this.capabilities.hasVertexArrays()) {
				this.touch(GlStateKey.VERTEX_ARRAY);
			}
			this.touch(GlStateKey.BUFFER, GlConstants.ARRAY_BUFFER);
			this.touch(GlStateKey.VERTEX_ATTRIBUTE, array, index);
		}
	}

	private void touch(final GlStateKey key, final int container, final int index) {
		if (!this.recording) {
			return;
		}

		for (int i = 0; i < this.size; i++) {
			final Entry entry = this.entryList.get(i);
			if (entry.key == key && entry.container == container && entry.index == index) {
				return;
			}
		}

		if (this.size == this.entryList.size()) {
			this.entryList.add(new Entry());
		}

		final Entry entry = this.entryList.get(this.size++);
		entry.key       = key;
		entry.container = container;
		entry.index     = index;
		this.capture(entry);
	}

	private void capture(final Entry entry) {
		final IGlBinding gl = this.binding;
		final int[] ints = entry.ints;
		switch (entry.key) {
		case READ_BUFFER:
			ints[0] = gl.getInteger(GlConstants.READ_BUFFER);
			break;
		case FRAMEBUFFER:
			ints[0] = gl.getInteger(entry.container == GlConstants.READ_FRAMEBUFFER ? GlConstants.READ_FRAMEBUFFER_BINDING : GlConstants.DRAW_FRAMEBUFFER_BINDING);
			break;
		case RENDERBUFFER:
			ints[0] = gl.getInteger(GlConstants.RENDERBUFFER_BINDING);
			break;
		case PROGRAM:
			ints[0] = gl.getInteger(GlConstants.CURRENT_PROGRAM);
			break;
		case VERTEX_ATTRIBUTE:
			ints[0] = gl.getVertexAttribi(entry.index, GlConstants.VERTEX_ATTRIB_ARRAY_ENABLED);
			ints[1] = gl.getVertexAttribi(entry.index, GlConstants.VERTEX_ATTRIB_ARRAY_BUFFER_BINDING);
			ints[2] = gl.getVertexAttribi(entry.index, GlConstants.VERTEX_ATTRIB_ARRAY_SIZE);
			ints[3] = gl.getVertexAttribi(entry.index, GlConstants.VERTEX_ATTRIB_ARRAY_TYPE);
			ints[4] = gl.getVertexAttribi(entry.index, GlConstants.VERTEX_ATTRIB_ARRAY_NORMALIZED);
			ints[5] = gl.getVertexAttribi(entry.index, GlConstants.VERTEX_ATTRIB_ARRAY_STRIDE);
			entry.pointer = gl.getVertexAttribPointer(entry.index, GlConstants.VERTEX_ATTRIB_ARRAY_POINTER);
			break;
		case VERTEX_ARRAY:
			ints[0] = gl.getInteger(GlConstants.VERTEX_ARRAY_BINDING);
			break;
		case BUFFER:
			ints[0] = gl.getInteger(GlStateJournal.getBufferBinding(entry.container));
			break;
		case TEXTURE_PARAMETER:
			ints[0] = gl.getTexParameteri(GlConstants.TEXTURE_2D, entry.index);
			ints[1] = this.getActiveUnit();
			break;
		case TEXTURE:
			ints[0] = gl.getInteger(GlConstants.TEXTURE_BINDING_2D);
			break;
		case SAMPLER:
			gl.getTextureBinding().activeTexture(GlConstants.TEXTURE0 + entry.container);
			ints[0] = gl.getInteger(GlConstants.SAMPLER_BINDING);
			gl.getTextureBinding().activeTexture(GlConstants.TEXTURE0 + this.getActiveUnit());
			break;
		case CURRENT_VERTEX_ATTRIBUTE:
			gl.getVertexAttribFloats(entry.container, GlConstants.CURRENT_VERTEX_ATTRIB, entry.floats);
			break;
		case CAPABILITY:
			ints[0] = gl.isEnabled(entry.container) ? 1 : 0;
			break;
		case BLEND_EQUATION:
			ints[0] = gl.getInteger(GlConstants.BLEND_EQUATION_RGB);
			ints[1] = gl.getInteger(GlConstants.BLEND_EQUATION_ALPHA);
			break;
		case BLEND_FUNCTION:
			ints[0] = gl.getInteger(GlConstants.BLEND_SRC_RGB);
			ints[1] = gl.getInteger(GlConstants.BLEND_DST_RGB);
			ints[2] = gl.getInteger(GlConstants.BLEND_SRC_ALPHA);
			ints[3] = gl.getInteger(GlConstants.BLEND_DST_ALPHA);
			break;
		case COLOR_MASK:
			gl.getIntegers(GlConstants.COLOR_WRITEMASK, ints);
			break;
		case CLEAR_COLOR:
			gl.getFloats(GlConstants.COLOR_CLEAR_VALUE, entry.floats);
			break;
		case CLEAR_DEPTH:
			gl.getFloats(GlConstants.DEPTH_CLEAR_VALUE, entry.floats);
			break;
		case CLEAR_STENCIL:
			ints[0] = gl.getInteger(GlConstants.STENCIL_CLEAR_VALUE);
			break;
		case DEPTH_MASK:
			ints[0] = gl.getInteger(GlConstants.DEPTH_WRITEMASK);
			break;
		case DEPTH_FUNCTION:
			ints[0] = gl.getInteger(GlConstants.DEPTH_FUNC);
			break;
		case STENCIL_MASK:
			ints[0] = gl.getInteger(GlConstants.STENCIL_WRITEMASK);
			ints[1] = gl.getInteger(GlConstants.STENCIL_BACK_WRITEMASK);
			break;
		case STENCIL_FUNCTION:
			ints[0] = gl.getInteger(GlConstants.STENCIL_FUNC);
			ints[1] = gl.getInteger(GlConstants.STENCIL_REF);
			ints[2] = gl.getInteger(GlConstants.STENCIL_VALUE_MASK);
			ints[3] = gl.getInteger(GlConstants.STENCIL_BACK_FUNC);
			ints[4] = gl.getInteger(GlConstants.STENCIL_BACK_REF);
			ints[5] = gl.getInteger(GlConstants.STENCIL_BACK_VALUE_MASK);
			break;
		case STENCIL_OPERATION:
			ints[0] = gl.getInteger(GlConstants.STENCIL_FAIL);
			ints[1] = gl.getInteger(GlConstants.STENCIL_PASS_DEPTH_FAIL);
			ints[2] = gl.getInteger(GlConstants.STENCIL_PASS_DEPTH_PASS);
			ints[3] = gl.getInteger(GlConstants.STENCIL_BACK_FAIL);
			ints[4] = gl.getInteger(GlConstants.STENCIL_BACK_PASS_DEPTH_FAIL);
			ints[5] = gl.getInteger(GlConstants.STENCIL_BACK_PASS_DEPTH_PASS);
			break;
		case VIEWPORT:
			gl.getIntegers(GlConstants.VIEWPORT, ints);
			break;
		case LINE_WIDTH:
			gl.getFloats(GlConstants.LINE_WIDTH, entry.floats);
			break;
		case POLYGON_MODE:
			gl.getIntegers(GlConstants.POLYGON_MODE, ints);
			break;
		case FRONT_FACE:
			ints[0] = gl.getInteger(GlConstants.FRONT_FACE);
			break;
		case CULL_FACE:
			ints[0] = gl.getInteger(GlConstants.CULL_FACE_MODE);
			break;
		case PIXEL_STORE:
			ints[0] = gl.getInteger(entry.container);
			break;
		default:
			ints[0] = gl.getInteger(GlConstants.ACTIVE_TEXTURE);
			break;
		}
	}

	private void restore(final Entry entry) {
		final IGlStateBinding state = this.binding.getStateBinding();
		final IGlBufferBinding buffers = this.binding.getBufferBinding();
		final IGlTextureBinding textures = this.binding.getTextureBinding();
		final int[] ints = entry.ints;
		switch (entry.key) {
		case READ_BUFFER:
			this.frameBufferBinding.bindFramebuffer(this.capabilities.hasFrameBufferBlit() ? GlConstants.READ_FRAMEBUFFER : GlConstants.FRAMEBUFFER, entry.container);
			this.binding.readBuffer(ints[0]);
			break;
		case FRAMEBUFFER:
			this.frameBufferBinding.bindFramebuffer(this.capabilities.hasFrameBufferBlit() ? entry.container : GlConstants.FRAMEBUFFER, ints[0]);
			break;
		case RENDERBUFFER:
			this.frameBufferBinding.bindRenderbuffer(GlConstants.RENDERBUFFER, ints[0]);
			break;
		case PROGRAM:
			this.binding.getProgramBinding().useProgram(ints[0]);
			break;
		case VERTEX_ATTRIBUTE:
			if (this.capabilities.hasVertexArrays()) {
				buffers.bindVertexArray(entry.container);
			}
			if (ints[1] != 0 || this.capabilities.getProfile() == GlProfile.COMPATIBILITY) {
				buffers.bindBuffer(GlConstants.ARRAY_BUFFER, ints[1]);
				buffers.vertexAttribPointer(entry.index, ints[2], ints[3], ints[4] != 0, ints[5], entry.pointer);
			}
			if (ints[0] != 0) {
				buffers.enableVertexAttribArray(entry.index);
			} else {
				buffers.disableVertexAttribArray(entry.index);
			}
			break;
		case VERTEX_ARRAY:
			buffers.bindVertexArray(ints[0]);
			break;
		case BUFFER:
			buffers.bindBuffer(entry.container, ints[0]);
			break;
		case TEXTURE_PARAMETER:
			textures.activeTexture(GlConstants.TEXTURE0 + ints[1]);
			textures.bindTexture(GlConstants.TEXTURE_2D, entry.container);
			textures.texParameteri(GlConstants.TEXTURE_2D, entry.index, ints[0]);
			break;
		case TEXTURE:
			textures.activeTexture(GlConstants.TEXTURE0 + entry.container);
			textures.bindTexture(GlConstants.TEXTURE_2D, ints[0]);
			break;
		case SAMPLER:
			textures.bindSampler(entry.container, ints[0]);
			break;
		case CURRENT_VERTEX_ATTRIBUTE:
			buffers.vertexAttrib4f(entry.container, entry.floats[0], entry.floats[1], entry.floats[2], entry.floats[3]);
			break;
		case CAPABILITY:
			if (ints[0] != 0) {
				this.binding.enable(entry.container);
			} else {
				this.binding.disable(entry.container);
			}
			break;
		case BLEND_EQUATION:
			state.blendEquationSeparate(ints[0], ints[1]);
			break;
		case BLEND_FUNCTION:
			state.blendFuncSeparate(ints[0], ints[1], ints[2], ints[3]);
			break;
		case COLOR_MASK:
			state.colorMask(ints[0] != 0, ints[1] != 0, ints[2] != 0, ints[3] != 0);
			break;
		case CLEAR_COLOR:
			state.clearColor(entry.floats[0], entry.floats[1], entry.floats[2], entry.floats[3]);
			break;
		case CLEAR_DEPTH:
			state.clearDepth(entry.floats[0]);
			break;
		case CLEAR_STENCIL:
			state.clearStencil(ints[0]);
			break;
		case DEPTH_MASK:
			state.depthMask(ints[0] != 0);
			break;
		case DEPTH_FUNCTION:
			state.depthFunc(ints[0]);
			break;
		case STENCIL_MASK:
			state.stencilMaskSeparate(GlConstants.FRONT, ints[0]);
			state.stencilMaskSeparate(GlConstants.BACK, ints[1]);
			break;
		case STENCIL_FUNCTION:
			state.stencilFuncSeparate(GlConstants.FRONT, ints[0], ints[1], ints[2]);
			state.stencilFuncSeparate(GlConstants.BACK, ints[3], ints[4], ints[5]);
			break;
		case STENCIL_OPERATION:
			state.stencilOpSeparate(GlConstants.FRONT, ints[0], ints[1], ints[2]);
			state.stencilOpSeparate(GlConstants.BACK, ints[3], ints[4], ints[5]);
			break;
		case VIEWPORT:
			state.viewport(ints[0], ints[1], ints[2], ints[3]);
			break;
		case LINE_WIDTH:
			state.lineWidth(entry.floats[0]);
			break;
		case POLYGON_MODE:
			if (this.capabilities.getProfile() == GlProfile.COMPATIBILITY) {
				state.polygonMode(GlConstants.FRONT, ints[0]);
				state.polygonMode(GlConstants.BACK, ints[1]);
			} else {
				state.polygonMode(GlConstants.FRONT_AND_BACK, ints[0]);
			}
			break;
		case FRONT_FACE:
			state.frontFace(ints[0]);
			break;
		case CULL_FACE:
			state.cullFace(ints[0]);
			break;
		case PIXEL_STORE:
			state.pixelStorei(entry.container, ints[0]);
			break;
		default:
			textures.activeTexture(ints[0]);
			break;
		}
	}

	private int getActiveUnit() {
		if (this.activeUnit < 0) {
			this.activeUnit = this.binding.getInteger(GlConstants.ACTIVE_TEXTURE) - GlConstants.TEXTURE0;
		}
		return this.activeUnit;
	}

	private int getVertexArray() {
		if (this.vertexArray < 0) {
			this.vertexArray = this.capabilities.hasVertexArrays() ? this.binding.getInteger(GlConstants.VERTEX_ARRAY_BINDING) : 0;
		}
		return this.vertexArray;
	}

	private static int getBufferBinding(final int target) {
		switch (target) {
		case GlConstants.PIXEL_PACK_BUFFER:
			return GlConstants.PIXEL_PACK_BUFFER_BINDING;
		case GlConstants.PIXEL_UNPACK_BUFFER:
			return GlConstants.PIXEL_UNPACK_BUFFER_BINDING;
		default:
			return GlConstants.ARRAY_BUFFER_BINDING;
		}
	}

	private static final class Entry {

		private final int[]   ints   = new int[16];
		private final float[] floats = new float[16];

		private int        index;
		private long       pointer;
		private int        container;
		private GlStateKey key;

	}

}