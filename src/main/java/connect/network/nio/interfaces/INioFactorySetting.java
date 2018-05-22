package connect.network.nio.interfaces;

public interface INioFactorySetting {

    void enableReadEvent();

    void disableReadEvent();

    void enableWriteEvent();

    void disableWriteEvent();

    void enableReadWriteEvent();

}
