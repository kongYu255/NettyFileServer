package app;


import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import springmvc.annonation.*;
import springmvc.util.HttpUtil;

import java.io.File;
import java.util.*;

@YController
@YRequestMapping("/delete")
public class DeleteController {

    /**
     * TODO 这里关于文件地址的参数有点乱，后续可以整理一下
     * @param request
     * @param map
     * @return
     */
    @YRequestMapping("/file")
    public FullHttpResponse delete(FullHttpRequest request, @YRequestBody Map<String, Object> map) {
        System.out.println(map);
        ByteBuf content = request.content();
        String json = content.toString(CharsetUtil.UTF_8);
        JSONObject jsonObject = JSONObject.parseObject(json);
        String filePath = "";
        if (jsonObject != null && jsonObject.get("filePath") != null) {
            filePath = System.getProperty("user.dir") + File.separatorChar + (String) jsonObject.get("filePath");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return HttpUtil.responseString("该文件不存在");
        }
        boolean delete = file.delete();
        if (delete) {
            return HttpUtil.responseString("删除成功!");
        } else {
            return HttpUtil.responseString("删除失败!");
        }
    }
}
