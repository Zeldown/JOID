package dev.joid.lib.signal.impl.iterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.Signal;

public class MapSignalTest {

	@Test
	public void startsWithAnEmptyMapWithoutDefault() {
		final MapSignal<String, Integer> signal = MapSignal.create();
		Assert.assertEquals(Collections.emptyMap(), signal.get());
		Assert.assertTrue(signal.isEmpty());
		Assert.assertNull(signal.get("a"));
	}

	@Test
	public void startsWithTheGivenMap() {
		final Map<String, Integer> map = new HashMap<>();
		final MapSignal<String, Integer> signal = MapSignal.create(map);
		Assert.assertEquals(map, signal.get());
		Assert.assertNotSame(map, signal.get());
	}

	@Test
	public void resetsToTheGivenMap() {
		final MapSignal<String, Integer> signal = MapSignal.create(new HashMap<>(Collections.singletonMap("a", 1)));
		signal.put("b", 2);
		signal.reset();
		Assert.assertEquals(Collections.singletonMap("a", 1), signal.get());
	}

	@Test
	public void readsItsMap() {
		final Map<String, Integer> map = new LinkedHashMap<>();
		map.put("a", 1);
		map.put("b", 2);
		final MapSignal<String, Integer> signal = MapSignal.create(map);
		Assert.assertEquals(2, signal.size());
		Assert.assertFalse(signal.isEmpty());
		Assert.assertEquals(Integer.valueOf(2), signal.get("b"));
		Assert.assertNull(signal.get("c"));
		Assert.assertTrue(signal.containsKey("a"));
		Assert.assertFalse(signal.containsKey("c"));
		Assert.assertEquals(new HashSet<>(Arrays.asList("a", "b")), signal.keySet());
		Assert.assertEquals(Arrays.asList(1, 2), new ArrayList<>(signal.values()));
		Assert.assertEquals(map.entrySet(), signal.entrySet());
	}

	@Test
	public void putsAndRemovesEntries() {
		final MapSignal<String, Integer> signal = MapSignal.create(new HashMap<>());
		Assert.assertSame(signal, signal.put("a", 1).put("a", 2));
		Assert.assertEquals(Collections.singletonMap("a", 2), signal.get());
		Assert.assertSame(signal, signal.remove("a").remove("a"));
		Assert.assertTrue(signal.isEmpty());
	}

	@Test
	public void clearsItsMap() {
		final MapSignal<String, Integer> signal = MapSignal.create(new HashMap<>(Collections.singletonMap("a", 1)));
		Assert.assertSame(signal, signal.clear());
		Assert.assertTrue(signal.isEmpty());
		Assert.assertEquals(0, signal.size());
	}

	@Test
	public void notifiesEveryMutation() {
		final List<Map<String, Integer>> received = new ArrayList<>();
		final MapSignal<String, Integer> signal = MapSignal.create(new HashMap<>());
		signal.onChange(value -> received.add(new HashMap<>(value)));
		signal.put("a", 1);
		signal.put("a", 2);
		signal.remove("a");
		signal.put("b", 3);
		signal.clear();
		Assert.assertEquals(Arrays.asList(Collections.singletonMap("a", 1), Collections.singletonMap("a", 2), Collections.emptyMap(), Collections.singletonMap("b", 3), Collections.emptyMap()), received);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("MapSignal{{a=1}}", MapSignal.create(Collections.singletonMap("a", 1)).toString());
		Assert.assertEquals("MapSignal{{}}", MapSignal.<String, Integer>create().toString());
	}

	@Test
	public void notifiesTheMutatedMapWhenBuiltWithADefault() {
		final List<Map<String, Integer>> received = new ArrayList<>();
		final MapSignal<String, Integer> signal = MapSignal.create(new HashMap<>());
		signal.onChange(received::add);
		signal.put("a", 1);
		Assert.assertEquals(Collections.singletonList(Collections.singletonMap("a", 1)), received);
	}

	@Test
	public void ignoresAMutationThatChangesNothing() {
		final List<Map<String, Integer>> received = new ArrayList<>();
		final MapSignal<String, Integer> signal = MapSignal.create(new HashMap<>(Collections.singletonMap("a", 1)));
		signal.onChange(received::add);
		signal.put("a", 1).remove("b");
		signal.remove("a");
		signal.clear();
		Assert.assertEquals(Collections.singletonList(Collections.emptyMap()), received);
	}

	@Test
	public void notifiesANullValuePutUnderANewKey() {
		final List<Map<String, Integer>> received = new ArrayList<>();
		final MapSignal<String, Integer> signal = MapSignal.create(new HashMap<>());
		signal.onChange(value -> received.add(new HashMap<>(value)));
		signal.put("a", null);
		signal.put("a", null);
		Assert.assertEquals(Collections.singletonList(Collections.singletonMap("a", null)), received);
	}


	@Test
	public void notifiesAValueDerivedFromItsMapAtEveryMutation() {
		final List<Map<String, Integer>> received = new ArrayList<>();
		final MapSignal<String, Integer> signal = MapSignal.create();
		final ComputedSignal<Map<String, Integer>> derived = Signal.from(() -> signal.get()).onChange(received::add);
		signal.put("a", 1);
		signal.put("a", 2);
		Assert.assertEquals(Collections.singletonMap("a", 2), derived.get());
		Assert.assertEquals(Arrays.asList(Collections.singletonMap("a", 1), Collections.singletonMap("a", 2)), received);
	}

	@Test
	public void resetsWithoutTheMapOfTheCaller() {
		final Map<String, Integer> map = new HashMap<>(Collections.singletonMap("a", 1));
		final MapSignal<String, Integer> signal = MapSignal.create(map);
		map.put("x", 0);
		signal.put("b", 2);
		signal.reset();
		Assert.assertEquals(Collections.singletonMap("a", 1), signal.get());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesReadOnlyEntries() {
		MapSignal.create(Collections.singletonMap("a", 1)).entrySet().clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesReadOnlyKeys() {
		MapSignal.create(Collections.singletonMap("a", 1)).keySet().remove("a");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesReadOnlyValues() {
		MapSignal.create(Collections.singletonMap("a", 1)).values().clear();
	}

	@Test
	public void neverExposesItsMap() {
		final Map<String, Integer> map = new HashMap<>(Collections.singletonMap("a", 1));
		final MapSignal<String, Integer> signal = MapSignal.create(map);
		MapSignalTest.assertReadOnly(signal.get());
		MapSignalTest.assertReadOnly(signal.peek());
		MapSignalTest.assertReadOnly(MapSignal.<String, Integer>create().get());
		signal.set(map);
		map.put("x", 0);
		Assert.assertEquals(Collections.singletonMap("a", 1), signal.get());
		signal.put("b", 2);
		MapSignalTest.assertReadOnly(signal.get());
		Assert.assertEquals(Collections.singletonMap("a", 1), signal.reset().get());
	}

	private static void assertReadOnly(final Map<String, Integer> map) {
		try {
			map.put("z", 0);
			Assert.fail("The map of a MapSignal is read-only");
		} catch (final UnsupportedOperationException expected) {
			Assert.assertFalse(map.containsKey("z"));
		}
	}
}