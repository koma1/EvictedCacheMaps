package pw.komarov.caches;

public class EvictedMapLRU<K, V> extends EvictedMap<K,V> {
    private static class ComparatorLRU implements EvictionComparator {
        @Override
        public int compare(EvictedMap.CacheEntry right, EvictedMap.CacheEntry left) {
            return Long.compare(right.getAccessedAt(), left.getAccessedAt());
        }
    }

    public EvictedMapLRU(int initialCapacity) {
        super(initialCapacity, new ComparatorLRU());
    }
}
