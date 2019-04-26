package org.smither.botLib.environment;

import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;

public class ChunkManager {
	private static ChunkManager instance;

	private ChunkManager() {

	}

	public static ChunkManager getInstance() {
		if (instance == null) {
			instance = new ChunkManager();
		}
		return instance;
	}

	public void updateChunk(ServerChunkDataPacket chunkDataPacket) {
		System.out.println(chunkDataPacket.getColumn().getTileEntities()[0]);
	}

}
