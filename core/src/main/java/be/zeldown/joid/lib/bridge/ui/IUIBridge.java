package be.zeldown.joid.lib.bridge.ui;

import java.util.List;

import be.zeldown.joid.lib.bridge.IBridge;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.list.IndexedList;
import lombok.NonNull;

public interface IUIBridge extends IBridge {

	public void close(final @NonNull UI ui);
	public void add(final @NonNull UI ui);

	public void open(final @NonNull UI ui);
	@NonNull public IUIBridge getInstance();

	public void remove(final @NonNull UI ui);

	public boolean isOnTop(final @NonNull UI ui);
	public boolean isOpened(final @NonNull UI ui);

	public boolean canHandle(final @NonNull UI ui);

	@NonNull public IndexedList<@NonNull UI> getUiList();
	public boolean canHandle(final @NonNull Class<? extends UI> clazz);
	public void drawHover(final @NonNull UI ui, final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY);

}