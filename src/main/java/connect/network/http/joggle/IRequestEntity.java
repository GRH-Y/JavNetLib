package connect.network.http.joggle;

import java.io.Serializable;
import java.util.Map;

public interface IRequestEntity extends Serializable {

    Map<String, String> getRequestProperty();

   byte[] getSendData();
}
