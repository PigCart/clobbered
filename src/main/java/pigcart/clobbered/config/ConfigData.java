package pigcart.clobbered.config;

import pigcart.clobbered.config.gui.Annotations.*;


public class ConfigData {
    @NoGUI public byte configVersion = 0;

    public int minimumDropStrength = 4;
    public int maximumDropStrength = 20;
    public boolean showStrengthBar = true;
    public float interactionRadius = 0.3F;
    public float damagePowerScaling = 0.2F;
    public boolean automaticItemPickUp = false;
    public boolean automaticArrowPickUp = false;
}