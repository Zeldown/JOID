# Signals

`Signal<T>` est le conteneur observable de JOID. On set une valeur, les subscribers se déclenchent. Reliez des nœuds à des signaux pour une UI réactive.

## Signal basique

```java
final Signal<String> name = new Signal<>("world");

name.subscribe(v -> {
    System.out.println("Name: " + v);
    return true;        // return false pour auto-unsubscribe
});

name.set("JOID");       // affiche : Name: JOID
```

`SignalSubscriber<T>` est une `@FunctionalInterface` à une seule méthode `boolean update(T value)`. Retourner `false` depuis la lambda retire le subscriber automatiquement après l'appel.

## Variantes primitives et string

Les signaux spécialisés évitent le boxing :

```java
new IntegerSignal(0);
new LongSignal(0L);
new FloatSignal(0F);
new DoubleSignal(0D);
new BooleanSignal(false);
new StringSignal("");
```

Elles étendent toutes `Signal<T>` avec le type boxé correspondant. Pas de `.set(primitive)` spécialisé — utilisez `.set(Integer.valueOf(0))` ou l'auto-boxing `int` fourni par `Integer`.

## Signaux iterable

```java
new ListSignal<Item>();                                     // backed par List<T>
new MapSignal<K, V>();                                       // backed par Map<K, V>
new SetSignal<T>();                                          // backed par Set<T>
```

Les signaux iterable enveloppent la collection et publient à chaque mutation. Surface de `ListSignal` :

```java
boolean add(E e)
boolean remove(E e)
E remove(int index)
E get(int index)
E set(int index, E element)
int indexOf(E e)
boolean contains(E e)
int size()
boolean isEmpty()
ListSignal<E> clear()
```

Chaque écriture appelle `publish()` en interne.

## Lecture

```java
String current = name.getOrDefault();                        // retourne la valeur, ou le défaut si null
boolean hasValue = name.isPresent();                          // value != null
```

Il n'y a pas de `signal.get()` — lisez via `getOrDefault()`.

## Subscribe / unsubscribe

```java
final SignalSubscriber<String> sub = v -> { react(v); return true; };
name.subscribe(sub);
// plus tard
name.unsubscribe(sub);
```

Gardez une référence de la lambda si vous voulez désinscrire plus tard. Les subscribers qui retournent `false` depuis `update(...)` sont retirés automatiquement.

La plupart du temps vous ne gérez pas les souscriptions à la main — utilisez `node.watch(signal)` et laissez le nœud nettoyer au detach.

## Updates silencieux

Skip le prochain `publish()` avec `silent()` :

```java
name.silent().set("loaded from disk");
```

Le flag ne s'applique qu'au `set(...)` immédiatement suivant — il n'est pas sticky. Utilisé par `UIStore` pendant l'hydratation JSON pour que les signaux stockés ne déclenchent pas de reloads au démarrage.

## Factories statiques

```java
Signal<T> Signal.of(T defaultValue)
Signal<T> Signal.of(CompletionStage<T> future)
```

La variante `CompletionStage` souscrit au future et set la valeur quand il complète.

## Autres opérations

```java
signal.reset();                                              // set(defaultValue), publish si différent
signal.publish();                                            // re-déclenche les subscribers avec la valeur courante
signal.getEventSet();                                         // le set interne de subscribers
```

## Lier aux nœuds

```java
final IntegerSignal count = new IntegerSignal(0);

TextNode.create(0, 0)
    .text(Text.create(() -> "Count: " + count.getOrDefault(), info))
    .watch(count)
    .attach(parent);
```

Voir [Watch](watch.md) pour toutes les options de liaison.

## Signaux calculés

Combinez des signaux en valeurs dérivées en souscrivant l'un aux autres :

```java
final IntegerSignal a = new IntegerSignal(3);
final IntegerSignal b = new IntegerSignal(4);
final IntegerSignal sum = new IntegerSignal(a.getOrDefault() + b.getOrDefault());

a.subscribe(v -> { sum.set(v + b.getOrDefault()); return true; });
b.subscribe(v -> { sum.set(a.getOrDefault() + v); return true; });
```

Il n'y a pas de helper `computed(...)` intégré — câblez-le vous-même si besoin.

## Thread safety

`Signal<T>` utilise un `HashSet` brut pour le set de subscribers et n'est pas synchronisé en interne — si vous publiez depuis plusieurs threads, le locking est à votre charge. Les subscribers se déclenchent synchrone sur le thread qui appelle `set` / `publish` ; les mutations d'UI depuis des threads workers doivent repasser sur le thread de rendu via `ui.schedule(...)`.

## Bonnes pratiques

- **Stockez les signaux sur l'UI ou un state holder partagé** — le cycle de vie correspond au propriétaire.
- **Évitez les graphes de signaux profonds.** 2–3 sauts c'est OK ; au-delà, refactorez.
- **Debounce les sets haute fréquence.** `onMouseDragged` se déclenche à chaque frame ; ne `set` pas un signal par frame si le consumer n'a besoin que de la valeur finale.

## Voir aussi

- [Watch](watch.md) — liaison signal au niveau du nœud.
- [Stores](stores.md) — signaux persistants.