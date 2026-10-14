package dev.joid.lib.ui.node.impl.design.resource;

import java.util.function.Supplier;

import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.box.BoundingBox;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ResourceNode extends Node {

	private Resource resource;
	private Resource hoveredResource;

	private Resource filteredResource;
	private Resource filteredHoveredResource;

	private Color color = Color.WHITE;
	private Color hoveredColor;

	private Boolean linear;

	private StretchType stretchType = StretchType.STRETCH;

	protected ResourceNode(final double x, final double y) {
		this(x, y, 0, 0);
	}

	protected ResourceNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.<ResourceNode>wait(node -> node.isReady());
	}

	public static @NonNull ResourceNode create(final double x, final double y) {
		return new ResourceNode(x, y);
	}

	public static @NonNull ResourceNode create(final double x, final double y, final double width, final double height) {
		return new ResourceNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.resource == null) {
			return;
		}

		this.resource.prepareBind();
		if (this.hoveredResource != null) {
			this.hoveredResource.prepareBind();
		}

		if (this.resource.isFailed()) {
			Color.WHITE.bind();
			DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.resource);
			return;
		}

		final double resourceWidth = this.resource.getWidth();
		final double resourceHeight = this.resource.getHeight();
		if (resourceWidth == 0 || resourceHeight == 0) {
			return;
		}

		if (super.getWidth() == 0 && super.getHeight() == 0) {
			super.width(resourceWidth);
			super.height(resourceHeight);
			return;
		}

		if (super.getWidth() == 0) {
			super.width(resourceWidth * super.getHeight() / resourceHeight);
			return;
		}

		if (super.getHeight() == 0) {
			super.height(resourceHeight * super.getWidth() / resourceWidth);
			return;
		}

		final BoundingBox canvas = BoundingBox.create(super.getX(), super.getY(), super.getWidth(), super.getHeight());
		final Color primary = this.hoveredColor != null && this.hoveredResource == null ? this.color.to(this.hoveredColor, super.hoverValue(1F)) : this.color;
		primary.bind(() -> this.drawResource(this.filteredResource), canvas, true);

		if (this.hoveredResource != null && this.hoveredResource != this.resource) {
			final Color secondary = (this.hoveredColor != null ? this.hoveredColor : this.color).copyAlpha(super.hoverValue(1F));
			secondary.bind(() -> this.drawResource(this.filteredHoveredResource), canvas, true);
		}
	}

	public final <T extends ResourceNode> @NonNull T resource(final @NonNull Resource resource) {
		return this.resource(Signal.from(resource));
	}

	public final <T extends ResourceNode> @NonNull T resource(final @NonNull Supplier<@NonNull Resource> resource) {
		return super.follow("resource", resource, (final ResourceNode node, final Resource value) -> {
			node.resource = value;
			node.filteredResource = node.filter(value);
		});
	}

	public final <T extends ResourceNode> @NonNull T hoveredResource(final Resource hoveredResource) {
		return this.hoveredResource(Signal.from(hoveredResource));
	}

	public final <T extends ResourceNode> @NonNull T hoveredResource(final @NonNull Supplier<Resource> hoveredResource) {
		return super.follow("hoveredResource", hoveredResource, (final ResourceNode node, final Resource value) -> {
			node.hoveredResource = value;
			node.filteredHoveredResource = node.filter(value);
		});
	}

	public final <T extends ResourceNode> @NonNull T color(final @NonNull Color color) {
		return this.color(Signal.from(color));
	}

	public final <T extends ResourceNode> @NonNull T color(final @NonNull Supplier<@NonNull Color> color) {
		return super.follow("color", color, (final ResourceNode node, final Color value) -> node.color = value);
	}

	public final <T extends ResourceNode> @NonNull T hoveredColor(final Color hoveredColor) {
		return this.hoveredColor(Signal.from(hoveredColor));
	}

	public final <T extends ResourceNode> @NonNull T hoveredColor(final @NonNull Supplier<Color> hoveredColor) {
		return super.follow("hoveredColor", hoveredColor, (final ResourceNode node, final Color value) -> node.hoveredColor = value);
	}

	public final <T extends ResourceNode> @NonNull T linear(final boolean linear) {
		return this.linear(Signal.from(linear));
	}

	public final <T extends ResourceNode> @NonNull T linear(final @NonNull Supplier<Boolean> linear) {
		return super.follow("linear", linear, (final ResourceNode node, final Boolean value) -> {
			node.linear = value;
			node.filteredResource = node.filter(node.resource);
			node.filteredHoveredResource = node.filter(node.hoveredResource);
		});
	}

	public final <T extends ResourceNode> @NonNull T stretch(final @NonNull StretchType stretchType) {
		return this.stretch(Signal.from(stretchType));
	}

	public final <T extends ResourceNode> @NonNull T stretch(final @NonNull Supplier<@NonNull StretchType> stretchType) {
		return super.follow("stretchType", stretchType, (final ResourceNode node, final StretchType value) -> node.stretchType = value);
	}

	private void drawResource(final @NonNull Resource resource) {
		this.stretchType.draw(super.getX(), super.getY(), super.getWidth(), super.getHeight(), resource);
	}

	private boolean isReady() {
		if (this.hoveredResource != null) {
			this.hoveredResource.prepareBind();
		}

		if (this.resource == null) {
			return false;
		}

		this.resource.prepareBind();
		if (this.resource.isFailed()) {
			return true;
		}

		if (this.resource.isLoaded() && (this.resource.getWidth() == 0 || this.resource.getHeight() == 0)) {
			DrawUtils.RESOURCE.drawResource(0, 0, 0, 0, this.resource);
		}
		return this.resource.isLoaded() && this.resource.getWidth() != 0 && this.resource.getHeight() != 0;
	}

	private Resource filter(final Resource resource) {
		if (resource == null || this.linear == null) {
			return resource;
		}
		return resource.copy().interpolation(this.linear ? TextureFilter.LINEAR : TextureFilter.NEAREST);
	}

	public static enum StretchType {

		STRETCH,
		CONTAIN,
		COVER;

		public void draw(final double x, final double y, final double width, final double height, final @NonNull Resource resource) {
			if (this == StretchType.STRETCH) {
				DrawUtils.RESOURCE.drawResource(x, y, width, height, resource);
				return;
			}

			final double scaleX = width / resource.getWidth();
			final double scaleY = height / resource.getHeight();
			if (this == StretchType.CONTAIN) {
				final double scale = Math.min(scaleX, scaleY);
				final double scaledWidth = resource.getWidth() * scale;
				final double scaledHeight = resource.getHeight() * scale;
				DrawUtils.RESOURCE.drawResource(x + (width - scaledWidth) / 2D, y + (height - scaledHeight) / 2D, scaledWidth, scaledHeight, resource);
				return;
			}

			final double scale = Math.max(scaleX, scaleY);
			final double regionWidth = width / scale;
			final double regionHeight = height / scale;
			DrawUtils.RESOURCE.drawResource(x, y, width, height, (resource.getWidth() - regionWidth) / 2D, (resource.getHeight() - regionHeight) / 2D, regionWidth, regionHeight, resource);
		}

	}

}