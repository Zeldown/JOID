package dev.joid.lib.bridge;

import dev.joid.lib.utils.list.IndexedElement;

public interface IBridge extends IndexedElement {

	@Override
	public default int getIndex() {
		return 0;
	}

}