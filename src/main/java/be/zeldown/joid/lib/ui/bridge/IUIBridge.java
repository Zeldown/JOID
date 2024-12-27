package be.zeldown.joid.lib.ui.bridge;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.list.IndexedLinkedList;
import lombok.NonNull;

public interface IUIBridge {

	void open(final @NonNull UI ui);
	void close(final @NonNull UI ui);

	void add(final @NonNull UI ui);
	void remove(final @NonNull UI ui);

	boolean isOpened(final @NonNull UI ui);
	boolean isOnTop(final @NonNull UI ui);

	@NonNull IndexedLinkedList<@NonNull UI> getUiList();

	boolean canHandle(final @NonNull Class<? extends UI> ui);

}