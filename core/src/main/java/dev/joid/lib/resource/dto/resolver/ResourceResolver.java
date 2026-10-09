package dev.joid.lib.resource.dto.resolver;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.resolver.impl.BufferedImageResourceResolver;
import dev.joid.lib.resource.dto.resolver.impl.TextureResourceResolver;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceResolver {

	private static final List<IResourceResolver> RESOLVERS = new LinkedList<>();

	static {
		ResourceResolver.register(new TextureResourceResolver());
		ResourceResolver.register(new BufferedImageResourceResolver());
	}

	public static void register(final @NonNull IResourceResolver resolver) {
		ResourceResolver.RESOLVERS.remove(resolver);
		ResourceResolver.RESOLVERS.add(0, resolver);
	}

	public static boolean supports(final @NonNull Object input) {
		for (final IResourceResolver resolver : ResourceResolver.RESOLVERS) {
			if (resolver.supports(input)) {
				return true;
			}
		}
		return false;
	}

	public static @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
		for (final IResourceResolver resolver : ResourceResolver.RESOLVERS) {
			if (resolver.supports(input)) {
				return resolver.resolve(builder, input, callback);
			}
		}
		throw new IllegalArgumentException("No resolver found for input of type " + input.getClass().getName());
	}

}