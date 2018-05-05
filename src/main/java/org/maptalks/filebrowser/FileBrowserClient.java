package org.maptalks.filebrowser;

import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.StringJoiner;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClients;

public class FileBrowserClient {

    private HttpClient client;
    private HttpHost host;
    private Gson gson = new Gson();

    public FileBrowserClient(String address) {
        this.client = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom().build())
            .build();
        this.host = HttpHost.create(address);
    }

    public FileInfo getFiles(String dir) throws IOException {
        HttpRequest request = new HttpGet("/api/resource/" + dir + "/");
        HttpResponse response = client.execute(host, request);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        response.getEntity().writeTo(stream);
        String json = stream.toString("UTF-8");
        return gson.fromJson(json, FileInfo.class);
    }

    public void createDir(String path) throws IOException {
        HttpRequest request = new HttpPost("/api/resource/" + path + "/");
        client.execute(host, request);
    }

    public void createFile(String path) throws IOException {
        createFile(path, false);
    }

    public void createFile(String path, boolean trunc) throws IOException {
        HttpPost request = new HttpPost("/api/resource/" + path);
        if (trunc) {
            request.addHeader("Action", "override");
        }
        client.execute(host, request);
    }

    public void uploadFile(File file, String path) throws IOException {
        uploadFile(file, path, false);
    }

    public void uploadFile(File file, String path, boolean override) throws IOException {
        InputStream stream = new FileInputStream(file);
        uploadFile(stream, path, override);
    }

    public void uploadFile(InputStream stream, String path) throws IOException {
        uploadFile(stream, path, false);
    }

    public void uploadFile(InputStream stream, String path, boolean override) throws IOException {
        HttpPost request = new HttpPost("/api/resource/" + path);
        if (override) {
            request.setHeader("Action", "override");
        }
        HttpEntity entity = EntityBuilder.create()
            .setStream(stream)
            .build();
        request.setEntity(entity);
        client.execute(host, request);
    }

    public void updateFile(File file, String path) throws IOException {
        InputStream stream = new FileInputStream(file);
        updateFile(stream, path);
    }

    public void updateFile(InputStream stream, String path) throws IOException {
        HttpPut request = new HttpPut("/api/resource/" + path);
        HttpEntity entity = EntityBuilder.create()
            .setStream(stream)
            .build();
        request.setEntity(entity);
        client.execute(host, request);
    }

    public void downloadFile(String dest, String path) throws IOException {
        HttpGet request = new HttpGet("/api/download/" + path);
        HttpResponse response = client.execute(host, request);
        try (FileOutputStream outputStream = new FileOutputStream(new File(dest))) {
            response.getEntity().writeTo(outputStream);
        }
    }

    public void downloadFiles(String dest, String... paths) throws IOException {
        Objects.requireNonNull(paths);
        StringJoiner joiner = new StringJoiner(",");
        for (String path : paths) {
            joiner.add(path);
        }
        HttpRequest request = new HttpGet("/api/download/?" + joiner.toString() + "&" + "format=zip");
        HttpResponse response = client.execute(host, request);
        try (FileOutputStream outputStream = new FileOutputStream(new File(dest))) {
            response.getEntity().writeTo(outputStream);
        }
    }

    public void delete(String path) throws IOException {
        HttpRequest request = new HttpDelete("/api/resource/" + path);
        client.execute(host, request);
    }

}
