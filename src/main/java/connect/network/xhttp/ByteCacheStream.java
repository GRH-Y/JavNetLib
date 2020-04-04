package connect.network.xhttp;

import java.io.ByteArrayOutputStream;

public class ByteCacheStream extends ByteArrayOutputStream {

    public byte[] getBuf() {
        return buf;
    }

}
