package springmvc.netty;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import springmvc.annonation.YRequestBody;
import springmvc.context.AnnotationApplicationContext;
import springmvc.util.HttpUtil;
import springmvc.util.RequestParamUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class DispatcherHandler extends SimpleChannelInboundHandler {

    private static final String CONNECTION_KEEP_ALIVE = "keep-alive";

    private static final String CONNECTION_CLOSE = "close";

    private AnnotationApplicationContext annotationApplicationContext;

    private FullHttpRequest request;

    private FullHttpResponse response;

    private Channel channel;

    public DispatcherHandler(AnnotationApplicationContext annotationApplicationContext){
        this.annotationApplicationContext = annotationApplicationContext;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if(o instanceof FullHttpRequest) {
            channel = channelHandlerContext.channel();
            request = (FullHttpRequest) o;
            String uri = request.uri();   //   /paul-mvc/com.paul.controller/method-com.paul.controller
            if(uri.contains("?")){
                int index = uri.indexOf("?");
                uri = uri.substring(0,index);
            }
            // 带 . 即代表是下载文件，把后缀带的文件路径截取掉
            if(uri.contains(".")) {
                String[] uris = uri.split("/");
                if (uris.length >= 3) {
                    uri = "/" + uris[1] + "/" + uris[2];
                }
            }
            Method m = (Method) annotationApplicationContext.handleMap.get(uri);
            if (null == m) {
                response = HttpUtil.getNotFoundResponse();
                writeResponse(true);
                return;
            }
            //从容器里拿到controller 实例
            Object instance = annotationApplicationContext.controllerMap.get(uri);
            Object[] args = handle(request, response, m);
            try {
                response = (FullHttpResponse) m.invoke(instance, args);
                writeResponse(false);
            } catch (Exception e) {
                e.printStackTrace();
                response = HttpUtil.getErrorResponse();
                writeResponse(true);
            }
        }
    }

    private static Object[] handle(FullHttpRequest req, FullHttpResponse resp, Method method) throws IOException, IllegalAccessException, InstantiationException {
        //拿到当前执行的方法的参数
        Class<?>[] paramClazzs = method.getParameterTypes();
        Object[] args = new Object[paramClazzs.length];

        //参数索引
        int args_i = 0;
        //获取参数注解的索引
        int index = 0;
        for(Class<?> paramClazz:paramClazzs){
            if(FullHttpRequest.class.isAssignableFrom(paramClazz)){
                args[args_i++] = req;
            }
            if(FullHttpResponse.class.isAssignableFrom(paramClazz)){
                args[args_i++] = resp;
            }
            //判断RequestBody的注解，解析POST请求的参数，返回的Map
            //该方法拿到index参数的所有注解
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if(paramAns.length > 0){
                for(Annotation paramAn:paramAns){
                    if(YRequestBody.class.isAssignableFrom(paramAn.getClass()) && req.method() == HttpMethod.POST) {
                        args[args_i++] = RequestParamUtil.getPostParamMap(req);
                    }
                }
            }
            index ++;
        }
        return  args;
    }

    /**
     * 返回response
     * @param forceClose 是否需要关闭连接
     */
    private void writeResponse(boolean forceClose){
        boolean isKeepAlive = isKeepAlive();
        if(!isKeepAlive && !forceClose){
            response.headers().add("Content-Length", String.valueOf(response.content().readableBytes()));
        }
        //把请求写入管道
        ChannelFuture future = channel.writeAndFlush(response);
        //长连接则监听
        if(isKeepAlive || forceClose){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 判断连接是否需要关闭/长连接
     * Http1.1以下版本不需要判断，Http1.1以上只需判断Connection中是否开启了长连接
     * @return
     */
    private boolean isKeepAlive(){
        if(request.headers().contains("Connection", CONNECTION_CLOSE, true) ||
                (request.protocolVersion().equals(HttpVersion.HTTP_1_0) &&
                        !request.headers().contains("Connection", CONNECTION_KEEP_ALIVE, true)))
            return true;
        return false;
    }
}
