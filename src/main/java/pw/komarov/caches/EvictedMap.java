package pw.komarov.caches;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EvictedMap<K,V> implements EvictedMapInterface<K,V> {
    public static class CacheEntry<O> {
        private int accessedCount;
        private long accessedAt;
        private final long createdAt;

        public int getAccessedCount() {
            return accessedCount;
        }

        public long getAccessedAt() {
            return accessedAt;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public O getObject() {
            return object;
        }

        private final O object;

        CacheEntry(O object) {
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

    private int capacity;

    public int getCapacity() {
        return capacity;
    }

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

    private EvictionComparator<K> evictionComparator;

    public EvictedMap(int initialCapacity, EvictionComparator<K> evictionComparator) {
        if(evictionComparator == null)
            throw new IllegalArgumentException("evictionComparator must be not null");
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

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    @Override
    public V get(Object key) {
        CacheEntry entry = getEntry(key);

        if(entry != null) {
            entry.increaseUsage();

            return data.get(entry);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
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

    @Override
    public V remove(Object o) {
        CacheEntry entry = getEntry(o);
        if(entry != null)
            return data.remove(entry);
        else
            return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        data.forEach((k,v) -> set.add((K)k.object));

        return set;
    }

    @Override
    public Collection<V> values() {
        return data.values();
    }

    private class Node implements Map.Entry<K,V> {
        private final K key;

        private V value;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

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

    @SuppressWarnings("unchecked")
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K,V>> set = new HashSet<>();

        data.forEach((k,v) -> set.add(new Node((K)k.object, v)));

        return set;
    }

    private int compare(CacheEntry<K> right, CacheEntry<K> left) {
        return compare(right, left, this.evictionComparator);
    }

    private int compare(CacheEntry<K> right, CacheEntry<K> left, EvictionComparator<K> evictionComparator) {
        return evictionComparator.compare(right, left);
    }

    private CacheEntry<?> getEvictedEntry() {
        Object cacheEntry = data.keySet().stream().min(this::compare).orElse(null);
        return (CacheEntry)cacheEntry;
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
