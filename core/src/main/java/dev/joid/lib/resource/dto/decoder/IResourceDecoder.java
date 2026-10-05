package dev.joid.lib.resource.dto.decoder;

import dev.joid.lib.resource.dto.ResourceData;
import lombok.NonNull;

public interface IResourceDecoder {

	public void init(final @NonNull ResourceData resource);
	public void clear(final @NonNull ResourceData resource);
	public void decode(final @NonNull ResourceData resource);
	public void update(final @NonNull ResourceData resource);
	public void upload(final @NonNull ResourceData resource);
	public void prepare(final @NonNull ResourceData resource);

	public default void request(final @NonNull ResourceData resource, final int width, final int height, final boolean async) {}

	public default boolean isSettled() {
		return true;
	}

	public default boolean isMipmappable() {
		return true;
	}

}