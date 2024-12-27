package be.zeldown.joid.lib.utils.clipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import lombok.NonNull;

public final class ClipboardUtils {

	public static @NonNull String getClipboard() {
		try {
			final Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				return (String) transferable.getTransferData(DataFlavor.stringFlavor);
			}
		} catch (final Exception silent) {}

		return "";
	}

	public static void setClipboard(final @NonNull String text) {
		try {
			final StringSelection stringselection = new StringSelection(text);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
		} catch (final Exception silent) {}
	}

}