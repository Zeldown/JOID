package dev.joid.lib.resource.resolver;

import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.resolver.impl.BufferedImageResourceResolver;
import dev.joid.lib.resource.resolver.impl.TextureResourceResolver;
import dev.joid.lib.utils.registry.Registry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceResolver {

	private static final Registry<IResourceResolver> RESOLVERS = Registry.create();

	static {
		ResourceResolver.register(new TextureResourceResolver());
		ResourceResolver.register(new BufferedImageResourceResolver());
	}

	public static void register(final @NonNull IResourceResolver resolver) {
		ResourceResolver.RESOLVERS.register(resolver);
	}

	public static void unregister(final @NonNull IResourceResolver resolver) {
		ResourceResolver.RESOLVERS.unregister(resolver);
	}

	public static boolean supports(final @NonNull Object input) {
		return ResourceResolver.RESOLVERS.find(resolver -> resolver.supports(input)) != null;
	}

	public static @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input) {
		final IResourceResolver resolver = ResourceResolver.RESOLVERS.find(candidate -> candidate.supports(input));
		if (resolver == null) {
			throw new IllegalArgumentException("No resource resolver knows the input " + input + " of type " + input.getClass().getName() + ", register an IResourceResolver that supports it with ResourceResolver.register(...)");
		}

		return resolver.resolve(builder, input);
	}

}