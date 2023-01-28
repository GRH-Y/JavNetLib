# Java Net Lib

网络通信框架，基于nio和aio方式实现tcp（https）,udp通信，实现服务端和客户端

***
2023-01-28 新增安全协议通道

2020-10-18 新增aio

2019-12-01 使用nio实现XHttp

# Tcp使用例子:

	//创建服务端
	NioServer nioServer = new NioServer();
       
    NioServerFactory serverFactory = NioServerFactory.getFactory();
    serverFactory.open();
    //添加任务到工厂,工厂会处理相应各类事件反馈到NioServer
    serverFactory.addNioTask(nioServer);
    
	

	//创建客户端连接		
    NioClient client = new NioClient();
    NioSocketFactory socketFactory = NioSocketFactory.getFactory();
    socketFactory.open();
    //添加任务到工厂,工厂会处理相应各类事件反馈到NioClient
    socketFactory.addNioTask(client);

# http使用例子:

     public static void main(String[] args) {
         QuireCard querCard = new QuireCard();
         XHttpConnect.getInstance().submitNioRequest(querCard, querCard);
     }
     
     @AXHttpRequest(url = "http://29c848e26bc82d7e9f7ac6382b564c1c.ccc3qqqq.top/index/index/cady",
            resultType = byte[].class, callBackSuccessMethod = "onQuireResult", callBackErrorMethod = "onQuireError", requestMode = RequestMode.POST)
    private static class QuireCard implements IXHttpRequestEntity {

        /**
        * 成功返回方法
        *
        * @param data 返回的方法定义在@ARequest注解successMethod字段 ，请求返回的类型定义在@ARequest注解的resultType字段，
        */
        @JavKeep
        private void onQuireResult(XRequest request, XResponse response){
            byte[] data = response.getHttpData();
            String str = new String(data);
            // if (str.contains("status\":1")) {
                LogDog.d("==> http data = " + str);
            // }
        }

        /**
        * 失败返回反对法
        *
        * @param entity 默认的参数
        */
        @JavKeep
        private void onQuireError(XRequest request, Throwable ex){
            ex.printStackTrace();
        }

        @Override
        public LinkedHashMap<Object, Object> getRequestProperty() {
            LinkedHashMap headData = new LinkedHashMap<String, String>();
            headData.put(XHttpProtocol.XY_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
            return headData;
        }

        @Override
        public byte[] getSendData() {
            return "oid=1&cady=6226912790544671".getBytes();
        }
    }


#混淆规则

    -keep class * extends java.lang.annotation.Annotation { *; }
    -keep interface * extends java.lang.annotation.Annotation { *; }
    # 保留Serializable序列化的类不被混淆
    -keepclassmembers class * implements java.io.Serializable {
        static final long serialVersionUID;
        private static final java.io.ObjectStreamField[] serialPersistentFields;
        !static !transient <fields>;
        !private <fields>;
        !private <methods>;
        private void writeObject(java.io.ObjectOutputStream);
        private void readObject(java.io.ObjectInputStream);
        java.lang.Object writeReplace();
        java.lang.Object readResolve();
    }