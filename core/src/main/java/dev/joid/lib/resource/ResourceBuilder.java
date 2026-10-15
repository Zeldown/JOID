package dev.joid.lib.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.resource.cache.IResourceCache;
import dev.joid.lib.resource.cache.impl.MemoryResourceCache;
import dev.joid.lib.resource.format.ResourceFormat;
import dev.joid.lib.resource.resolver.ResourceResolver;
import lombok.Getter;
import lombok.NonNull;

public final class ResourceBuilder {

	private static final IResourceCache       DEFAULT_CACHE = MemoryResourceCache.create();
	private static final Set<ResourceBuilder> BUILDER_SET   = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	private final Set<String> uniqueIdSet;

	private final ResourceProperties properties;
	@Getter private final IResourceCache cache;

	private ResourceBuilder(final IResourceCache cache, final @NonNull ResourceProperties properties) {
		this.cache = cache;
		this.properties = properties;
		this.uniqueIdSet = ConcurrentHashMap.newKeySet();

		ResourceBuilder.BUILDER_SET.add(this);
	}

	public static @NonNull ResourceBuilder create() {
		return new ResourceBuilder(ResourceBuilder.DEFAULT_CACHE, new ResourceProperties());
	}

	public @NonNull ResourceBuilder cache(final IResourceCache cache) {
		return new ResourceBuilder(cache, this.properties);
	}

	public @NonNull ResourceBuilder interpolation(final @NonNull TextureFilter interpolation) {
		return new ResourceBuilder(this.cache, this.properties.copy().interpolation(interpolation));
	}

	public @NonNull ResourceBuilder linear() {
		return new ResourceBuilder(this.cache, this.properties.copy().linear());
	}

	public @NonNull ResourceBuilder nearest() {
		return new ResourceBuilder(this.cache, this.properties.copy().nearest());
	}

	public @NonNull ResourceBuilder mipmap(final boolean mipmap) {
		return new ResourceBuilder(this.cache, this.properties.copy().mipmap(mipmap));
	}

	public @NonNull ResourceBuilder textureCoords(final double u, final double v, final double width, final double height) {
		return new ResourceBuilder(this.cache, this.properties.copy().textureCoords(u, v, width, height));
	}

	public @NonNull Resource of(final @NonNull Object input) {
		if (ResourceResolver.supports(input)) {
			return ResourceResolver.resolve(this, input);
		}

		final Asset asset;
		try {
			asset = Asset.of(input);
		} catch (final IllegalArgumentException exception) {
			if (!(input instanceof String)) {
				throw exception;
			}

			return this.compute((String) input, () -> ResourceBuilder.failed((String) input, exception));
		}

		if (asset.isRemote()) {
			return this.compute(asset.getUniqueId(), () -> {
				final ResourceData data = new ResourceData(asset.getUniqueId(), null);
				data.dispatch(() -> {
					try {
						data.decoder(ResourceFormat.decoder(asset));
					} catch (final RuntimeException exception) {
						data.fail(exception);
					}
				});
				return data;
			});
		}

		return this.compute(asset.getUniqueId(), () -> ResourceBuilder.read(asset));
	}

	public @NonNull Resource compute(final @NonNull String uniqueId, final @NonNull Supplier<ResourceData> dataSupplier) {
		if (this.cache == null) {
			return new Resource(this, dataSupplier.get());
		}

		this.uniqueIdSet.add(uniqueId);
		final ResourceData cached = this.cache.get(uniqueId);
		if (cached != null) {
			return new Resource(this, cached);
		}

		final ResourceData data = dataSupplier.get();
		this.cache.put(uniqueId, data);
		return new Resource(this, data);
	}

	public @NonNull ResourceProperties getProperties() {
		return this.properties.copy();
	}

	public void reload() {
		if (this.cache == null) {
			return;
		}
		for (final String uniqueId : this.uniqueIdSet) {
			this.cache.remove(uniqueId);
		}
		this.uniqueIdSet.clear();
	}

	public static @NonNull List<@NonNull ResourceBuilder> getBuilders() {
		synchronized (ResourceBuilder.BUILDER_SET) {
			return new ArrayList<>(ResourceBuilder.BUILDER_SET);
		}
	}

	private static @NonNull ResourceData read(final @NonNull Asset asset) {
		try {
			return new ResourceData(asset.getUniqueId(), ResourceFormat.decoder(asset));
		} catch (final RuntimeException exception) {
			return ResourceBuilder.failed(asset.getUniqueId(), exception);
		}
	}

	private static @NonNull ResourceData failed(final @NonNull String uniqueId, final @NonNull RuntimeException exception) {
		final ResourceData data = new ResourceData(uniqueId, null);
		data.fail(exception);
		return data;
	}

}