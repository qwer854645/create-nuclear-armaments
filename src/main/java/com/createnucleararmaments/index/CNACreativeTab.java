package com.createnucleararmaments.index;

import com.createnucleararmaments.CNArmaments;
import com.createnucleararmaments.CreateNuclearArmaments;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashSet;
import java.util.Set;

public final class CNACreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateNuclearArmaments.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.createnucleararmaments"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> CNAItems.ARMAMENT_URANIUM_BILLET.asStack())
            .displayItems((parameters, output) -> {
                Set<Item> displayedItems = new LinkedHashSet<>();

                for (var item : CNArmaments.REGISTRATE.getAll(Registries.ITEM)) {
                    Item registeredItem = item.get();
                    if (displayedItems.add(registeredItem)) {
                        output.accept(new ItemStack(registeredItem));
                    }
                }

                for (var block : CNArmaments.REGISTRATE.getAll(Registries.BLOCK)) {
                    Item blockItem = block.get().asItem();
                    if (blockItem != Items.AIR && displayedItems.add(blockItem)) {
                        output.accept(new ItemStack(blockItem));
                    }
                }
            })
            .build());

    private CNACreativeTab() {
    }
}
