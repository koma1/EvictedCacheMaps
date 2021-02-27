package pw.komarov.caches;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EvictedCache<K,V> implements Map<K,V> {
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    class CacheEntry {
        @Getter int accessedCount;
        @Getter long accessedAt;
        @Getter final long createdAt;
        @EqualsAndHashCode.Include final K object;

        CacheEntry(K object) {
            this.object = object;
            this.createdAt = System.currentTimeMillis();

            increaseUsage();
        }

        private void increaseUsage() {
            ++accessedCount;
            accessedAt = System.currentTimeMillis();
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
        return data.keySet().stream().filter(entry -> (entry.object.equals(o))).findAny().orElse(null);
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
            evictItem();

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

    public V getEvicted() {
        if(!isEmpty())
            return data.get(getEvictedEntry());

        return null;
    }

    public void evict(int count) {
        for(int i = 1; i <= count; i++) {
            evictItem();
        }
    }

    public void evictItem() {
        if(!isEmpty())
            data.remove(getEvictedEntry());
    }
}
