package de.unistuttgart.dsass2025.ex10.p5;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Realizes a basic hash map with closed hashing and quadratic probing. It uses an array of
 * {@link KeyValuePair} as basic data structure.
 *
 * @param <V> the value type of the key-value-pairs in the HashMap
 */
public class ClosedHashMap<V> implements Iterable<KeyValuePair<V>> {

    public static final int DEFAULT_SIZE = 23;
    public static final float resizeThreshold = 0.8f;
    public KeyValuePair<V> map[];

    /**
     * Initializes a ClosedHashMap with size <code>DEFAULT_SIZE</code>
     * @throws IllegalArgumentException
     */
    public ClosedHashMap() throws IllegalArgumentException {
        this(DEFAULT_SIZE);
    }

    /**
     * Initializes a ClosedHashMap with the given size.
     * The size must be a prime number congruent to 3 modulo 4.
     *
     * @param size the size of the map
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public ClosedHashMap(int size) throws IllegalArgumentException {
        if (size < 1)
            throw new IllegalArgumentException(
                    "initialSize must be be greater than 0");

        if (!isPrime(size) || size % 4 != 3) {
            throw new IllegalArgumentException("size must be a prime congruent 3 mod 4");
        }

        this.map = new KeyValuePair[size];
    }

    /**
     * Adds a value with the given key to the hash map. Returns the previous value if the hash map
     * contained one with the same key.
     *
     * If, after adding, the underlying array is more than 80% full, the map is rehashed into a
     * bigger array. The new array size is the smallest number with the following properties:
     *
     * * must be at least twice the old size
     * * must be prime number
     * * must be congruent to 3 modulo 4
     *
     * Quadratic probing is used to determine the index where the key-value-pair is stored.
     *
     * @param key key
     * @param value new value
     * @return previous value if present, otherwise null
     */
    public V put(int key, V value) throws IllegalStateException {
        for(int i=0; i<this.map.length; i++){
            int h = key % this.map.length;
            int index = h + (int) (Math.pow(-1, i-1)) * (int) Math.pow(i,2);
            if(this.map[index] == null){
                this.map[index] = new KeyValuePair<>(key, value);
                break;
            }else{
                if (this.map[index].getKey() == key) {
                    return this.map[index].getValue();
                }
            }
        }
        Iterator<KeyValuePair<V>> iterator = this.iterator();
        int i = 0;
        while(iterator.hasNext()){
            i+=1;
            iterator.next();
        }
        if((float) i /this.map.length > resizeThreshold){
            int j = this.map.length*2;
            // is there a more efficient way to do this?
            while(!(j%4==3) && isPrime(j)){
                j+=1;
            }
            KeyValuePair<V>[] newMap = new KeyValuePair[j];

            for(KeyValuePair<V> pair: this.map){
                for(int k=0; k<newMap.length; k++) {
                    int h = pair.getKey() % newMap.length;
                    int index = h + (int) (Math.pow(-1, k - 1)) * (int) Math.pow(k, 2);
                    if (newMap[index] == null) {
                        newMap[index] = pair;
                    }
                }
            }

            this.map=newMap;
        }
        return null;
    }

    /**
     * Tests whether the hash map contains a given key or not.
     *
     * @param key given key
     * @return whether the hash map contains a given key or not
     */
    public boolean containsKey(int key) {
        return get(key) != null;
    }

    /**
     * Returns the value of a specified key if present, otherwise null.
     *
     * @param key current key
     * @return the value or null
     */
    public V get(int key) {
        int h = key% this.map.length;
        for(int i = 0; i<map.length; i++){
            int index = h + (int) (Math.pow(-1, i-1)) * (int) Math.pow(i,2);
            if(key==this.map[index].getKey()){
                return this.map[index].getValue();
            }
        }
        return null;

    }

    private boolean isPrime(int p) {
        for(int i = 2; i < p; i++) {
            if (p % i == 0) {
                return false;
            }
        }
        return true;
    }

    public Iterator<KeyValuePair<V>> iterator() {
        return new Iterator<KeyValuePair<V>>() {

            int index = 0;

            @Override
            public boolean hasNext() {
                boolean result = false;
                int i = index;
                while (i < map.length && !result) {
                    result = (map[i] != null);
                    i++;
                }
                return result;
            }

            @Override
            public KeyValuePair<V> next() throws NoSuchElementException {
                int i = index;
                KeyValuePair<V> result = null;
                while (i < map.length && map[i] == null) {
                    index++;
                    i = index;
                }
                if (index >= map.length) {
                    throw new NoSuchElementException("No such element!");
                }
                result = map[i];
                index++;
                return result;
            }
        };
    }
}