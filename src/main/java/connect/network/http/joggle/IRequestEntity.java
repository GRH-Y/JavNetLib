package connect.network.http.joggle;

import java.util.Map;

public interface IRequestEntity {

    Map<String, String> getRequestProperty();

   byte[] getSendData();
}
