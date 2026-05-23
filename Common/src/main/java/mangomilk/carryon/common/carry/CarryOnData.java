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

package mangomilk.carryon.common.carry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import mangomilk.carryon.Constants;
import mangomilk.carryon.common.scripting.CarryOnScript;

import javax.annotation.Nullable;
import java.util.Optional;

public class CarryOnData {

    private CarryType type;
    private CompoundTag nbt;
    private boolean keyPressed = false;
    private CarryOnScript activeScript;
    private int selectedSlot = 0;


    public static final Codec<CarryOnData> CODEC = CompoundTag.CODEC.flatXmap(
            tag -> {
                try {
                    return DataResult.success(new CarryOnData(tag));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            },
            carry -> {
                try {
                    return DataResult.success(carry.getNbt());
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CarryOnData> STREAM_CODEC = StreamCodec.of(
            (buf, carry) -> ByteBufCodecs.TRUSTED_COMPOUND_TAG.encode(buf, carry.getNbt()),
            buf -> new CarryOnData(ByteBufCodecs.TRUSTED_COMPOUND_TAG.decode(buf))
    );

    public static final String SERIALIZATION_KEY = "CarryOnData";

    public static CarryOnData empty() {
        return new CarryOnData(new CompoundTag());
    }

    public CarryOnData(CompoundTag data)
    {
        this.type = parseType(data);

        this.nbt = this.type == CarryType.INVALID ? new CompoundTag() : data.copy();

        if(data.contains("keyPressed"))
            this.keyPressed = data.getBoolean("keyPressed");

        if(data.contains("activeScript"))
            this.activeScript = decodeActiveScript(data.get("activeScript"));

        if(data.contains("selected"))
            this.selectedSlot = data.getInt("selected");

    }

    public CompoundTag getNbt()
    {
        nbt.putString("type", type.toString());
        nbt.putBoolean("keyPressed", keyPressed);
        Optional<Tag> encodedScript = encodeActiveScript();
        if(encodedScript.isPresent())
            nbt.put("activeScript", encodedScript.get());
        else
            nbt.remove("activeScript");
        nbt.putInt("selected", this.selectedSlot);
        return nbt;
    }

    public CompoundTag getContentNbt()
    {
        if(type == CarryType.BLOCK && nbt.contains("block"))
            return nbt.getCompound("block");
        else if(type == CarryType.ENTITY && nbt.contains("entity"))
            return nbt.getCompound("entity");
        return null;
    }

    public void setBlock(BlockState state, @Nullable BlockEntity tile)
    {
        this.type = CarryType.BLOCK;

        if(state.hasProperty(BlockStateProperties.WATERLOGGED))
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);

        CompoundTag stateData = NbtUtils.writeBlockState(state);
        nbt.put("block", stateData);

        if(tile != null)
        {
            CompoundTag tileData = tile.saveWithId(tile.getLevel().registryAccess());
            nbt.put("tile", tileData);
        }
    }

    public BlockState getBlock()
    {
        if(this.type != CarryType.BLOCK)
            throw new IllegalStateException("Called getBlock on data that contained " + this.type);

        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("block"));
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos, HolderLookup.Provider lookup)
    {
        if(this.type != CarryType.BLOCK)
            throw new IllegalStateException("Called getBlockEntity on data that contained " + this.type);

        if(!nbt.contains("tile"))
            return null;

        try {
            return BlockEntity.loadStatic(pos, this.getBlock(), nbt.getCompound("tile"), lookup);
        } catch (Exception e) {
            Constants.LOG.warn("Failed to restore carried block entity at {}, placing block without block entity data", pos, e);
            return null;
        }
    }

    public void setEntity(Entity entity)
    {
        this.type = CarryType.ENTITY;
        CompoundTag entityData = new CompoundTag();
        entity.save(entityData);
        nbt.put("entity", entityData);
    }

    public Entity getEntity(Level level)
    {
        if(this.type != CarryType.ENTITY)
            throw new IllegalStateException("Called getEntity on data that contained " + this.type);

        try {
            var optionalEntity = EntityType.create(nbt.getCompound("entity"), level);
            if(optionalEntity.isPresent())
                return optionalEntity.get();
        } catch (Exception e) {
            Constants.LOG.warn("Failed to restore carried entity from data: " + nbt, e);
        }

        Constants.LOG.error("Could not restore carried entity. Data: " + nbt);
        this.clear();
        return new AreaEffectCloud(level, 0, 0, 0);
    }

    private static CarryType parseType(CompoundTag data) {
        if(!data.contains("type"))
            return CarryType.INVALID;

        String rawType = data.getString("type");
        try {
            return CarryType.valueOf(rawType);
        } catch (IllegalArgumentException e) {
            Constants.LOG.warn("Invalid CarryOnRevamped data type '{}', clearing carried content", rawType);
            return CarryType.INVALID;
        }
    }

    @Nullable
    private static CarryOnScript decodeActiveScript(Tag scriptTag) {
        return CarryOnScript.CODEC.parse(NbtOps.INSTANCE, scriptTag)
                .resultOrPartial(message -> Constants.LOG.warn("Failed to decode CarryOnRevamped active script, ignoring it: {}", message))
                .orElse(null);
    }

    private Optional<Tag> encodeActiveScript() {
        if(activeScript == null)
            return Optional.empty();

        Optional<Tag> encoded = CarryOnScript.CODEC.encodeStart(NbtOps.INSTANCE, activeScript)
                .resultOrPartial(message -> Constants.LOG.warn("Failed to encode CarryOnRevamped active script, dropping it: {}", message));
        if(encoded.isEmpty())
            activeScript = null;
        return encoded;
    }

    public Optional<CarryOnScript> getActiveScript()
    {
        if(activeScript == null)
            return Optional.empty();
        return Optional.of(activeScript);
    }

    public void setActiveScript(CarryOnScript script)
    {
        this.activeScript = script;
    }

    public void setCarryingPlayer() {
        this.type = CarryType.PLAYER;
    }

    public boolean isCarrying()
    {
        return this.type != CarryType.INVALID;
    }

    public boolean isCarrying(CarryType type)
    {
        return this.type == type;
    }

    public boolean isKeyPressed() {return this.keyPressed;}

    public void setKeyPressed(boolean val) {
        this.keyPressed = val;
        this.nbt.putBoolean("keyPressed", val);
    }

    public void setSelected(int selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public int getSelected() {
        return this.selectedSlot;
    }

    public void clear()
    {
        this.type = CarryType.INVALID;
        this.nbt = new CompoundTag();
        this.activeScript = null;
    }

    public CarryOnData clone() {
        return new CarryOnData(nbt.copy());
    }

    public int getTick()
    {
        if(!this.nbt.contains("tick"))
            return -1;
        return this.nbt.getInt("tick");
    }

    public void setTick(int tick) {
        this.nbt.putInt("tick", tick);
    }


    public enum CarryType {
        BLOCK,
        ENTITY,
        PLAYER,
        INVALID
    }
}
