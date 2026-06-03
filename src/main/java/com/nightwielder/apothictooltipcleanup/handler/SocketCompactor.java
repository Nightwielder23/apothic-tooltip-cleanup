package com.nightwielder.apothictooltipcleanup.handler;

import com.mojang.datafixers.util.Either;
import com.nightwielder.apothictooltipcleanup.ApothicTooltipCleanup;
import com.nightwielder.apothictooltipcleanup.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// Merges empty sockets on the icon render path where Apotheosis inserts its SocketComponent, reached
// by reflection to avoid a compile time dependency. With two or more empty sockets, all empty
// collapses to one text line and mixed keeps the filled gems with a summary below.
@Mod.EventBusSubscriber(modid = ApothicTooltipCleanup.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SocketCompactor {
    private static final String SOCKET_COMPONENT_FQN = "dev.shadowsoffire.apotheosis.adventure.client.SocketTooltipRenderer$SocketComponent";

    private static volatile Method gemsAccessor;
    private static volatile Method socketedAccessor;
    private static volatile Method isValidAccessor;
    private static volatile Constructor<?> socketedGemsCtor;
    private static volatile Constructor<?> socketComponentCtor;
    private static volatile boolean reflectionFailed;

    private SocketCompactor() {}

    // Collapses two or more empty sockets in Apotheosis's socket component into one summary line.
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        if (!Config.MERGE_EMPTY_SOCKETS.get()) {
            return;
        }
        if (reflectionFailed) {
            return;
        }

        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        for (int i = 0; i < elements.size(); i++) {
            TooltipComponent tc = elements.get(i).right().orElse(null);
            if (tc == null) {
                continue;
            }
            if (!SOCKET_COMPONENT_FQN.equals(tc.getClass().getName())) {
                continue;
            }

            try {
                if (gemsAccessor == null) {
                    gemsAccessor = tc.getClass().getMethod("gems");
                }
                if (socketedAccessor == null) {
                    socketedAccessor = tc.getClass().getMethod("socketed");
                }
                Object gems = gemsAccessor.invoke(tc);
                if (!(gems instanceof List<?> list)) {
                    continue;
                }

                List<Object> valid = new ArrayList<>();
                int empty = 0;
                for (Object inst : list) {
                    if (inst == null) {
                        empty++;
                        continue;
                    }
                    if (isValidAccessor == null) {
                        isValidAccessor = inst.getClass().getMethod("isValid");
                    }
                    if ((boolean) isValidAccessor.invoke(inst)) {
                        valid.add(inst);
                    } else {
                        empty++;
                    }
                }
                if (empty < 2) {
                    continue;
                }

                Component summary = Component.literal("◇ x" + empty + " empty sockets")
                        .withStyle(ChatFormatting.GRAY);

                if (valid.isEmpty()) {
                    elements.set(i, Either.<FormattedText, TooltipComponent>left(summary));
                    continue;
                }

                // mixed: rebuild with only the filled gems, then add the summary line after.
                if (socketedGemsCtor == null) {
                    socketedGemsCtor = gems.getClass().getConstructor(List.class);
                }
                Object newSocketedGems = socketedGemsCtor.newInstance(valid);
                ItemStack stack = (ItemStack) socketedAccessor.invoke(tc);
                if (socketComponentCtor == null) {
                    socketComponentCtor = tc.getClass().getConstructor(ItemStack.class, gems.getClass());
                }
                Object newSocketComponent = socketComponentCtor.newInstance(stack, newSocketedGems);
                elements.set(i, Either.<FormattedText, TooltipComponent>right((TooltipComponent) newSocketComponent));
                elements.add(i + 1, Either.<FormattedText, TooltipComponent>left(summary));
                i++;
            } catch (Throwable ignored) {
                reflectionFailed = true;
                return;
            }
        }
    }
}
