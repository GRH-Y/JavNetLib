# CurrencyNetConnect
这是个通用的网络通信框架，包含tcp,udp,http等，nio的服务端和客户端


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
         JavHttpConnect httpConnect = JavHttpConnect.getInstance();
         IHttpTaskConfig config = httpConnect.getHttpTaskConfig();
         //设置ssl，用于https请求
         config.setHttpSSLFactory();
         //转换请求结果
         config.setConvertResult();
         //http请求会话监听
         config.setSessionCallBack();
         //中断请求配置
         config.setInterceptRequest();
         //BaiduRequest 请求任务，Test 请求结果返回目标
         httpConnect.submitEntity(new BaiduRequest(), new Test());
     }
     
     @ARequest(requestMethod = GET.class, url = "https://www.baidu.com", successMethod = "httpCallBack", errorMethod = "errorCallBack", resultType = byte[].class)
     private static class BaiduRequest implements IRequestEntity {
 
         @Override
         public Map<String, String> getRequestProperty() {
             return null;
         }
 
         @Override
         public byte[] getSendData() {
             return null;
         }
     }
     
     /**
      * 成功返回方法
      *
      * @param data 返回的方法定义在@ARequest注解successMethod字段 ，请求返回的类型定义在@ARequest注解的resultType字段，
      */
     private void httpCallBack(byte[] data) {
     }
 
     /**
      * 失败返回反对法
      *
      * @param entity 默认的参数
      */
     private void errorCallBack(RequestEntity entity) {
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