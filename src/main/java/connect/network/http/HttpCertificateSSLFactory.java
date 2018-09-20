package connect.network.http;


import connect.network.http.joggle.IHttpSSLFactory;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class HttpCertificateSSLFactory implements IHttpSSLFactory {

    private InputStream mCertificate;

    public HttpCertificateSSLFactory(InputStream certificate) {
        mCertificate = certificate;
    }


    @Override
    public SSLSocketFactory getSSLSocketFactory() throws CertificateException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext tls = SSLContext.getInstance("TLS");
        //get certificate
        X509Certificate x509Certificate = getX509Certificate(mCertificate);
        //check certificate
        CheckCertificate checkCertificate = new CheckCertificate(x509Certificate);
        TrustManager[] trustManagers = {checkCertificate};
        tls.init(null, trustManagers, new SecureRandom());
        return tls.getSocketFactory();
    }


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
            return new X509Certificate[0];
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
