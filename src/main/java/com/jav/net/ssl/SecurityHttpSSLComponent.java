package com.jav.net.ssl;


import com.jav.common.util.StringEnvoy;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SecurityHttpSSLComponent extends DefaultHttpSSLComponent {

    private SSLContext mSSLContext = null;

    public SecurityHttpSSLComponent(String hostname, String protocol) throws Exception {
        init(hostname, protocol, new TrustManager[]{new SimpleX509TrustManager()});
    }

    public SecurityHttpSSLComponent(String hostname, String protocol, InputStream certificate) throws Exception {
        //get certificate
        X509Certificate x509Certificate = getX509Certificate(certificate);
        //check certificate
        CheckCertificate checkCertificate = new CheckCertificate(x509Certificate);
        TrustManager[] trustManagers = {checkCertificate};
        init(hostname, protocol, trustManagers);
    }

    private void init(String hostname, String protocol, TrustManager[] trustManagers) throws Exception {
        if (StringEnvoy.isEmpty(hostname)) {
            throw new NullPointerException("hostname is null !!!");
        }
        mSSLContext = SSLContext.getInstance(protocol);
        mSSLContext.init(null, trustManagers, new SecureRandom());
        this.mHostname = hostname.replace("http://", "").replace("https://", "")
                .replace("/", "");
    }


    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return mSSLContext != null ? mSSLContext.getSocketFactory() : null;
    }

    @Override
    public SSLServerSocketFactory getSSLServerSocketFactory() {
        return null;
    }


    public static class SimpleX509TrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] client, String authType) throws CertificateException {
            for (X509Certificate certificate : client) {
                //检查证书是否有效
                certificate.checkValidity();
//                LogDog.d("==> client = " + certificate.toString() + " authType = " + authType);
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] server, String authType) throws CertificateException {
            for (X509Certificate certificate : server) {
                //检查证书是否有效
                certificate.checkValidity();
//                LogDog.d("==> server = " + certificate.toString() + " authType = " + authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }


    public static class CheckCertificate implements X509TrustManager {
        //如果需要对证书进行校验，需要这里去实现，如果不实现的话是不安全
        private final X509Certificate mX509Certificate;


        public CheckCertificate(X509Certificate x509Certificate) {
            this.mX509Certificate = x509Certificate;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            for (X509Certificate certificate : chain) {
                //检查证书是否有效
                certificate.checkValidity();
                try {
                    certificate.verify(mX509Certificate.getPublicKey());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CertificateException(e);
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

}
