package me.hsgamer.bettergui.manager;

import me.hsgamer.hscore.addon.object.Addon;
import me.hsgamer.hscore.bukkit.addon.PluginAddonManager;
import me.hsgamer.hscore.bukkit.utils.BukkitUtils;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.common.Validate;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class ExtraAddonManager extends PluginAddonManager {
  private static final Comparator<Map.Entry<String, Addon>> dependComparator = (entry1, entry2) -> {
    Addon addon1 = entry1.getValue();
    String name1 = entry1.getKey();
    List<String> depends1 = getDepends(addon1);
    List<String> softDepends1 = getSoftDepends(addon1);

    Addon addon2 = entry2.getValue();
    String name2 = entry2.getKey();
    List<String> depends2 = getDepends(addon2);
    List<String> softDepends2 = getSoftDepends(addon2);

    depends1 = depends1 == null ? Collections.emptyList() : depends1;
    softDepends1 = softDepends1 == null ? Collections.emptyList() : softDepends1;

    depends2 = depends2 == null ? Collections.emptyList() : depends2;
    softDepends2 = softDepends2 == null ? Collections.emptyList() : softDepends2;

    if (depends1.contains(name2) || softDepends1.contains(name2)) {
      return 1;
    } else if (depends2.contains(name1) || softDepends2.contains(name1)) {
      return -1;
    } else {
      return 0;
    }
  };

  public ExtraAddonManager(JavaPlugin javaPlugin) {
    super(javaPlugin);
  }

  /**
   * Get the authors of the addon
   *
   * @param addon the addon
   *
   * @return the authors
   */
  public static List<String> getAuthors(Addon addon) {
    Object value = addon.getDescription().getData().get("authors");
    if (value == null) {
      return Collections.emptyList();
    }
    return CollectionUtils.createStringListFromObject(value, true);
  }

  /**
   * Get the description of the addon
   *
   * @param addon the addon
   *
   * @return the description
   */
  public static String getDescription(Addon addon) {
    Object value = addon.getDescription().getData().get("description");
    return Objects.toString(value, "");
  }

  private static List<String> getDepends(Addon addon) {
    Object value = addon.getDescription().getData().get("depend");
    if (value == null) {
      return Collections.emptyList();
    }
    return CollectionUtils.createStringListFromObject(value, true);
  }

  private static List<String> getSoftDepends(Addon addon) {
    Object value = addon.getDescription().getData().get("soft-depend");
    if (value == null) {
      return Collections.emptyList();
    }
    return CollectionUtils.createStringListFromObject(value, true);
  }

  private static List<String> getPluginDepends(Addon addon) {
    Object value = addon.getDescription().getData().get("plugin-depend");
    if (value == null) {
      return Collections.emptyList();
    }
    return CollectionUtils.createStringListFromObject(value, true);
  }

  @Override
  protected @NotNull Map<String, Addon> sortAndFilter(@NotNull Map<String, Addon> original) {
    Map<String, Addon> sorted = new LinkedHashMap<>();
    Map<String, Addon> remaining = new HashMap<>();

    // Start with addons with no dependency and get the remaining
    Consumer<Map.Entry<String, Addon>> consumer = entry -> {
      Addon addon = entry.getValue();
      if (Validate.isNullOrEmpty(getDepends(addon)) && Validate.isNullOrEmpty(getSoftDepends(addon))) {
        sorted.put(entry.getKey(), entry.getValue());
      } else {
        remaining.put(entry.getKey(), entry.getValue());
      }
    };
    original.entrySet().forEach(consumer);

    // Organize the remaining
    if (remaining.isEmpty()) {
      return sorted;
    }

    remaining.entrySet().stream().filter(stringAddonEntry -> {
      Addon addon = stringAddonEntry.getValue();
      String name = stringAddonEntry.getKey();

      // Check if the required dependencies are loaded
      List<String> depends = getDepends(addon);
      if (Validate.isNullOrEmpty(depends)) {
        return true;
      }

      for (String depend : depends) {
        if (!original.containsKey(depend)) {
          getPlugin().getLogger().warning("Missing dependency for " + name + ": " + depend);
          return false;
        }
      }

      return true;
    }).sorted(dependComparator).forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));

    return sorted;
  }

  @Override
  protected boolean onAddonLoading(@NotNull Addon addon) {
    List<String> requiredPlugins = getPluginDepends(addon);
    if (Validate.isNullOrEmpty(requiredPlugins)) {
      return true;
    }

    List<String> missing = BukkitUtils.getMissingDepends(requiredPlugins);
    if (!missing.isEmpty()) {
      getPlugin().getLogger().warning(() -> "Missing plugin dependency for " + addon.getDescription().getName() + ": " + Arrays.toString(missing.toArray()));
      return false;
    }

    return true;
  }

  /**
   * Get addon count
   *
   * @return the addon count
   */
  public Map<String, Integer> getAddonCount() {
    Map<String, Integer> map = new HashMap<>();
    getLoadedAddons().keySet().forEach(s -> map.put(s, 1));
    return map;
  }
}
