package me.hsgamer.bettergui.manager;

import me.hsgamer.bettergui.Permissions;
import me.hsgamer.bettergui.api.menu.Menu;
import me.hsgamer.bettergui.builder.MenuBuilder;
import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.config.Config;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * The Menu Manager
 */
public final class MenuManager {

  private final Map<String, Menu> menuMap = new HashMap<>();
  private final JavaPlugin plugin;

  public MenuManager(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  /**
   * Get all parent menus from a menu
   *
   * @param menu the start menu
   * @param uuid the unique id
   *
   * @return all parent menus
   */
  public static List<Menu> getAllParentMenu(Menu menu, UUID uuid) {
    List<Menu> list = new LinkedList<>();
    Optional<Menu> optional = menu.getParentMenu(uuid);
    while (optional.isPresent()) {
      Menu parentMenu = optional.get();
      if (list.contains(parentMenu)) {
        break;
      }
      list.add(parentMenu);
      optional = parentMenu.getParentMenu(uuid);
    }
    return list;
  }

  /**
   * Load the menu config
   */
  public void loadMenuConfig() {
    File menusFolder = new File(plugin.getDataFolder(), "menu");
    if (!menusFolder.exists() && menusFolder.mkdirs()) {
      plugin.saveResource("menu" + File.separator + "example.yml", false);
      plugin.saveResource("menu" + File.separator + "addondownloader.yml", false);
    }
    LinkedList<File> files = new LinkedList<>();
    files.add(menusFolder);
    while (!files.isEmpty()) {
      File file = files.pop();
      if (file.isDirectory()) {
        files.addAll(Arrays.asList(Objects.requireNonNull(file.listFiles())));
      } else if (file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".yml")) {
        Config pluginConfig = new BukkitConfig(file);
        pluginConfig.setup();
        this.registerMenu(pluginConfig);
      }
    }
  }

  /**
   * Register the menu
   *
   * @param config the menu config
   */
  public void registerMenu(Config config) {
    String name = config.getName();
    if (menuMap.containsKey(name)) {
      plugin.getLogger().log(Level.WARNING, "\"{0}\" is already available in the menu manager. Ignored", name);
    } else {
      MenuBuilder.INSTANCE.build(config).ifPresent(menu -> menuMap.put(name, menu));
    }
  }

  /**
   * Clear all menus
   */
  public void clear() {
    menuMap.values().forEach(Menu::closeAll);
    menuMap.clear();
  }

  /**
   * Check if the menu exists
   *
   * @param name the menu name
   *
   * @return true if it exists, otherwise false
   */
  public boolean contains(String name) {
    return menuMap.containsKey(name);
  }

  /**
   * Open the menu for the player
   *
   * @param name   the menu name
   * @param player the player
   * @param args   the arguments from the open command
   * @param bypass whether the plugin ignores the permission check
   */
  public void openMenu(String name, Player player, String[] args, boolean bypass) {
    menuMap.get(name).create(player, args, bypass || player.hasPermission(Permissions.OPEN_MENU_BYPASS));
  }

  /**
   * Open the menu for the player
   *
   * @param name       the menu name
   * @param player     the player
   * @param args       the arguments from the open command
   * @param parentMenu the former menu that causes the player to open this menu
   * @param bypass     whether the plugin ignores the permission check
   */
  public void openMenu(String name, Player player, String[] args, Menu parentMenu, boolean bypass) {
    Menu menu = menuMap.get(name);
    menu.setParentMenu(player.getUniqueId(), parentMenu);
    menu.create(player, args, bypass || player.hasPermission(Permissions.OPEN_MENU_BYPASS));
  }

  /**
   * Get the name of all menus
   *
   * @return the list of the names
   */
  public Collection<String> getMenuNames() {
    return menuMap.keySet();
  }

  /**
   * Get the menu
   *
   * @param name the menu name
   *
   * @return the menu
   */
  public Menu getMenu(String name) {
    return menuMap.get(name);
  }
}
