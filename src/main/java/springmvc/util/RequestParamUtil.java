package springmvc.util;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求参数解析器
 */
public class RequestParamUtil {

    /**
     * 解析Get请求的参数
     * @param fullRequest
     * @return
     */
    public static Map<String, Object> getGetParamMap(FullHttpRequest fullRequest) {
        Map<String, Object> paramMap = new HashMap<>();
        String uri = fullRequest.uri();
        QueryStringDecoder queryDecoder = new QueryStringDecoder(uri, Charset.forName("UTF-8"));
        for (Map.Entry<String, List<String>> item : queryDecoder.parameters().entrySet()) {
            if (item.getValue().get(0) != null) {
                paramMap.put(item.getKey(), item.getValue().get(0));
            }
        }
        return paramMap;
    }

    /**
     * 解析POST请求的参数
     * @param fullRequest
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getPostParamMap(HttpRequest fullRequest, HttpContent content) {
        HttpHeaders headers = fullRequest.headers();
        String contentType = getContentType(headers);

        Map<String, Object> param = new HashMap<>();
        // TODO 目前这里仅支持application/json格式的body参数，其他格式后续添加
        if(contentType.equals("application/json")) {
            String jsonStr = content.content().toString(Charset.forName("UTF-8"));
            JSONObject obj = JSON.parseObject(jsonStr);
            for (Map.Entry<String, Object> item : obj.entrySet()) {
                String key = item.getKey();
                Object value = item.getValue();
                param.put(key, value);
            }
        }
        return param;
    }


    /**
     * 获取参数类型
     * @param headers
     * @return
     */
    private static String getContentType(HttpHeaders headers){
        String contentType = headers.get("Content-Type");
        if (contentType != null && !contentType.equals("")) {
            String[] list = contentType.split(";");
            return list[0];
        }
        return "";
    }

}

