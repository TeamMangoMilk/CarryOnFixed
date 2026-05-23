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

package mangomilk.carryon.networking.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import mangomilk.carryon.Constants;
import mangomilk.carryon.common.carry.CarryOnData;
import mangomilk.carryon.common.carry.CarryOnDataManager;
import mangomilk.carryon.networking.PacketBase;

public record ServerboundCarryKeyPressedPacket(boolean pressed) implements PacketBase
{
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCarryKeyPressedPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, ServerboundCarryKeyPressedPacket::pressed,
			ServerboundCarryKeyPressedPacket::new
	);

	public static final CustomPacketPayload.Type<ServerboundCarryKeyPressedPacket> TYPE = new Type<>(Constants.PACKET_ID_KEY_PRESSED);

	@Override
	public void handle(Player player)
	{
		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		carry.setKeyPressed(this.pressed);
		CarryOnDataManager.setCarryData(player, carry);
	}

	@Override
	public Type<ServerboundCarryKeyPressedPacket> type() {
		return TYPE;
	}
}
