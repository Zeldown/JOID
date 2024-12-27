package be.zeldown.joid.lib.ui.node.impl.design.resource;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class ResourceNode extends Node {

	private Resource resource;
	private Resource hoveredResource;

	private Color color = Color.WHITE;
	private Color hoveredColor = Color.WHITE;

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
		}

		if (super.getWidth() == 0 && super.getHeight() != 0) {
			this.color.bind();
			DrawUtils.RESOURCE.drawScaledResourceHeight(super.getX(), super.getY(), super.getHeight(), this.resource);
			if (this.hoveredResource != null && this.hoveredResource != this.resource) {
				this.hoveredColor.copyAlpha(super.hoverValue(1F)).bind();
				DrawUtils.RESOURCE.drawScaledResourceHeight(super.getX(), super.getY(), super.getHeight(), this.hoveredResource);
			}
			super.width(resourceWidth * super.getHeight() / resourceHeight);
		} else if (super.getHeight() == 0 && super.getWidth() != 0) {
			this.color.bind();
			DrawUtils.RESOURCE.drawScaledResourceWidth(super.getX(), super.getY(), super.getWidth(), this.resource);
			if (this.hoveredResource != null && this.hoveredResource != this.resource) {
				this.hoveredColor.copyAlpha(super.hoverValue(1F)).bind();
				DrawUtils.RESOURCE.drawScaledResourceWidth(super.getX(), super.getY(), super.getWidth(), this.hoveredResource);
			}
			super.height(resourceHeight * super.getWidth() / resourceWidth);
		} else if (this.stretchType == StretchType.STRETCH) {
			this.color.bind();
			DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.resource);
			if (this.hoveredResource != null && this.hoveredResource != this.resource) {
				this.hoveredColor.copyAlpha(super.hoverValue(1F)).bind();
				DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.hoveredResource);
			}
		} else if (this.stretchType == StretchType.CONTAIN) {
			this.color.bind();
			DrawUtils.RESOURCE.drawCenteredResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.resource);
			if (this.hoveredResource != null && this.hoveredResource != this.resource) {
				this.hoveredColor.copyAlpha(super.hoverValue(1F)).bind();
				DrawUtils.RESOURCE.drawCenteredResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.hoveredResource);
			}
		}
	}

	public final <T extends ResourceNode> @NonNull T resource(final @NonNull Resource resource) {
		this.resource = resource;
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T resource(final @NonNull String url) {
		this.resource = Resource.of(url);
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T resource(final @NonNull Resource resource, final Resource hoveredResource) {
		this.resource = resource;
		this.hoveredResource = hoveredResource;
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T resource(final @NonNull String url, final String hoveredUrl) {
		this.resource = Resource.of(url);
		if (hoveredUrl != null) {
			this.hoveredResource = Resource.of(hoveredUrl);
		}
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T hoverResource(final Resource resource) {
		this.hoveredResource = resource;
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T hoverResource(final String url) {
		if (url != null) {
			this.hoveredResource = Resource.of(url);
		}
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T color(final @NonNull Color color) {
		this.color = color;
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T hoveredColor(final @NonNull Color color) {
		this.hoveredColor = color;
		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T linear(final boolean linearInterpolation) {
		if (this.resource != null) {
			this.resource.interpolation(linearInterpolation ? GL11.GL_LINEAR : GL11.GL_NEAREST);
		}

		if (this.hoveredResource != null) {
			this.hoveredResource.interpolation(linearInterpolation ? GL11.GL_LINEAR : GL11.GL_NEAREST);
		}

		return (T) this;
	}

	public final <T extends ResourceNode> @NonNull T stretch(final StretchType stretchType) {
		this.stretchType = stretchType;
		return (T) this;
	}

	public static enum StretchType {

		STRETCH,
		CONTAIN;

	}

}
