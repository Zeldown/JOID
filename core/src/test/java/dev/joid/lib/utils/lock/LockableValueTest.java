package dev.joid.lib.utils.lock;

import org.junit.Assert;
import org.junit.Test;

public class LockableValueTest {

	@Test
	public void changesUntilLocked() {
		final Box box = new Box().value(2);
		Assert.assertFalse(box.isLocked());
		Assert.assertSame(box, box.lock());
		Assert.assertTrue(box.isLocked());
		try {
			box.value(3);
			Assert.fail("A locked value must refuse a change");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("This Box is locked, use withValue(...) to get a modified copy", expected.getMessage());
		}
		Assert.assertEquals(2, box.value);
	}

	@Test
	public void derivesAnUnlockedCopy() {
		final Box box = new Box().value(2).lock();
		final Box copy = box.withValue(5);
		Assert.assertNotSame(box, copy);
		Assert.assertFalse(copy.isLocked());
		Assert.assertEquals(5, copy.value);
		Assert.assertEquals(2, box.value);
	}

	private static final class Box extends LockableValue<Box> {

		private int value;

		@Override
		public Box copy() {
			return new Box().value(this.value);
		}

		public Box value(final int value) {
			return super.mutate("withValue", () -> this.value = value);
		}

		public Box withValue(final int value) {
			return super.derive(box -> box.value(value));
		}

	}

}