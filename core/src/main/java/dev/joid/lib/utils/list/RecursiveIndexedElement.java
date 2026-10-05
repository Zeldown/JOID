package dev.joid.lib.utils.list;

public interface RecursiveIndexedElement extends IndexedElement {

	public IndexedList<? extends RecursiveIndexedElement> getChildren();

}