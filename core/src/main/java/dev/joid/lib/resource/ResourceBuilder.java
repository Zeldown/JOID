package dev.joid.lib.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.resource.cache.IResourceCache;
import dev.joid.lib.resource.cache.MemoryResourceCache;
import dev.joid.lib.resource.resolver.ResourceResolver;
import lombok.Getter;
import lombok.NonNull;

public final class ResourceBuilder {

	private static final IResourceCache       DEFAULT_CACHE = MemoryResourceCache.create();
	private static final Set<ResourceBuilder> BUILDER_SET   = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	private final Map<ResourceData, String> computed;

	private final ResourceProperties properties;
	@Getter private final IResourceCache cache;

	private ResourceBuilder(final IResourceCache cache, final @NonNull ResourceProperties properties) {
		this.cache = cache;
		this.properties = properties;
		this.computed = Collections.synchronizedMap(new WeakHashMap<>());

		ResourceBuilder.BUILDER_SET.add(this);
	}

	public static @NonNull ResourceBuilder create() {
		return new ResourceBuilder(ResourceBuilder.DEFAULT_CACHE, ResourceProperties.create());
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

			return this.compute((String) input, () -> ResourceBuilder.detect((String) input, () -> Asset.of(input)));
		}

		return this.compute(asset.getUniqueId(), () -> ResourceBuilder.detect(asset.getUniqueId(), () -> asset));
	}

	public @NonNull Resource compute(final @NonNull String uniqueId, final @NonNull Supplier<ResourceData> dataSupplier) {
		if (this.cache == null) {
			return Resource.create(this, dataSupplier.get());
		}

		final ResourceData cached = this.cache.get(uniqueId);
		if (cached != null) {
			this.computed.put(cached, uniqueId);
			return Resource.create(this, cached);
		}

		final ResourceData data = dataSupplier.get();
		this.cache.put(uniqueId, data);
		this.computed.put(data, uniqueId);
		return Resource.create(this, data);
	}

	public @NonNull ResourceProperties getProperties() {
		return this.properties.copy();
	}

	public void remove(final @NonNull ResourceData data) {
		if (this.cache == null) {
			return;
		}

		final String uniqueId = this.computed.getOrDefault(data, data.getUniqueId());
		if (this.cache.get(uniqueId) == data) {
			this.cache.remove(uniqueId);
		}
		this.computed.remove(data);
	}

	public void reload() {
		if (this.cache == null) {
			return;
		}
		synchronized (this.computed) {
			for (final Map.Entry<ResourceData, String> entry : this.computed.entrySet()) {
				if (this.cache.get(entry.getValue()) == entry.getKey()) {
					this.cache.remove(entry.getValue());
				}
			}
			this.computed.clear();
		}
	}

	public static @NonNull List<@NonNull ResourceBuilder> getBuilders() {
		synchronized (ResourceBuilder.BUILDER_SET) {
			return new ArrayList<>(ResourceBuilder.BUILDER_SET);
		}
	}

	private static @NonNull ResourceData detect(final @NonNull String uniqueId, final @NonNull Supplier<Asset> assetSource) {
		final ResourceData data = ResourceData.create(uniqueId, null).assetSource(assetSource);
		data.detectDecoder(true);
		return data;
	}

}