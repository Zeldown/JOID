package be.zeldown.joid.lib.resource.dto.resolver;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import be.zeldown.joid.lib.resource.dto.resolver.impl.BufferedImageResourceResolver;
import be.zeldown.joid.lib.resource.dto.resolver.impl.InputStreamResourceResolver;
import be.zeldown.joid.lib.resource.dto.resolver.impl.TextureIdResourceResolver;
import be.zeldown.joid.lib.resource.dto.resolver.impl.UrlResourceResolver;
import lombok.NonNull;

public class ResourceResolver {

	private static final List<IResourceResolver> RESOLVERS = new LinkedList<>();

	static {
		ResourceResolver.register(new InputStreamResourceResolver());
		ResourceResolver.register(new BufferedImageResourceResolver());
		ResourceResolver.register(new UrlResourceResolver());
		ResourceResolver.register(new TextureIdResourceResolver());
	}

	public static void register(final @NonNull IResourceResolver resolver) {
		ResourceResolver.RESOLVERS.add(0, resolver);
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