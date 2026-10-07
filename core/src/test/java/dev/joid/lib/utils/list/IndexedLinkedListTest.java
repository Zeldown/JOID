package dev.joid.lib.utils.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class IndexedLinkedListTest {

	@Test
	public void startsEmpty() {
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
		Assert.assertTrue(list.isEmpty());
		Assert.assertEquals(0, list.size());
	}

	@Test
	public void sortsItsElementsByIndex() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final Element third = new Element(2);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
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
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
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
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>(Arrays.asList(first, second, third));
		Assert.assertSame(first, list.getFirst());
		Assert.assertSame(second, list.get(1));
		Assert.assertSame(third, list.getLast());
	}

	@Test
	public void knowsWhatItContains() {
		final Element element = new Element(0);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
		list.add(element);
		Assert.assertTrue(list.contains(element));
		Assert.assertFalse(list.contains(new Element(0)));
	}

	@Test
	public void showsItsElementsInReverse() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final Element third = new Element(2);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
		final List<Element> reversed = list.reversed();
		list.add(first);
		list.add(third);
		list.add(second);
		Assert.assertEquals(Arrays.asList(third, second, first), reversed);
	}

	@Test
	public void exposesItsLiveOrder() {
		final Element element = new Element(0);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
		Assert.assertSame(list.ordered(), list.ordered());
		list.add(element);
		Assert.assertSame(element, list.ordered().getFirst());
	}

	@Test
	public void removesAnElement() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
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
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>(Arrays.asList(first, second, third));
		first.index = 5;
		list.sort();
		Assert.assertEquals(Arrays.asList(second, third, first), list.ordered());
	}

	@Test
	public void keepsTheOrderOfEqualIndexesWhenSortingAgain() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final Element third = new Element(2);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>(Arrays.asList(first, second, third));
		first.index = 2;
		third.index = 1;
		list.sort();
		Assert.assertEquals(Arrays.asList(second, third, first), list.ordered());
	}

	@Test
	public void staysInOrderWhenSortedAgain() {
		final Element first = new Element(0);
		final Element second = new Element(0);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>(Arrays.asList(first, second));
		final List<Element> ordered = list.ordered();
		second.index = 3;
		list.sort();
		Assert.assertEquals(Arrays.asList(first, second), ordered);
	}

	@Test
	public void clearsEveryElement() {
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
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
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
		list.add(first);
		final IndexedLinkedList<Element> copy = list.copy();
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
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>(source);
		list.add(second);
		Assert.assertEquals(Arrays.asList(first, second, third), list.ordered());
		Assert.assertEquals(Arrays.asList(first, third), source);
	}

	@Test
	public void iteratesInOrder() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
		list.add(second);
		list.add(first);
		final List<Element> visited = new ArrayList<>();
		for (final Element element : list) {
			visited.add(element);
		}
		Assert.assertEquals(Arrays.asList(first, second), visited);
	}

	@Test
	public void flattensATreeDepthFirst() {
		final Branch leaf = new Branch(0);
		final Branch left = new Branch(0, leaf);
		final Branch right = new Branch(1);
		final Branch root = new Branch(0, left, right);
		final Branch other = new Branch(1);
		final IndexedLinkedList<Branch> list = new IndexedLinkedList<>();
		list.add(other);
		list.add(root);
		final IndexedLinkedList<Branch> flat = list.recursive();
		Assert.assertNotSame(list, flat);
		Assert.assertEquals(Arrays.asList(root, left, leaf, right, other), flat.ordered());
		Assert.assertEquals(Arrays.asList(root, other), list.ordered());
	}

	@Test
	public void staysFlatWithoutRecursiveElements() {
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
		Assert.assertSame(list, list.recursive());
		list.add(new Element(0));
		Assert.assertSame(list, list.recursive());
	}

	@Test
	public void keepsThePlaceOfAnElementAddedAgainWithTheSameIndex() {
		final Element first = new Element(1);
		final Element second = new Element(1);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
		list.add(first);
		list.add(second);
		list.add(first);
		Assert.assertEquals(Arrays.asList(first, second), list.ordered());
	}

	@Test
	public void sortsAnElementAddedAgainWithAnotherIndex() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
		list.add(first);
		list.add(second);
		first.index = 2;
		list.add(first);
		Assert.assertEquals(Arrays.asList(second, first), list.ordered());
	}

	@Test
	public void ignoresAnElementAddedTwice() {
		final Element first = new Element(0);
		final Element second = new Element(1);
		final IndexedLinkedList<Element> list = new IndexedLinkedList<>();
		list.add(first);
		list.add(second);
		list.add(first);
		list.add(second);
		Assert.assertEquals(Arrays.asList(first, second), list.ordered());
	}

	@Getter
	@AllArgsConstructor
	private static final class Element implements IndexedElement {

		private int index;

	}

	@Getter
	private static final class Branch implements RecursiveIndexedElement {

		private final int                       index;
		private final IndexedLinkedList<Branch> children;

		private Branch(final int index, final Branch... children) {
			this.index    = index;
			this.children = new IndexedLinkedList<>(Arrays.asList(children));
		}

	}

}