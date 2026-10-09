package dev.joid.lib.utils.key.resolver;

import dev.joid.lib.utils.key.Key;
import lombok.NonNull;

public interface IKeyResolver {

	public boolean supports(final @NonNull Object binding);

	public Key resolve(final @NonNull Object binding);

}