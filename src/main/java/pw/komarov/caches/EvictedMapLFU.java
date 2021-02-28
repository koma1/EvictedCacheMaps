package pw.komarov.caches;

public class EvictedMapLFU<K, V> extends EvictedMap<K,V> {
    private static class ComparatorLFU implements EvictionComparator {
        @Override
        public int compare(EvictedMap.CacheEntry right, EvictedMap.CacheEntry left) {
            return right.getAccessedCount() - left.getAccessedCount();
        }
    }

    public EvictedMapLFU(int initialCapacity) {
        super(initialCapacity, new ComparatorLFU());
    }
}
