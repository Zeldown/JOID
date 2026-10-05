# Assets

Un `Asset` est une **source d'octets paresseuse et nommée**. Il répond à une seule question — *d'où viennent ces octets ?* — et à rien d'autre. Le décodage, l'envoi en texture et le cache se passent au-dessus, dans [Resource](resource-builder.md).

C'est le point d'extension unique pour apprendre à JOID la façon dont votre projet adresse ses fichiers : une `ResourceLocation` Minecraft, une entrée de conteneur de mod, une clé de CDN, une archive chiffrée. Enregistrez un localisateur au démarrage et **tous** les sous-systèmes qui lisent des octets acceptent votre handle — textures, vidéos et polices comprises.

## `Asset`

```java
public abstract class Asset {

    public static @NonNull Asset of(final @NonNull Object handle);

    public abstract @NonNull InputStream open() throws IOException;

    public boolean isReopenable();
    public boolean isRemote();

    public @NonNull byte[] peek(final int length);
    public final @NonNull byte[] read() throws IOException;

    public final @NonNull String getUniqueId();

}
```

- **`open()`** est paresseux et appelé par le thread qui a besoin des octets, jamais à la création.
- **`getUniqueId()`** sert de clé au cache de ressources. Deux handles désignant le même fichier doivent donner le même identifiant ; deux sources distinctes ne doivent pas entrer en collision.
- **`isReopenable()`** ne vaut `false` que pour un `InputStream` déjà ouvert, consommable une seule fois.
- **`isRemote()`** indique à JOID si l'ouverture risque de bloquer sur le réseau. Un asset distant fait choisir son décodeur sur un thread de travail ; un asset local est résolu en ligne.
- **`peek(length)`** lit les premiers octets **sans consommer** l'asset, et renvoie un tableau vide si la source ne s'ouvre pas.

## Assets fournis

| Handle | Asset | Identifiant unique |
|---|---|---|
| `InputStream` | `StreamAsset` | l'identité du flux — non réouvrable, non cachable d'un appel à l'autre |
| `File` | `FileAsset` | le chemin absolu |
| `String` | `UrlAsset` | l'URL elle-même — distant, avec repli HTTPS → HTTP |
| `Asset` | lui-même | inchangé |

Les localisateurs sont enregistrés dans l'initialiseur statique d'`AssetLocator` et occupent le bas du registre : les vôtres passent donc toujours devant.

## Écrire un localisateur

Deux petites classes : l'asset dit comment ouvrir, le localisateur dit quels handles il reconnaît.

```java
public final class ModAsset extends Asset {

    private final ResourceLocation location;

    private ModAsset(final @NonNull ResourceLocation location) {
        super(location.toString());
        this.location = location;
    }

    public static @NonNull ModAsset create(final @NonNull ResourceLocation location) {
        return new ModAsset(location);
    }

    @Override
    public @NonNull InputStream open() throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(this.location).getInputStream();
    }

}
```

```java
public class ModAssetLocator implements IAssetLocator {

    @Override
    public boolean supports(final @NonNull Object handle) {
        return handle instanceof ResourceLocation;
    }

    @Override
    public @NonNull Asset locate(final @NonNull Object handle) {
        return ModAsset.create((ResourceLocation) handle);
    }

}
```

Enregistrez-le une fois, avant le premier chargement :

```java
AssetLocator.register(new ModAssetLocator());
```

Dès lors, le handle fonctionne partout :

```java
Resource.of(new ResourceLocation(MOD_ID, "textures/gui/panel.png"));
MsdfFontLoader.load(new ResourceLocation(MOD_ID, "fonts/Inter/font.msdf")).thenAccept(font -> this.font = font);
```

`register(...)` insère en tête : le registre est parcouru du plus récent au plus ancien, et un localisateur personnalisé bat un localisateur fourni sur un handle que les deux acceptent.

## Pourquoi la paresse compte

Un `Asset` ne porte aucun octet. Rien n'est lu tant qu'un décodeur ne le demande, si bien que le travail retombe sur le thread prévu pour lui :

- Une vidéo de 200 Mo est recopiée dans son fichier temporaire depuis `decode()`, sur le worker de ressources, et non au moment de `Resource.of(...)`.
- Un atlas de police est lu sur le pool du chargeur de polices, et un échec complète son `CompletableFuture` exceptionnellement au lieu d'être jeté à l'appelant.
- Un asset distant n'ouvre une connexion qu'au moment où son décodeur est choisi, sur un thread de travail.

## Voir aussi

- [ResourceBuilder](resource-builder.md) — cache et pipeline texture au-dessus des assets.
- [Résolveurs](resolvers.md) — les entrées déjà décodées.
- [Décodeurs](decoders.md) — ce qui transforme les octets en texture.