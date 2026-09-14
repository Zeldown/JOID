# Custom Shaders

Écrire vos propres shaders et les câbler dans le pipeline.

## Le flow

1. Écrivez vos fichiers `.vsh` / `.fsh` en GLSL JOID et placez-les dans `/assets/shaders/<name>/`.
2. Étendez `ShaderImpl` pour charger et exposer les uniforms.
3. Implémentez un `ShaderPass` qui bind le shader.
4. Optionnellement, enveloppez dans un `NodeEffect` pour un usage ergonomique.

## 1. Fichiers shader

Les shaders s'écrivent une seule fois, en GLSL JOID, et chaque backend les traduit au chargement — GLSL 120 sur LWJGL 2, GLSL 330 sur LWJGL 3, GLSL 450 Vulkan sur Vulkan. Le GLSL JOID est du GLSL classique sans les parties propres au moteur :

- Pas de ligne `#version`.
- Les attributs de sommet, les matrices et la sortie du fragment sont intégrés — utilisez-les sans les déclarer.
- Déclarez les varyings avec `out` dans le vertex shader et `in` dans le fragment shader, un par ligne. `flat` est autorisé.
- Déclarez les uniforms et les samplers avec des lignes `uniform`, un par ligne.
- Échantillonnez les textures avec `texture(...)` et écrivez la couleur finale dans `fragColor`.

| Intégré | Type | Étape |
|---|---|---|
| `aPosition` | `vec3` | vertex |
| `aTexCoord` | `vec2` | vertex |
| `aColor` | `vec4` | vertex |
| `aNormal` | `vec3` | vertex |
| `uProjectionMatrix` | `mat4` | vertex, fragment |
| `uModelViewMatrix` | `mat4` | vertex, fragment |
| `uNormalMatrix` | `mat3` | vertex, fragment |
| `uLighting` | `bool` | vertex, fragment |
| `fragColor` | `vec4` | fragment |

Tant que le backend LWJGL 2 est supporté, restez dans les fonctionnalités communes au GLSL 120 et aux versions récentes : pas d'opérateurs bit à bit, de `uint`, de `switch` ni de blocs d'uniforms.

**`/assets/shaders/outline/outline.vsh`** :
```glsl
out vec2 vTexCoord;

void main() {
    vTexCoord = aTexCoord;
    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}
```

**`/assets/shaders/outline/outline.fsh`** :
```glsl
in vec2 vTexCoord;

uniform sampler2D tex;
uniform vec4 u_OutlineColor;
uniform float u_Thickness;
uniform vec2 u_TexelSize;

void main() {
    vec4 color = texture(tex, vTexCoord);
    float alpha = 0.0;
    for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            vec2 offset = vec2(i, j) * u_TexelSize * u_Thickness;
            alpha = max(alpha, texture(tex, vTexCoord + offset).a);
        }
    }
    fragColor = color.a > 0.5 ? color : vec4(u_OutlineColor.rgb, alpha * u_OutlineColor.a);
}
```

Les erreurs de compilation indiquent les numéros de ligne de votre propre fichier. Le côté Java est identique sur tous les backends : les uniforms sont récupérés par nom.

## 2. Classe shader

```java
package your.package.shader;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.shader.impl.ShaderImpl;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatUniform;
import lombok.NonNull;

public class OutlineShader extends ShaderImpl {

    private static final OutlineShader INSTANCE = new OutlineShader();

    private OutlineShader() {
        this.load(
            JOID.class.getResourceAsStream("/assets/shaders/outline/outline.vsh"),
            JOID.class.getResourceAsStream("/assets/shaders/outline/outline.fsh")
        );
    }

    public void bind(final float thickness, final @NonNull Color color, final float texelW, final float texelH) {
        OutlineShader.INSTANCE.bind();

        final FloatUniform thick = INSTANCE.shader.getFloatUniform("u_Thickness");
        thick.setValue(thickness);

        final Float4Uniform col = INSTANCE.shader.getFloat4Uniform("u_OutlineColor");
        col.setValue(color.r, color.g, color.b, color.a);

        final Float2Uniform tex = INSTANCE.shader.getFloat2Uniform("u_TexelSize");
        tex.setValue(texelW, texelH);
    }

    public static @NonNull OutlineShader inst() {
        return OutlineShader.INSTANCE;
    }
}
```

## 3. Shader pass

```java
public class OutlineShaderPass implements ShaderPass {

    private final float thickness;
    private final Color color;

    public OutlineShaderPass(final float thickness, final @NonNull Color color) {
        this.thickness = thickness;
        this.color = color;
    }

    @Override
    public void bindDirect(final Node node) {
        this.bindInternal(node);
    }

    @Override
    public void bindForTexture(final Node node) {
        this.bindInternal(node);
    }

    @Override
    public void unbind() {
        OutlineShader.inst().unbind();
    }

    @Override
    public int priority() {
        return 180;  // avant border, après blur
    }

    @Override
    public float expansion() {
        return this.thickness;
    }

    @Override
    public boolean supportsDirectBind() {
        return false;  // on échantillonne le framebuffer
    }

    private void bindInternal(final Node node) {
        if (!OutlineShader.inst().isAvailable()) return;

        final int scaleFactor = ShaderPipeline.scaleFactor(node != null ? node.getUi() : null);
        final double w = node != null ? node.getWidth() : 200D;
        final double h = node != null ? node.getHeight() : 120D;
        final float pixelW = (float) Math.ceil((w + this.thickness * 2F) * scaleFactor);
        final float pixelH = (float) Math.ceil((h + this.thickness * 2F) * scaleFactor);
        final float texelW = 1F / Math.max(1F, pixelW);
        final float texelH = 1F / Math.max(1F, pixelH);

        OutlineShader.inst().bind(this.thickness * scaleFactor, this.color, texelW, texelH);
    }
}
```

## 4. Effet de nœud (optionnel)

```java
@Getter
@SuppressWarnings("unchecked")
public class OutlineNodeEffect<T extends Node> extends NodeEffect<T> {

    private Supplier<Color> colorSupplier;
    private Supplier<Float> thicknessSupplier;

    private OutlineNodeEffect(final @NonNull Color color, final float thickness) {
        this.colorSupplier = () -> color;
        this.thicknessSupplier = () -> thickness;
    }

    public static <T extends Node> @NonNull OutlineNodeEffect<T> create(final @NonNull Color color, final float thickness) {
        return new OutlineNodeEffect<>(color, thickness);
    }

    @Override
    public boolean isShaderEffect() {
        return true;
    }

    @Override
    public ShaderPass toShaderPass(final @NonNull T node) {
        return new OutlineShaderPass(this.thicknessSupplier.get(), this.colorSupplier.get());
    }
}
```

## Utilisation

```java
node.effect(OutlineNodeEffect.create(Color.WHITE, 2F));
```

Voilà — votre effet custom se compose maintenant avec tous les intégrés.

## Bonnes pratiques

- **Utilisez `BooleanUniform` / `IntUniform` / `FloatUniform` / `Float2Uniform` / etc.** depuis `lib/bridge/render/shader/uniform` plutôt que des appels moteur.
- **Ne fuitez pas les lookups d'uniform.** Le shader les met en cache ; des `getXUniform(name)` répétés sont peu coûteux.
- **Testez à plusieurs scale factors.** Le pipeline supersample sur les écrans high-DPI — assurez-vous que votre `u_TexelSize` en tient compte.
- **Respectez le contrat `supportsDirectBind()`.** Si votre shader doit lire le framebuffer précédent, retournez `false`.

## Voir aussi

- [Shader Pipeline](pipeline.md) — l'orchestrateur.
- [Effects Overview](../effects/overview.md).