package app;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import springmvc.annonation.YController;
import springmvc.annonation.YRequestMapping;
import springmvc.util.HttpUtil;

import java.io.*;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

@YController
@YRequestMapping("/upload")
public class UploadController {

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private static final String DEFAULT_ADDRESS = "http://localhost:8080/download/file";

    private static final String DEFAULT_DIR = System.getProperty("user.dir");

    private HttpPostRequestDecoder decoder;

    @YRequestMapping("/file")
    public FullHttpResponse upload(HttpRequest request, HttpContent content) throws Exception {
        System.out.println("开始上传");
        String filePath = "";
        String result = "";
        if (decoder == null) {
            decoder = new HttpPostRequestDecoder(factory, request);
        }
        if (request.headers().get("filePath") != null) {
            filePath = DEFAULT_DIR + request.headers().get("filePath");
            filePath = filePath.replace('/', File.separatorChar);
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        if(decoder != null && content != null) {
            decoder.offer(content);
            result = readFileIntoDisk(filePath);
        }
        System.out.println("该部分上传成功");
        return HttpUtil.responseString("上传成功！\r\n" + "文件路径:" + DEFAULT_ADDRESS + request.headers().get("filePath") + result);
    }

    private String readFileIntoDisk(String filePath) throws IOException {
        String file = "";
        while (decoder.hasNext()) {
            InterfaceHttpData data = decoder.next();
            if (data != null) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload fileUpload = (FileUpload) data;
                    if (fileUpload.isCompleted()) {
                        fileUpload.isInMemory();
                        file = fileUpload.getFilename();
                        fileUpload.renameTo(new File(filePath + File.separatorChar + fileUpload.getFilename()));
                        decoder.removeHttpDataFromClean(fileUpload);
                    }
                }
            }
        }
        return file;
    }
}
