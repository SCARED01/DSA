package de.unistuttgart.dsass2025.ex10.p5;

/**
 * Key-value-pairs are the entries in a hash map. The key is expected to be an integer and value is
 * of generic type V.
 *
 * @param <V> the value type of the key-value-pairs in the hash map
 */
public final class KeyValuePair<V> {

    private final int key;
    private final V value;

    /**
     * Creates a new key value pair.
     *
     * @param key   key for the key value pair; cannot be null
     * @param value the value associated with the given key
     */
    public KeyValuePair(int key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key of the pair.
     *
     * @return the key
     */
    public int getKey() {
        return this.key;
    }

    /**
     * Returns the value of the pair.
     *
     * @return the value
     */
    public V getValue() {
        return this.value;
    }
}