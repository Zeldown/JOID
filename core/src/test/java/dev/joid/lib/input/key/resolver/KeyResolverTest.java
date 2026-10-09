package dev.joid.lib.input.key.resolver;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.input.key.Key;

import lombok.NonNull;

public class KeyResolverTest {

	@Test
	public void resolvesAKeyToItself() {
		Assert.assertTrue(KeyResolver.supports(Key.A));
		Assert.assertSame(Key.A, KeyResolver.resolve(Key.A));
	}

	@Test
	public void supportsNothingElseWithoutResolver() {
		Assert.assertFalse(KeyResolver.supports("A"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesToResolveABindingWithoutResolver() {
		KeyResolver.resolve("A");
	}

	@Test
	public void resolvesABindingWithTheResolverThatSupportsIt() {
		final IKeyResolver resolver = new NameKeyResolver(false);
		KeyResolver.register(resolver);
		try {
			Assert.assertTrue(KeyResolver.supports("ESCAPE"));
			Assert.assertSame(Key.ESCAPE, KeyResolver.resolve("ESCAPE"));
		} finally {
			KeyResolver.unregister(resolver);
		}
		Assert.assertFalse(KeyResolver.supports("ESCAPE"));
	}

	@Test
	public void givesPriorityToTheLatestRegistration() {
		final IKeyResolver names = new NameKeyResolver(false);
		final IKeyResolver unbound = new NameKeyResolver(true);
		KeyResolver.register(names);
		KeyResolver.register(unbound);
		try {
			Assert.assertNull(KeyResolver.resolve("ESCAPE"));
			KeyResolver.register(names);
			Assert.assertSame(Key.ESCAPE, KeyResolver.resolve("ESCAPE"));
		} finally {
			KeyResolver.unregister(names);
			KeyResolver.unregister(unbound);
		}
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullBinding() {
		KeyResolver.resolve(null);
	}

	public static final class NameKeyResolver implements IKeyResolver {

		private final boolean unbound;

		public NameKeyResolver(final boolean unbound) {
			this.unbound = unbound;
		}

		@Override
		public boolean supports(final @NonNull Object binding) {
			return binding instanceof String;
		}

		@Override
		public Key resolve(final @NonNull Object binding) {
			return this.unbound ? null : Key.valueOf((String) binding);
		}

	}

}