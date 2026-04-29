package be.zeldown.joid.lib.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.ResourceProperties;
import be.zeldown.joid.lib.resource.dto.resolver.ResourceResolver;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ResourceBuilder {

	private static final List<ResourceBuilder> BUILDER_LIST = new ArrayList<>();

	public static final Cache<String, ResourceData> DEFAULT_CACHE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

	private Cache<String, ResourceData> cache;
	private ResourceProperties properties;

	private ResourceBuilder() {
		this.cache = ResourceBuilder.DEFAULT_CACHE;
		this.properties = new ResourceProperties();

		ResourceBuilder.BUILDER_LIST.add(this);
	}

	public static @NonNull ResourceBuilder create() {
		return new ResourceBuilder();
	}

	/* [ Query Section ] */
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

	public final @NonNull ResourceBuilder interpolation(final int interpolation) {
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

	public final @NonNull ResourceBuilder textureCoords(final double u, final double v, final double u2, final double v2) {
		this.properties.textureCoords(u, v, u2, v2);
		return this;
	}

	public final @NonNull ResourceBuilder copy() {
		final ResourceBuilder copy = new ResourceBuilder();
		copy.cache = this.cache;
		copy.properties = this.properties.copy();
		return copy;
	}

	/* [ Resource Section ] */
	public @NonNull Resource of(final @NonNull Object input) {
		return this.of(input, null);
	}

	public @NonNull Resource of(final @NonNull Object input, final Consumer<Resource> callback) {
		return ResourceResolver.resolve(this, input, callback);
	}

	public final @NonNull Resource compute(final @NonNull String uniqueId, final @NonNull Supplier<Resource> supplier) {
		if (this.cache == null) {
			return supplier.get();
		}

		final ResourceData data = this.cache.getIfPresent(uniqueId);
		if (data != null) {
			return new Resource(this, data);
		}

		final Resource resource = supplier.get();
		this.cache.put(uniqueId, resource.getResourceData());
		return resource;
	}

	public final void reload() {
		if (this.cache == null) {
			return;
		}

		this.cache.invalidateAll();
	}

	/* [ Static Section ] */
	public static @NonNull List<@NonNull ResourceBuilder> getBuilders() {
		return ResourceBuilder.BUILDER_LIST;
	}

}