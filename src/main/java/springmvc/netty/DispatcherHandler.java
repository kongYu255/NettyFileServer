package springmvc.netty;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
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
            // 带.即代表是下载文件，把后缀带的文件路径截取掉
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
                response = HttpUtil.getErroResponse();
                writeResponse(true);
            }
        }
    }

    private static Object[] handle(FullHttpRequest req, FullHttpResponse resp, Method method) throws IOException, IllegalAccessException, InstantiationException {
        //拿到当前执行的方法有哪些参数
        Class<?>[] paramClazzs = method.getParameterTypes();
        //根据参数的个数，new 一个参数的数据
        Object[] args = new Object[paramClazzs.length];
        int args_i = 0;
        int index = 0;
        for(Class<?> paramClazz:paramClazzs){
            if(FullHttpRequest.class.isAssignableFrom(paramClazz)){
                args[args_i++] = req;
            }
            if(FullHttpResponse.class.isAssignableFrom(paramClazz)){
                args[args_i++] = resp;
            }
            //判断requestParam  注解
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if(paramAns.length > 0){
                for(Annotation paramAn:paramAns){
                    if(YRequestBody.class.isAssignableFrom(paramAn.getClass())) {
                        args[args_i++] = RequestParamUtil.getPostParamMap(req);
                    }
                }
            }
            index ++;
        }
        return  args;
    }

    private void writeResponse(boolean forceClose){
        boolean close = isClose();
        if(!close && !forceClose){
            response.headers().add("Content-Length", String.valueOf(response.content().readableBytes()));
        }
        ChannelFuture future = channel.writeAndFlush(response);
        if(close || forceClose){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
    private boolean isClose(){
        if(request.headers().contains("Connection", CONNECTION_CLOSE, true) ||
                (request.protocolVersion().equals(HttpVersion.HTTP_1_0) &&
                        !request.headers().contains("Connection", CONNECTION_KEEP_ALIVE, true)))
            return true;
        return false;
    }
}
