package dev.joid.lib.utils.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class SetSignalTest {

	@Test
	public void startsEmptyWithoutDefault() {
		final SetSignal<String> signal = new SetSignal<>();
		Assert.assertFalse(signal.isPresent());
		Assert.assertNull(signal.get());
	}

	@Test
	public void fallsBackOnTheGivenSet() {
		final Set<String> set = new HashSet<>(Arrays.asList("a", "b"));
		final SetSignal<String> signal = new SetSignal<>(set);
		Assert.assertFalse(signal.isPresent());
		Assert.assertSame(set, signal.get());
	}

	@Test
	public void copiesAGivenCollectionWithoutDuplicates() {
		final List<String> source = new ArrayList<>(Arrays.asList("a", "b", "a"));
		final SetSignal<String> signal = new SetSignal<>(source);
		Assert.assertEquals(new HashSet<>(Arrays.asList("a", "b")), signal.get());
		signal.add("c");
		Assert.assertEquals(Arrays.asList("a", "b", "a"), source);
	}

	@Test
	public void startsWithTheGivenSet() {
		final Set<String> set = new HashSet<>(Arrays.asList("a", "b"));
		final SetSignal<String> signal = SetSignal.of(set);
		Assert.assertTrue(signal.isPresent());
		Assert.assertSame(set, signal.get());
	}

	@Test
	public void readsItsSet() {
		final SetSignal<String> signal = SetSignal.of(new HashSet<>(Arrays.asList("a", "b")));
		Assert.assertEquals(2, signal.size());
		Assert.assertFalse(signal.isEmpty());
		Assert.assertTrue(signal.contains("a"));
		Assert.assertFalse(signal.contains("c"));
	}

	@Test
	public void addsAndRemovesElements() {
		final SetSignal<String> signal = SetSignal.of(new HashSet<>());
		Assert.assertTrue(signal.add("a"));
		Assert.assertFalse(signal.add("a"));
		Assert.assertTrue(signal.remove("a"));
		Assert.assertFalse(signal.remove("a"));
		Assert.assertTrue(signal.isEmpty());
	}

	@Test
	public void clearsItsSet() {
		final SetSignal<String> signal = SetSignal.of(new HashSet<>(Arrays.asList("a", "b")));
		Assert.assertSame(signal, signal.clear());
		Assert.assertTrue(signal.isEmpty());
		Assert.assertEquals(0, signal.size());
	}

	@Test
	public void notifiesEveryMutation() {
		final List<Set<String>> received = new ArrayList<>();
		final SetSignal<String> signal = SetSignal.of(new HashSet<>());
		signal.subscribe(value -> received.add(new HashSet<>(value)));
		signal.add("a");
		signal.add("b");
		signal.remove("a");
		signal.clear();
		Assert.assertEquals(Arrays.asList(Collections.singleton("a"), new HashSet<>(Arrays.asList("a", "b")), Collections.singleton("b"), Collections.emptySet()), received);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("SetSignal{[a]}", SetSignal.of(Collections.singleton("a")).toString());
		Assert.assertEquals("SetSignal{null}", new SetSignal<String>().toString());
	}

	@Test
	public void notifiesTheMutatedSetWhenBuiltWithADefault() {
		final List<Set<String>> received = new ArrayList<>();
		final SetSignal<String> signal = new SetSignal<>(new HashSet<>());
		signal.subscribe(received::add);
		signal.add("a");
		Assert.assertEquals(Collections.singletonList(Collections.singleton("a")), received);
	}

}