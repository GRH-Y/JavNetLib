# CurrencyNetConnect
这是个通用的网络通信框架，包含tcp,udp,http等，nio的服务端和客户端


#使用例子:

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
    
        
# tcp p2p使用例子:
     
     
     int port = 9876;
     P2PServer nioServer = new P2PServer(port);
     NioServerFactory serverFactory = NioServerFactory.getFactory();
     serverFactory.open();
     serverFactory.addNioTask(nioServer);
 
 
     String ip = NetUtils.getLocalIp("wlan");
     NioSocketFactory socketFactory = NioSocketFactory.getFactory();
     socketFactory.open();
     P2PSClient client1 = new P2PSClient(ip, port);
     socketFactory.addNioTask(client1);
     P2PSClient client2 = new P2PSClient(ip, port);
     socketFactory.addNioTask(client2);
