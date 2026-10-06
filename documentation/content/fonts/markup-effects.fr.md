# Balisage & effets

Le moteur de texte de JOID est neutre : il ne connaît ni code couleur, ni balise, ni décoration. Deux points d'extension permettent à un projet d'apporter les siens — le **balisage** transforme des codes en ligne en changements de style, les **effets** transforment et décorent chaque glyphe. Les deux fonctionnent avec toute police à glyphes, MSDF compris, au dessin comme à la mesure.

## `TextStyle`

Chaque glyphe est dessiné avec un `TextStyle` : une graisse, un indicateur d'italique, une couleur et une liste d'effets. Le dessin part du style du `TextInfo`, et le balisage modifie une copie de travail au fil de la chaîne.

```java
T weight(FontWeight weight)
T italic(boolean italic)
T color(Color color)
T effect(ITextEffect effect)        // ajouté une fois, ignoré s'il est déjà là
T removeEffect(ITextEffect effect)
T reset()                           // retour au style du TextInfo
TextStyle getBase()                 // le style du TextInfo
```

## Balisage

Un `ITextMarkup` est interrogé, à chaque position de la chaîne, pour savoir si un code y commence :

```java
public interface ITextMarkup {

	public int parse(String text, int index, TextStyle style);

}
```

Il renvoie le nombre de caractères occupés par le code, après avoir modifié le style, ou `0` quand rien ne commence à `index`. Les caractères consommés ne sont ni dessinés ni mesurés.

Un balisage minimal à base de balises :

```java
public final class TagTextMarkup implements ITextMarkup {

	private static final Pattern TAG = Pattern.compile("<(/?)(b|i)>");

	@Override
	public int parse(final String text, final int index, final TextStyle style) {
		final Matcher matcher = TagTextMarkup.TAG.matcher(text).region(index, text.length());
		if (!matcher.lookingAt()) {
			return 0;
		}

		final boolean open = matcher.group(1).isEmpty();
		if (matcher.group(2).equals("b")) {
			style.weight(open ? FontWeight.BOLD : style.getBase().getWeight());
		} else {
			style.italic(open || style.getBase().isItalic());
		}
		return matcher.end() - index;
	}

}
```

### Enregistrement

```java
TextMarkup.register(new TagTextMarkup());   // tous les TextInfo, le dernier enregistré est essayé en premier
TextMarkup.unregister(markup);

TextInfo.create(font, 16).markups(markup);  // uniquement ces balisages pour ce TextInfo
TextInfo.create(font, 16).markups();        // texte brut, aucun balisage
```

Un `TextInfo` suit les balisages enregistrés jusqu'à ce qu'on appelle `markups(...)` dessus. Coupez le balisage pour le texte saisi par les utilisateurs, afin qu'il s'affiche exactement tel quel.

### Codes de formatage d'un hôte

Rien dans JOID ne lit les codes `§`. Un hôte qui en a, comme une intégration Minecraft, enregistre son propre balisage une fois au démarrage :

```java
public final class FormattingTextMarkup implements ITextMarkup {

	private static final ITextEffect UNDERLINE  = new UnderlineTextEffect();
	private static final ITextEffect OBFUSCATED = new ObfuscatedTextEffect();

	private static final String   CODES  = "0123456789abcdef";
	private static final String[] COLORS = {"000000", "0000aa", "00aa00", "00aaaa", "aa0000", "aa00aa", "ffaa00", "aaaaaa", "555555", "5555ff", "55ff55", "55ffff", "ff5555", "ff55ff", "ffff55", "ffffff"};

	@Override
	public int parse(final String text, final int index, final TextStyle style) {
		if (text.charAt(index) != '§' || index + 1 >= text.length()) {
			return 0;
		}

		final char code = Character.toLowerCase(text.charAt(index + 1));
		final int color = FormattingTextMarkup.CODES.indexOf(code);
		if (color >= 0) {
			style.reset().color(Color.decode("#" + FormattingTextMarkup.COLORS[color]));
		} else if (code == 'l') {
			style.weight(FontWeight.BOLD);
		} else if (code == 'o') {
			style.italic(true);
		} else if (code == 'n') {
			style.effect(FormattingTextMarkup.UNDERLINE);
		} else if (code == 'k') {
			style.effect(FormattingTextMarkup.OBFUSCATED);
		} else if (code == 'r') {
			style.reset();
		} else {
			return 0;
		}
		return 2;
	}

}
```

## Effets

Un `ITextEffect` participe au dessin de chaque glyphe auquel il est attaché. Les trois points d'accroche sont optionnels :

```java
public interface ITextEffect {

	public default void apply(ITextGlyph glyph) {}       // avant le dessin : change le caractère, la couleur ou le décalage
	public default void background(ITextGlyph glyph) {}  // dessine derrière le texte
	public default void decorate(ITextGlyph glyph) {}    // dessine par-dessus le texte

}
```

Attachez un effet depuis le balisage avec `style.effect(effect)`, ou à tout le texte avec `TextInfo.effects(effect)`. Un style ne garde chaque effet qu'une fois : partagez une instance par effet plutôt que d'en créer une à chaque code.

L'ordre de dessin est fixe :

1. `apply` s'exécute une fois par glyphe.
2. `background` s'exécute une fois par glyphe, derrière l'ombre et le texte.
3. L'ombre est dessinée à partir des glyphes transformés, puis `decorate` s'exécute sur chacun avec `isShadow()` à vrai.
4. Le texte est dessiné, puis `decorate` s'exécute sur chaque glyphe.

L'ombre réutilise les glyphes déjà modifiés par `apply` : un effet aléatoire ou animé est identique sur le texte et sur son ombre.

### `ITextGlyph`

| Méthode | Valeur |
|---|---|
| `getIndex()` | Position du glyphe dans la chaîne source, balisage compris |
| `getCodepoint()` | Le caractère dessiné |
| `getX()`, `getBaseline()` | Position de la plume et ligne de base, en unités d'UI |
| `getSize()` | Taille de police |
| `getAdvance()` | Distance jusqu'au glyphe suivant, crénage et espacement compris — deux glyphes consécutifs se touchent |
| `getOffsetX()`, `getOffsetY()` | Décalage appliqué au dessin du glyphe |
| `getAscender()`, `getDescender()` | Métriques de la face, en unités d'UI |
| `getUnderlineY()`, `getUnderlineThickness()` | Position et épaisseur du soulignement fournies par la police, en unités d'UI |
| `getColor()`, `getStyle()` | Couleur dessinée et style du glyphe |
| `isShadow()` | `true` pendant le dessin de l'ombre |
| `hasGlyph(int)`, `getAdvance(int)` | Si la face possède un autre caractère, et son avance |
| `codepoint(int)`, `color(Color)`, `offset(double, double)` | Modifications, pour `apply` |

### Exemples

Un soulignement : `drawRect` le dessine comme un trait d'épaisseur constante sur les pixels de la fenêtre, pour qu'il reste net et garde son poids à toutes les échelles :

```java
public final class UnderlineTextEffect implements ITextEffect {

	@Override
	public void decorate(final ITextGlyph glyph) {
		DrawUtils.SHAPE.drawRect(glyph.getX(), glyph.getUnderlineY(), glyph.getAdvance(), glyph.getUnderlineThickness(), glyph.getColor());
	}

}
```

Une vague, qui déplace chaque glyphe sans toucher à la mise en page :

```java
public final class WaveTextEffect implements ITextEffect {

	@Override
	public void apply(final ITextGlyph glyph) {
		glyph.offset(0D, Math.sin(BridgeHandler.CLOCK.get().currentTimeMillis() / 150D + glyph.getIndex()) * glyph.getSize() / 8D);
	}

}
```

Des caractères brouillés qui gardent la largeur de l'original :

```java
public final class ObfuscatedTextEffect implements ITextEffect {

	private static final String POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	@Override
	public void apply(final ITextGlyph glyph) {
		for (int attempt = 0; attempt < 8; attempt++) {
			final char candidate = ObfuscatedTextEffect.POOL.charAt(ThreadLocalRandom.current().nextInt(ObfuscatedTextEffect.POOL.length()));
			if (glyph.hasGlyph(candidate) && glyph.getAdvance(candidate) == glyph.getAdvance(glyph.getCodepoint())) {
				glyph.codepoint(candidate);
				return;
			}
		}
	}

}
```

Un surlignage derrière le texte :

```java
public final class HighlightTextEffect implements ITextEffect {

	@Override
	public void background(final ITextGlyph glyph) {
		DrawUtils.SHAPE.drawRect(glyph.getX(), glyph.getBaseline() - glyph.getAscender(), glyph.getAdvance(), glyph.getAscender() - glyph.getDescender(), Color.YELLOW.copyAlpha(0.4F));
	}

}
```

## Dans la démo

`UIDemoFont` met tout cela en œuvre, à côté des familles et des graisses : `DemoTextMarkup` lit `<b>`, `<i>`, `<u>`, `<h>`, `<w=NNN>` et `<c=RRGGBB>` avec leurs balises fermantes, et le package `demo.ui.font.effect` contient un soulignement, un surlignage, une vague, un arc-en-ciel et un brouillage.

## Mesure

`TextInfo.getWidth` lit le balisage comme le dessin : les caractères consommés ne prennent aucune place et un changement de graisse se mesure avec la face correspondante. Les effets ne modifient jamais la mise en page — décalages et décorations sont purement visuels.

## Voir aussi

- [Custom Fonts](custom-font.md) — charger une famille et choisir une graisse.
- [Texte](../drawing/text.md) — dessiner du texte directement.