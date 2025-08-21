package BobloyPatches;

import basemod.BaseMod;
import basemod.interfaces.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;

@SuppressWarnings({"unused", "WeakerAccess"})
@SpireInitializer
public class LaunchPad implements PostInitializeSubscriber {

    public static final String modID = "bobloypatches";

    public static String makeID(String idText) {
        return modID + ":" + idText;
    }

    public LaunchPad() {
        BaseMod.subscribe(this);
    }

    public static String makePath(String resourcePath) {
        return modID + "Resources/" + resourcePath;
    }

    public static String makeImagePath(String resourcePath) {
        return modID + "Resources/images/" + resourcePath;
    }

    public static void initialize() {
        LaunchPad thismod = new LaunchPad();
    }

    @Override
    public void receivePostInitialize() {

    }
}
