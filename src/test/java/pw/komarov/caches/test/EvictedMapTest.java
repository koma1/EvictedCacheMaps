package pw.komarov.caches.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opentest4j.AssertionFailedError;
import pw.komarov.caches.EvictedMap;

class EvictedMapTest {
    @Test
    void capacityTest() {
        int CAPACITY = 15;

        //initial capacity test
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(CAPACITY, (l,r) -> 0);
        assertEquals(CAPACITY, cache.getCapacity());
        for (int i = 1; i <= CAPACITY + 100; i++)
            cache.put(i, i);
        assertEquals(CAPACITY, cache.size());

        //increased capacity test
        cache.setCapacity(CAPACITY*=2);
        for (int i = 1; i <= CAPACITY + 100; i++)
            cache.put(i, i);
        assertEquals(CAPACITY, cache.size());
        assertEquals(CAPACITY, cache.getCapacity());

        //trimmed capacity test
        cache.setCapacity(CAPACITY/=2);
        assertEquals(CAPACITY, cache.size());
        assertEquals(CAPACITY, cache.getCapacity());
    }

    @Test
    void sizeTest() {
        final int SIZE = 10;

        EvictedMap<Integer,Integer> cache = new EvictedMap<>(SIZE + 1, (l,r) -> 0);
        for (int i = 1; i <= SIZE; i++)
            cache.put(i, i);

        assertEquals(SIZE, cache.size());
    }

    @Test
    void replaceValueTest() {
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(10, (l,r) -> 0);
        cache.put(1,1);
        cache.put(2,4);
        cache.put(3,6);
        assertEquals(cache.size(), 3);
            assertEquals(cache.get(1), 1);
            assertEquals(cache.get(2), 4);
            assertEquals(cache.get(3), 6);

        cache.put(2, 6);
        assertEquals(cache.size(), 3);
            assertEquals(cache.get(1), 1);
            assertEquals(cache.get(2), 6);
            assertEquals(cache.get(3), 6);
    }

    @Test
    void containsTest() {
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(10, (l,r) -> 0);
        cache.put(5,15);
        cache.put(10,35);
        cache.put(12,66);

        assertFalse(cache.containsKey(1));
        assertFalse(cache.containsValue(1));

        assertTrue(cache.containsKey(5));
        assertTrue(cache.containsValue(15));
        assertTrue(cache.containsKey(10));
        assertTrue(cache.containsValue(35));
        assertTrue(cache.containsKey(12));
        assertTrue(cache.containsValue(66));
    }

    @Test
    void removeTest() {
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(10, (l,r) -> 0);
        cache.put(100, 200);
        cache.put(300, 400);
        cache.put(500, 600);
        cache.put(700, 800);
        cache.put(900, 1000);

        assertEquals(cache.size(), 5);
        cache.remove(50);
        assertEquals(cache.size(), 5);
        cache.remove(100);
        assertEquals(cache.size(), 4);
        cache.remove(400);
        assertEquals(cache.size(), 4);
        cache.remove(300);
        assertEquals(cache.size(), 3);
        cache.remove(800);
        assertEquals(cache.size(), 3);
        cache.remove(900);
        assertEquals(cache.size(), 2);
    }

    @Test
    void putAllTest() {
        Map<Integer,Integer> map = new HashMap<>();
        map.put(1,2);
        map.put(3,4);
        map.put(5,6);
        map.put(7,8);
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(map.size(), (l,r) -> 0);
        cache.putAll(map);

        assertEquals(map, cache);
    }

    @Test
    void clearTest() {
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(10, (l,r) -> 0);
        cache.put(1,2);
        cache.put(3,4);
        cache.put(5,6);
        cache.put(7,8);

        assertEquals(cache.size(), 4);
        cache.clear();
        assertEquals(cache.size(), 0);
    }

    @Test
    void entriesTest() {
        Map<Integer,Integer> map = new HashMap<>();
        map.put(1,2);
        map.put(3,4);
        map.put(5,6);
        map.put(7,8);
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(map.size() + 5, (l,r) -> 0);
        cache.putAll(map);

        assertEqualsItems(map.entrySet(), cache.entrySet());
        map.put(9, 10);
        assertNotEqualsItems(map.entrySet(), cache.entrySet());
        cache.put(9,10);
        assertEqualsItems(map.entrySet(), cache.entrySet());

        assertEquals(map.keySet(), cache.keySet());
        assertEquals(cache.keySet(), map.keySet());

        assertEqualsItems(cache.keySet(), map.keySet());
        assertEqualsItems(map.values(), cache.values());
    }

    @Test
    void equalsAndHashCodeTest() {
        Map<Integer,Integer> map = new HashMap<>();
        map.put(1,2);
        map.put(3,4);
        map.put(5,6);
        map.put(7,8);
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(map.size(), (l,r) -> 0);
        cache.putAll(map);

        assertEquals(map.hashCode(), cache.hashCode());

        assertEquals(map, cache);
        assertEquals(cache, map);
    }

    @Test
    void toStringTest() {
        EvictedMap<Integer,String> cache = new EvictedMap<>(1, (l,r) -> 0);
        cache.put(5, "String value for five");
        assertEquals(cache.toString(), "{5=String value for five}");
    }

    @Test
    void evictTest() {
        EvictedMap<Integer,String> cache = new EvictedMap<>(1, (l,r) -> 0);
        cache.put(5, "String value for five");
        assertEquals(cache.toString(), "{5=String value for five}");
    }

    @Test
    void nullAsKeyTest() {
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(2, (l,r) -> 0);

        cache.put(1, 7);
        assertEquals(cache.size(), 1);
        assertFalse(cache.containsValue(5));
        assertFalse(cache.containsKey(null));
        assertNotEquals(cache.get(null), 5);

        cache.put(null, 5);
        assertEquals(cache.size(), 2);
        assertTrue(cache.containsValue(5));
        assertTrue(cache.containsKey(null));
        assertEquals(cache.get(null), 5);
    }

    @Test
    void nullAsValueTest() {
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(2, (l,r) -> 0);
        cache.put(5, null);
        assertEquals(cache.size(), 1);
        assertTrue(cache.containsKey(5));
        assertTrue(cache.containsValue(null));
        assertNull(cache.get(5));
    }

    @Test
    void nullAsKeyAndValueTest() {
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(2, (l,r) -> 0);
        cache.put(5, null);
        cache.put(null, 5);
        cache.put(null, null);
        assertEquals(cache.size(), 2);
        assertNull(cache.get(null));
    }

    private void assertEqualsItems(Collection expected, Collection actual) {
        int expectedSize = expected.size();
        int actualSize   = actual.size();
        if(expectedSize != actualSize)
            assertFail(expected, actual);

        for(Object actualObject : actual)
            if(!expected.contains(actualObject))
                assertFail(expected, actual);
    }

    private void assertNotEqualsItems(Collection expected, Collection actual) {
        assertThrows(AssertionFailedError.class, () -> assertEqualsItems(expected, actual),
                "expected: not equal but was: <" + actual + ">");
    }

    private void assertFail(Object expected, Object actual) {
        throw new AssertionFailedError("expected: %s but was: %s", expected, actual);
    }



    @Test
    void suppress() {
        EvictedMap<Integer,Integer> cache = new EvictedMap<>(2, (l,r) -> (int)(l.getObject() == null ? 0 : 1 + r.getAccessedCount() + l.getAccessedAt() + l.getCreatedAt()));
        cache.put(1,1);
        assertEquals(cache.get(1), 1);
    }

}