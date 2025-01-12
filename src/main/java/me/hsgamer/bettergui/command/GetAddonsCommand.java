package me.hsgamer.bettergui.command;

import me.hsgamer.bettergui.BetterGUI;
import me.hsgamer.bettergui.Permissions;
import me.hsgamer.bettergui.manager.ExtraAddonManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.Arrays;

import static me.hsgamer.hscore.bukkit.utils.MessageUtils.sendMessage;

public final class GetAddonsCommand extends BukkitCommand {
  private final BetterGUI plugin;

  public GetAddonsCommand(BetterGUI plugin) {
    super("addons", "Get the loaded addons", "/addons", Arrays.asList("menuaddons", "getmenuaddons"));
    this.plugin = plugin;
    setPermission(Permissions.ADDONS.getName());
  }

  @Override
  public boolean execute(CommandSender sender, String commandLabel, String[] args) {
    if (!testPermission(sender)) {
      return false;
    }

    boolean shortMessage = args.length > 0 && args[0].equalsIgnoreCase("short");

    sendMessage(sender, "&b&lLoaded Addons:");
    plugin.getAddonManager().getLoadedAddons().forEach((name, addon) -> {
      sendMessage(sender, "  &f- &a" + name);
      if (!shortMessage) {
        sendMessage(sender, "    &eVersion: &f" + addon.getDescription().getVersion());
        sendMessage(sender, "    &eAuthors: &f" + ExtraAddonManager.getAuthors(addon));
        sendMessage(sender, "    &eDescription: &f" + ExtraAddonManager.getDescription(addon));
      }
    });
    return true;
  }
}
