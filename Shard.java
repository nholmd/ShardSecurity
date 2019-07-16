package SecretShare;

public class Shard {
    private int value;
    private int index;
    private String name;

    public int getValue() {
        return value;
    }

    public Shard(String name, int val, int index) {
        value = val;
        this.index = index;
        this.name = name;
    }

    public String toString() {
        return name + " (" + index + "," + value + ")";
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}