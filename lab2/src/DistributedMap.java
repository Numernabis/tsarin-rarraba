import java.util.Map;

public class DistributedMap implements SimpleStringMap {

    private Map<String, Integer> map;

    public DistributedMap(Map<String, Integer> distributedMap) {
        this.map = distributedMap;
    }

    public Map<String, Integer> getMap() {
        return map;
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public Integer get(String key) {
        return map.get(key);
    }

    @Override
    public Integer remove(String key) {
        return map.remove(key);
    }

    @Override
    public void put(String key, Integer value) {
        map.put(key, value);
    }
}