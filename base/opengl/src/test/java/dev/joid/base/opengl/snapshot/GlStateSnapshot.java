package dev.joid.base.opengl.snapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlProfile;
import lombok.NonNull;

public final class GlStateSnapshot {

	private static final int DITHER              = 0x0BD0;
	private static final int LIGHT0              = 0x4000;
	private static final int COLOR_ARRAY         = 0x8076;
	private static final int VERTEX_ARRAY        = 0x8074;
	private static final int NORMAL_ARRAY        = 0x8075;
	private static final int TEXTURE_COORD_ARRAY = 0x8078;

	private static final int AMBIENT                 = 0x1200;
	private static final int DIFFUSE                 = 0x1201;
	private static final int BLEND_COLOR             = 0x8005;
	private static final int SCISSOR_BOX             = 0x0C10;
	private static final int SHADE_MODEL             = 0x0B54;
	private static final int MATRIX_MODE             = 0x0BA0;
	private static final int CURRENT_COLOR           = 0x0B00;
	private static final int LOGIC_OP_MODE           = 0x0BF0;
	private static final int ALPHA_TEST_REF          = 0x0BC2;
	private static final int CURRENT_NORMAL          = 0x0B02;
	private static final int ALPHA_TEST_FUNC         = 0x0BC1;
	private static final int CLIENT_ACTIVE_TEXTURE   = 0x84E1;
	private static final int CURRENT_TEXTURE_COORDS  = 0x0B03;
	private static final int PRIMITIVE_RESTART_INDEX = 0x8F9E;

	private final IGlBinding          binding;
	private final GlCapabilities      capabilities;
	private final Map<String, String> stateMap;
	private final MaterialReader      materialReader;

	private GlStateSnapshot(final IGlBinding binding, final GlCapabilities capabilities, final MaterialReader materialReader) {
		this.binding        = binding;
		this.capabilities   = capabilities;
		this.stateMap       = new TreeMap<>();
		this.materialReader = materialReader;
	}

	public static @NonNull Map<@NonNull String, @NonNull String> read(final @NonNull IGlBinding binding, final @NonNull GlCapabilities capabilities, final @NonNull MaterialReader materialReader) {
		return new GlStateSnapshot(binding, capabilities, materialReader).read();
	}

	private Map<String, String> read() {
		final boolean compatibility = this.capabilities.getProfile() == GlProfile.COMPATIBILITY;
		this.capability("BLEND", GlConstants.BLEND);
		this.capability("CULL_FACE", GlConstants.CULL_FACE);
		this.capability("DEPTH_TEST", GlConstants.DEPTH_TEST);
		this.capability("LINE_SMOOTH", GlConstants.LINE_SMOOTH);
		this.capability("STENCIL_TEST", GlConstants.STENCIL_TEST);
		this.capability("SCISSOR_TEST", GlConstants.SCISSOR_TEST);
		this.capability("COLOR_LOGIC_OP", GlConstants.COLOR_LOGIC_OP);
		this.capability("POLYGON_OFFSET_FILL", GlConstants.POLYGON_OFFSET_FILL);
		this.capability("DITHER", GlStateSnapshot.DITHER);
		if (this.capabilities.hasFrameBufferSrgb()) {
			this.capability("FRAMEBUFFER_SRGB", GlConstants.FRAMEBUFFER_SRGB);
		}
		if (this.capabilities.hasRasterizerDiscard()) {
			this.capability("RASTERIZER_DISCARD", GlConstants.RASTERIZER_DISCARD);
		}
		if (this.capabilities.hasPrimitiveRestart()) {
			this.capability("PRIMITIVE_RESTART", GlConstants.PRIMITIVE_RESTART);
			this.integer("PRIMITIVE_RESTART_INDEX", GlStateSnapshot.PRIMITIVE_RESTART_INDEX);
		}
		if (this.capabilities.hasDepthClamp()) {
			this.capability("DEPTH_CLAMP", GlConstants.DEPTH_CLAMP);
		}

		this.integer("CURRENT_PROGRAM", GlConstants.CURRENT_PROGRAM);
		this.integer("ARRAY_BUFFER_BINDING", GlConstants.ARRAY_BUFFER_BINDING);
		this.integer("RENDERBUFFER_BINDING", GlConstants.RENDERBUFFER_BINDING);
		this.integer("DRAW_FRAMEBUFFER_BINDING", GlConstants.DRAW_FRAMEBUFFER_BINDING);
		if (this.capabilities.hasFrameBufferBlit()) {
			this.integer("READ_FRAMEBUFFER_BINDING", GlConstants.READ_FRAMEBUFFER_BINDING);
		}
		this.integer("PIXEL_PACK_BUFFER_BINDING", GlConstants.PIXEL_PACK_BUFFER_BINDING);
		this.integer("PIXEL_UNPACK_BUFFER_BINDING", GlConstants.PIXEL_UNPACK_BUFFER_BINDING);
		this.integer("READ_BUFFER", GlConstants.READ_BUFFER);
		this.integer("ACTIVE_TEXTURE", GlConstants.ACTIVE_TEXTURE);

		this.integer("BLEND_SRC_RGB", GlConstants.BLEND_SRC_RGB);
		this.integer("BLEND_DST_RGB", GlConstants.BLEND_DST_RGB);
		this.integer("BLEND_SRC_ALPHA", GlConstants.BLEND_SRC_ALPHA);
		this.integer("BLEND_DST_ALPHA", GlConstants.BLEND_DST_ALPHA);
		this.integer("BLEND_EQUATION_RGB", GlConstants.BLEND_EQUATION_RGB);
		this.integer("BLEND_EQUATION_ALPHA", GlConstants.BLEND_EQUATION_ALPHA);
		this.integer("DEPTH_FUNC", GlConstants.DEPTH_FUNC);
		this.integer("DEPTH_WRITEMASK", GlConstants.DEPTH_WRITEMASK);
		this.integer("CULL_FACE_MODE", GlConstants.CULL_FACE_MODE);
		this.integer("FRONT_FACE", GlConstants.FRONT_FACE);
		this.integer("LOGIC_OP_MODE", GlStateSnapshot.LOGIC_OP_MODE);
		this.integer("STENCIL_FUNC", GlConstants.STENCIL_FUNC);
		this.integer("STENCIL_REF", GlConstants.STENCIL_REF);
		this.integer("STENCIL_VALUE_MASK", GlConstants.STENCIL_VALUE_MASK);
		this.integer("STENCIL_FAIL", GlConstants.STENCIL_FAIL);
		this.integer("STENCIL_PASS_DEPTH_FAIL", GlConstants.STENCIL_PASS_DEPTH_FAIL);
		this.integer("STENCIL_PASS_DEPTH_PASS", GlConstants.STENCIL_PASS_DEPTH_PASS);
		this.integer("STENCIL_WRITEMASK", GlConstants.STENCIL_WRITEMASK);
		this.integer("STENCIL_BACK_FUNC", GlConstants.STENCIL_BACK_FUNC);
		this.integer("STENCIL_BACK_REF", GlConstants.STENCIL_BACK_REF);
		this.integer("STENCIL_BACK_VALUE_MASK", GlConstants.STENCIL_BACK_VALUE_MASK);
		this.integer("STENCIL_BACK_FAIL", GlConstants.STENCIL_BACK_FAIL);
		this.integer("STENCIL_BACK_PASS_DEPTH_FAIL", GlConstants.STENCIL_BACK_PASS_DEPTH_FAIL);
		this.integer("STENCIL_BACK_PASS_DEPTH_PASS", GlConstants.STENCIL_BACK_PASS_DEPTH_PASS);
		this.integer("STENCIL_BACK_WRITEMASK", GlConstants.STENCIL_BACK_WRITEMASK);

		this.integer("PACK_ALIGNMENT", GlConstants.PACK_ALIGNMENT);
		this.integer("PACK_ROW_LENGTH", GlConstants.PACK_ROW_LENGTH);
		this.integer("PACK_SKIP_ROWS", GlConstants.PACK_SKIP_ROWS);
		this.integer("PACK_SKIP_PIXELS", GlConstants.PACK_SKIP_PIXELS);
		this.integer("UNPACK_ALIGNMENT", GlConstants.UNPACK_ALIGNMENT);
		this.integer("UNPACK_ROW_LENGTH", GlConstants.UNPACK_ROW_LENGTH);
		this.integer("UNPACK_SKIP_ROWS", GlConstants.UNPACK_SKIP_ROWS);
		this.integer("UNPACK_SKIP_PIXELS", GlConstants.UNPACK_SKIP_PIXELS);

		this.integers("VIEWPORT", GlConstants.VIEWPORT, 4);
		this.integers("SCISSOR_BOX", GlStateSnapshot.SCISSOR_BOX, 4);
		this.integers("POLYGON_MODE", GlConstants.POLYGON_MODE, compatibility ? 2 : 1);
		this.integers("COLOR_WRITEMASK", GlConstants.COLOR_WRITEMASK, 4);
		this.floats("BLEND_COLOR", GlStateSnapshot.BLEND_COLOR, 4);
		this.floats("COLOR_CLEAR_VALUE", GlConstants.COLOR_CLEAR_VALUE, 4);
		this.floats("DEPTH_CLEAR_VALUE", GlConstants.DEPTH_CLEAR_VALUE, 1);
		this.integer("STENCIL_CLEAR_VALUE", GlConstants.STENCIL_CLEAR_VALUE);
		this.floats("LINE_WIDTH", GlConstants.LINE_WIDTH, 1);

		final int activeTexture = this.binding.getInteger(GlConstants.ACTIVE_TEXTURE);
		for (int unit = 0; unit < 4; unit++) {
			this.readUnit(unit, compatibility);
		}
		this.binding.activeTexture(activeTexture);

		final int vertexArray = this.capabilities.hasVertexArrays() ? this.binding.getInteger(GlConstants.VERTEX_ARRAY_BINDING) : 0;
		this.stateMap.put("VERTEX_ARRAY_BINDING", Integer.toString(vertexArray));
		for (int index = 1; index < 4; index++) {
			final float[] values = new float[4];
			this.binding.getVertexAttribFloats(index, GlConstants.CURRENT_VERTEX_ATTRIB, values);
			this.stateMap.put("CURRENT_VERTEX_ATTRIB " + index, Arrays.toString(values));
		}
		if (vertexArray != 0 || compatibility) {
			for (int index = 0; index < 8; index++) {
				this.readAttribute(index);
			}
		}

		if (compatibility) {
			this.capability("ALPHA_TEST", GlConstants.ALPHA_TEST);
			this.capability("LIGHTING", GlConstants.LIGHTING);
			this.capability("LIGHT0", GlStateSnapshot.LIGHT0);
			this.capability("FOG", GlConstants.FOG);
			this.capability("COLOR_MATERIAL", GlConstants.COLOR_MATERIAL);
			this.capability("VERTEX_ARRAY", GlStateSnapshot.VERTEX_ARRAY);
			this.capability("COLOR_ARRAY", GlStateSnapshot.COLOR_ARRAY);
			this.capability("NORMAL_ARRAY", GlStateSnapshot.NORMAL_ARRAY);
			this.capability("TEXTURE_COORD_ARRAY", GlStateSnapshot.TEXTURE_COORD_ARRAY);
			this.integer("ALPHA_TEST_FUNC", GlStateSnapshot.ALPHA_TEST_FUNC);
			this.integer("SHADE_MODEL", GlStateSnapshot.SHADE_MODEL);
			this.integer("MATRIX_MODE", GlStateSnapshot.MATRIX_MODE);
			this.integer("CLIENT_ACTIVE_TEXTURE", GlStateSnapshot.CLIENT_ACTIVE_TEXTURE);
			this.floats("ALPHA_TEST_REF", GlStateSnapshot.ALPHA_TEST_REF, 1);
			this.floats("CURRENT_COLOR", GlStateSnapshot.CURRENT_COLOR, 4);
			this.floats("CURRENT_NORMAL", GlStateSnapshot.CURRENT_NORMAL, 3);
			for (final int face : new int[] {GlConstants.FRONT, GlConstants.BACK}) {
				for (final int name : new int[] {GlStateSnapshot.AMBIENT, GlStateSnapshot.DIFFUSE}) {
					final float[] values = new float[4];
					this.materialReader.read(face, name, values);
					this.stateMap.put("MATERIAL " + face + " " + name, Arrays.toString(values));
				}
			}
		}
		return Collections.unmodifiableMap(this.stateMap);
	}

	private void readUnit(final int unit, final boolean compatibility) {
		this.binding.activeTexture(GlConstants.TEXTURE0 + unit);
		final int texture = this.binding.getInteger(GlConstants.TEXTURE_BINDING_2D);
		this.stateMap.put("TEXTURE_BINDING_2D " + unit, Integer.toString(texture));
		if (this.capabilities.hasSamplerObjects()) {
			this.stateMap.put("SAMPLER_BINDING " + unit, Integer.toString(this.binding.getInteger(GlConstants.SAMPLER_BINDING)));
		}
		if (compatibility) {
			this.stateMap.put("TEXTURE_2D " + unit, Boolean.toString(this.binding.isEnabled(GlConstants.TEXTURE_2D)));
			this.floats("CURRENT_TEXTURE_COORDS " + unit, GlStateSnapshot.CURRENT_TEXTURE_COORDS, 4);
		}
		if (texture != 0) {
			for (final int name : new int[] {GlConstants.TEXTURE_MIN_FILTER, GlConstants.TEXTURE_MAG_FILTER, GlConstants.TEXTURE_WRAP_S, GlConstants.TEXTURE_WRAP_T, GlConstants.TEXTURE_BASE_LEVEL, GlConstants.TEXTURE_MAX_LEVEL}) {
				this.stateMap.put("TEXTURE " + texture + " " + name, Integer.toString(this.binding.getTexParameteri(GlConstants.TEXTURE_2D, name)));
			}
		}
	}

	private void readAttribute(final int index) {
		final int[] names = {GlConstants.VERTEX_ATTRIB_ARRAY_ENABLED, GlConstants.VERTEX_ATTRIB_ARRAY_BUFFER_BINDING, GlConstants.VERTEX_ATTRIB_ARRAY_SIZE, GlConstants.VERTEX_ATTRIB_ARRAY_TYPE, GlConstants.VERTEX_ATTRIB_ARRAY_NORMALIZED, GlConstants.VERTEX_ATTRIB_ARRAY_STRIDE};
		final int[] values = new int[names.length];
		for (int i = 0; i < names.length; i++) {
			values[i] = this.binding.getVertexAttribi(index, names[i]);
		}
		this.stateMap.put("VERTEX_ATTRIB " + index, Arrays.toString(values) + " @" + this.binding.getVertexAttribPointer(index, GlConstants.VERTEX_ATTRIB_ARRAY_POINTER));
	}

	private void capability(final String name, final int capability) {
		this.stateMap.put(name, Boolean.toString(this.binding.isEnabled(capability)));
	}

	private void integer(final String name, final int value) {
		this.stateMap.put(name, Integer.toString(this.binding.getInteger(value)));
	}

	private void integers(final String name, final int value, final int count) {
		final int[] values = new int[16];
		this.binding.getIntegers(value, values);
		this.stateMap.put(name, Arrays.toString(Arrays.copyOf(values, count)));
	}

	private void floats(final String name, final int value, final int count) {
		final float[] values = new float[16];
		this.binding.getFloats(value, values);
		this.stateMap.put(name, Arrays.toString(Arrays.copyOf(values, count)));
	}

	@FunctionalInterface
	public interface MaterialReader {

		public void read(final int face, final int name, final @NonNull float[] values);

	}

}