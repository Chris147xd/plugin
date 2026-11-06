package dev.keepinv;

import io.papermc.paper.potion.PotionMix;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Locale;

/** Crea las pociones, las identifica y registra las recetas del alambique. */
public final class KeepInvPotions {

    /** Efecto "marcador": si el jugador lo tiene, esta protegido. (Cambia el icono con el texture pack.) */
    public static final PotionEffectType MARKER_EFFECT = PotionEffectType.LUCK;

    /** 10 minutos = 12000 ticks. */
    public static final int DURATION_TICKS = 12000;

    public enum Kind {
        NORMAL(Material.POTION, 1, "Poción de Keep Inventory"),
        SPLASH(Material.SPLASH_POTION, 1, "Poción arrojadiza de Keep Inventory"),
        // Vanilla divide x4 la duracion de las persistentes, asi que guardamos x4 para que la nube aplique 10 min.
        LINGERING(Material.LINGERING_POTION, 4, "Poción persistente de Keep Inventory");

        final Material material;
        final int durationMultiplier;
        final String displayName;

        Kind(Material material, int durationMultiplier, String displayName) {
            this.material = material;
            this.durationMultiplier = durationMultiplier;
            this.displayName = displayName;
        }
    }

    private final JavaPlugin plugin;
    private final NamespacedKey potionKey;
    private final List<NamespacedKey> mixKeys;
    private Material fragmentMaterial;
    private NamespacedKey fragmentModel;

    public KeepInvPotions(JavaPlugin plugin) {
        this.plugin = plugin;
        this.potionKey = new NamespacedKey(plugin, "keep_inventory_potion");
        this.mixKeys = List.of(
                new NamespacedKey(plugin, "keepinv_base"),
                new NamespacedKey(plugin, "keepinv_splash"),
                new NamespacedKey(plugin, "keepinv_lingering"));
    }

    public ItemStack create(Kind kind) {
        ItemStack item = new ItemStack(kind.material);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setColor(Color.fromRGB(0x8C8C8C)); // pocion gris
        meta.itemName(Component.text(kind.displayName));
        meta.addCustomEffect(new PotionEffect(
                MARKER_EFFECT, DURATION_TICKS * kind.durationMultiplier, 0,
                false, true, true), true);
        meta.getPersistentDataContainer().set(potionKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isKeepInvPotion(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(potionKey, PersistentDataType.BYTE);
    }

    private boolean isKind(ItemStack item, Kind kind) {
        return item != null && item.getType() == kind.material && isKeepInvPotion(item);
    }

    private boolean isAwkward(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) return false;
        ItemMeta meta = item.getItemMeta();
        return meta instanceof PotionMeta pm
                && pm.getBasePotionType() == PotionType.AWKWARD
                && !isKeepInvPotion(item);
    }

    /**
     * Ingrediente personalizado: Fragmento de Reaparicion (del datapack).
     * Se reconoce por el tipo de item base y por su item_model.
     */
    private boolean isRespawnFragment(ItemStack item) {
        if (item == null || item.getType() != fragmentMaterial) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasItemModel() && fragmentModel.equals(meta.getItemModel());
    }

    /** Registra las recetas del alambique. */
    public void registerBrewing(Material fragmentMaterial, NamespacedKey fragmentModel) {
        this.fragmentMaterial = fragmentMaterial;
        this.fragmentModel = fragmentModel;
        var brewer = Bukkit.getPotionBrewer();

        // Pocion rara + Fragmento de Reaparicion -> Pocion de Keep Inventory
        brewer.addPotionMix(new PotionMix(
                mixKeys.get(0), create(Kind.NORMAL),
                PotionMix.createPredicateChoice(this::isAwkward),
                PotionMix.createPredicateChoice(this::isRespawnFragment)));

        // + polvora -> arrojadiza
        brewer.addPotionMix(new PotionMix(
                mixKeys.get(1), create(Kind.SPLASH),
                PotionMix.createPredicateChoice(i -> isKind(i, Kind.NORMAL)),
                new RecipeChoice.MaterialChoice(Material.GUNPOWDER)));

        // + aliento de dragon -> persistente
        brewer.addPotionMix(new PotionMix(
                mixKeys.get(2), create(Kind.LINGERING),
                PotionMix.createPredicateChoice(i -> isKind(i, Kind.SPLASH)),
                new RecipeChoice.MaterialChoice(Material.DRAGON_BREATH)));
    }

    public void unregisterBrewing() {
        var brewer = Bukkit.getPotionBrewer();
        for (NamespacedKey k : mixKeys) brewer.removePotionMix(k);
    }
}
