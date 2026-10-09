package dev.joid.lib.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class ListSignalTest {

	@Test
	public void startsEmptyWithoutDefault() {
		final ListSignal<String> signal = new ListSignal<>();
		Assert.assertFalse(signal.isPresent());
		Assert.assertNull(signal.get());
	}

	@Test
	public void fallsBackOnTheGivenList() {
		final List<String> list = new ArrayList<>(Arrays.asList("a", "b"));
		final ListSignal<String> signal = new ListSignal<>(list);
		Assert.assertFalse(signal.isPresent());
		Assert.assertSame(list, signal.get());
	}

	@Test
	public void copiesAGivenCollection() {
		final Set<String> source = new LinkedHashSet<>(Arrays.asList("a", "b"));
		final ListSignal<String> signal = new ListSignal<>(source);
		Assert.assertEquals(Arrays.asList("a", "b"), signal.get());
		signal.add("c");
		Assert.assertEquals(new LinkedHashSet<>(Arrays.asList("a", "b")), source);
	}

	@Test
	public void startsWithTheGivenList() {
		final List<String> list = new ArrayList<>(Arrays.asList("a", "b"));
		final ListSignal<String> signal = ListSignal.of(list);
		Assert.assertTrue(signal.isPresent());
		Assert.assertSame(list, signal.get());
	}

	@Test
	public void readsItsList() {
		final ListSignal<String> signal = ListSignal.of(new ArrayList<>(Arrays.asList("a", "b")));
		Assert.assertEquals(2, signal.size());
		Assert.assertFalse(signal.isEmpty());
		Assert.assertEquals("b", signal.get(1));
		Assert.assertEquals(1, signal.indexOf("b"));
		Assert.assertEquals(-1, signal.indexOf("c"));
		Assert.assertTrue(signal.contains("a"));
		Assert.assertFalse(signal.contains("c"));
	}

	@Test
	public void addsAndRemovesElements() {
		final ListSignal<String> signal = ListSignal.of(new ArrayList<>(Arrays.asList("a")));
		Assert.assertTrue(signal.add("b"));
		Assert.assertTrue(signal.add("c"));
		Assert.assertTrue(signal.remove("a"));
		Assert.assertFalse(signal.remove("a"));
		Assert.assertEquals("c", signal.remove(1));
		Assert.assertEquals(Collections.singletonList("b"), signal.get());
	}

	@Test
	public void replacesAnElement() {
		final ListSignal<String> signal = ListSignal.of(new ArrayList<>(Arrays.asList("a", "b")));
		Assert.assertEquals("b", signal.set(1, "c"));
		Assert.assertEquals(Arrays.asList("a", "c"), signal.get());
	}

	@Test
	public void clearsItsList() {
		final ListSignal<String> signal = ListSignal.of(new ArrayList<>(Arrays.asList("a", "b")));
		Assert.assertSame(signal, signal.clear());
		Assert.assertTrue(signal.isEmpty());
		Assert.assertEquals(0, signal.size());
	}

	@Test
	public void notifiesEveryMutation() {
		final List<List<String>> received = new ArrayList<>();
		final ListSignal<String> signal = ListSignal.of(new ArrayList<>(Arrays.asList("a")));
		signal.subscribe(value -> received.add(new ArrayList<>(value)));
		signal.add("b");
		signal.set(0, "c");
		signal.remove("b");
		signal.remove(0);
		signal.add("d");
		signal.clear();
		Assert.assertEquals(Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "b"), Arrays.asList("c"), Arrays.asList(), Arrays.asList("d"), Arrays.asList()), received);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void refusesAnIndexOutOfBounds() {
		ListSignal.of(new ArrayList<>(Arrays.asList("a"))).get(1);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("ListSignal{[a, b]}", ListSignal.of(Arrays.asList("a", "b")).toString());
		Assert.assertEquals("ListSignal{null}", new ListSignal<String>().toString());
	}

	@Test
	public void notifiesTheMutatedListWhenBuiltWithADefault() {
		final List<List<String>> received = new ArrayList<>();
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Collections.singletonList("a")));
		signal.subscribe(received::add);
		signal.add("b");
		Assert.assertEquals(Collections.singletonList(Arrays.asList("a", "b")), received);
	}

	@Test
	public void ignoresAMutationThatChangesNothing() {
		final List<List<String>> received = new ArrayList<>();
		final ListSignal<String> signal = ListSignal.of(new ArrayList<>(Arrays.asList("a")));
		signal.subscribe(received::add);
		Assert.assertFalse(signal.remove("b"));
		Assert.assertEquals("a", signal.set(0, "a"));
		signal.remove(0);
		signal.clear();
		Assert.assertEquals(Collections.singletonList(Collections.emptyList()), received);
	}

	@Test
	public void keepsItsSilenceForTheFirstMutationOfItsDefault() {
		final List<List<String>> received = new ArrayList<>();
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Collections.singletonList("a")));
		signal.subscribe(received::add);
		signal.silent();
		signal.add("b");
		Assert.assertTrue(received.isEmpty());
		signal.add("c");
		Assert.assertEquals(Collections.singletonList(Arrays.asList("a", "b", "c")), received);
	}

	@Test
	public void disarmsItsSilenceOnAMutationThatChangesNothing() {
		final List<List<String>> received = new ArrayList<>();
		final ListSignal<String> signal = ListSignal.of(new ArrayList<>(Arrays.asList("a")));
		signal.subscribe(received::add);
		signal.silent();
		signal.remove("b");
		signal.add("c");
		Assert.assertEquals(Collections.singletonList(Arrays.asList("a", "c")), received);
	}

}