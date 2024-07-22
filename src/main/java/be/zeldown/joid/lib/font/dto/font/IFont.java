package be.zeldown.joid.lib.font.dto.font;

import be.zeldown.joid.lib.font.FontProvider;
import lombok.NonNull;

public interface IFont {

	@NonNull FontProvider getFontProvider();

}