package dev.keepinv;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class KeepInventoryPotion extends JavaPlugin {

    private KeepInvPotions potions;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        potions = new KeepInvPotions(this);

        if (getConfig().getBoolean("brewing.enabled", true)) {
            String matName = getConfig().getString("brewing.fragment-material", "COMMAND_BLOCK_MINECART");
            Material material = Material.matchMaterial(matName);
            if (material == null) {
                getLogger().warning("Material invalido '" + matName + "', usando COMMAND_BLOCK_MINECART.");
                material = Material.COMMAND_BLOCK_MINECART;
            }

            String modelStr = getConfig().getString("brewing.fragment-item-model", "soulshards:respawn_fragment");
            NamespacedKey model = NamespacedKey.fromString(modelStr);
            if (model == null) {
                getLogger().warning("item_model invalido '" + modelStr + "', usando soulshards:respawn_fragment.");
                model = NamespacedKey.fromString("soulshards:respawn_fragment");
            }

            potions.registerBrewing(material, model);
            getLogger().info("Receta: Pocion rara + " + material + " [" + model + "] -> Pocion de Keep Inventory");
        }

        getServer().getPluginManager().registerEvents(new DeathListener(), this);

        var cmd = getCommand("keepinv");
        if (cmd != null) {
            var handler = new GiveCommand(potions);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
    }

    @Override
    public void onDisable() {
        if (potions != null) potions.unregisterBrewing();
    }
}
