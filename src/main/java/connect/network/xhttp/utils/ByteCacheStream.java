package connect.network.xhttp.utils;

import java.io.ByteArrayOutputStream;

public class ByteCacheStream extends ByteArrayOutputStream {

    public byte[] getBuf() {
        return buf;
    }

}
