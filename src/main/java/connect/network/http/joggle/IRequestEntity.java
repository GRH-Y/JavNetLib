package connect.network.http.joggle;

import java.io.Serializable;
import java.util.Map;

public interface IRequestEntity extends Serializable {

    Map<String, Object> getRequestProperty();

   byte[] getSendData();
}
