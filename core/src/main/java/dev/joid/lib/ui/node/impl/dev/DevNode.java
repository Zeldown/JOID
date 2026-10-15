package dev.joid.lib.ui.node.impl.dev;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import dev.joid.internal.JOIDLog;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalContext;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.view.UIView;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.point.Point;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class DevNode extends Node {

	private static final Color RED         = new Color(239, 57, 38).lock();
	private static final Color ACTION      = new Color(57, 120, 255).lock();

	private static final Color BLACK       = new Color(23, 23, 25).lock();
	private static final Color WHITE       = new Color(250, 250, 250).lock();
	private static final Color LIGHT_BLACK = new Color(56, 56, 62).lock();
	private static final Color LIGHT_WHITE = new Color(161, 161, 170).lock();

	private static final Color[] GRID_COLORS = {DevNode.ACTION, DevNode.RED};

	@Getter(AccessLevel.NONE) private final BooleanSignal gridSignal;
	@Getter(AccessLevel.NONE) private final BooleanSignal inspectSignal;

	@Getter(AccessLevel.NONE) private final Signal<Node>         inspectedNode;
	@Getter(AccessLevel.NONE) private final ComputedSignal<Node> lockedNode;
	@Getter(AccessLevel.NONE) private final BooleanSignal        inspectedNodeLocked;

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

	private DevNode(final double x, final double y) {
		super(x, y, 275, 53);
		super.anchor(Align.END);

		this.inspectSignal = new BooleanSignal(true);
		this.gridSignal = new BooleanSignal(false);

		this.inspectedNode = new Signal<>();
		this.inspectedNodeLocked = new BooleanSignal();
		this.lockedNode = Signal.from(() -> this.inspectedNodeLocked.get() ? this.inspectedNode.get() : null);

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
				.resource(Resource.of("assets/dev/textures/icons/inspect.png"))
				.color(this.inspectSignal.map(inspect -> inspect ? DevNode.ACTION : DevNode.WHITE))
				.hoveredColor(this.inspectSignal.map(inspect -> (inspect ? DevNode.ACTION : DevNode.WHITE).darker(0.3F)))
				.onClick(event -> {
					this.inspectSignal.set(!this.inspectSignal.peek());
					this.inspectedNode.set(null);
					this.inspectedNodeLocked.set(false);
				})
				.hover(() -> "[I] Inspect")
				.attach(flex);

				ResourceNode
				.create(0, 0, 24, 24)
				.resource(Resource.of("assets/dev/textures/icons/reload.png"))
				.color(() -> DevNode.WHITE.to(DevNode.ACTION, this.reloadAnimator.get()))
				.hoveredColor(() -> DevNode.WHITE.to(DevNode.ACTION, this.reloadAnimator.get()).darker(0.3F))
				.onClick(event -> {
					event.getNode().getUi().reload();
				})
				.hover(() -> "[R] Reload")
				.attach(flex);

				ResourceNode
				.create(0, 0, 24, 24)
				.resource(Resource.of("assets/dev/textures/icons/grid.png"))
				.color(this.gridSignal.map(grid -> grid ? DevNode.ACTION : DevNode.WHITE))
				.hoveredColor(this.gridSignal.map(grid -> (grid ? DevNode.ACTION : DevNode.WHITE).darker(0.3F)))
				.onClick(event -> {
					this.gridSignal.set(!this.gridSignal.peek());
				})
				.hover(() -> "[G] Grid")
				.attach(flex);
			} catch (final Exception e) {
				JOIDLog.warn("The dev panel cannot show its tool icons (" + e + "), check that the JOID jar is complete", e);
			}
		})
		.attach(this);

		RectNode
		.create(122, super.getHeight() - super.getDefaultHeight(), 2, super.getDefaultHeight())
		.color(DevNode.LIGHT_BLACK)
		.attach(this);

		RectNode
		.create(0, super.getHeight() - super.getDefaultHeight(), super.getWidth(), 2)
		.color(DevNode.LIGHT_BLACK)
		.visible(this.inspectedNodeLocked)
		.attach(this);

		ContainerNode
		.create(0, 0, super.getWidth(), super.getHeight() - super.getDefaultHeight())
		.body(node -> {
			node.width(super.getWidth()).height(super.getHeight() - super.getDefaultHeight());
			final Node inspectedNode = this.lockedNode.peek();
			if (inspectedNode == null) {
				return;
			}

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
				.text(Text.create(inspectedNode.getClass().getSimpleName(), TextInfo.create(DevFont.MONTSERRAT, FontWeight.SEMI_BOLD, 17, DevNode.WHITE)).alignX(Align.START).alignY(Align.CENTER))
				.anchorY(Align.CENTER)
				.attach(flex);

				CircleNode
				.create(0, flex.dh(2) - 3.5D, 7)
				.color(DevNode.LIGHT_WHITE)
				.attach(flex);

				TextNode
				.create(0, flex.dh(2))
				.text(Text.create(String.format(Locale.ROOT, "%.2f", inspectedNode.getRenderTime() / 1000000F) + "ms", TextInfo.create(DevFont.MONTSERRAT, FontWeight.MEDIUM, 15, DevNode.LIGHT_WHITE)).alignX(Align.START).alignY(Align.CENTER))
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
					final TextInfo categoryInfo = TextInfo.create(DevFont.MONTSERRAT, FontWeight.MEDIUM, 15, DevNode.LIGHT_WHITE);
					final TextInfo labelInfo = TextInfo.create(DevFont.MONTSERRAT, FontWeight.SEMI_BOLD, 15, DevNode.ACTION.brighter(0.3F));
					final TextInfo valueInfo = TextInfo.create(DevFont.MONTSERRAT, FontWeight.MEDIUM, 15, DevNode.ACTION);

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

					if (!inspectedNode.getEventBus().isEmpty()) {
						RectNode
						.create(0, 0, 0, 5)
						.color(Color.TRANSPARENT)
						.attach(scroll);

						TextNode
						.create(0, 0)
						.text(Text.create("Listeners", categoryInfo))
						.attach(scroll);

						for (final EventType<?> type : inspectedNode.getEventBus().getTypes()) {
							TextNode
							.create(10, 0)
							.text(Text.create(type.getName() + ": ", labelInfo).add(Text.create(inspectedNode.getEventBus().count(type), valueInfo)))
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
					.onClick(event -> {
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
						.onClick(event -> {
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
							if (Modifier.isStatic(field.getModifiers())) {
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
							try {
								field.setAccessible(true);
								final Object raw = field.get(inspectedNode);
								final Object value = raw instanceof Supplier ? SignalContext.current().untracked((Supplier<?>) raw) : raw;

								TextNode
								.create(10, 0)
								.text(Text.create(field.getName() + ": ", labelInfo).add(Text.create(value == null ? "null" : value, valueInfo)))
								.mode(TextMode.SPLIT)
								.width(scroll.aw(-20))
								.attach(scroll);
							} catch (final Exception e) {
								JOIDLog.warn("The dev panel cannot read the field " + field.getName() + " of " + inspectedNode.getClass().getSimpleName() + " (" + e + "), the panel leaves it out", e);
							}
						}
					}
				})
				.attach(container);
			})
			.attach(node);
		})
		.watch(this.lockedNode, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
		.visible(this.inspectedNodeLocked)
		.attach(this);

		TextNode
		.create(123 + super.aw(-123) / 2, super.getHeight() - super.getDefaultHeight() + super.getDefaultHeight() / 2)
		.text(Text.create(() -> String.format(Locale.ROOT, "%.0f fps", super.getUi().getFps()), TextInfo.create(DevFont.MONTSERRAT, FontWeight.MEDIUM, 17, DevNode.WHITE)).align(Align.CENTER))
		.anchor(Align.CENTER)
		.attach(this);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.inspectSignal.peek()) {
			if (!this.inspectedNodeLocked.peek()) {
				this.inspectedNode.set(null);
				final List<Node> nodeList = super.getUi().getNodeListAt(mouseX, mouseY);
				if (UI.isCtrlKeyDown()) {
					Collections.reverse(nodeList);
				}

				for (final Node node : nodeList) {
					if (node instanceof DevNode || this.isAncestorOf(node)) {
						continue;
					}

					this.inspectedNode.set(node);
					break;
				}
			}

			if (this.inspectedNode.peek() != null) {
				this.targetInspectX = this.inspectedNode.peek().getAbsoluteX();
				this.targetInspectY = this.inspectedNode.peek().getAbsoluteY();
				this.targetInspectWidth = this.inspectedNode.peek().getWidth();
				this.targetInspectHeight = this.inspectedNode.peek().getHeight();
			}

			this.inspectX = super.getUi().lerpByFramerate(this.inspectX, this.targetInspectX, 0.5D, 0.5D, true);
			this.inspectY = super.getUi().lerpByFramerate(this.inspectY, this.targetInspectY, 0.5D, 0.5D, true);
			this.inspectWidth = super.getUi().lerpByFramerate(this.inspectWidth, this.targetInspectWidth, 0.5D, 0.5D, true);
			this.inspectHeight = super.getUi().lerpByFramerate(this.inspectHeight, this.targetInspectHeight, 0.5D, 0.5D, true);

			if (this.inspectedNode.peek() != null) {
				this.drawInfoBox(this.inspectX, this.inspectY, this.inspectWidth, this.inspectHeight, this.inspectedNode.peek());
			}
		}

		if (super.getHeight() == super.getDefaultHeight() && this.inspectedNodeLocked.peek()) {
			super.layoutSize(super.getDefaultWidth() + 200, super.getDefaultHeight() + 220);
			this.init(super.getUi());
		} else if (super.getHeight() != super.getDefaultHeight() && !this.inspectedNodeLocked.peek()) {
			super.layoutSize(super.getDefaultWidth(), super.getDefaultHeight());
			this.init(super.getUi());
		}

		if (this.gridSignal.peek()) {
			final Color gridColor = DevNode.GRID_COLORS[this.gridColorIndex];
			DrawUtils.SHAPE.drawLine(gridColor, 3F, Point.create(UIView.DESIGN_WIDTH / 2D - 1D, 0), Point.create(UIView.DESIGN_WIDTH / 2D + 1D, UIView.DESIGN_HEIGHT));
			DrawUtils.SHAPE.drawLine(gridColor, 3F, Point.create(0, UIView.DESIGN_HEIGHT / 2D - 1D), Point.create(UIView.DESIGN_WIDTH, UIView.DESIGN_HEIGHT / 2D + 1D));

			DrawUtils.SHAPE.drawRect(0, mouseY, mouseX, 3, gridColor);
			DrawUtils.TEXT.drawText(mouseX / 2, mouseY + 5, Text.create((int) mouseX + "px", TextInfo.create(DevFont.MONTSERRAT, 15, gridColor).shadow().shadow(Color.BLACK.withAlpha(0.7F))).alignX(Align.CENTER));

			DrawUtils.SHAPE.drawRect(mouseX, mouseY, UIView.DESIGN_WIDTH - mouseX, 3, gridColor);
			DrawUtils.TEXT.drawText(mouseX + (UIView.DESIGN_WIDTH - mouseX) / 2, mouseY + 5, Text.create((int) (UIView.DESIGN_WIDTH - mouseX) + "px", TextInfo.create(DevFont.MONTSERRAT, 15, gridColor).shadow().shadow(Color.BLACK.withAlpha(0.7F))).alignX(Align.CENTER));

			DrawUtils.SHAPE.drawRect(mouseX, 0, 3, mouseY, gridColor);
			DrawUtils.TEXT.drawText(mouseX + 5, mouseY / 2, Text.create((int) mouseY + "px", TextInfo.create(DevFont.MONTSERRAT, 15, gridColor).shadow().shadow(Color.BLACK.withAlpha(0.7F))));

			DrawUtils.SHAPE.drawRect(mouseX, mouseY, 3, UIView.DESIGN_HEIGHT - mouseY, gridColor);
			DrawUtils.TEXT.drawText(mouseX + 5, mouseY + (UIView.DESIGN_HEIGHT - mouseY) / 2, Text.create((int) (UIView.DESIGN_HEIGHT - mouseY) + "px", TextInfo.create(DevFont.MONTSERRAT, 15, gridColor).shadow().shadow(Color.BLACK.withAlpha(0.7F))));
		}

		DrawUtils.SHAPE.drawRoundedRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), DevNode.BLACK, 10F);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !super.isEnabled()) {
			return;
		}

		if (this.inspectSignal.peek() && this.inspectedNode.peek() != null) {
			if (event.getButton().isLeft() && !this.inspectedNodeLocked.peek()) {
				this.inspectedNodeLocked.set(true);
				event.consume();
			} else if (event.getButton().isRight() && this.inspectedNodeLocked.peek()) {
				this.inspectedNodeLocked.set(false);
				event.consume();
			}
		}

		if (event.getButton().isRight() && this.gridSignal.peek()) {
			this.gridColorIndex = (this.gridColorIndex + 1) % DevNode.GRID_COLORS.length;
		}
	}

	@Override
	public void keyPressed(final @NonNull NodeKeyEvent<?> event) {
		if (event.isConsumed() || !super.isEnabled()) {
			return;
		}

		final Key key = event.getKey();

		if (key == Key.I) {
			this.inspectSignal.set(!this.inspectSignal.peek());
			this.inspectedNode.set(null);
			this.inspectedNodeLocked.set(false);
			return;
		}

		if (key == Key.R && !Key.LEFT_CONTROL.isDown()) {
			super.getUi().reload();
			return;
		}

		if (key == Key.G) {
			this.gridSignal.set(!this.gridSignal.peek());
			return;
		}

		if (key != Key.NUMPAD_ENTER && key != Key.ENTER || this.inspectedNode.peek() == null || this.inspectedNode.peek().getParent() == null) {
			return;
		}

		this.inspectedNode.set(this.inspectedNode.peek().getParent());
		this.inspectedNodeLocked.set(true);
	}

	private boolean isAncestorOf(final Node node) {
		for (Node parent = node.getParent(); parent != null; parent = parent.getParent()) {
			if (parent == this) {
				return true;
			}
		}
		return false;
	}

	private void drawInfoBox(final double x, final double y, final double width, final double height, final Node node) {
		DrawUtils.SHAPE.drawRect(x, y, width, height, DevNode.ACTION.withAlpha(0.3F));
		DrawUtils.SHAPE.drawDashedLine(DevNode.ACTION, 7, 2F, Point.create(x, y), Point.create(x + width, y));
		DrawUtils.SHAPE.drawDashedLine(DevNode.ACTION, 7, 2F, Point.create(x, y), Point.create(x, y + height));
		DrawUtils.SHAPE.drawDashedLine(DevNode.ACTION, 7, 2F, Point.create(x + width, y), Point.create(x + width, y + height));
		DrawUtils.SHAPE.drawDashedLine(DevNode.ACTION, 7, 2F, Point.create(x, y + height), Point.create(x + width, y + height));

		final TextInfo textInfo = TextInfo.create(DevFont.MONTSERRAT, 15, DevNode.WHITE).shadow().shadow(Color.BLACK.withAlpha(0.7F));
		final Text nodeNameText = Text.create(node.getClass().getSimpleName(), textInfo);
		final Text nodeTimeText = Text.create(String.format(Locale.ROOT, "%.2f", node.getRenderTime() / 1000000F) + "ms", textInfo);

		final double margin = 15;
		final double padding = 10;

		final double infoBoxX = x;
		final double infoBoxY = y - 35 < 0 ? y + height + 5 : y - 35;
		final double infoBoxWidth = margin + padding * 2 + nodeNameText.getWidth() + nodeTimeText.getWidth();

		DrawUtils.SHAPE.drawRoundedRect(infoBoxX, infoBoxY, infoBoxWidth, 30, DevNode.BLACK.withAlpha(0.8F), 5F);

		double ox = infoBoxX + padding;
		ox += DrawUtils.TEXT.drawText(ox, infoBoxY + 15, nodeNameText.alignY(Align.CENTER)).getWidth() + margin;
		DrawUtils.SHAPE.drawCircle(ox - margin / 2 + 1, infoBoxY + 15, DevNode.WHITE, 2);
		DrawUtils.TEXT.drawText(ox, infoBoxY + 15, nodeTimeText.alignY(Align.CENTER));
	}

}