package net.hockeyapp.android.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hockeyapp.android.Constants;

public class HttpURLConnectionBuilder {
    public static final String DEFAULT_CHARSET = "UTF-8";
    private static final int DEFAULT_TIMEOUT = 120000;
    public static final int FIELDS_LIMIT = 25;
    public static final long FORM_FIELD_LIMIT = 4194304;
    private final Map<String, String> mHeaders;
    private SimpleMultipartEntity mMultipartEntity;
    private String mRequestBody;
    private String mRequestMethod;
    private int mTimeout = DEFAULT_TIMEOUT;
    private final String mUrlString;

    public HttpURLConnectionBuilder(String urlString) {
        this.mUrlString = urlString;
        this.mHeaders = new HashMap();
        this.mHeaders.put("User-Agent", Constants.SDK_USER_AGENT);
    }

    public HttpURLConnectionBuilder setRequestMethod(String requestMethod) {
        this.mRequestMethod = requestMethod;
        return this;
    }

    public HttpURLConnectionBuilder setRequestBody(String requestBody) {
        this.mRequestBody = requestBody;
        return this;
    }

    public HttpURLConnectionBuilder writeFormFields(Map<String, String> fields) {
        if (fields.size() > 25) {
            throw new IllegalArgumentException("Fields size too large: " + fields.size() + " - max allowed: " + 25);
        }
        for (String key : fields.keySet()) {
            String value = (String) fields.get(key);
            if (value != null && ((long) value.length()) > FORM_FIELD_LIMIT) {
                throw new IllegalArgumentException("Form field " + key + " size too large: " + value.length() + " - max allowed: " + FORM_FIELD_LIMIT);
            }
        }
        try {
            String formString = getFormString(fields, "UTF-8");
            setHeader("Content-Type", "application/x-www-form-urlencoded");
            setRequestBody(formString);
            return this;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpURLConnectionBuilder writeMultipartData(Map<String, String> fields, Context context, List<Uri> attachmentUris) {
        try {
            this.mMultipartEntity = new SimpleMultipartEntity(File.createTempFile("multipart", null, context.getCacheDir()));
            this.mMultipartEntity.writeFirstBoundaryIfNeeds();
            for (String key : fields.keySet()) {
                this.mMultipartEntity.addPart(key, (String) fields.get(key));
            }
            int i = 0;
            while (i < attachmentUris.size()) {
                Uri attachmentUri = (Uri) attachmentUris.get(i);
                boolean lastFile = i == attachmentUris.size() + -1;
                this.mMultipartEntity.addPart("attachment" + i, attachmentUri.getLastPathSegment(), context.getContentResolver().openInputStream(attachmentUri), lastFile);
                i++;
            }
            this.mMultipartEntity.writeLastBoundaryIfNeeds();
            setHeader("Content-Type", "multipart/form-data; boundary=" + this.mMultipartEntity.getBoundary());
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpURLConnectionBuilder setTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout has to be positive.");
        }
        this.mTimeout = timeout;
        return this;
    }

    public HttpURLConnectionBuilder setHeader(String name, String value) {
        this.mHeaders.put(name, value);
        return this;
    }

    public HttpURLConnectionBuilder setBasicAuthorization(String username, String password) {
        setHeader("Authorization", "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), 2));
        return this;
    }

    public HttpURLConnection build() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(this.mUrlString).openConnection();
        connection.setConnectTimeout(this.mTimeout);
        connection.setReadTimeout(this.mTimeout);
        if (!TextUtils.isEmpty(this.mRequestMethod)) {
            connection.setRequestMethod(this.mRequestMethod);
            if (!TextUtils.isEmpty(this.mRequestBody) || this.mRequestMethod.equalsIgnoreCase("POST") || this.mRequestMethod.equalsIgnoreCase("PUT")) {
                connection.setDoOutput(true);
            }
        }
        for (String name : this.mHeaders.keySet()) {
            connection.setRequestProperty(name, (String) this.mHeaders.get(name));
        }
        if (!TextUtils.isEmpty(this.mRequestBody)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
            writer.write(this.mRequestBody);
            writer.flush();
            writer.close();
        }
        if (this.mMultipartEntity != null) {
            connection.setRequestProperty("Content-Length", String.valueOf(this.mMultipartEntity.getContentLength()));
            this.mMultipartEntity.writeTo(connection.getOutputStream());
        }
        return connection;
    }

    private static String getFormString(Map<String, String> params, String charset) throws UnsupportedEncodingException {
        List<String> protoList = new ArrayList();
        for (String key : params.keySet()) {
            String value = (String) params.get(key);
            String key2 = URLEncoder.encode(key2, charset);
            protoList.add(key2 + "=" + URLEncoder.encode(value, charset));
        }
        return TextUtils.join("&", protoList);
    }
}
