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

package mangomilk.carryon.config.neoforge;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import mangomilk.carryon.Constants;
import mangomilk.carryon.config.AnnotationData;
import mangomilk.carryon.config.BuiltCategory;
import mangomilk.carryon.config.BuiltConfig;
import mangomilk.carryon.config.PropertyData;

import java.util.*;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ConfigLoaderImpl {

    public static final Map<ModConfigSpec, BuiltConfig> CONFIGS = new HashMap<>();

    public static void initialize(ModContainer container) {

        ConfigLoaderImpl.CONFIGS.forEach((spec, config) -> {

            if(config.fileName.contains("client"))
                container.registerConfig(ModConfig.Type.CLIENT, spec, config.fileName+".toml");
            else
                container.registerConfig(ModConfig.Type.COMMON, spec, config.fileName+".toml");
        });
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading loading) {
        loadConfig((ModConfigSpec) loading.getConfig().getSpec());

//        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
//            ConfigLoader.onConfigLoaded(Minecraft.getInstance().level.registryAccess());
//        });
//
//        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
//            ConfigLoader.onConfigLoaded(ServerLifecycleHooks.getCurrentServer().registryAccess());
//        });
    }
    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading loading) {
          loadConfig((ModConfigSpec) loading.getConfig().getSpec());
//        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
//            ConfigLoader.onConfigLoaded(Minecraft.getInstance().level.registryAccess());
//        });
//
//        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
//            ConfigLoader.onConfigLoaded(ServerLifecycleHooks.getCurrentServer().registryAccess());
//        });
    }

    private static void loadConfig(ModConfigSpec spec) {
        BuiltConfig builtConfig = CONFIGS.get(spec);
        if (builtConfig == null) return;
        loadConfig(builtConfig, spec.getValues());
    }

    private static void loadConfig(BuiltCategory category, UnmodifiableConfig config) {
        config.valueMap().forEach((id, value) -> {
            if (value instanceof ModConfigSpec.ConfigValue<?> configValue) {
                category.getProperty(id).ifPresent(data -> {
                    if (configValue instanceof ModConfigSpec.BooleanValue booleanValue)
                        data.setBoolean(booleanValue.get());
                    if (configValue instanceof ModConfigSpec.IntValue intValue)
                        data.setInt(intValue.get());
                    if (configValue instanceof ModConfigSpec.DoubleValue doubleValue)
                        data.setDouble(doubleValue.get());
                    if(configValue.get() instanceof List<?> listVal)
                        data.setStringArray(listVal.toArray(new String[listVal.size()]));
                });
            } else if (value instanceof AbstractConfig subConfig) {
                category.getCategory(id).ifPresent(cat -> loadConfig(cat, subConfig));
            }
        });
    }

    public static void registerConfig(BuiltConfig config) {
        try {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            for (PropertyData property : config.properties) buildProperty(builder, property);
            for (BuiltCategory category : config.categories) buildCategory(builder, category);
            CONFIGS.put(builder.build(), config);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildCategory(ModConfigSpec.Builder builder, BuiltCategory category) throws IllegalAccessException {
        builder.push(category.category);
        if (category.categoryDesc != null) builder.comment(category.categoryDesc);
        for (PropertyData property : category.properties) buildProperty(builder, property);
        for (BuiltCategory builtCategory : category.categories) buildCategory(builder, builtCategory);
        builder.pop();
    }

    private static void buildProperty(ModConfigSpec.Builder builder, PropertyData data) throws IllegalAccessException {
        AnnotationData annotationData = data.data();
        builder.comment(annotationData.description());

        switch (annotationData.type()) {
            case BOOLEAN -> builder.define(data.getId(), data.getBoolean());
            case INT -> builder.defineInRange(data.getId(), data.getInt(), annotationData.min(), annotationData.max());
            case DOUBLE -> builder.defineInRange(data.getId(), data.getDouble(), annotationData.minD(), annotationData.maxD());
            case STRING_ARRAY -> builder.defineListAllowEmpty(List.of(data.getId()), () -> {
                try {
                    return Arrays.asList(data.getStringArray());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return new ArrayList<>();
            }, obj -> obj instanceof String);
            default -> throw new IllegalAccessException("Unknown property type.");
        }
    }
}
