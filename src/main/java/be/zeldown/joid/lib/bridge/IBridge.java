package be.zeldown.joid.lib.bridge;

import be.zeldown.joid.lib.utils.list.IndexedElement;

public interface IBridge extends IndexedElement {

	@Override
	public default int getIndex() {
		return 0;
	}

}