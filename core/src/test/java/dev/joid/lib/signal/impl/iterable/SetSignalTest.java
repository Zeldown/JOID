package dev.joid.lib.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.Signal;

public class SetSignalTest {

	@Test
	public void startsWithAnEmptySetWithoutDefault() {
		final SetSignal<String> signal = SetSignal.create();
		Assert.assertEquals(Collections.emptySet(), signal.get());
		Assert.assertTrue(signal.isEmpty());
		Assert.assertFalse(signal.contains("a"));
	}

	@Test
	public void copiesAGivenCollectionWithoutDuplicates() {
		final List<String> source = new ArrayList<>(Arrays.asList("a", "b", "a"));
		final SetSignal<String> signal = SetSignal.create(source);
		Assert.assertEquals(new HashSet<>(Arrays.asList("a", "b")), signal.get());
		signal.add("c");
		Assert.assertEquals(Arrays.asList("a", "b", "a"), source);
	}

	@Test
	public void startsWithTheGivenSet() {
		final Set<String> set = new HashSet<>(Arrays.asList("a", "b"));
		final SetSignal<String> signal = SetSignal.create(set);
		Assert.assertEquals(set, signal.get());
		Assert.assertNotSame(set, signal.get());
	}

	@Test
	public void resetsToTheGivenSet() {
		final SetSignal<String> signal = SetSignal.create(new HashSet<>(Arrays.asList("a")));
		signal.add("b");
		signal.reset();
		Assert.assertEquals(Collections.singleton("a"), signal.get());
	}

	@Test
	public void readsItsSet() {
		final SetSignal<String> signal = SetSignal.create(new HashSet<>(Arrays.asList("a", "b")));
		Assert.assertEquals(2, signal.size());
		Assert.assertFalse(signal.isEmpty());
		Assert.assertTrue(signal.contains("a"));
		Assert.assertFalse(signal.contains("c"));
	}

	@Test
	public void addsAndRemovesElements() {
		final SetSignal<String> signal = SetSignal.create(new HashSet<>());
		Assert.assertSame(signal, signal.add("a").add("a"));
		Assert.assertEquals(Collections.singleton("a"), signal.get());
		Assert.assertSame(signal, signal.remove("a").remove("a"));
		Assert.assertTrue(signal.isEmpty());
	}

	@Test
	public void clearsItsSet() {
		final SetSignal<String> signal = SetSignal.create(new HashSet<>(Arrays.asList("a", "b")));
		Assert.assertSame(signal, signal.clear());
		Assert.assertTrue(signal.isEmpty());
		Assert.assertEquals(0, signal.size());
	}

	@Test
	public void notifiesEveryMutation() {
		final List<Set<String>> received = new ArrayList<>();
		final SetSignal<String> signal = SetSignal.create(new HashSet<>());
		signal.onChange(value -> received.add(new HashSet<>(value)));
		signal.add("a");
		signal.add("b");
		signal.remove("a");
		signal.clear();
		Assert.assertEquals(Arrays.asList(Collections.singleton("a"), new HashSet<>(Arrays.asList("a", "b")), Collections.singleton("b"), Collections.emptySet()), received);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("SetSignal{[a]}", SetSignal.create(Collections.singleton("a")).toString());
		Assert.assertEquals("SetSignal{[]}", SetSignal.<String>create().toString());
	}

	@Test
	public void notifiesTheMutatedSetWhenBuiltWithADefault() {
		final List<Set<String>> received = new ArrayList<>();
		final SetSignal<String> signal = SetSignal.create(new HashSet<>());
		signal.onChange(received::add);
		signal.add("a");
		Assert.assertEquals(Collections.singletonList(Collections.singleton("a")), received);
	}

	@Test
	public void ignoresAMutationThatChangesNothing() {
		final List<Set<String>> received = new ArrayList<>();
		final SetSignal<String> signal = SetSignal.create(new HashSet<>(Collections.singleton("a")));
		signal.onChange(received::add);
		signal.add("a").remove("b");
		signal.remove("a");
		signal.clear();
		Assert.assertEquals(Collections.singletonList(Collections.emptySet()), received);
	}


	@Test
	public void notifiesAValueDerivedFromItsSetAtEveryMutation() {
		final List<Set<String>> received = new ArrayList<>();
		final SetSignal<String> signal = SetSignal.create(Collections.singleton("a"));
		final ComputedSignal<Set<String>> derived = Signal.from(() -> signal.get()).onChange(received::add);
		signal.add("b");
		signal.add("c");
		Assert.assertEquals(new HashSet<>(Arrays.asList("a", "b", "c")), derived.get());
		Assert.assertEquals(Arrays.asList(new HashSet<>(Arrays.asList("a", "b")), new HashSet<>(Arrays.asList("a", "b", "c"))), received);
	}

	@Test
	public void resetsWithoutTheSetOfTheCaller() {
		final Set<String> set = new HashSet<>(Collections.singleton("a"));
		final SetSignal<String> signal = SetSignal.create(set);
		set.add("x");
		signal.add("b");
		signal.reset();
		Assert.assertEquals(Collections.singleton("a"), signal.get());
	}

	@Test
	public void neverExposesItsSet() {
		final Set<String> set = new HashSet<>(Collections.singleton("a"));
		final SetSignal<String> signal = SetSignal.create(set);
		SetSignalTest.assertReadOnly(signal.get());
		SetSignalTest.assertReadOnly(signal.peek());
		SetSignalTest.assertReadOnly(SetSignal.<String>create().get());
		signal.set(set);
		set.add("x");
		Assert.assertEquals(Collections.singleton("a"), signal.get());
		signal.add("b");
		SetSignalTest.assertReadOnly(signal.get());
		Assert.assertEquals(Collections.singleton("a"), signal.reset().get());
	}

	private static void assertReadOnly(final Set<String> set) {
		try {
			set.add("z");
			Assert.fail("The set of a SetSignal is read-only");
		} catch (final UnsupportedOperationException expected) {
			Assert.assertFalse(set.contains("z"));
		}
	}
}