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

package mangomilk.carryon;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import mangomilk.carryon.common.carry.CarryOnData;
import mangomilk.carryon.config.neoforge.ConfigLoaderImpl;

import java.util.function.Supplier;

@Mod(Constants.MOD_ID)
public class CarryOnNeoForge {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

    public static final Supplier<AttachmentType<CarryOnData>> CARRY_ON_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "carry_on_data",
            () -> AttachmentType.builder(CarryOnData::empty)
                    //.sync(new CarryOnDataSyncHandler())
                    //.serialize(CarryOnData.CODEC.fieldOf(CarryOnData.SERIALIZATION_KEY))
                    .serialize(CarryOnData.CODEC)
                    .build()
    );

    public CarryOnNeoForge(ModContainer container) {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
        // Use Forge to bootstrap the Common mod.
        CarryOnCommon.registerConfig();
        container.getEventBus().addListener(this::setup);
        container.getEventBus().addListener(this::registerPackets);

        ConfigLoaderImpl.initialize(container);

        ATTACHMENT_TYPES.register(container.getEventBus());
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }

    public void registerPackets(final RegisterPayloadHandlersEvent event) {

        final PayloadRegistrar registrar = event.registrar("1.0.0");

        CarryOnCommon.registerServerPackets(registrar);
        CarryOnCommon.registerClientPackets(registrar);
    }

}
