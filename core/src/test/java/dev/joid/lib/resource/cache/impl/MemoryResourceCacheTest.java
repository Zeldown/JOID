package dev.joid.lib.resource.cache.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

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
		final ResourceData first = new ResourceData("first", null);
		final ResourceData second = new ResourceData("second", null);
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
		cache.put("first", new ResourceData("first", null));
		cache.put("second", new ResourceData("second", null));
		Assert.assertEquals(1, cache.getDataList().size());
		Assert.assertNotNull(cache.get("second"));
	}

	@Test
	public void forgetsItsDataAfterItsExpiry() throws InterruptedException {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		Assert.assertSame(cache, cache.expiry(20L));
		Assert.assertEquals(20L, cache.getExpiry());
		cache.put("image", new ResourceData("image", null));
		Thread.sleep(100L);
		Assert.assertNull(cache.get("image"));
	}

	@Test
	public void keepsItsDataWhenItsSettingsChange() {
		final MemoryResourceCache cache = MemoryResourceCache.create();
		final ResourceData data = new ResourceData("image", null);
		cache.put("image", data);
		cache.size(10).expiry(60000L);
		Assert.assertSame(data, cache.get("image"));
	}

}