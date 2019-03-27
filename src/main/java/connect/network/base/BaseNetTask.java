package connect.network.base;

public class BaseNetTask {

    public static int tag;

    public BaseNetTask() {
        tag = hashCode();
    }

    public int getTag() {
        return tag;
    }
}
