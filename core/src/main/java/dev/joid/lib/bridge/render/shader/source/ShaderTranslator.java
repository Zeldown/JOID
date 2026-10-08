package dev.joid.lib.bridge.render.shader.source;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import lombok.NonNull;

public abstract class ShaderTranslator {

	public abstract @NonNull String translateVertex(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment);
	public abstract @NonNull String translateFragment(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment);

	public final @NonNull UniformBlock createBlock(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		return UniformBlock.create(this.getUniforms(vertex, fragment), vertex.getBody() + fragment.getBody());
	}

	public final @NonNull List<@NonNull ShaderVariable> getUniforms(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final List<ShaderVariable> uniformList = new ArrayList<>();
		for (final ShaderBuiltin builtin : ShaderBuiltin.values()) {
			if (builtin.getKind() == ShaderBuiltin.Kind.UNIFORM && this.isUniform(builtin) && (vertex.getBuiltins().contains(builtin) || fragment.getBuiltins().contains(builtin))) {
				uniformList.add(ShaderVariable.create(builtin.getType(), builtin.getIdentifier(), "", false));
			}
		}

		uniformList.addAll(this.getInternals(vertex, fragment));
		for (final ShaderSource source : new ShaderSource[] {vertex, fragment}) {
			for (final ShaderVariable uniform : source.getUniforms()) {
				if (uniformList.stream().noneMatch(variable -> variable.getName().equals(uniform.getName()))) {
					uniformList.add(uniform);
				}
			}
		}
		return uniformList;
	}

	public final @NonNull List<@NonNull ShaderVariable> getSamplers(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		final List<ShaderVariable> samplerList = new ArrayList<>();
		for (final ShaderSource source : new ShaderSource[] {vertex, fragment}) {
			for (final ShaderVariable sampler : source.getSamplers()) {
				if (samplerList.stream().noneMatch(variable -> variable.getName().equals(sampler.getName()))) {
					samplerList.add(sampler);
				}
			}
		}
		return samplerList;
	}

	protected boolean isUniform(final @NonNull ShaderBuiltin builtin) {
		return true;
	}

	protected @NonNull List<@NonNull ShaderVariable> getInternals(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment) {
		return new ArrayList<>();
	}

}