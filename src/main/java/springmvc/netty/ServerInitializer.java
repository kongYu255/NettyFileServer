package springmvc.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import springmvc.context.AnnotationApplicationContext;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private AnnotationApplicationContext applicationContext;

    public ServerInitializer(AnnotationApplicationContext applicationContext){
        this.applicationContext = applicationContext;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(1024*1024*1024));
//        pipeline.addLast(new HttpContentCompressor());
//        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast("handler", new DispatcherHandler(applicationContext));
    }
}
