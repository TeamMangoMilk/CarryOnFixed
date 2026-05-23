/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package mangomilk.carryon.networking.clientbound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import mangomilk.carryon.Constants;
import mangomilk.carryon.common.scripting.CarryOnScript;
import mangomilk.carryon.common.scripting.ScriptManager;
import mangomilk.carryon.networking.PacketBase;

import java.util.List;

public record ClientboundSyncScriptsPacket(Tag serialized) implements PacketBase
{
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncScriptsPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.TAG, ClientboundSyncScriptsPacket::serialized,
			ClientboundSyncScriptsPacket::new
	);

	public static final CustomPacketPayload.Type<ClientboundSyncScriptsPacket> TYPE = new Type<>(Constants.PACKET_ID_SYNC_SCRIPTS);

	@Override
	public void handle(Player player)
	{
		DataResult<List<CarryOnScript>> res = Codec.list(CarryOnScript.CODEC).parse(NbtOps.INSTANCE, serialized);
		res.resultOrPartial(message -> Constants.LOG.warn("Failed deserializing CarryOnRevamped scripts on the client: {}", message))
				.ifPresent(scripts -> {
					ScriptManager.SCRIPTS.clear();
					ScriptManager.SCRIPTS.addAll(scripts);
				});
	}

	@Override
	public Type<ClientboundSyncScriptsPacket> type() {
		return TYPE;
	}
}
