package dev.joid.lib.resource.resolver;

import java.util.LinkedList;
import java.util.List;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.resolver.impl.BufferedImageResourceResolver;
import dev.joid.lib.resource.resolver.impl.TextureResourceResolver;
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

	public static @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input) {
		for (final IResourceResolver resolver : ResourceResolver.RESOLVERS) {
			if (resolver.supports(input)) {
				return resolver.resolve(builder, input);
			}
		}
		throw new IllegalArgumentException("No resource resolver knows the input " + input + " of type " + input.getClass().getName() + ", register an IResourceResolver that supports it with ResourceResolver.register(...)");
	}

}