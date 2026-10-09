package dev.joid.lib.utils.key.resolver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import dev.joid.lib.utils.key.Key;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyResolver {

	private static final List<IKeyResolver> RESOLVERS = new CopyOnWriteArrayList<>();

	public static void register(final @NonNull IKeyResolver resolver) {
		KeyResolver.RESOLVERS.remove(resolver);
		KeyResolver.RESOLVERS.add(0, resolver);
	}

	public static void unregister(final @NonNull IKeyResolver resolver) {
		KeyResolver.RESOLVERS.remove(resolver);
	}

	public static boolean supports(final @NonNull Object binding) {
		if (binding instanceof Key) {
			return true;
		}

		for (final IKeyResolver resolver : KeyResolver.RESOLVERS) {
			if (resolver.supports(binding)) {
				return true;
			}
		}
		return false;
	}

	public static Key resolve(final @NonNull Object binding) {
		if (binding instanceof Key) {
			return (Key) binding;
		}

		for (final IKeyResolver resolver : KeyResolver.RESOLVERS) {
			if (resolver.supports(binding)) {
				return resolver.resolve(binding);
			}
		}
		throw new IllegalArgumentException("No key resolver found for a binding of type " + binding.getClass().getName());
	}

}