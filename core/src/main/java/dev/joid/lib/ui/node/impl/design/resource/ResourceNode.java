package dev.joid.lib.ui.node.impl.design.resource;

import javax.vecmath.Vector4f;

import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class ResourceNode extends Node {

	private Resource resource;
	private Resource hoveredResource;

	private Color color = Color.WHITE;
	private Color hoveredColor;

	private StretchType stretchType = StretchType.STRETCH;

	protected ResourceNode(final double x, final double y) {
		this(x, y, 0, 0);
	}

	protected ResourceNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull ResourceNode create(final double x, final double y) {
		return new ResourceNode(x, y);
	}

	public static @NonNull ResourceNode create(final double x, final double y, final double width, final double height) {
		return new ResourceNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.resource != null) {
			this.resource.prepareBind();
		}

		if (this.hoveredResource != null) {
			this.hoveredResource.prepareBind();
		}

		if (this.resource == null || !this.resource.isLoaded()) {
			final double skeletonWidth = super.getWidth();
			final double skeletonHeight = super.getHeight();
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), skeletonWidth, skeletonHeight, Color.LOADING());
			return;
		}

		final double resourceWidth = this.resource.getWidth();
		final double resourceHeight = this.resource.getHeight();
		if (resourceWidth == 0 || resourceHeight == 0) {
			DrawUtils.RESOURCE.drawResource(0, 0, 0, 0, this.resource);
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.LOADING());
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

		final Vector4f canvas = new Vector4f((float) super.getX(), (float) super.getY(), (float) (super.getX() + super.getWidth()), (float) (super.getY() + super.getHeight()));
		final Color primary = this.hoveredColor != null && this.hoveredResource == null ? this.color.to(this.hoveredColor, super.hoverValue(1F)) : this.color;
		primary.bind(() -> this.drawResource(this.resource), canvas, true);

		if (this.hoveredResource != null && this.hoveredResource != this.resource) {
			final Color secondary = (this.hoveredColor != null ? this.hoveredColor : this.color).copyAlpha(super.hoverValue(1F));
			secondary.bind(() -> this.drawResource(this.hoveredResource), canvas, true);
		}
	}

	public final <T extends ResourceNode> @NonNull T resource(final @NonNull String url) {
		this.resource = Resource.of(url);
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T resource(final @NonNull Resource resource) {
		this.resource = resource;
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T resource(final @NonNull String url, final String hoveredUrl) {
		this.resource = Resource.of(url);
		if (hoveredUrl != null) {
			this.hoveredResource = Resource.of(hoveredUrl);
		}
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T resource(final @NonNull Resource resource, final Resource hoveredResource) {
		this.resource = resource;
		this.hoveredResource = hoveredResource;
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T hoverResource(final String url) {
		if (url != null) {
			this.hoveredResource = Resource.of(url);
		}
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T hoverResource(final Resource resource) {
		this.hoveredResource = resource;
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T color(final @NonNull Color color) {
		this.color = color;
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T hoveredColor(final Color color) {
		this.hoveredColor = color;
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T linear(final boolean linearInterpolation) {
		if (this.resource != null) {
			this.resource.interpolation(linearInterpolation ? TextureFilter.LINEAR : TextureFilter.NEAREST);
		}

		if (this.hoveredResource != null) {
			this.hoveredResource.interpolation(linearInterpolation ? TextureFilter.LINEAR : TextureFilter.NEAREST);
		}

		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T stretch(final @NonNull StretchType stretchType) {
		this.stretchType = stretchType;
		return (T) this;
	}

	private void drawResource(final @NonNull Resource resource) {
		this.stretchType.draw(super.getX(), super.getY(), super.getWidth(), super.getHeight(), resource);
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