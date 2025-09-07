package be.zeldown.joid.lib.ui.bridge;

import java.util.List;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.list.IndexedElement;
import be.zeldown.joid.lib.utils.list.IndexedList;
import lombok.NonNull;

public interface IUIBridge extends IndexedElement {

	void open(final @NonNull UI ui);
	void close(final @NonNull UI ui);

	void add(final @NonNull UI ui);
	void remove(final @NonNull UI ui);

	void drawHover(final @NonNull UI ui, final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY);

	boolean isOpened(final @NonNull UI ui);
	boolean isOnTop(final @NonNull UI ui);

	@NonNull IndexedList<@NonNull UI> getUiList();

	@NonNull IUIBridge getInstance();
	boolean canHandle(final @NonNull UI ui);
	boolean canHandle(final @NonNull Class<? extends UI> clazz);

}