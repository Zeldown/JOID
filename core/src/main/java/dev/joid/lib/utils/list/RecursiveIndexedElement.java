package dev.joid.lib.utils.list;

public interface RecursiveIndexedElement extends IndexedElement {

	public IIndexedList<? extends RecursiveIndexedElement> getChildren();

}