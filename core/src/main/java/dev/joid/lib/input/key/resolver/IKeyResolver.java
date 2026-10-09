package dev.joid.lib.input.key.resolver;

import dev.joid.lib.input.key.Key;
import lombok.NonNull;

public interface IKeyResolver {

	public boolean supports(final @NonNull Object binding);

	public Key resolve(final @NonNull Object binding);

}