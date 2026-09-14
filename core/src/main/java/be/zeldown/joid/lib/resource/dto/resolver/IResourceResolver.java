package be.zeldown.joid.lib.resource.dto.resolver;

import java.util.function.Consumer;

import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import lombok.NonNull;

public interface IResourceResolver {

	public boolean supports(final @NonNull Object input);

	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback);

}