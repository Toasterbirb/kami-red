package me.zeroeightsix.kami.module.modules.gui;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.modules.movement.TimerSpeed;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.ColourUtils;
import me.zeroeightsix.kami.util.InfoCalculator;
import me.zeroeightsix.kami.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;

import static me.zeroeightsix.kami.util.ColourUtils.getStringColour;

/**
 * @author S-B99
 * Created by S-B99 on 04/12/19
 * Updated by S-B99 on 05/03/20
 * PVP Information by Polymer on 04/03/20
 */
@Module.Info(name = "InfoOverlay", category = Module.Category.GUI, description = "Configures the game information overlay", showOnArray = Module.ShowOnArray.OFF)
public class InfoOverlay extends Module {
    /* This is so horrible but there's no other way */
    private Setting<Page> page = register(Settings.enumBuilder(Page.class).withName("Page").withValue(Page.ONE).build());
    /* Page One */
    private Setting<Boolean> version = register(Settings.booleanBuilder("Version").withValue(true).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> username = register(Settings.booleanBuilder("Username").withValue(true).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> tps = register(Settings.booleanBuilder("TPS").withValue(true).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> fps = register(Settings.booleanBuilder("FPS").withValue(true).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> ping = register(Settings.booleanBuilder("Ping").withValue(false).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> durability = register(Settings.booleanBuilder("Item Damage").withValue(false).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> memory = register(Settings.booleanBuilder("RAM Used").withValue(false).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    private Setting<Boolean> timerSpeed = register(Settings.booleanBuilder("Timer Speed").withValue(false).withVisibility(v -> page.getValue().equals(Page.ONE)).build());
    /* Page Two */
    private Setting<Boolean> totems = register(Settings.booleanBuilder("Totems").withValue(false).withVisibility(v -> page.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> endCrystals = register(Settings.booleanBuilder("End Crystals").withValue(false).withVisibility(v -> page.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> expBottles = register(Settings.booleanBuilder("EXP Bottles").withValue(false).withVisibility(v -> page.getValue().equals(Page.TWO)).build());
    private Setting<Boolean> godApples = register(Settings.booleanBuilder("God Apples").withValue(false).withVisibility(v -> page.getValue().equals(Page.TWO)).build());
    /* Page Three */
    private Setting<Boolean> speed = register(Settings.booleanBuilder("Speed").withValue(true).withVisibility(v -> page.getValue().equals(Page.THREE)).build());
    private Setting<SpeedUnit> speedUnit = register(Settings.enumBuilder(SpeedUnit.class).withName("Speed Unit").withValue(SpeedUnit.KMH).withVisibility(v -> page.getValue().equals(Page.THREE) && speed.getValue()).build());
    private Setting<Boolean> time = register(Settings.booleanBuilder("Time").withValue(true).withVisibility(v -> page.getValue().equals(Page.THREE)).build());
    private Setting<TimeUtil.TimeType> timeTypeSetting = register(Settings.enumBuilder(TimeUtil.TimeType.class).withName("Time Format").withValue(TimeUtil.TimeType.HHMMSS).withVisibility(v -> page.getValue().equals(Page.THREE) && time.getValue()).build());
    private Setting<TimeUtil.TimeUnit> timeUnitSetting = register(Settings.enumBuilder(TimeUtil.TimeUnit.class).withName("Time Unit").withValue(TimeUtil.TimeUnit.H12).withVisibility(v -> page.getValue().equals(Page.THREE) && time.getValue()).build());
    private Setting<Boolean> doLocale = register(Settings.booleanBuilder("Time Show AMPM").withValue(true).withVisibility(v -> page.getValue().equals(Page.THREE) && time.getValue()).build());
    private Setting<ColourUtils.ColourCode> firstColour = register(Settings.enumBuilder(ColourUtils.ColourCode.class).withName("First Colour").withValue(ColourUtils.ColourCode.WHITE).withVisibility(v -> page.getValue().equals(Page.THREE)).build());
    private Setting<ColourUtils.ColourCode> secondColour = register(Settings.enumBuilder(ColourUtils.ColourCode.class).withName("Second Colour").withValue(ColourUtils.ColourCode.BLUE).withVisibility(v -> page.getValue().equals(Page.THREE)).build());

    private enum SpeedUnit {
        MPS, KMH;
    }

    private enum Page {
        ONE, TWO, THREE
    }

    public boolean useUnitKmH() {
        return speedUnit.getValue().equals(SpeedUnit.KMH);
    }

    private String unitType(SpeedUnit s) {
        switch (s) {
            case MPS: return "m/s";
            case KMH: return "km/h";
            default: return "Invalid unit type (mps or kmh)";
        }
    }

    private String formatTimerSpeed() {
        String formatted = textColour(secondColour.getValue()) + "." + textColour(firstColour.getValue());
        return TimerSpeed.returnGui().replace(".", formatted);
    }

    private String textColour(ColourUtils.ColourCode c) {
        return getStringColour(c);
    }

    public static int getItems(Item i) {
        return mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == i).mapToInt(ItemStack::getCount).sum() + mc.player.inventory.offHandInventory.stream().filter(itemStack -> itemStack.getItem() == i).mapToInt(ItemStack::getCount).sum();
    }

    public ArrayList<String> infoContents() {
        ArrayList<String> infoContents = new ArrayList<>();
        if (version.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + KamiMod.KAMI_KANJI + textColour(secondColour.getValue()) + " " + KamiMod.MODVER);
        } if (username.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + "Welcome" + textColour(secondColour.getValue()) + " " + mc.getSession().getUsername() + "!");
        } if (time.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + TimeUtil.getFinalTime(secondColour.getValue(), firstColour.getValue(), timeUnitSetting.getValue(), timeTypeSetting.getValue(), doLocale.getValue()) + TextFormatting.RESET);
        } if (tps.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + InfoCalculator.tps() + textColour(secondColour.getValue()) + " tps");
        } if (fps.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + Minecraft.debugFPS + textColour(secondColour.getValue()) + " fps");
        } if (speed.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + InfoCalculator.speed() + textColour(secondColour.getValue()) + " " + unitType(speedUnit.getValue()));
        } if (timerSpeed.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + formatTimerSpeed() + textColour(secondColour.getValue()) + "t");
        } if (ping.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + InfoCalculator.ping() + textColour(secondColour.getValue()) + " ms");
        } if (durability.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + InfoCalculator.dura() + textColour(secondColour.getValue()) + " dura");
        } if (memory.getValue()) {
            infoContents.add(textColour(firstColour.getValue()) + InfoCalculator.memory() + textColour(secondColour.getValue()) + "mB free");
        } if (totems.getValue()) {
        	infoContents.add(textColour(firstColour.getValue()) + getItems(Items.TOTEM_OF_UNDYING) + textColour(secondColour.getValue()) + " Totems");
        } if (endCrystals.getValue()) {
        	infoContents.add(textColour(firstColour.getValue()) + getItems(Items.END_CRYSTAL) + textColour(secondColour.getValue()) + " Crystals");
        } if (expBottles.getValue()) {
        	infoContents.add(textColour(firstColour.getValue()) + getItems(Items.EXPERIENCE_BOTTLE) + textColour(secondColour.getValue()) + " EXP Bottles");
        } if (godApples.getValue()) {
        	infoContents.add(textColour(firstColour.getValue()) + getItems(Items.GOLDEN_APPLE) + textColour(secondColour.getValue()) + " God Apples");
        }
        return infoContents;
    }

    public void onDisable() { this.enable(); }
}
