package dev.joid.lib.resource.cache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.cache.Cache;

import dev.joid.internal.TestUtils;
import dev.joid.lib.resource.ResourceData;

public class MemoryResourceCacheTest {

	@Test
	public void keepsItsDataWithoutLimitForFiveMinutesByDefault() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		Assert.assertEquals(Integer.MAX_VALUE, cache.getSize());
		Assert.assertEquals(300000L, cache.getExpiry());
	}

	@Test
	public void storesAndRemovesItsData() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceData first = ResourceData.create("first", null);
		final ResourceData second = ResourceData.create("second", null);
		cache.put("first", first);
		cache.put("second", second);
		Assert.assertSame(first, cache.get("first"));
		Assert.assertEquals(new HashSet<>(Arrays.asList(first, second)), new HashSet<>(cache.getDataList()));
		cache.remove("first");
		Assert.assertNull(cache.get("first"));
		Assert.assertEquals(Collections.singletonList(second), cache.getDataList());
		cache.clear();
		Assert.assertTrue(cache.getDataList().isEmpty());
	}

	@Test
	public void evictsBeyondItsSize() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		Assert.assertSame(cache, cache.size(1));
		Assert.assertEquals(1, cache.getSize());
		cache.put("first", ResourceData.create("first", null));
		cache.put("second", ResourceData.create("second", null));
		Assert.assertEquals(1, cache.getDataList().size());
		Assert.assertNotNull(cache.get("second"));
	}

	@Test
	public void forgetsItsDataAfterItsExpiry() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		Assert.assertSame(cache, cache.expiry(20L));
		Assert.assertEquals(20L, cache.getExpiry());
		Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(20L), TestUtils.field(TestUtils.field(TestUtils.field(cache, "cache"), "localCache"), "expireAfterAccessNanos"));
	}

	@Test
	public void findsItsDataStillInUseAfterItsExpiry() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceData data = ResourceData.create("image", null);
		cache.put("image", data);
		((Cache<?, ?>) TestUtils.field(cache, "cache")).invalidateAll();
		Assert.assertTrue(cache.getDataList().isEmpty());
		Assert.assertSame(data, cache.get("image"));
		Assert.assertEquals(Collections.singletonList(data), cache.getDataList());
	}

	@Test
	public void forgetsItsRemovedDataEvenInUse() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceData data = ResourceData.create("image", null);
		cache.put("image", data);
		cache.remove("image");
		Assert.assertNull(cache.get("image"));
		cache.put("image", data);
		cache.clear();
		Assert.assertNull(cache.get("image"));
	}

	@Test
	public void keepsItsDataWhenItsSettingsChange() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceData data = ResourceData.create("image", null);
		cache.put("image", data);
		cache.size(10).expiry(60000L);
		Assert.assertSame(data, cache.get("image"));
	}

}