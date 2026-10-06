package dev.joid.lib.animation.tweenengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PoolTest {

	@Test
	public void createsAnObjectWhenEmpty() {
		final RecordingCallback callback = new RecordingCallback();
		final Pool<Object> pool = PoolTest.pool(callback);
		final Object object = pool.get();
		Assert.assertNotNull(object);
		Assert.assertEquals(0, pool.size());
		Assert.assertEquals(Collections.singletonList(object), callback.unpooled);
	}

	@Test
	public void reusesTheLastFreedObjectFirst() {
		final Pool<Object> pool = PoolTest.pool(null);
		final Object first = new Object();
		final Object second = new Object();
		pool.free(first);
		pool.free(second);
		Assert.assertEquals(2, pool.size());
		Assert.assertSame(second, pool.get());
		Assert.assertSame(first, pool.get());
		Assert.assertEquals(0, pool.size());
	}

	@Test
	public void notifiesEachObjectPooledAndUnpooled() {
		final RecordingCallback callback = new RecordingCallback();
		final Pool<Object> pool = PoolTest.pool(callback);
		final Object first = new Object();
		final Object second = new Object();
		pool.free(first);
		pool.free(second);
		pool.get();
		Assert.assertEquals(Arrays.asList(first, second), callback.pooled);
		Assert.assertEquals(Collections.singletonList(second), callback.unpooled);
	}

	@Test
	public void keepsAnObjectFreedTwiceOnce() {
		final RecordingCallback callback = new RecordingCallback();
		final Pool<Object> pool = PoolTest.pool(callback);
		final Object object = new Object();
		pool.free(object);
		pool.free(object);
		Assert.assertEquals(1, pool.size());
		Assert.assertEquals(Collections.singletonList(object), callback.pooled);
	}

	@Test
	public void emptiesOnClear() {
		final Pool<Object> pool = PoolTest.pool(null);
		pool.free(new Object());
		pool.free(new Object());
		pool.clear();
		Assert.assertEquals(0, pool.size());
	}

	@Test
	public void reservesRoomWithoutCreatingObjects() {
		final Pool<Object> pool = PoolTest.pool(null);
		pool.ensureCapacity(100);
		Assert.assertEquals(0, pool.size());
	}

	private static Pool<Object> pool(final Pool.Callback<Object> callback) {
		return new Pool<Object>(2, callback) {

			@Override
			protected Object create() {
				return new Object();
			}

		};
	}

	private static final class RecordingCallback implements Pool.Callback<Object> {

		private final List<Object> pooled   = new ArrayList<>();
		private final List<Object> unpooled = new ArrayList<>();

		@Override
		public void onPool(final Object obj) {
			this.pooled.add(obj);
		}

		@Override
		public void onUnPool(final Object obj) {
			this.unpooled.add(obj);
		}

	}

}