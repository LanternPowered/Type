package org.lanternpowered.jtype;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

final class WeakKeyConcurrentCache<K, V> {

  private final ReferenceQueue<K> keyReferenceQueue = new ReferenceQueue<>();
  private final Map<KeyRef<K>, V> map = new ConcurrentHashMap<>();
  private final Function<K, V> loadFunction;

  public WeakKeyConcurrentCache(Function<K, V> loadFunction) {
    requireNonNull(loadFunction, "loadFunction");
    this.loadFunction = loadFunction;
  }

  public V get(K key) {
    requireNonNull(key, "key");
    // Purge expired refs
    Reference<? extends K> reference;
    while ((reference = keyReferenceQueue.poll()) != null) {
      //noinspection unchecked
      map.remove((KeyRef<K>) reference);
    }
    var keyRef = new KeyRef<>(key, keyReferenceQueue);
    return map.computeIfAbsent(keyRef, k -> loadFunction.apply(key));
  }

  private static final class KeyRef<K> extends WeakReference<K> {

    private final int hash;

    KeyRef(K referent, ReferenceQueue<K> referenceQueue) {
      super(referent, referenceQueue);
      hash = referent.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj || (obj instanceof KeyRef && Objects.equals(((KeyRef<?>) obj).get(), get()));
    }

    @Override
    public int hashCode() {
      return hash;
    }
  }
}
