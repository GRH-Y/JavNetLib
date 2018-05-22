# CurrencyNetConnect
这是个通用的网络通信框架，包含tcp,udp,http等，nio的服务端和客户端


#使用例子:

	//创建服务端
	NioServer nioServer = new NioServer();
        try {
            NioServerFactory serverFactory = NioServerFactory.getFactory();
            serverFactory.open();
	   //添加任务到工厂,工厂会处理相应各类事件反馈到NioServer
            serverFactory.addNioTask(nioServer);
        } catch (Exception e) {
            e.printStackTrace();
        }
	

	//创建客户端连接		
        NioClient client = new NioClient();
        try {
            NioSocketFactory socketFactory = NioSocketFactory.getFactory();
            socketFactory.open();
	    //添加任务到工厂,工厂会处理相应各类事件反馈到NioClient
            socketFactory.addNioTask(client);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
# tcp p2p使用例子:
     
     
         int port = 9876;
         try {
             P2PServer nioServer = new P2PServer(port);
             NioServerFactory serverFactory = NioServerFactory.getFactory();
             serverFactory.open();
             serverFactory.addNioTask(nioServer);
         } catch (Exception e) {
             e.printStackTrace();
         }
     
     
         try {
             String ip = NetUtils.getLocalIp("wlan");
             NioSocketFactory socketFactory = NioSocketFactory.getFactory();
             socketFactory.open();
             P2PSClient client1 = new P2PSClient(ip, port);
             socketFactory.addNioTask(client1);
             P2PSClient client2 = new P2PSClient(ip, port);
             socketFactory.addNioTask(client2);
         } catch (IOException e) {
             e.printStackTrace();
         }
