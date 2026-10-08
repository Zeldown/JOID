package dev.joid.lib.bridge.render.shader.source;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class StencilShaderTranslator extends BlockShaderTranslator {

	public static final String STENCIL           = "joid_Stencil";
	public static final String STENCIL_TEST      = "joid_StencilTest";
	public static final String STENCIL_MASK      = "joid_StencilMask";
	public static final String STENCIL_FAIL      = "joid_StencilFail";
	public static final String STENCIL_PASS      = "joid_StencilPass";
	public static final String STENCIL_FUNCTION  = "joid_StencilFunction";
	public static final String STENCIL_REFERENCE = "joid_StencilReference";

	private final boolean write;

	public static @NonNull StencilShaderTranslator create() {
		return new StencilShaderTranslator(false);
	}

	public static @NonNull StencilShaderTranslator write() {
		return new StencilShaderTranslator(true);
	}

	@Override
	protected @NonNull String getMain() {
		return "\nuniform sampler2D " + StencilShaderTranslator.STENCIL + ";\n"
		+ "\nint joid_stencilValue() {\n\treturn int(texelFetch(joid_Stencil, ivec2(gl_FragCoord.xy), 0).r * 255.0 + 0.5);\n}\n"
		+ "\nbool joid_stencilCompare(int value) {\n\tint reference = joid_StencilReference & joid_StencilMask;\n\tint current = value & joid_StencilMask;\n\tswitch (joid_StencilFunction) {\n\tcase 0:\n\t\treturn false;\n\tcase 1:\n\t\treturn reference < current;\n\tcase 2:\n\t\treturn reference <= current;\n\tcase 3:\n\t\treturn reference > current;\n\tcase 4:\n\t\treturn reference >= current;\n\tcase 5:\n\t\treturn reference == current;\n\tcase 6:\n\t\treturn reference != current;\n\tdefault:\n\t\treturn true;\n\t}\n}\n"
		+ "\nint joid_stencilApply(int operation, int value) {\n\tswitch (operation) {\n\tcase 1:\n\t\treturn 0;\n\tcase 2:\n\t\treturn joid_StencilReference & 255;\n\tcase 3:\n\t\treturn min(value + 1, 255);\n\tcase 4:\n\t\treturn max(value - 1, 0);\n\tcase 5:\n\t\treturn ~value & 255;\n\tdefault:\n\t\treturn value;\n\t}\n}\n"
		+ "\nvoid main() {\n\tfragColor = vec4(0.0);\n\tjoid_main();\n\tif (joid_AlphaTest != 0 && fragColor.a <= joid_AlphaThreshold) {\n\t\tdiscard;\n\t}\n\n"
		+ (this.write ? "\tint value = joid_stencilValue();\n\tvalue = joid_stencilApply(joid_stencilCompare(value) ? joid_StencilPass : joid_StencilFail, value);\n\tfragColor = vec4(float(value) / 255.0, 0.0, 0.0, 1.0);\n}\n" : "\tif (joid_StencilTest != 0 && !joid_stencilCompare(joid_stencilValue())) {\n\t\tdiscard;\n\t}\n}\n");
	}

	@Override
	protected @NonNull List<@NonNull ShaderVariable> getInternals(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final List<ShaderVariable> internalList = super.getInternals(vertex, fragment);
		for (final String name : new String[] {StencilShaderTranslator.STENCIL_TEST, StencilShaderTranslator.STENCIL_FUNCTION, StencilShaderTranslator.STENCIL_REFERENCE, StencilShaderTranslator.STENCIL_MASK, StencilShaderTranslator.STENCIL_FAIL, StencilShaderTranslator.STENCIL_PASS}) {
			internalList.add(ShaderVariable.create("int", name, "", false));
		}
		return internalList;
	}

}