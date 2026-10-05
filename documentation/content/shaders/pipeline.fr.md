# Shader Pipeline

Le pipeline de shaders de JOID compose des effets GPU multi-passes par-dessus la sortie rendue d'un nœud. Il gère le pool de framebuffers, la projection, le blending et l'expansion imbriquée — il vous suffit de fournir une liste de `ShaderPass`.

## La classe `ShaderPipeline`

```java
ShaderPipeline.render(node, passes, baseDraw);
```

- `node` — le nœud en cours de rendu (fournit position et taille).
- `passes` — `List<ShaderPass>` — triés par priorité, exécutés dans l'ordre.
- `baseDraw` — un `Runnable` qui peint le contenu brut (la géométrie du nœud).

Formes alternatives quand vous n'avez pas de `Node` :

```java
ShaderPipeline.render(x, y, width, height, passes, baseDraw);
ShaderPipeline.render(x, y, width, height, baseDraw, pass1, pass2, ...);
```

## Fonctionnement

1. **Tri des passes par `priority()`** — plus bas en premier.
2. **Voie rapide** — s'il y a exactement 1 pass, pas d'expansion, et `supportsDirectBind()` est `true`, bind le shader directement et draw. Pas de FBO.
3. **Voie multi-passe** :
   - Rend `baseDraw` dans FBO A, dimensionné pour le nœud + expansion max au pixel scale de la transformation courante — un texel par pixel écran, zoom et échelle d'interface compris.
   - Pour chaque passe sauf la dernière : bind le shader de la passe → blit FBO A sur FBO B avec ce shader → swap A et B.
   - Bind la dernière passe → blit sur l'écran → unbind.
4. Le pool FBO est indexé par `(pipelineDepth, width, height)` pour que les appels imbriqués aient des framebuffers séparés.

## Écrire une passe

Une passe implémente `ShaderPass` :

```java
public class MyShaderPass implements ShaderPass {

    @Override
    public void bindDirect(@NonNull ShaderPassContext context) {
        // Bind votre shader pour la voie simple (pas de texture intermédiaire).
    }

    @Override
    public void bindForTexture(@NonNull ShaderPassContext context) {
        // Bind votre shader en sachant que l'entrée sera la texture du FBO précédent.
    }

    @Override
    public void unbind() {
        MyShader.inst().unbind();
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public float expansion() {
        return 0F;  // ou `radius` pour blur, `width` pour border
    }

    @Override
    public boolean supportsDirectBind() {
        return true;  // false = force la voie FBO (nécessaire si baseDraw change de shader, comme le texte)
    }
}
```

Le pipeline transmet à chaque passe un `ShaderPassContext`, calculé une fois par dessin : la zone dessinée (`getX`, `getY`, `getWidth`, `getHeight`), la marge `getExpansion()` autour, la grille de pixels `getGrid()` de la transformation courante, la région couverte par la texture (`getRegionX`, `getRegionY`, `getRegionWidth`, `getRegionHeight`) — la zone étendue agrandie jusqu'aux pixels entiers de la fenêtre, pour que le résultat soit recopié pixel pour pixel — et la taille de la texture en pixels avec son texel (`getTexelWidth`, `getTexelHeight`). Convertissez les tailles logiques en pixels avec la grille — un rayon de `r` unités fait `r * context.getGrid().getScaleX()` pixels — sans jamais mesurer vous-même la fenêtre ou le nœud.

Les textures du pipeline contiennent des couleurs prémultipliées : `bindForTexture` lit une texture prémultipliée et son shader doit écrire une couleur prémultipliée (`vec4(rgb * a, a)`). Le pipeline compose chaque résultat avec `BlendState.PREMULTIPLIED`, pour que les bords semi-transparents gardent leur couleur au lieu de s'assombrir.

## Cheat-sheet de priorités

Passes intégrées :

| Pass | Priorité |
|---|---|
| `RoundedShaderPass` | 100 |
| `CircleShaderPass` | 100 |
| `BlurShaderPass` (horizontal) | 150, 152, … |
| `BlurShaderPass` (vertical) | 151, 153, … |
| `BorderShaderPass` | 200 |

Choisissez des priorités respectant cet ordre pour des passes custom :
- Masquage de forme en premier.
- Blur après.
- Contour en dernier.

> Les remplissages en gradient ne sont plus une passe séparée — `Color` bind le shader gradient nativement quand on l'utilise dans `RectNode.color(...)`, `BorderNodeEffect`, ou `TextInfo` (voir [Color](../drawing/color.md)).

## Expansion

Quand une passe comme `BlurShaderPass` ou `BorderShaderPass` retourne `expansion() > 0`, le FBO est agrandi de ce montant sur tous les côtés. Ça empêche le clipping près des bords du nœud.

## Cleanup

À appeler à la fermeture de l'app :

```java
ShaderPipeline.cleanup();
```

Libère les framebuffers du pool. Le shutdown hook de `VideoAudioPlayer` se déclenche aussi ; câblez le vôtre pour le pipeline de shaders si vous embarquez JOID dans un hôte de longue durée.

## Bonnes pratiques

- **Ne vous battez pas avec le système.** `effect(...)` sur un nœud construit la liste de passes automatiquement ; utilisez-le avant de tirer vers `ShaderPipeline.render(x, y, w, h, ...)` directement.
- **`supportsDirectBind() = false`** est un must pour les passes qui doivent lire le framebuffer (par ex. gradients sur du texte).
- **Gardez l'expansion serrée.** Trop d'expansion gaspille des pixels.

## Voir aussi

- [Effects Overview](../effects/overview.md).
- [Custom Shaders](custom.md) — écrire votre propre passe.