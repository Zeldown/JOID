package dev.joid.lib.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.ResourceProperties;
import dev.joid.lib.resource.dto.format.ResourceFormat;
import dev.joid.lib.resource.dto.resolver.ResourceResolver;
import lombok.Getter;
import lombok.NonNull;

public final class ResourceBuilder {

	private static final Set<ResourceBuilder> BUILDER_SET = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	public static final Cache<String, ResourceData> DEFAULT_CACHE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

	private final Set<String> uniqueIdSet;

	@Getter private ResourceProperties properties;
	@Getter private Cache<String, ResourceData> cache;

	private ResourceBuilder() {
		this.cache = ResourceBuilder.DEFAULT_CACHE;
		this.properties = new ResourceProperties();
		this.uniqueIdSet = ConcurrentHashMap.newKeySet();

		ResourceBuilder.BUILDER_SET.add(this);
	}

	public static @NonNull ResourceBuilder create() {
		return new ResourceBuilder();
	}

	public final @NonNull ResourceBuilder cache(final Cache<String, ResourceData> cache) {
		this.cache = cache;
		return this;
	}

	public final @NonNull ResourceBuilder async() {
		this.properties.async();
		return this;
	}

	public final @NonNull ResourceBuilder blocking() {
		this.properties.blocking();
		return this;
	}

	public final @NonNull ResourceBuilder interpolation(final @NonNull TextureFilter interpolation) {
		this.properties.interpolation(interpolation);
		return this;
	}

	public final @NonNull ResourceBuilder linear() {
		this.properties.linear();
		return this;
	}

	public final @NonNull ResourceBuilder nearest() {
		this.properties.nearest();
		return this;
	}

	public final @NonNull ResourceBuilder mipmap(final boolean mipmap) {
		this.properties.mipmap(mipmap);
		return this;
	}

	public final @NonNull ResourceBuilder textureCoords(final double u, final double v, final double width, final double height) {
		this.properties.textureCoords(u, v, width, height);
		return this;
	}

	public final @NonNull ResourceBuilder copy() {
		final ResourceBuilder copy = new ResourceBuilder();
		copy.cache = this.cache;
		copy.properties = this.properties.copy();
		return copy;
	}

	public @NonNull Resource of(final @NonNull Object input) {
		return this.of(input, null);
	}

	public @NonNull Resource of(final @NonNull Object input, final Consumer<Resource> callback) {
		if (ResourceResolver.supports(input)) {
			return ResourceResolver.resolve(this, input, callback);
		}

		final Asset asset = Asset.of(input);
		if (asset.isRemote()) {
			final AtomicBoolean created = new AtomicBoolean();
			final Resource resource = this.compute(asset.getUniqueId(), () -> new ResourceData(asset.getUniqueId(), null), computed -> {
				created.set(true);
				computed.dispatch(() -> {
					computed.decoder(ResourceFormat.decoder(asset));
					if (callback != null) {
						callback.accept(computed);
					}
				});
			});
			if (!created.get() && callback != null) {
				callback.accept(resource);
			}
			return resource;
		}

		final Resource resource = this.compute(asset.getUniqueId(), () -> new ResourceData(asset.getUniqueId(), ResourceFormat.decoder(asset)));
		if (callback != null) {
			callback.accept(resource);
		}
		return resource;
	}

	public final @NonNull Resource compute(final @NonNull String uniqueId, final @NonNull Supplier<ResourceData> dataSupplier) {
		return this.compute(uniqueId, dataSupplier, null);
	}

	public final @NonNull Resource compute(final @NonNull String uniqueId, final @NonNull Supplier<ResourceData> dataSupplier, final Consumer<Resource> onCreate) {
		if (this.cache == null) {
			final Resource resource = new Resource(this, dataSupplier.get());
			if (onCreate != null) {
				onCreate.accept(resource);
			}
			return resource;
		}

		this.uniqueIdSet.add(uniqueId);
		final ResourceData cached = this.cache.getIfPresent(uniqueId);
		if (cached != null) {
			return new Resource(this, cached);
		}

		final ResourceData data = dataSupplier.get();
		this.cache.put(uniqueId, data);
		final Resource resource = new Resource(this, data);
		if (onCreate != null) {
			onCreate.accept(resource);
		}
		return resource;
	}

	public final void reload() {
		if (this.cache == null) {
			return;
		}
		this.cache.invalidateAll(this.uniqueIdSet);
		this.uniqueIdSet.clear();
	}

	public static @NonNull List<@NonNull ResourceBuilder> getBuilders() {
		synchronized (ResourceBuilder.BUILDER_SET) {
			return new ArrayList<>(ResourceBuilder.BUILDER_SET);
		}
	}

}