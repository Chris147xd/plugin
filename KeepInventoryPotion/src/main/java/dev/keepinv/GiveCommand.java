package dev.keepinv;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** /keepinv give <jugador> <normal|splash|lingering> [cantidad] */
public final class GiveCommand implements TabExecutor {

    private final KeepInvPotions potions;

    public GiveCommand(KeepInvPotions potions) {
        this.potions = potions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage("Uso: /" + label + " give <jugador> <normal|splash|lingering> [cantidad]");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Jugador no encontrado: " + args[1]);
            return true;
        }
        KeepInvPotions.Kind kind;
        try {
            kind = KeepInvPotions.Kind.valueOf(args[2].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Tipo invalido. Usa: normal, splash o lingering.");
            return true;
        }
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Math.min(64, Integer.parseInt(args[3])));
            } catch (NumberFormatException e) {
                sender.sendMessage("Cantidad invalida.");
                return true;
            }
        }
        for (int i = 0; i < amount; i++) {
            ItemStack stack = potions.create(kind);
            var leftover = target.getInventory().addItem(stack);
            leftover.values().forEach(it -> target.getWorld().dropItemNaturally(target.getLocation(), it));
        }
        sender.sendMessage("Diste " + amount + " pocion(es) " + kind.name().toLowerCase(Locale.ROOT) + " a " + target.getName() + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) out.add("give");
        else if (args.length == 2) Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        else if (args.length == 3) out.addAll(List.of("normal", "splash", "lingering"));
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(s -> !s.toLowerCase(Locale.ROOT).startsWith(prefix));
        return out;
    }
}
