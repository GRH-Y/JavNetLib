package connect.network.http;


import connect.network.base.joggle.ISSLFactory;
import sun.security.x509.X509CertImpl;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CustomHttpSSLFactory implements ISSLFactory {

    private SSLContext sslcontext = null;

    public CustomHttpSSLFactory(String protocol, InputStream certificate) throws Exception {

//        sslcontext = SSLContext.getInstance(protocol);
//        sslcontext.init(null, new TrustManager[]{new MyX509TrustManager()}, new SecureRandom());

        sslcontext = SSLContext.getInstance(protocol);
        //get certificate
        X509Certificate x509Certificate = getX509Certificate(certificate);
        //check certificate
        CheckCertificate checkCertificate = new CheckCertificate(x509Certificate);
        TrustManager[] trustManagers = {checkCertificate};
        sslcontext.init(null, trustManagers, new SecureRandom());
    }


    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return sslcontext.getSocketFactory();
    }

    @Override
    public SSLServerSocketFactory getSSLServerSocketFactory() {
        return null;
    }


//    public class MyX509TrustManager implements X509TrustManager {
//
//        @Override
//        public void checkClientTrusted(X509Certificate certificates[], String authType) {
//        }
//
//        @Override
//        public void checkServerTrusted(X509Certificate[] ax509certificate, String s) {
//        }
//
//        @Override
//        public X509Certificate[] getAcceptedIssuers() {
//            return null;
//        }
//    }


    public class CheckCertificate implements X509TrustManager {
        //如果需要对证书进行校验，需要这里去实现，如果不实现的话是不安全
        private X509Certificate mX509Certificate;


        public CheckCertificate(X509Certificate x509Certificate) {
            this.mX509Certificate = x509Certificate;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            for (X509Certificate certificate : chain) {
                //检查证书是否有效
                try {
                    certificate.checkValidity();
                    certificate.verify(mX509Certificate.getPublicKey());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509CertImpl[0];
        }
    }

    //拿到自己的证书
    private X509Certificate getX509Certificate(InputStream in) throws CertificateException {
        CertificateFactory instance = CertificateFactory.getInstance("X.509");
        return (X509Certificate) instance.generateCertificate(in);
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return (string, sslSession) -> string.equals(sslSession.getPeerHost());
    }

}
