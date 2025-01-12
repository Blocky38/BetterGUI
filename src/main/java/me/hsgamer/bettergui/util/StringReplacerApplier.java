package me.hsgamer.bettergui.util;

import me.hsgamer.bettergui.api.menu.Menu;
import me.hsgamer.bettergui.api.menu.MenuElement;
import me.hsgamer.hscore.bukkit.item.ItemBuilder;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import me.hsgamer.hscore.common.interfaces.StringReplacer;
import me.hsgamer.hscore.variable.VariableManager;

import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * A utility class to apply StringReplacer
 */
public final class StringReplacerApplier {
  /**
   * A replacer to colorize the string
   */
  public static final StringReplacer COLORIZE = (original, uuid) -> MessageUtils.colorize(original);
  private static final LinkedHashMap<String, StringReplacer> STRING_REPLACER_MAP = new LinkedHashMap<>();

  static {
    STRING_REPLACER_MAP.put("colorize", COLORIZE);
  }

  private StringReplacerApplier() {
    // EMPTY
  }

  /**
   * Get a mutable map of string replacers
   *
   * @return the map
   */
  public static LinkedHashMap<String, StringReplacer> getStringReplacerMap() {
    return STRING_REPLACER_MAP;
  }

  /**
   * Apply the string replacers to the item builder
   *
   * @param itemBuilder              the item builder
   * @param useGlobalVariableManager whether to use the global variable manager
   *
   * @return the item builder
   */
  public static ItemBuilder apply(ItemBuilder itemBuilder, boolean useGlobalVariableManager) {
    if (useGlobalVariableManager) {
      itemBuilder.addStringReplacer("variable-replacer", VariableManager::setVariables);
    }
    STRING_REPLACER_MAP.forEach(itemBuilder::addStringReplacer);
    return itemBuilder;
  }

  /**
   * Apply the string replacers to the item builder
   *
   * @param itemBuilder the item builder
   * @param menu        the menu
   *
   * @return the item builder
   */
  public static ItemBuilder apply(ItemBuilder itemBuilder, Menu menu) {
    return apply(itemBuilder.addStringReplacer(menu.getName() + "menu-replacer", menu::replace), false);
  }

  /**
   * Apply the string replacers to the item builder
   *
   * @param itemBuilder the item builder
   * @param menuElement the menu element
   *
   * @return the item builder
   */
  public static ItemBuilder apply(ItemBuilder itemBuilder, MenuElement menuElement) {
    return apply(itemBuilder, menuElement.getMenu());
  }

  /**
   * Apply the string replacers to the string
   *
   * @param string                   the string
   * @param uuid                     the unique id
   * @param useGlobalVariableManager whether to use the global variable manager
   *
   * @return the replaced string
   */
  public static String replace(String string, UUID uuid, boolean useGlobalVariableManager) {
    String replaced = string;
    if (useGlobalVariableManager) {
      replaced = VariableManager.setVariables(replaced, uuid);
    }
    for (StringReplacer replacer : STRING_REPLACER_MAP.values()) {
      replaced = replacer.replace(replaced, uuid);
    }
    return replaced;
  }

  /**
   * Apply the string replacers to the string
   *
   * @param string the string
   * @param uuid   the unique id
   * @param menu   the menu
   *
   * @return the replaced string
   */
  public static String replace(String string, UUID uuid, Menu menu) {
    return replace(
      menu.canBeReplaced(string)
        ? menu.replace(string, uuid)
        : string,
      uuid,
      false
    );
  }

  /**
   * Apply the string replacers to the string
   *
   * @param string      the string
   * @param uuid        the unique id
   * @param menuElement the menu element
   *
   * @return the replaced string
   */
  public static String replace(String string, UUID uuid, MenuElement menuElement) {
    return replace(string, uuid, menuElement.getMenu());
  }
}
