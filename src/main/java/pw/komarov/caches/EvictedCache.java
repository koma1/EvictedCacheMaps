package pw.komarov.caches;

import lombok.*;

import java.util.*;

public class EvictedCache<K,V> implements EvictedMap<K,V> {
    public class CacheEntry {
        @Getter private int accessedCount;
        @Getter private long accessedAt;
        @Getter private final long createdAt;
        @Getter private final K object;

        CacheEntry(K object) {
            this.object = object;
            this.createdAt = System.currentTimeMillis();

            increaseUsage();
        }

        private void increaseUsage() {
            ++accessedCount;
            accessedAt = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            if(object == null)
                return "null";

            return object.toString();
        }

        @Override
        public boolean equals(Object o) {
            if(object == null)
                return false;
            if(o.getClass() != object.getClass())
                return false;

            return object.equals(o);
        }

        @Override
        public int hashCode() {
            if(object == null)
                return 0;

            return object.hashCode();
        }
    }

    private HashMap<CacheEntry, V> data;
    @Getter
    private int capacity;

    public void setCapacity(int newCapacity) {
        if(newCapacity < 1)
            throw new IllegalArgumentException("newCapacity must be greather than zero");
        if(newCapacity != this.capacity) {
            int size = data.size();
            if(newCapacity < size) //need to evict items
                evict(size - newCapacity);

            HashMap<CacheEntry, V> oldData = data;
            data = new HashMap<>(newCapacity);
            data.putAll(oldData);

            this.capacity = newCapacity;
        }
    }

    @NonNull
    private EvictionComparator evictionComparator;

    public EvictedCache(int initialCapacity, @NonNull EvictionComparator evictionComparator) {
        if(initialCapacity > 0) {
            this.evictionComparator = evictionComparator;
            this.capacity = initialCapacity;
            data = new HashMap<>(initialCapacity);
        } else
            throw new IllegalArgumentException("initialCapacity must be greather than zero");
    }

    private CacheEntry getEntry(Object o) {
        return data.keySet().stream().filter(entry -> ((o == null && entry.object == null) || (entry.object != null && entry.object.equals(o)) )).findAny().orElse(null);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public int size() {
        return data.size();
    }

    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    public V get(Object key) {
        CacheEntry entry = getEntry(key);

        if(entry != null) {
            entry.increaseUsage();

            return data.get(entry);
        }

        return null;
    }

    public V put(K k, V v) {
        CacheEntry entry = getEntry(k);

        if(entry != null)
            entry.increaseUsage();
        else
            entry = new CacheEntry(k);

        if(data.size() == capacity)
            evict();

        return data.put(entry, v);
    }

    public V remove(Object o) {
        CacheEntry entry = getEntry(o);
        if(entry != null)
            return data.remove(entry);
        else
            return null;
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    public void clear() {
        data.clear();
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        data.forEach((k,v) -> set.add(k.object));

        return set;
    }

    @Override
    public Collection<V> values() {
        return data.values();
    }

    @AllArgsConstructor
    private class Node implements Map.Entry<K,V> {
        private K key;

        private V value;

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V v) {
            return this.value = v;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K,V>> set = new HashSet<>();

        data.forEach((k,v) -> set.add(new Node(k.object, v)));

        return set;
    }

    private int compare(CacheEntry right, CacheEntry left) {
        return compare(right, left, this.evictionComparator);
    }

    private int compare(CacheEntry right, CacheEntry left, EvictionComparator evictionComparator) {
        return evictionComparator.compare(right, left);
    }

    private CacheEntry getEvictedEntry() {
        return data.keySet().stream().min(this::compare).orElse(null);
    }

    @Override
    public void evict() {
        if(!isEmpty())
            data.remove(getEvictedEntry());
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Map)) return false;

        return data.equals(o);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
