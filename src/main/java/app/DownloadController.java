package app;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import springmvc.annonation.RequestMethod;
import springmvc.annonation.YAutowired;
import springmvc.annonation.YController;
import springmvc.annonation.YRequestMapping;
import springmvc.util.HttpUtil;

import java.io.File;
import java.io.FileInputStream;

@YController
@YRequestMapping(value = "/download")
public class DownloadController {

    @YRequestMapping(value = "/file", method = RequestMethod.GET)
    public FullHttpResponse download(FullHttpRequest request) {
        String uri = request.uri();
        StringBuffer sb = new StringBuffer();
        String[] uris = uri.split("/");
        if (uris.length >= 3) {
            for (int i = 3; i < uris.length; i++) {
                sb.append("/");
                sb.append(uris[i]);
            }
        }
        File file = new File(System.getProperty("user.dir") + File.separatorChar + sb.toString());
        byte[] bytes = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytes);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return HttpUtil.response("下载异常", "text/plain", HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
        return HttpUtil.response(new String(bytes), "multipart/form-data", HttpResponseStatus.OK);
    }
}
