package net.yiran.tetrajs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.yiran.tetrajs.data.RequirementDataManager;
import se.mickelus.mutil.util.HexCodec;
import se.mickelus.tetra.blocks.scroll.ScrollData;
import se.mickelus.tetra.blocks.scroll.ScrollItem;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static net.yiran.tetrajs.TetraJS.MODID;
import static net.yiran.tetrajs.TetraJS.ModEventBus;

public class CreativeTabHandler {
    public static void init() {
        if (needCreateCreativeTab()) {
            registerCreativeModeTab();
        } else {
            ModEventBus.addListener(CreativeTabHandler::onCreativeTabsBuild);
        }
    }

    private static boolean needCreateCreativeTab() {
        var path = FMLPaths.CONFIGDIR.get().resolve("tetrajs-common.toml");
        if (!Files.exists(path)) return true;
        try {
            return Files.readAllLines(path)
                    .stream()
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith("#"))
                    .filter(line -> line.contains("CreateSingleCreativeTab"))
                    .anyMatch(line -> line.contains("true"));
        } catch (IOException e) {
            return true;
        }
    }

    private static void addScrollItems(CreativeModeTab.Output output) {
        RequirementDataManager.instance.scrolls
                .getData()
                .values()
                .stream()
                .flatMap(Stream::of)
                .map(CreativeTabHandler::getScrollItem)
                .forEach(output::accept);
    }

    public static void writeScrollData(ItemStack stack) {
        var data = new JsonArray();
        var data1 = getScrollData(stack).getAsJsonObject();
        data1.addProperty("ribbon", Integer.parseInt(data1.get("ribbon").getAsString(), 16));
        data.add(data1);
        var path = FMLPaths.GAMEDIR.get()
                .resolve("kubejs")
                .resolve("data")
                .resolve("tetrajs")
                .resolve("scrolls")
                .resolve(data1.get("key").getAsString().replace(":", "_").replace("/", "_") + ".json");
        if (!Files.exists(path.getParent())) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        try (Writer fileWriter = Files.newBufferedWriter(path)) {
            JsonWriter jsonWriter = new JsonWriter(fileWriter);
            jsonWriter.setIndent("\t");
            jsonWriter.setSerializeNulls(true);
            jsonWriter.setLenient(true);
            Streams.write(data, jsonWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonElement getScrollData(ItemStack stack) {
        var data1 = ScrollData.read(stack);
        return ScrollDataCodec.encodeStart(JsonOps.INSTANCE, data1).result().get();
    }

    private static ItemStack getScrollItem(ScrollData scrollData) {
        var stack = new ItemStack(ScrollItem.instance);
        var tag = new CompoundTag();
        stack.addTagElement("BlockEntityTag", tag);
        var list = new ListTag();
        tag.put("data", list);
        list.add(ScrollDataCodec.encodeStart(NbtOps.INSTANCE, scrollData).result().get());
        return stack;
    }

    private static final Codec<Integer> IntClamp = Codec.INT.xmap(
            i -> Mth.clamp(i, 0, 15),
            i -> Mth.clamp(i, 0, 15)
    );

    private static final Codec<ScrollData> ScrollDataCodec = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(i -> i.key),
            Codec.STRING.optionalFieldOf("details").forGetter(i -> Optional.ofNullable(i.details)),
            Codec.BOOL.fieldOf("intricate").forGetter(i -> i.isIntricate),
            Codec.INT.fieldOf("material").forGetter(i -> i.material),
            HexCodec.instance.fieldOf("ribbon").forGetter(i -> i.ribbon),
            IntClamp.listOf().optionalFieldOf("glyphs", Collections.emptyList()).forGetter(i -> i.glyphs),
            ResourceLocation.CODEC.listOf().optionalFieldOf("schematics", Collections.emptyList()).forGetter(i -> i.schematics),
            ResourceLocation.CODEC.listOf().optionalFieldOf("effects", Collections.emptyList()).forGetter(i -> i.craftingEffects)
    ).apply(instance, ScrollData::new));

    private static void onCreativeTabsBuild(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation("tetra", "default"))) {
            addScrollItems(event);
        }
    }

    private static void registerCreativeModeTab() {
        DeferredRegister<CreativeModeTab> creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
        creativeTabs.register("scroll", () -> CreativeModeTab.builder()
                .icon(() -> new ItemStack(ScrollItem.instance))
                .title(Component.translatable("tetrajs.scrollGroup"))
                .displayItems((itemDisplayParameters, output) -> {
                    addScrollItems(output);
                    ScrollItem.instance.getCreativeTabItems().forEach(output::accept);
                }).build());
        creativeTabs.register(ModEventBus);
    }
}
