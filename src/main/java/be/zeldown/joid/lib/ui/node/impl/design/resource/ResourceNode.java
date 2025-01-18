package be.zeldown.joid.lib.ui.node.impl.design.resource;

import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.shader.impl.RoundedShader;
import be.zeldown.joid.lib.ui.node.Node;
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

	private float borderRadius;

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
			this.rounded(() -> {
				DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), skeletonWidth, skeletonHeight, Color.LOADING());
			});
			return;
		}

		final double resourceWidth = this.resource.getWidth();
		final double resourceHeight = this.resource.getHeight();
		if (resourceWidth == 0 || resourceHeight == 0) {
			DrawUtils.RESOURCE.drawResource(0, 0, 0, 0, this.resource);
			this.rounded(() -> {
				DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.LOADING());
			});
			return;
		}

		if (super.getWidth() == 0 && super.getHeight() == 0) {
			super.width(resourceWidth);
			super.height(resourceHeight);
			return;
		}

		if (super.getWidth() == 0 && super.getHeight() != 0) {
			this.color.bind();
			if (this.hoveredResource == null && this.hoveredColor != null) {
				this.color.to(this.hoveredColor, super.hoverValue(1F)).bind();
			}

			final double lastWidth = super.getWidth();
			final double newWidth = resourceWidth * super.getHeight() / resourceHeight;
			if (lastWidth != newWidth) {
				super.width(newWidth);
			} else {
				this.rounded(() -> {
					DrawUtils.RESOURCE.drawScaledResourceHeight(super.getX(), super.getY(), super.getHeight(), this.resource);
					if (this.hoveredResource != null && this.hoveredResource != this.resource) {
						this.hoveredColor.copyAlpha(super.hoverValue(1F)).bind();
						DrawUtils.RESOURCE.drawScaledResourceHeight(super.getX(), super.getY(), super.getHeight(), this.hoveredResource);
					}
				});
			}
		} else if (super.getHeight() == 0 && super.getWidth() != 0) {
			this.color.bind();
			if (this.hoveredResource == null && this.hoveredColor != null) {
				this.color.to(this.hoveredColor, super.hoverValue(1F)).bind();
			}

			final double lastHeight = super.getHeight();
			final double newHeight = resourceHeight * super.getWidth() / resourceWidth;
			if (lastHeight != newHeight) {
				super.height(newHeight);
			} else {
				this.rounded(() -> {
					DrawUtils.RESOURCE.drawScaledResourceWidth(super.getX(), super.getY(), super.getWidth(), this.resource);
					if (this.hoveredResource != null && this.hoveredResource != this.resource) {
						if (this.hoveredColor != null) {
							this.hoveredColor.copyAlpha(super.hoverValue(1F)).bind();
						}
						DrawUtils.RESOURCE.drawScaledResourceWidth(super.getX(), super.getY(), super.getWidth(), this.hoveredResource);
					}
				});
			}
		} else if (this.stretchType == StretchType.STRETCH) {
			this.color.bind();
			if (this.hoveredResource == null && this.hoveredColor != null) {
				this.color.to(this.hoveredColor, super.hoverValue(1F)).bind();
			}

			this.rounded(() -> {
				DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.resource);
				if (this.hoveredResource != null && this.hoveredResource != this.resource) {
					this.hoveredColor.copyAlpha(super.hoverValue(1F)).bind();
					DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.hoveredResource);
				}
			});
		} else if (this.stretchType == StretchType.CONTAIN) {
			this.color.bind();
			if (this.hoveredResource == null && this.hoveredColor != null) {
				this.color.to(this.hoveredColor, super.hoverValue(1F)).bind();
			}

			this.rounded(() -> {
				DrawUtils.RESOURCE.drawCenteredResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.resource);
				if (this.hoveredResource != null && this.hoveredResource != this.resource) {
					this.hoveredColor.copyAlpha(super.hoverValue(1F)).bind();
					DrawUtils.RESOURCE.drawCenteredResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.hoveredResource);
				}
			});
		}
	}

	private void rounded(final @NonNull Runnable runnable) {
		if (this.borderRadius <= 0F) {
			runnable.run();
			return;
		}

		RoundedShader.use(this.borderRadius, () -> {
			runnable.run();
		}, new Vector4f((float) super.getX(), (float) super.getY(), (float) (super.getX() + super.getWidth()), (float) (super.getY() + super.getHeight())));
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

	public final <T extends ResourceNode> @NonNull T hoveredColor(final Color color) {
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

	public final <T extends ResourceNode> @NonNull T borderRadius(final float borderRadius) {
		this.borderRadius = borderRadius;
		return (T) this;
	}

	public static enum StretchType {

		STRETCH,
		CONTAIN;

	}

}
