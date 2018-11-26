package connect.network.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @className: RequestMultipartFormData
 * @classDescription:
 * @author: yyz
 * @createTime: 11/26/2018
 */
public class RequestMultipartFormData {

    //编节符
    private String boundary = "-------------------------" + System.currentTimeMillis();
    private String dispositionBoundary = "-----------------------------" + System.currentTimeMillis();
    private String endDispositionBoundary = "----------------------------";
    private String contentType = "multipart/form-data; boundary=" + boundary;
    //前缀 上传时需要多出两个-- 一定需要注意！！！
    private String prefix = "--";
    //这里也需要注意，在html协议中，用 “\r\n” 换行，而不是 “\n”。
    private String end = "\r\n";

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

    public RequestMultipartFormData setName(String name, String filename) {
        this.name = name;
        this.filename = filename;
        return this;
    }

    public RequestMultipartFormData addContentDisposition(String key, String value) {
        if (disposition == null) {
            disposition = new StringBuilder();
        }
        disposition.append(dispositionBoundary);
        disposition.append(" Content-Disposition: form-data; name=\"" + key + "\"");
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
                builder.append("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"");
                builder.append(filename);
                builder.append("\"");
                //换行
                builder.append(end);
                builder.append("Content-Type:application/octet-stream\r\n\r\n");
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
                    builder.append(disposition.toString());
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
