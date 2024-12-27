package be.zeldown.joid.lib.ui.node.impl.dev;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.vecmath.Vector2d;

import org.lwjgl.input.Keyboard;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.internal.font.InternalFont;
import be.zeldown.joid.lib.animation.animator.TweenAnimator;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.draw.text.utils.TextMode;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackObject;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.impl.design.resource.ResourceNode;
import be.zeldown.joid.lib.ui.node.impl.design.shape.CircleNode;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.container.ContainerNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;
import be.zeldown.joid.lib.ui.node.property.overflow.OverflowProperty;
import be.zeldown.joid.lib.ui.node.property.watch.WatchProperty;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.signal.Signal;
import be.zeldown.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class DevNode extends Node {

	private static final Color ACTION      = new Color(57, 120, 255);
	private static final Color UPDATE      = new Color(239, 57, 38);

	private static final Color BLACK       = new Color(23, 23, 25);
	private static final Color LIGHT_BLACK = new Color(56, 56, 62);
	private static final Color WHITE       = new Color(250, 250, 250);
	private static final Color LIGHT_WHITE = new Color(161, 161, 170);

	private static final Color[] GRID_COLORS = {DevNode.ACTION, DevNode.UPDATE};

	private final BooleanSignal inspectSignal;
	private final BooleanSignal reloadSignal;
	private final BooleanSignal eyeSignal;
	private final BooleanSignal gridSignal;

	private final Signal<Node> inspectedNode;
	private final BooleanSignal inspectedNodeLocked;

	private final TweenAnimator reloadAnimator;

	private double inspectX;
	private double inspectY;
	private double inspectWidth;
	private double inspectHeight;
	private double targetInspectX;
	private double targetInspectY;
	private double targetInspectWidth;
	private double targetInspectHeight;

	private int gridColorIndex = 0;

	protected DevNode(final double x, final double y) {
		super(x, y, 275, 53);
		super.anchor(Align.END);

		this.inspectSignal = new BooleanSignal(true);
		this.reloadSignal = new BooleanSignal(false);
		this.eyeSignal = new BooleanSignal(true);
		this.gridSignal = new BooleanSignal(false);

		this.inspectedNode = new Signal<>();
		this.inspectedNodeLocked = new BooleanSignal();

		this.reloadAnimator = TweenAnimator.create(0F);
	}

	public static @NonNull DevNode create(final double x, final double y) {
		return new DevNode(x, y);
	}

	@Override
	public void init(final UI ui) {
		super.clearChildren();

		FlexNode
		.horizontal(15, super.getHeight() - super.getDefaultHeight() + 15, super.getDefaultHeight() - 30)
		.margin(10)
		.body(flex -> {
			try {
				ResourceNode
				.create(0, 0, 24, 24)
				.resource(Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/inspect.png")), Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/inspect.png")))
				.watch(this.inspectSignal)
				.<ResourceNode>onInit(node -> {
					node.color(this.inspectSignal.getOrDefault() ? DevNode.ACTION : DevNode.WHITE);
					node.hoveredColor(node.getColor().darker(0.3F));
				})
				.onClick((node, mouseX, mouseY, clickType) -> {
					this.inspectSignal.set(!this.inspectSignal.getOrDefault());
					this.inspectedNode.set(null);
					this.inspectedNodeLocked.set(false);
				})
				.hover(() -> "[I] Inspect")
				.attach(flex);

				ResourceNode
				.create(0, 0, 24, 24)
				.resource(Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/reload.png")), Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/reload.png")))
				.watch(this.reloadSignal)
				.<ResourceNode>onInit(node -> {
					node.color(DevNode.WHITE.to(DevNode.ACTION, this.reloadAnimator.getValue()));
					node.hoveredColor(node.getColor().darker(0.3F));
				})
				.onClick((node, mouseX, mouseY, clickType) -> {
					node.getUi().reload();
				})
				.hover(() -> "[R] Reload")
				.attach(flex);

				ResourceNode
				.create(0, 0, 24, 24)
				.resource(Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/eye.png")), Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/eye.png")))
				.watch(this.eyeSignal)
				.<ResourceNode>onInit(node -> {
					node.color(this.eyeSignal.getOrDefault() ? DevNode.ACTION : DevNode.WHITE);
					node.hoveredColor(node.getColor().darker(0.3F));
				})
				.onClick((node, mouseX, mouseY, clickType) -> {
					this.eyeSignal.set(!this.eyeSignal.getOrDefault());
				})
				.hover(() -> "[U] Update")
				.attach(flex);

				ResourceNode
				.create(0, 0, 24, 24)
				.resource(Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/grid.png")), Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/grid.png")))
				.watch(this.gridSignal)
				.<ResourceNode>onInit(node -> {
					node.color(this.gridSignal.getOrDefault() ? DevNode.ACTION : DevNode.WHITE);
					node.hoveredColor(node.getColor().darker(0.3F));
				})
				.onClick((node, mouseX, mouseY, clickType) -> {
					this.gridSignal.set(!this.gridSignal.getOrDefault());
				})
				.hover(() -> "[G] Grid")
				.attach(flex);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		})
		.attach(this);

		RectNode
		.create(156, super.getHeight() - super.getDefaultHeight(), 2, super.getDefaultHeight())
		.color(DevNode.LIGHT_BLACK)
		.attach(this);

		RectNode
		.create(0, super.getHeight() - super.getDefaultHeight(), super.getWidth(), 2)
		.color(DevNode.LIGHT_BLACK)
		.visible(node -> this.inspectedNodeLocked.getOrDefault())
		.attach(this);

		ContainerNode
		.create(0, 0, super.getWidth(), super.getHeight() - super.getDefaultHeight())
		.onInit(node -> {
			node.size(super.getWidth(), super.getHeight() - super.getDefaultHeight());
			if (!this.inspectedNodeLocked.getOrDefault()) {
				return;
			}

			final Node inspectedNode = this.inspectedNode.getOrDefault();

			RectNode
			.create(0, 53, node.getWidth(), 2)
			.color(DevNode.LIGHT_BLACK)
			.attach(node);

			FlexNode
			.horizontal(10, 0, 53)
			.margin(15)
			.body(flex -> {
				TextNode
				.create(0, flex.dh(2))
				.text(Text.create(inspectedNode.getClass().getSimpleName(), TextInfo.create(InternalFont.MONTSERRAT_SEMI_BOLD, 17, DevNode.WHITE), Align.START, Align.CENTER))
				.anchorY(Align.CENTER)
				.attach(flex);

				TextNode
				.create(0, flex.dh(2))
				.text(Text.create(inspectedNode.getUpdateCount() + " update"  + (inspectedNode.getUpdateCount() > 1 ? "s" : ""), TextInfo.create(InternalFont.MONTSERRAT_MEDIUM, 15, DevNode.LIGHT_WHITE), Align.START, Align.CENTER))
				.anchorY(Align.CENTER)
				.attach(flex);

				CircleNode
				.create(0, flex.dh(2) - 3.5, 7)
				.color(DevNode.LIGHT_WHITE)
				.attach(flex);

				TextNode
				.create(0, flex.dh(2))
				.text(Text.create(String.format("%.2f", inspectedNode.getRenderTime() / 1000000F) + "ms", TextInfo.create(InternalFont.MONTSERRAT_MEDIUM, 15, DevNode.LIGHT_WHITE), Align.START, Align.CENTER))
				.anchorY(Align.CENTER)
				.attach(flex);
			})
			.attach(node);

			ContainerNode
			.create(0, 53, super.getWidth(), super.getHeight() - super.getDefaultHeight() - 53)
			.overflow(OverflowProperty.SCROLL)
			.body(container -> {
				FlexNode
				.vertical(10, 10, container.aw(-20))
				.body(scroll -> {
					final TextInfo categoryInfo = TextInfo.create(InternalFont.MONTSERRAT_MEDIUM, 15, DevNode.LIGHT_WHITE);
					final TextInfo labelInfo = TextInfo.create(InternalFont.MONTSERRAT_SEMI_BOLD, 15, DevNode.ACTION.brighter(0.3F));
					final TextInfo valueInfo = TextInfo.create(InternalFont.MONTSERRAT_MEDIUM, 15, DevNode.ACTION);

					TextNode
					.create(0, 0)
					.text(Text.create("Bounds", categoryInfo))
					.attach(scroll);

					TextNode
					.create(10, 0)
					.text(Text.create("position: ", labelInfo).add(Text.create(inspectedNode.getPosition(), valueInfo)))
					.attach(scroll);

					TextNode
					.create(10, 0)
					.text(Text.create("x: ", labelInfo).add(Text.create(inspectedNode.getX() + " / " + inspectedNode.getAbsoluteX() + " (" + inspectedNode.getDefaultX() + ")", valueInfo)))
					.attach(scroll);

					TextNode
					.create(10, 0)
					.text(Text.create("y: ", labelInfo).add(Text.create(inspectedNode.getY() + " / " + inspectedNode.getAbsoluteY() + " (" + inspectedNode.getDefaultY() + ")", valueInfo)))
					.attach(scroll);

					TextNode
					.create(10, 0)
					.text(Text.create("width: ", labelInfo).add(Text.create(inspectedNode.getWidth() + " (" + inspectedNode.getDefaultWidth() + ")", valueInfo)))
					.attach(scroll);

					TextNode
					.create(10, 0)
					.text(Text.create("height: ", labelInfo).add(Text.create(inspectedNode.getHeight() + " (" + inspectedNode.getDefaultHeight() + ")", valueInfo)))
					.attach(scroll);

					if (!inspectedNode.getCallbackMap().isEmpty()) {
						RectNode
						.create(0, 0, 0, 5)
						.color(Color.TRANSPARENT)
						.attach(scroll);

						TextNode
						.create(0, 0)
						.text(Text.create("Callbacks", categoryInfo))
						.attach(scroll);

						for (final Entry<Integer, List<NodeCallbackObject<?>>> callback : inspectedNode.getCallbackMap().entrySet()) {
							final int id = callback.getKey();
							final List<NodeCallbackObject<?>> callbackList = callback.getValue();

							TextNode
							.create(10, 0)
							.text(Text.create(NodeCallbackRegistry.get(id).getSimpleName() + ": ", labelInfo).add(Text.create(callbackList.size(), valueInfo)))
							.attach(scroll);
						}
					}

					RectNode
					.create(0, 0, 0, 5)
					.color(Color.TRANSPARENT)
					.attach(scroll);

					TextNode
					.create(0, 0)
					.text(Text.create("Hierarchy", categoryInfo))
					.attach(scroll);

					TextNode
					.create(10, 0)
					.text(Text.create("hierarchy: ", labelInfo).add(Text.create(inspectedNode.getHierarchy(), valueInfo)))
					.onClick((clickedNode, mouseX, mouseY, clickType) -> {
						if (inspectedNode.getParent() != null) {
							this.inspectedNode.set(inspectedNode.getParent());
							this.inspectedNodeLocked.set(true);
						}
					})
					.attach(scroll);

					TextNode
					.create(10, 0)
					.text(Text.create("children: ", labelInfo).add(Text.create(inspectedNode.getChildren().size(), valueInfo)))
					.attach(scroll);

					for (final Node child : inspectedNode.getChildren()) {
						TextNode
						.create(20, 0)
						.text(Text.create(child.getClass().getSimpleName(), valueInfo))
						.onClick((clickedNode, mouseX, mouseY, clickType) -> {
							this.inspectedNode.set(child);
							this.inspectedNodeLocked.set(true);
						})
						.attach(scroll);
					}

					final Set<Field> fields = new HashSet<>();

					Class<?> clazz = inspectedNode.getClass();
					while (clazz != null) {
						if (clazz.equals(Node.class)) {
							break;
						}

						for (final Field field : clazz.getDeclaredFields()) {
							if ((field.getModifiers() & 8) != 0) {
								continue;
							}

							fields.add(field);
						}

						clazz = clazz.getSuperclass();
					}

					if (!fields.isEmpty()) {
						RectNode
						.create(0, 0, 0, 5)
						.color(Color.TRANSPARENT)
						.attach(scroll);

						TextNode
						.create(0, 0)
						.text(Text.create("Properties", categoryInfo))
						.attach(scroll);

						for (final Field field : fields) {
							if (field.getDeclaringClass().equals(Node.class)) {
								continue;
							}

							try {
								field.setAccessible(true);
								final Object value = field.get(inspectedNode);

								TextNode
								.create(10, 0)
								.text(Text.create(field.getName() + ": ", labelInfo).add(Text.create(value == null ? "null" : value, valueInfo)))
								.mode(TextMode.SPLIT)
								.width(scroll.aw(-20))
								.attach(scroll);
							} catch (final Exception e) {
								e.printStackTrace();
							}
						}
					}
				})
				.attach(container);
			})
			.attach(node);
		})
		.watch(this.inspectedNodeLocked, WatchProperty.CLEAR_CHILDREN, WatchProperty.RELOAD)
		.visible(node -> this.inspectedNodeLocked.getOrDefault())
		.attach(this);

		TextNode
		.create(157 + super.aw(-157) / 2, super.getHeight() - super.getDefaultHeight() + super.getDefaultHeight() / 2)
		.text(Text.create(String.format("%.0f fps", super.getUi().getFps()), TextInfo.create(InternalFont.MONTSERRAT_MEDIUM, 17, DevNode.WHITE), Align.CENTER, Align.CENTER))
		.<TextNode>onUpdate(node -> node.getText().text(String.format("%.0f fps", super.getUi().getFps())))
		.anchor(Align.CENTER)
		.attach(this);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final float oldReloadAnimatorValue = this.reloadAnimator.getValue();
		this.reloadAnimator.update();
		final float newReloadAnimatorValue = this.reloadAnimator.getValue();
		if (oldReloadAnimatorValue != newReloadAnimatorValue) {
			this.reloadSignal.set(newReloadAnimatorValue > 0F);
		}

		if (this.inspectSignal.getOrDefault()) {
			if (!this.inspectedNodeLocked.getOrDefault()) {
				this.inspectedNode.set(null);
				for (final Node node : UI.isCtrlKeyDown() ? super.getUi().getNodeList().recursive() : super.getUi().getNodeList().recursive().reversed()) {
					if (!node.isHovered(mouseX, mouseY, false) || node instanceof DevNode || this.getChildren().recursive().contains(node)) {
						continue;
					}

					this.inspectedNode.set(node);
					break;
				}
			}

			if (this.inspectedNode.getOrDefault() != null) {
				this.targetInspectX = this.inspectedNode.getOrDefault().getAbsoluteX();
				this.targetInspectY = this.inspectedNode.getOrDefault().getAbsoluteY();
				this.targetInspectWidth = this.inspectedNode.getOrDefault().getWidth();
				this.targetInspectHeight = this.inspectedNode.getOrDefault().getHeight();
			}

			this.inspectX = super.getUi().lerpByFramerate(this.inspectX, this.targetInspectX, 0.5D, 0.5D, true);
			this.inspectY = super.getUi().lerpByFramerate(this.inspectY, this.targetInspectY, 0.5D, 0.5D, true);
			this.inspectWidth = super.getUi().lerpByFramerate(this.inspectWidth, this.targetInspectWidth, 0.5D, 0.5D, true);
			this.inspectHeight = super.getUi().lerpByFramerate(this.inspectHeight, this.targetInspectHeight, 0.5D, 0.5D, true);

			if (this.inspectedNode.getOrDefault() != null) {
				this.drawInfoBox(this.inspectX, this.inspectY, this.inspectWidth, this.inspectHeight, DevNode.ACTION, 1F, this.inspectedNode.getOrDefault());
			}
		}

		if (super.getHeight() == super.getDefaultHeight() && this.inspectedNodeLocked.getOrDefault()) {
			super.width(super.getDefaultWidth() + 200);
			super.height(super.getDefaultHeight() + 220);
			super.reload();
		} else if (super.getHeight() != super.getDefaultHeight() && !this.inspectedNodeLocked.getOrDefault()) {
			super.width(super.getDefaultWidth());
			super.height(super.getDefaultHeight());
			super.reload();
		}

		if (this.eyeSignal.getOrDefault()) {
			final long now = System.currentTimeMillis();
			final long duration = 2000L;
			for (final Node node : super.getUi().getNodeList().recursive()) {
				if (node instanceof DevNode || this.getChildren().recursive().contains(node)) {
					continue;
				}

				final long lastUpdate = node.getLastUpdate();
				if (now - lastUpdate > duration) {
					continue;
				}

				final float value = Math.min(1F, 2F - (now - lastUpdate) / (duration / 2F));
				final double x = node.getAbsoluteX();
				final double y = node.getAbsoluteY();
				final double width = node.getWidth();
				final double height = node.getHeight();

				this.drawInfoBox(x, y, width, height, DevNode.UPDATE, value, node);
			}
		}

		if (this.gridSignal.getOrDefault()) {
			final Color gridColor = DevNode.GRID_COLORS[this.gridColorIndex];
			DrawUtils.SHAPE.drawLine(gridColor, 3F, new Vector2d(1920 / 2 - 1, 0), new Vector2d(1920 / 2 + 1, 1080));
			DrawUtils.SHAPE.drawLine(gridColor, 3F, new Vector2d(0, 1080 / 2 - 1), new Vector2d(1920, 1080 / 2 + 1));

			DrawUtils.SHAPE.drawRect(0, mouseY, mouseX, 3, gridColor);
			DrawUtils.TEXT.drawText(mouseX / 2, mouseY + 5, Text.create((int) mouseX + "px", TextInfo.create(InternalFont.MONTSERRAT_REGULAR, 15, gridColor).shadow().shadow(Color.BLACK.copyAlpha(0.7F)), Align.CENTER));

			DrawUtils.SHAPE.drawRect(mouseX, mouseY, 1920 - mouseX, 3, gridColor);
			DrawUtils.TEXT.drawText(mouseX + (1920 - mouseX) / 2, mouseY + 5, Text.create((int) (1920 - mouseX) + "px", TextInfo.create(InternalFont.MONTSERRAT_REGULAR, 15, gridColor).shadow().shadow(Color.BLACK.copyAlpha(0.7F)), Align.CENTER));

			DrawUtils.SHAPE.drawRect(mouseX, 0, 3, mouseY, gridColor);
			DrawUtils.TEXT.drawText(mouseX + 5, mouseY / 2, Text.create((int) mouseY + "px", TextInfo.create(InternalFont.MONTSERRAT_REGULAR, 15, gridColor).shadow().shadow(Color.BLACK.copyAlpha(0.7F))));

			DrawUtils.SHAPE.drawRect(mouseX, mouseY, 3, 1080 - mouseY, gridColor);
			DrawUtils.TEXT.drawText(mouseX + 5, mouseY + (1080 - mouseY) / 2, Text.create((int) (1080 - mouseY) + "px", TextInfo.create(InternalFont.MONTSERRAT_REGULAR, 15, gridColor).shadow().shadow(Color.BLACK.copyAlpha(0.7F))));
		}

		DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), DevNode.BLACK, 10F);
	}

	private void drawInfoBox(final double x, final double y, final double width, final double height, final Color color, final float opacity, final Node node) {
		DrawUtils.SHAPE.drawRect(x, y, width, height, color.copyAlpha(0.3F * opacity));
		DrawUtils.SHAPE.drawDashedLine(color.copyAlpha(opacity), 7, 2F, new Vector2d(x, y), new Vector2d(x + width, y));
		DrawUtils.SHAPE.drawDashedLine(color.copyAlpha(opacity), 7, 2F, new Vector2d(x, y), new Vector2d(x, y + height));
		DrawUtils.SHAPE.drawDashedLine(color.copyAlpha(opacity), 7, 2F, new Vector2d(x + width, y), new Vector2d(x + width, y + height));
		DrawUtils.SHAPE.drawDashedLine(color.copyAlpha(opacity), 7, 2F, new Vector2d(x, y + height), new Vector2d(x + width, y + height));

		final TextInfo textInfo = TextInfo.create(InternalFont.MONTSERRAT_REGULAR, 15, DevNode.WHITE.copyAlpha(opacity)).shadow().shadow(Color.BLACK.copyAlpha(0.7F * opacity));
		final Text nodeNameText = Text.create(node.getClass().getSimpleName(), textInfo);
		final Text nodeUpdateText = Text.create(node.getUpdateCount() + " update" + (node.getUpdateCount() > 1 ? "s" : ""), textInfo);
		final Text nodeTimeText = Text.create(String.format("%.2f", node.getRenderTime() / 1000000F) + "ms", textInfo);

		final double margin = 15;
		final double padding = 10;

		final double infoBoxX = x;
		final double infoBoxY = y - 35 < 0 ? y + height + 5 : y - 35;
		final double infoBoxWidth = margin * 2 + padding * 2 + nodeNameText.getWidth() + nodeUpdateText.getWidth() + nodeTimeText.getWidth();

		DrawUtils.SHAPE.drawRoundedRect(infoBoxX, infoBoxY, infoBoxWidth, 30, DevNode.BLACK.copyAlpha(0.8F * opacity), 5F);

		double ox = infoBoxX + padding;
		ox += DrawUtils.TEXT.drawText(ox, infoBoxY + 15, nodeNameText.verticalAlign(Align.CENTER)).getWidth() + margin;
		DrawUtils.SHAPE.drawCircle(ox - margin / 2 + 1, infoBoxY + 15, DevNode.WHITE.copyAlpha(opacity), 2);
		ox += DrawUtils.TEXT.drawText(ox, infoBoxY + 15, nodeUpdateText.verticalAlign(Align.CENTER)).getWidth() + margin;
		DrawUtils.SHAPE.drawCircle(ox - margin / 2 + 1, infoBoxY + 15, DevNode.WHITE.copyAlpha(opacity), 2);
		ox += DrawUtils.TEXT.drawText(ox, infoBoxY + 15, nodeTimeText.verticalAlign(Align.CENTER)).getWidth() + margin;
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final int clickType, final InternalContext context) {
		if (context.isCancelled() || !super.isEnabled()) {
			return;
		}

		if (this.inspectSignal.getOrDefault() && this.inspectedNode.getOrDefault() != null) {
			if (clickType == 0 && !this.inspectedNodeLocked.getOrDefault()) {
				this.inspectedNodeLocked.set(true);
				context.cancel();
			} else if (clickType == 1 && this.inspectedNodeLocked.getOrDefault()) {
				this.inspectedNodeLocked.set(false);
				context.cancel();
			}
		}

		if (clickType == 1 && this.gridSignal.getOrDefault()) {
			this.gridColorIndex = (this.gridColorIndex + 1) % DevNode.GRID_COLORS.length;
		}
	}

	@Override
	public void keyPressed(final char c, final int keyCode, final InternalContext context) {
		if (context.isCancelled() || !super.isEnabled()) {
			return;
		}

		if (keyCode == Keyboard.KEY_I) {
			this.inspectSignal.set(!this.inspectSignal.getOrDefault());
			this.inspectedNode.set(null);
			this.inspectedNodeLocked.set(false);
			return;
		}

		if (keyCode == Keyboard.KEY_R && !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			super.getUi().reload();
			return;
		}

		if (keyCode == Keyboard.KEY_U) {
			this.eyeSignal.set(!this.eyeSignal.getOrDefault());
			return;
		}

		if (keyCode == Keyboard.KEY_G) {
			this.gridSignal.set(!this.gridSignal.getOrDefault());
			return;
		}

		if (keyCode != Keyboard.KEY_NUMPADENTER && keyCode != Keyboard.KEY_RETURN || this.inspectedNode.getOrDefault() == null || this.inspectedNode.getOrDefault().getParent() == null) {
			return;
		}

		this.inspectedNode.set(this.inspectedNode.getOrDefault().getParent());
		this.inspectedNodeLocked.set(true);
	}

}