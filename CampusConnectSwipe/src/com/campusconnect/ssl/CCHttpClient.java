package com.campusconnect.ssl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import com.campusconnect.R;

import android.content.Context;
import android.util.Log;

public class CCHttpClient extends DefaultHttpClient {
	
	private static Context appContext = null;
    private static Scheme httpsScheme = null;
    private static Scheme httpScheme = null;
    private static String TAG = "CCHttpClient";

    public CCHttpClient(Context myContext) {

        appContext = myContext;

        if (httpScheme == null || httpsScheme == null) {
            httpScheme = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
            httpsScheme = new Scheme("https", CC_SSLSocketFactory(), 8443);
        }

        getConnectionManager().getSchemeRegistry().register(httpScheme);
        getConnectionManager().getSchemeRegistry().register(httpsScheme);

    }
    
    public CCHttpClient(Context myContext, HttpParams httpParameters) {
    	appContext = myContext;
    	
    	if (httpScheme == null || httpsScheme == null) {
            httpScheme = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
            httpsScheme = new Scheme("https", CC_SSLSocketFactory(), 8443);
        }

        getConnectionManager().getSchemeRegistry().register(httpScheme);
        getConnectionManager().getSchemeRegistry().register(httpsScheme);
    }

    private SSLSocketFactory CC_SSLSocketFactory() {
        SSLSocketFactory ret = null;
        try {
        	InputStream caInput = new BufferedInputStream(appContext.getResources().openRawResource(R.raw.cert));
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate ca = cf.generateCertificate(caInput);
			
            final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            final InputStream inputStream = appContext.getResources().openRawResource(R.raw.cert);

            ks.load(null, null);
			ks.setCertificateEntry("ca", ca);
            inputStream.close();

            ret = new SSLSocketFactory(ks);
        } catch (UnrecoverableKeyException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (KeyStoreException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (KeyManagementException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
        return ret;
    }
}
