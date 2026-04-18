# ScrollbarNode

Scrollbar visuelle qui suit la position de scroll d'un nœud cible. `ScrollbarNode` est **abstract** — vous la sous-classez pour dessiner la piste et le thumb, pendant que la classe de base gère le tracking du drag et le mapping pourcentage-pixel.

## Construction

Le constructeur est `protected` et prend les bounds usuels plus une `BoundingBox` décrivant le contenu scrollable :

```java
protected ScrollbarNode(double x, double y, double width, double height, BoundingBox scroll)
```

## Utilisation

```java
public class MyScrollbar extends ScrollbarNode {

    public MyScrollbar(double x, double y, double w, double h, BoundingBox scroll) {
        super(x, y, w, h, scroll);
    }

    @Override
    public void drawScrollbar(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY(), getWidth(), getHeight(),
            Color.decode("#3b82f6"), 3F);
    }
}
```

Puis attachez-la à côté du contenu scrollable et appelez `scrollNode(node)` pour lier les deux :

```java
final Node content = /* votre contenu scrollable */;
new MyScrollbar(392, 0, 8, 80, content.getBoundingBox())
    .scrollNode(content)
    .attach(parent);
```

## API

```java
T scrollNode(Node node)              // le nœud en train d'être scrollé

double getScrollWidth()               // scroll.width - this.width
double getScrollHeight()              // scroll.height - this.height
boolean isDragging()
BoundingBox getScroll()
Node getScrollNode()

abstract void drawScrollbar(double mouseX, double mouseY)
```

## Comportement

Au `mousePressed` sur la scrollbar, `dragging` passe à `true`. Pendant le drag :

- Si le `scrollNode` lié a un overflow X, la scrollbar glisse horizontalement et met à jour le `scrollX` (pourcentage 0..1) de la cible.
- Sinon si la cible a un overflow Y, la scrollbar glisse verticalement et met à jour `scrollY`.

`mouseReleased` nettoie le flag dragging. Implémentez `drawScrollbar(mouseX, mouseY)` pour rendre la piste/thumb — les bounds et la position ont déjà été mis à jour par la classe de base à ce moment.

## Voir aussi

- `Node Fundamentals` — propriétés d'overflow et de scroll.