package springmvc.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import springmvc.context.AnnotationApplicationContext;

public class NettyHttpServer {
    private static NettyHttpServer instance = new NettyHttpServer();

    private NettyHttpServer(){

    }

    public static NettyHttpServer getInstance(){
        return instance;
    }

    private final int port = 8080;

    public void start(AnnotationApplicationContext applicationContext) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 服务端启动器
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer(applicationContext));

            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("服务端启动，监听端口：8080");

            future.channel().closeFuture().sync();
        }finally {
            //优雅的关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
