import connect.json.JsonUtils;
import connect.network.base.RequestEntity;
import connect.network.http.joggle.ARequest;
import connect.network.http.joggle.GET;
import connect.network.http.joggle.IRequestEntity;
import connect.network.http.joggle.POST;
import util.Logcat;

import java.util.ArrayList;
import java.util.List;

public class HttpTestMain {

    public static void main(String[] arg) {
        Login login = new Login();
        String data = JsonUtils.toJson(login);
        Login newLogin = JsonUtils.toNewEntity(Login.class, data);
//        try {
//            IHttpTaskConfig config = JavHttpConnect.getInstance().getHttpTaskConfig();
//            config.setSessionCallBack(new JavSessionCallBack());
//            config.setHttpSSLFactory(new HttpSSLFactory());
////            BaiduRequestEntity entity = new BaiduRequestEntity();
////            JavHttpConnect.getInstance().submitEntity(entity, entity);
//            CzhRequestEntity czhRequestEntity = new CzhRequestEntity();
//            JavHttpConnect.getInstance().submitEntity(czhRequestEntity, czhRequestEntity);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @ARequest(requestType = GET.class, url = "https://kyfw.12306.cn/otn/login/inits", resultType = byte[].class, successMethod = "successCallBack")
    public static class BaiduRequestEntity implements IRequestEntity {

        private void successCallBack(RequestEntity entity) {
            Object data = entity.getResultData();
            if (data instanceof byte[]) {
                Logcat.d("==> entity  =" + new String((byte[]) data));
            }
        }

        @Override
        public byte[] postData() {
            return null;
        }
    }

    @ARequest(requestType = POST.class, url = "https://test-xmall-app-api.carrieym.com", resultType = BaseRequest.class, successMethod = "successCallBack")
    public static class CzhRequestEntity implements IRequestEntity {

        private void successCallBack(RequestEntity entity) {
            Object data = entity.getResultData();
            if (data instanceof BaseRequest) {
                BaseRequest request = (BaseRequest) data;
                Logcat.d("==> CzhRequestEntity entity  =" + request.toString());
            }
        }

        @Override
        public byte[] postData() {
            Login login = new Login();
            String data = JsonUtils.toJson(login);
            return data.getBytes();
        }
    }

    private static class Login {
        private Info data = new Info();

        private List<Info> list = new ArrayList<>();

        public Login() {
            list.add(data);
        }

        private class Info {
            public String mobile = "15521382959";
            public String password = "123456";
        }
    }
}
