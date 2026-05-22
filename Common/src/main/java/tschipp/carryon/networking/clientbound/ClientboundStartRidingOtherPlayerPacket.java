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

package tschipp.carryon.networking.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import tschipp.carryon.Constants;
import tschipp.carryon.networking.PacketBase;

public record ClientboundStartRidingOtherPlayerPacket(int mount, int rider, boolean ride) implements PacketBase
{
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundStartRidingOtherPlayerPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ClientboundStartRidingOtherPlayerPacket::mount,
			ByteBufCodecs.INT, ClientboundStartRidingOtherPlayerPacket::rider,
			ByteBufCodecs.BOOL, ClientboundStartRidingOtherPlayerPacket::ride,
			ClientboundStartRidingOtherPlayerPacket::new
	);

	public static final CustomPacketPayload.Type<ClientboundStartRidingOtherPlayerPacket> TYPE = new CustomPacketPayload.Type<>(Constants.PACKET_ID_START_RIDING_OTHER);

	@Override
	public void handle(Player player)
	{
		Entity mount = player.level().getEntity(this.mount);
		Entity rider = player.level().getEntity(this.rider);

		if(mount != null && rider != null)
			if(ride)
				rider.startRiding(mount, true);
			else
				rider.stopRiding();
	}

	@Override
	public Type<ClientboundStartRidingOtherPlayerPacket> type() {
		return TYPE;
	}
}
