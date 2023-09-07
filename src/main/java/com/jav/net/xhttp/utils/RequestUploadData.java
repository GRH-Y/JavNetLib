package com.jav.net.xhttp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * http上传文件工具类
 */
public class RequestUploadData {

    //编节符
    private final String boundary = "-------------------------" + System.currentTimeMillis();
    private final String dispositionBoundary = "-----------------------------" + System.currentTimeMillis();
    private final static String endDispositionBoundary = "----------------------------";
    private final String contentType = "multipart/form-data; boundary=" + boundary;
    //前缀 上传时需要多出两个-- 一定需要注意！！！
    private final static String prefix = "--";
    //这里也需要注意，在html协议中，用 “\r\n” 换行，而不是 “\n”。
    private final String end = "\r\n";

    private StringBuilder disposition;

    private String name;
    private String filename;

    /* *
       -----------------------------7e020233150564
       Content-Disposition: form-data; name="file"; filename="I:\迅雷下载\18fb1f51c9eb63489cce9e029154782e.jpg"
       Content-Type: image/jpeg
       //这里是空一行  需要注意
       <图片数据>
       ---------------------------7e020233150564--

       -----------------------------11734007861738381608866601737
       Content-Disposition: form-data; name="uName"

       admin
       ----------------------------
   */

    public RequestUploadData setName(String name, String filePath) {
        this.name = name;
        this.filename = filePath;
        return this;
    }

    public RequestUploadData addContentDisposition(String key, String value) {
        if (disposition == null) {
            disposition = new StringBuilder();
        }
        disposition.append(dispositionBoundary);
        disposition.append("Content-Disposition: form-data; name=\"");
        disposition.append(key);
        disposition.append("\"");
        disposition.append(end);
        disposition.append(value);
        disposition.append(endDispositionBoundary);
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getBody(byte[] fileData) {
        byte[] sendData = null;
        if (fileData != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StringBuilder builder = new StringBuilder();
            try {
                builder.append(prefix);
                builder.append(boundary);
                //换行
                builder.append(end);
                name = name == null ? "file" : name;
                builder.append("Content-Disposition: form-data; name=\"");
                builder.append(name);
                builder.append("\"; filename=\"");
                builder.append(filename);
                builder.append("\"");
                //换行
                builder.append(end);
                builder.append("Content-Type:image/jpeg\r\n\r\n");
                outputStream.write(builder.toString().getBytes());
                //写入图片数据
                outputStream.write(fileData);
                //写完数据后 回车换行
                builder.delete(0, builder.length());

                builder.append(end);
                builder.append(prefix);
                builder.append(boundary);
                builder.append(prefix);
                builder.append(end);
                if (disposition != null) {
                    builder.append(disposition);
                }
                outputStream.write(builder.toString().getBytes());
                sendData = outputStream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sendData;
    }

}
