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
    public FullHttpResponse upload(FullHttpRequest request) throws Exception {
        System.out.println("开始上传");
        String fileName = "";
        String filePath = "";
        if (decoder == null) {
            decoder = new HttpPostRequestDecoder(factory, request);
        }
        if (request.headers().get("filePath") != null) {
            filePath = DEFAULT_DIR + File.separatorChar + request.headers().get("filePath");
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        List<InterfaceHttpData> bodyHttpDatas = decoder.getBodyHttpDatas();
        if (bodyHttpDatas != null && bodyHttpDatas.size() != 0) {
            for (InterfaceHttpData data : bodyHttpDatas) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload fileUpload = (FileUpload) data;
                    fileName = fileUpload.getFilename();
                    if (fileUpload.isCompleted()) {
                        fileUpload.isInMemory();
                        fileUpload.renameTo(new File(filePath + File.separatorChar + fileName));
                        decoder.removeHttpDataFromClean(fileUpload);
                    }
                }
            }
        }
        System.out.println("上传成功");
        return HttpUtil.constructText("上传成功!\r\n" + "文件路径：" + DEFAULT_ADDRESS + request.headers().get("filePath") + "/" + fileName);
    }


//    private void readFileIntoDisk() throws IOException {
//        while (decoder.hasNext()) {
//            InterfaceHttpData data = decoder.next();
//            if (data != null) {
//                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
//                    FileUpload fileUpload = (FileUpload) data;
//                    if (fileUpload.isCompleted()) {
//                        fileUpload.isInMemory();
//                        fileUpload.renameTo(new File("E:\\" + fileUpload.getFilename()));
//                        decoder.removeHttpDataFromClean(fileUpload);
//                    }
//                }
//            }
//        }
//    }
}
