package dev.joid.lib.resource.cache;

import java.util.Collection;

import dev.joid.lib.resource.ResourceData;
import lombok.NonNull;

public interface IResourceCache {

	public void put(final @NonNull String uniqueId, final @NonNull ResourceData data);
	public void remove(final @NonNull String uniqueId);

	public void clear();

	public ResourceData get(final @NonNull String uniqueId);
	public @NonNull Collection<@NonNull ResourceData> getDataList();

}