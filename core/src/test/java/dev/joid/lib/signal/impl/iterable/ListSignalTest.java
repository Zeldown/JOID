package dev.joid.lib.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.thread.FrameThreadBridge;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.utils.thread.ThreadUtils;

public class ListSignalTest {

	@Test
	public void startsWithAnEmptyListWithoutDefault() {
		final ListSignal<String> signal = new ListSignal<>();
		Assert.assertEquals(Collections.emptyList(), signal.get());
		Assert.assertTrue(signal.isEmpty());
		Assert.assertEquals(0, signal.size());
		Assert.assertFalse(signal.contains("a"));
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
		final ListSignal<String> signal = new ListSignal<>(list);
		Assert.assertEquals(list, signal.get());
		Assert.assertNotSame(list, signal.get());
	}

	@Test
	public void resetsToTheGivenList() {
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Arrays.asList("a")));
		signal.add("b");
		signal.reset();
		Assert.assertEquals(Collections.singletonList("a"), signal.get());
	}

	@Test
	public void readsItsList() {
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Arrays.asList("a", "b")));
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
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Arrays.asList("a")));
		Assert.assertSame(signal, signal.add("b").add("c").remove("a").remove("a"));
		Assert.assertSame(signal, signal.remove(1));
		Assert.assertEquals(Collections.singletonList("b"), signal.get());
	}

	@Test
	public void replacesAnElement() {
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Arrays.asList("a", "b")));
		Assert.assertSame(signal, signal.set(1, "c"));
		Assert.assertEquals(Arrays.asList("a", "c"), signal.get());
	}

	@Test
	public void clearsItsList() {
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Arrays.asList("a", "b")));
		Assert.assertSame(signal, signal.clear());
		Assert.assertTrue(signal.isEmpty());
		Assert.assertEquals(0, signal.size());
	}

	@Test
	public void notifiesEveryMutation() {
		final List<List<String>> received = new ArrayList<>();
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Arrays.asList("a")));
		signal.onChange(value -> received.add(new ArrayList<>(value)));
		signal.add("b");
		signal.set(0, "c");
		signal.remove("b");
		signal.remove(0);
		signal.add("d");
		signal.clear();
		Assert.assertEquals(Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c", "b"), Arrays.asList("c"), Arrays.asList(), Arrays.asList("d"), Arrays.asList()), received);
	}

	@Test
	public void neverExposesItsList() {
		final List<String> list = new ArrayList<>(Collections.singletonList("a"));
		final ListSignal<String> signal = new ListSignal<>(list);
		ListSignalTest.assertReadOnly(signal.get());
		ListSignalTest.assertReadOnly(signal.peek());
		ListSignalTest.assertReadOnly(new ListSignal<String>().get());
		signal.set(list);
		list.add("x");
		Assert.assertEquals(Collections.singletonList("a"), signal.get());
		ListSignalTest.assertReadOnly(signal.get());
		signal.add("b");
		ListSignalTest.assertReadOnly(signal.get());
		Assert.assertEquals(Collections.singletonList("a"), signal.reset().get());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void refusesAnIndexOutOfBounds() {
		new ListSignal<>(new ArrayList<>(Arrays.asList("a"))).get(1);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("ListSignal{[a, b]}", new ListSignal<>(Arrays.asList("a", "b")).toString());
		Assert.assertEquals("ListSignal{[]}", new ListSignal<String>().toString());
	}

	@Test
	public void notifiesTheMutatedListWhenBuiltWithADefault() {
		final List<List<String>> received = new ArrayList<>();
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Collections.singletonList("a")));
		signal.onChange(received::add);
		signal.add("b");
		Assert.assertEquals(Collections.singletonList(Arrays.asList("a", "b")), received);
	}

	@Test
	public void ignoresAMutationThatChangesNothing() {
		final List<List<String>> received = new ArrayList<>();
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Arrays.asList("a")));
		signal.onChange(received::add);
		signal.remove("b").set(0, "a");
		signal.remove(0);
		signal.clear();
		Assert.assertEquals(Collections.singletonList(Collections.emptyList()), received);
	}

	@Test
	public void silencesTheMutationsOfItsScope() {
		final List<List<String>> received = new ArrayList<>();
		final ListSignal<String> signal = new ListSignal<>(new ArrayList<>(Collections.singletonList("a")));
		signal.onChange(received::add);
		signal.<ListSignal<String>>silent(list -> list.add("b").add("c"));
		Assert.assertTrue(received.isEmpty());
		signal.add("d");
		Assert.assertEquals(Collections.singletonList(Arrays.asList("a", "b", "c", "d")), received);
	}


	@Test
	public void notifiesAValueDerivedFromItsListAtEveryMutation() {
		final List<List<String>> received = new ArrayList<>();
		final ListSignal<String> signal = new ListSignal<>(Collections.singletonList("a"));
		final ComputedSignal<List<String>> derived = Signal.from(() -> signal.get()).onChange(received::add);
		signal.add("b");
		signal.add("c");
		signal.remove(0);
		Assert.assertEquals(Arrays.asList("b", "c"), derived.get());
		Assert.assertEquals(Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("a", "b", "c"), Arrays.asList("b", "c")), received);
	}

	@Test
	public void keepsTheListItGaveBeforeAMutation() {
		final ListSignal<String> signal = new ListSignal<>(Collections.singletonList("a"));
		final List<String> before = signal.get();
		signal.add("b");
		signal.set(0, "c");
		Assert.assertEquals(Collections.singletonList("a"), before);
		Assert.assertEquals(Arrays.asList("c", "b"), signal.get());
	}

	@Test
	public void resetsWithoutTheListOfTheCaller() {
		final List<String> list = new ArrayList<>(Collections.singletonList("a"));
		final ListSignal<String> signal = new ListSignal<>(list);
		list.add("x");
		signal.add("b");
		signal.reset();
		Assert.assertEquals(Collections.singletonList("a"), signal.get());
		Assert.assertEquals(Arrays.asList("a", "x"), list);
	}

	@Test
	public void appliesADeferredMutationOnTheCurrentList() throws InterruptedException {
		final FrameThreadBridge thread = FrameThreadBridge.create();
		final ListSignal<String> signal = new ListSignal<>(Collections.singletonList("a"));
		BridgeHandler.THREAD.register(thread);
		try {
			final Thread worker = ThreadUtils.daemonThread(() -> signal.add("b").remove("a"), "worker");
			worker.start();
			worker.join();
			Assert.assertEquals(Collections.singletonList("a"), signal.peek());
			signal.add("c");
			thread.runTasks();
			Assert.assertEquals(Arrays.asList("c", "b"), signal.peek());
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	private static void assertReadOnly(final List<String> list) {
		try {
			list.add("z");
			Assert.fail("The list of a ListSignal is read-only");
		} catch (final UnsupportedOperationException expected) {
			Assert.assertFalse(list.contains("z"));
		}
	}

}