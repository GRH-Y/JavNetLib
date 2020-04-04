package connect.network.base.joggle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface ITLSHandler {

    void doHandshake() throws IOException;

    ByteBuffer wrapAndWrite(ByteBuffer needWarp, ByteBuffer result) throws IOException;

    void readAndUnwrap(ByteArrayOutputStream result, ByteBuffer unwrap, boolean isDoHandshake) throws IOException;
}

