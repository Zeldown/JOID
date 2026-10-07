package dev.joid.lib.utils.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class IndexedConcurrentListTest {

	@Test
	public void startsEmpty() {
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		Assert.assertTrue(list.isEmpty());
		Assert.assertEquals(0, list.size());
	}

	@Test
	public void sortsItsElementsByIndex() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final Element third = new Element(2);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		list.add(third);
		list.add(first);
		list.add(second);
		Assert.assertEquals(Arrays.asList(first, second, third), list.ordered());
		Assert.assertEquals(3, list.size());
		Assert.assertFalse(list.isEmpty());
	}

	@Test
	public void keepsTheInsertionOrderOfEqualIndexes() {
		final Element first = new Element(1);
		final Element second = new Element(1);
		final Element lower = new Element(0);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		list.add(first);
		list.add(second);
		list.add(lower);
		Assert.assertEquals(Arrays.asList(lower, first, second), list.ordered());
	}

	@Test
	public void readsItsElementsByPosition() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final Element third = new Element(2);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>(Arrays.asList(first, second, third));
		Assert.assertSame(first, list.getFirst());
		Assert.assertSame(second, list.get(1));
		Assert.assertSame(third, list.getLast());
	}

	@Test
	public void hasNoLastElementWhileEmpty() {
		Assert.assertNull(new IndexedConcurrentList<Element>().getLast());
	}

	@Test
	public void knowsWhatItContains() {
		final Element element = new Element(0);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		list.add(element);
		Assert.assertTrue(list.contains(element));
		Assert.assertFalse(list.contains(new Element(0)));
	}

	@Test
	public void showsItsElementsInReverse() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final Element third = new Element(2);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		final List<Element> reversed = list.reversed();
		list.add(first);
		list.add(third);
		list.add(second);
		Assert.assertEquals(Arrays.asList(third, second, first), reversed);
	}

	@Test
	public void exposesItsLiveOrder() {
		final Element element = new Element(0);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		Assert.assertSame(list.ordered(), list.ordered());
		list.add(element);
		Assert.assertSame(element, list.ordered().get(0));
	}

	@Test
	public void removesAnElement() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		list.add(first);
		list.add(second);
		list.remove(first);
		list.remove(new Element(1));
		Assert.assertEquals(Collections.singletonList(second), list.ordered());
		Assert.assertFalse(list.contains(first));
		Assert.assertEquals(1, list.size());
	}

	@Test
	public void sortsItsElementsAgainOnceAnIndexChanges() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final Element third = new Element(2);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>(Arrays.asList(first, second, third));
		first.index = 5;
		list.sort();
		Assert.assertEquals(Arrays.asList(second, third, first), list.ordered());
	}

	@Test
	public void keepsTheOrderOfEqualIndexesWhenSortingAgain() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final Element third = new Element(2);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>(Arrays.asList(first, second, third));
		first.index = 2;
		third.index = 1;
		list.sort();
		Assert.assertEquals(Arrays.asList(second, third, first), list.ordered());
	}

	@Test
	public void staysInOrderWhenSortedAgain() {
		final Element first = new Element(0);
		final Element second = new Element(0);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>(Arrays.asList(first, second));
		final List<Element> ordered = list.ordered();
		second.index = 3;
		list.sort();
		Assert.assertEquals(Arrays.asList(first, second), ordered);
	}

	@Test
	public void clearsEveryElement() {
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		list.add(new Element(0));
		list.add(new Element(1));
		list.clear();
		Assert.assertTrue(list.isEmpty());
		Assert.assertTrue(list.reversed().isEmpty());
	}

	@Test
	public void copiesItsElementsIntoAnIndependentList() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		list.add(first);
		final IndexedConcurrentList<Element> copy = list.copy();
		copy.add(second);
		Assert.assertNotSame(list, copy);
		Assert.assertEquals(Collections.singletonList(first), list.ordered());
		Assert.assertEquals(Arrays.asList(first, second), copy.ordered());
	}

	@Test
	public void startsFromAnExistingList() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final Element third = new Element(2);
		final List<Element> source = new ArrayList<>(Arrays.asList(first, third));
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>(source);
		list.add(second);
		Assert.assertEquals(Arrays.asList(first, second, third), list.ordered());
		Assert.assertEquals(Arrays.asList(first, third), source);
	}

	@Test
	public void iteratesInOrder() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		list.add(second);
		list.add(first);
		final List<Element> visited = new ArrayList<>();
		for (final Element element : list) {
			visited.add(element);
		}
		Assert.assertEquals(Arrays.asList(first, second), visited);
	}

	@Test
	public void iteratesOverASnapshotWhileChanging() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final Element third = new Element(2);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		list.add(first);
		list.add(second);
		final List<Element> visited = new ArrayList<>();
		for (final Element element : list) {
			visited.add(element);
			list.remove(second);
			list.add(third);
		}
		Assert.assertEquals(Arrays.asList(first, second), visited);
		Assert.assertEquals(Arrays.asList(first, third), list.ordered());
	}

	@Test
	public void flattensATreeDepthFirst() {
		final Branch leaf = new Branch(0);
		final Branch left = new Branch(0, leaf);
		final Branch right = new Branch(1);
		final Branch root = new Branch(0, left, right);
		final Branch other = new Branch(1);
		final IndexedConcurrentList<Branch> list = new IndexedConcurrentList<>();
		list.add(other);
		list.add(root);
		final IndexedConcurrentList<Branch> flat = list.recursive();
		Assert.assertNotSame(list, flat);
		Assert.assertEquals(Arrays.asList(root, left, leaf, right, other), flat.ordered());
		Assert.assertEquals(Arrays.asList(root, other), list.ordered());
	}

	@Test
	public void staysFlatWithoutRecursiveElements() {
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		Assert.assertSame(list, list.recursive());
		list.add(new Element(0));
		Assert.assertSame(list, list.recursive());
	}

	@Test
	public void ignoresAnElementAddedTwice() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		list.add(first);
		list.add(second);
		list.add(first);
		list.add(second);
		Assert.assertEquals(Arrays.asList(first, second), list.ordered());
	}

	@Test
	public void removesAnElementAddedTwice() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final IndexedConcurrentList<Element> list = new IndexedConcurrentList<>();
		list.add(first);
		list.add(second);
		list.add(first);
		list.remove(first);
		Assert.assertFalse(list.contains(first));
	}

	@Test
	public void hasNoFirstElementWhileEmpty() {
		Assert.assertNull(new IndexedConcurrentList<Element>().getFirst());
	}

	@Getter
	@AllArgsConstructor
	private static final class Element implements IndexedElement {

		private int index;

	}

	@Getter
	private static final class Branch implements RecursiveIndexedElement {

		private final int                           index;
		private final IndexedConcurrentList<Branch> children;

		private Branch(final int index, final Branch... children) {
			this.index    = index;
			this.children = new IndexedConcurrentList<>(Arrays.asList(children));
		}

	}

}