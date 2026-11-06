package dev.keepinv;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class DeathListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.hasPotionEffect(KeepInvPotions.MARKER_EFFECT)) return;

        // Keep inventory total: objetos, armadura, mano secundaria y experiencia.
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
        event.setDroppedExp(0);

        // El efecto se consume sin importar el tiempo restante.
        player.removePotionEffect(KeepInvPotions.MARKER_EFFECT);

        player.sendMessage(Component.text("Tu Poción de Keep Inventory se consumió.", NamedTextColor.GRAY));
    }
}
