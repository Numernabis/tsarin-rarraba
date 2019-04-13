public interface SimpleStringMap {
    boolean containsKey(String key);
    Integer get(String key);
    Integer remove(String key);
    void put(String key, Integer value);
}