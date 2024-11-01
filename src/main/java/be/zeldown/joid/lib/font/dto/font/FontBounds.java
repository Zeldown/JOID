package be.zeldown.joid.lib.font.dto.font;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FontBounds {

	private final double width;
	private final double height;

	public static @NonNull FontBounds empty() {
		return new FontBounds(0, 0);
	}

}