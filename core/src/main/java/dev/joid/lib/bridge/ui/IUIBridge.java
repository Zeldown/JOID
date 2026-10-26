package dev.joid.lib.bridge.ui;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.bridge.IBridge;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

public interface IUIBridge extends IBridge {

	public void open(final @NonNull UI ui);
	public void close(final @NonNull UI ui);

	public void add(final @NonNull UI ui);
	public void remove(final @NonNull UI ui);

	public boolean isOpen(final @NonNull UI ui);
	public boolean isOnTop(final @NonNull UI ui);

	public default boolean canHandle(final @NonNull UI ui) {
		return this.canHandle(ui.getClass());
	}

	public boolean canHandle(final @NonNull Class<? extends UI> clazz);

	@NonNull public List<@NonNull UI> getUiList();

	public void drawHover(final @NonNull UI ui, final @NonNull Object content, final double mouseX, final double mouseY);

	public default void closeAll() {
		for (final UI ui : new ArrayList<>(this.getUiList())) {
			ui.dispose();
			this.remove(ui);
		}
	}

	public default void forceOpen(final @NonNull UI ui) {
		this.closeAll();
		this.open(ui);
	}

	public default void replace(final @NonNull UI ui, final @NonNull UI replacement) {
		ui.dispose();
		this.remove(ui);
		this.add(replacement);
	}

	public default double getInterfaceScale(final @NonNull UI ui) {
		return 1D;
	}

}