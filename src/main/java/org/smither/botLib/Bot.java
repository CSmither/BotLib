package org.smither.botLib;

import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.data.message.TranslationMessage;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoHandler;
import com.github.steveice10.mc.protocol.data.status.handler.ServerPingTimeHandler;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.mc.protocol.packet.login.server.LoginSuccessPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import org.smither.botLib.environment.ChunkManager;

import java.net.Proxy;
import java.util.Arrays;

public class Bot {

	private static final Proxy PROXY = Proxy.NO_PROXY;

	private static final Proxy AUTH_PROXY = Proxy.NO_PROXY;

	private ChunkManager chunkManager = ChunkManager.getInstance();

	public Bot() {
		status();
		login();
	}

	private void status() {
		MinecraftProtocol protocol = new MinecraftProtocol(SubProtocol.STATUS);
		Client client = new Client("smither.org", 25566, protocol, new TcpSessionFactory(PROXY));
		client.getSession().setFlag(MinecraftConstants.AUTH_PROXY_KEY, AUTH_PROXY);
		client.getSession().setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY,
			(ServerInfoHandler) (session, info) -> {
			System.out.println("Version: " + info.getVersionInfo().getVersionName() + ", " + info.getVersionInfo().getProtocolVersion());
			System.out.println("Player Count: " + info.getPlayerInfo().getOnlinePlayers() + " / " + info.getPlayerInfo().getMaxPlayers());
			System.out.println("Players: " + Arrays.toString(info.getPlayerInfo().getPlayers()));
			System.out.println("Description: " + info.getDescription().getFullText());
			System.out.println("Icon: " + info.getIcon());
		});
		client.getSession().setFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, (ServerPingTimeHandler) (session,
		                                                                                                      pingTime) -> System.out.println("Server ping took " + pingTime + "ms"));
		client.getSession().connect();
	}

	private void login() {
		MinecraftProtocol protocol = null;
		protocol = new MinecraftProtocol("bot");
		Client client = new Client("smither.org", 25566, protocol, new TcpSessionFactory(PROXY));
		client.getSession().setFlag(MinecraftConstants.AUTH_PROXY_KEY, AUTH_PROXY);
		client.getSession().addListener(new SessionAdapter() {
			@Override
			public void packetReceived(PacketReceivedEvent event) {
				System.out.println(event.getPacket().getClass().getCanonicalName());
				if (event.getPacket() instanceof LoginSuccessPacket) {
				} else if (event.getPacket() instanceof ServerChatPacket) {
					Message message = event.<ServerChatPacket>getPacket().getMessage();
					System.out.println("Received Message: " + message.getFullText());
					if (message instanceof TranslationMessage) {
						System.out.println("Received Translation Components: " + Arrays.toString(((TranslationMessage) message).getTranslationParams()));
					}
				} else if (event.getPacket() instanceof ServerChunkDataPacket) {
					chunkManager.updateChunk(event.getPacket());
				}
			}

			@Override
			public void disconnected(DisconnectedEvent event) {
				System.out.println("Disconnected: " + Message.fromString(event.getReason()).getFullText());
				if (event.getCause() != null) {
					event.getCause().printStackTrace();
				}
			}
		});
		client.getSession().connect();
	}
}
