package dev.joid.lib.input.key.resolver;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.utils.registry.Registry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyResolver {

	private static final Registry<IKeyResolver> RESOLVERS = Registry.create();

	public static void register(final @NonNull IKeyResolver resolver) {
		KeyResolver.RESOLVERS.register(resolver);
	}

	public static void unregister(final @NonNull IKeyResolver resolver) {
		KeyResolver.RESOLVERS.unregister(resolver);
	}

	public static boolean supports(final @NonNull Object binding) {
		return binding instanceof Key || KeyResolver.RESOLVERS.find(resolver -> resolver.supports(binding)) != null;
	}

	public static Key resolve(final @NonNull Object binding) {
		if (binding instanceof Key) {
			return (Key) binding;
		}

		final IKeyResolver resolver = KeyResolver.RESOLVERS.find(candidate -> candidate.supports(binding));
		if (resolver == null) {
			throw new IllegalArgumentException("No key resolver knows the binding " + binding + " of type " + binding.getClass().getName() + ", pass a Key or register an IKeyResolver with KeyResolver.register(...)");
		}

		return resolver.resolve(binding);
	}

}