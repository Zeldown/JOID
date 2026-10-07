package dev.joid.lib.utils.signal.impl.iterable;

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

public class MapSignalTest {

	@Test
	public void startsEmptyWithoutDefault() {
		final MapSignal<String, Integer> signal = new MapSignal<>();
		Assert.assertFalse(signal.isPresent());
		Assert.assertNull(signal.get());
	}

	@Test
	public void fallsBackOnTheGivenMap() {
		final Map<String, Integer> map = new HashMap<>();
		final MapSignal<String, Integer> signal = new MapSignal<>(map);
		Assert.assertFalse(signal.isPresent());
		Assert.assertSame(map, signal.get());
	}

	@Test
	public void startsWithTheGivenMap() {
		final Map<String, Integer> map = new HashMap<>();
		final MapSignal<String, Integer> signal = MapSignal.of(map);
		Assert.assertTrue(signal.isPresent());
		Assert.assertSame(map, signal.get());
	}

	@Test
	public void readsItsMap() {
		final Map<String, Integer> map = new LinkedHashMap<>();
		map.put("a", 1);
		map.put("b", 2);
		final MapSignal<String, Integer> signal = MapSignal.of(map);
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
		final MapSignal<String, Integer> signal = MapSignal.of(new HashMap<>());
		Assert.assertNull(signal.put("a", 1));
		Assert.assertEquals(Integer.valueOf(1), signal.put("a", 2));
		Assert.assertEquals(Integer.valueOf(2), signal.remove("a"));
		Assert.assertNull(signal.remove("a"));
		Assert.assertTrue(signal.isEmpty());
	}

	@Test
	public void clearsItsMap() {
		final MapSignal<String, Integer> signal = MapSignal.of(new HashMap<>(Collections.singletonMap("a", 1)));
		Assert.assertSame(signal, signal.clear());
		Assert.assertTrue(signal.isEmpty());
		Assert.assertEquals(0, signal.size());
	}

	@Test
	public void notifiesEveryMutation() {
		final List<Map<String, Integer>> received = new ArrayList<>();
		final MapSignal<String, Integer> signal = MapSignal.of(new HashMap<>());
		signal.subscribe(value -> received.add(new HashMap<>(value)));
		signal.put("a", 1);
		signal.put("a", 2);
		signal.remove("a");
		signal.put("b", 3);
		signal.clear();
		Assert.assertEquals(Arrays.asList(Collections.singletonMap("a", 1), Collections.singletonMap("a", 2), Collections.emptyMap(), Collections.singletonMap("b", 3), Collections.emptyMap()), received);
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("MapSignal{{a=1}}", MapSignal.of(Collections.singletonMap("a", 1)).toString());
		Assert.assertEquals("MapSignal{null}", new MapSignal<String, Integer>().toString());
	}

	@Test
	public void notifiesTheMutatedMapWhenBuiltWithADefault() {
		final List<Map<String, Integer>> received = new ArrayList<>();
		final MapSignal<String, Integer> signal = new MapSignal<>(new HashMap<>());
		signal.subscribe(received::add);
		signal.put("a", 1);
		Assert.assertEquals(Collections.singletonList(Collections.singletonMap("a", 1)), received);
	}

	@Test
	public void ignoresAMutationThatChangesNothing() {
		final List<Map<String, Integer>> received = new ArrayList<>();
		final MapSignal<String, Integer> signal = MapSignal.of(new HashMap<>(Collections.singletonMap("a", 1)));
		signal.subscribe(received::add);
		Assert.assertEquals(Integer.valueOf(1), signal.put("a", 1));
		Assert.assertNull(signal.remove("b"));
		signal.remove("a");
		signal.clear();
		Assert.assertEquals(Collections.singletonList(Collections.emptyMap()), received);
	}

	@Test
	public void notifiesANullValuePutUnderANewKey() {
		final List<Map<String, Integer>> received = new ArrayList<>();
		final MapSignal<String, Integer> signal = MapSignal.of(new HashMap<>());
		signal.subscribe(value -> received.add(new HashMap<>(value)));
		signal.put("a", null);
		signal.put("a", null);
		Assert.assertEquals(Collections.singletonList(Collections.singletonMap("a", null)), received);
	}

}