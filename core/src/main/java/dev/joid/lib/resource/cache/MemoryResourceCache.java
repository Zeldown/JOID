package dev.joid.lib.resource.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import dev.joid.lib.resource.ResourceData;
import lombok.Getter;
import lombok.NonNull;

public final class MemoryResourceCache implements IResourceCache {

	@Getter private int size;
	@Getter private long expiry;

	private Cache<String, ResourceData> cache;

	private MemoryResourceCache() {
		this.size = Integer.MAX_VALUE;
		this.expiry = Duration.ofMinutes(5).toMillis();
		this.cache = MemoryResourceCache.build(this.size, this.expiry);
	}

	public static @NonNull MemoryResourceCache create() {
		return new MemoryResourceCache();
	}

	public @NonNull MemoryResourceCache size(final int size) {
		this.size = size;
		this.rebuild();
		return this;
	}

	public @NonNull MemoryResourceCache expiry(final long expiry) {
		this.expiry = expiry;
		this.rebuild();
		return this;
	}

	@Override
	public void put(final @NonNull String uniqueId, final @NonNull ResourceData data) {
		this.cache.put(uniqueId, data);
	}

	@Override
	public void remove(final @NonNull String uniqueId) {
		this.cache.invalidate(uniqueId);
	}

	@Override
	public void clear() {
		this.cache.invalidateAll();
	}

	@Override
	public ResourceData get(final @NonNull String uniqueId) {
		return this.cache.getIfPresent(uniqueId);
	}

	@Override
	public @NonNull Collection<@NonNull ResourceData> getDataList() {
		return new ArrayList<>(this.cache.asMap().values());
	}

	private void rebuild() {
		final Cache<String, ResourceData> previous = this.cache;
		this.cache = MemoryResourceCache.build(this.size, this.expiry);
		this.cache.putAll(previous.asMap());
	}

	private static Cache<String, ResourceData> build(final int size, final long expiry) {
		return CacheBuilder.newBuilder().maximumSize(size).expireAfterAccess(Duration.ofMillis(expiry)).build();
	}

}