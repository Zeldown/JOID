package be.zeldown.joid.lib.utils.list;

public interface RecursiveIndexedElement extends IndexedElement {

	IndexedList<? extends RecursiveIndexedElement> getChildren();

}