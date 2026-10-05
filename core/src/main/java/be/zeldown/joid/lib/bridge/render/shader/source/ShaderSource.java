package be.zeldown.joid.lib.bridge.render.shader.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ShaderSource {

	private static final Pattern VERSION     = Pattern.compile("^\\s*#version\\b.*$");
	private static final Pattern DECLARATION = Pattern.compile("^\\s*(?:layout\\s*\\([^)]*\\)\\s*)?(flat\\s+)?(in|out|uniform)\\s+(\\w+)\\s+(\\w+)\\s*(\\[\\s*\\w+\\s*\\])?\\s*;\\s*$");

	private final ShaderStage          stage;
	private final List<ShaderVariable> inputs;
	private final Set<ShaderBuiltin>   builtins;
	private final List<ShaderVariable> outputs;
	private final List<ShaderVariable> uniforms;
	private final List<ShaderVariable> samplers;

	private String body;

	private ShaderSource(final ShaderStage stage) {
		this.stage    = stage;
		this.inputs   = new ArrayList<>();
		this.outputs  = new ArrayList<>();
		this.uniforms = new ArrayList<>();
		this.samplers = new ArrayList<>();
		this.builtins = EnumSet.noneOf(ShaderBuiltin.class);
	}

	public static @NonNull ShaderSource parse(final @NonNull ShaderStage stage, final @NonNull String code) {
		final ShaderSource source = new ShaderSource(stage);
		final StringBuilder body = new StringBuilder();
		final StringBuilder instructions = new StringBuilder();

		boolean comment = false;
		for (final String line : code.split("\r?\n", -1)) {
			final StringBuilder stripped = new StringBuilder();
			comment = ShaderSource.stripComments(line, comment, stripped);

			final Matcher declaration = ShaderSource.DECLARATION.matcher(stripped);
			if (ShaderSource.VERSION.matcher(stripped).matches()) {
				body.append('\n');
			} else if (declaration.matches()) {
				source.declare(declaration);
				body.append('\n');
			} else {
				body.append(line).append('\n');
				instructions.append(stripped).append('\n');
			}
		}

		for (final ShaderBuiltin builtin : ShaderBuiltin.values()) {
			if (Pattern.compile("\\b" + builtin.getIdentifier() + "\\b").matcher(instructions).find()) {
				source.builtins.add(builtin);
			}
		}

		source.body = body.toString();
		return source;
	}

	public static @NonNull ShaderSource read(final @NonNull ShaderStage stage, final @NonNull InputStream stream) {
		try {
			return ShaderSource.parse(stage, IOUtils.toString(stream, StandardCharsets.UTF_8));
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void declare(final Matcher declaration) {
		final String name = declaration.group(4);
		if (ShaderBuiltin.find(name) != null) {
			return;
		}

		final String array = declaration.group(5) == null ? "" : declaration.group(5).replaceAll("\\s", "");
		final ShaderVariable variable = ShaderVariable.create(declaration.group(3), name, array, declaration.group(1) != null);
		if ("uniform".equals(declaration.group(2))) {
			(variable.getType().startsWith("sampler") ? this.samplers : this.uniforms).add(variable);
		} else if ("in".equals(declaration.group(2))) {
			if (this.stage == ShaderStage.VERTEX) {
				throw new IllegalArgumentException("Vertex shaders cannot declare the input " + name + ", use the built-in attributes aPosition, aTexCoord, aColor and aNormal");
			}
			this.inputs.add(variable);
		} else {
			if (this.stage == ShaderStage.FRAGMENT) {
				throw new IllegalArgumentException("Fragment shaders cannot declare the output " + name + ", write the color to fragColor");
			}
			this.outputs.add(variable);
		}
	}

	private static boolean stripComments(final String line, final boolean comment, final StringBuilder output) {
		boolean inComment = comment;
		for (int i = 0; i < line.length(); i++) {
			if (inComment) {
				if (line.startsWith("*/", i)) {
					inComment = false;
					i++;
				}
				continue;
			}

			if (line.startsWith("//", i)) {
				break;
			}

			if (line.startsWith("/*", i)) {
				inComment = true;
				i++;
				continue;
			}

			output.append(line.charAt(i));
		}
		return inComment;
	}

}