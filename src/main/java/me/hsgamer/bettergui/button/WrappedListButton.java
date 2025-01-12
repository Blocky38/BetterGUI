package me.hsgamer.bettergui.button;

import me.hsgamer.bettergui.api.button.BaseWrappedButton;
import me.hsgamer.bettergui.api.button.WrappedButton;
import me.hsgamer.bettergui.builder.ButtonBuilder;
import me.hsgamer.bettergui.util.MapUtil;
import me.hsgamer.hscore.bukkit.gui.button.Button;
import me.hsgamer.hscore.bukkit.gui.button.impl.ListButton;
import me.hsgamer.hscore.collections.map.CaseInsensitiveStringMap;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WrappedListButton extends BaseWrappedButton {
  public WrappedListButton(ButtonBuilder.Input input) {
    super(input);
  }

  @Override
  protected Button createButton(Map<String, Object> section) {
    Map<String, Object> keys = new CaseInsensitiveStringMap<>(section);
    boolean keepCurrentIndex = Optional.ofNullable(keys.get("keep-current-index")).map(String::valueOf).map(Boolean::parseBoolean).orElse(false);
    return Optional.ofNullable(keys.get("child"))
      .flatMap(MapUtil::castOptionalStringObjectMap)
      .map(o -> new LinkedList<Button>(ButtonBuilder.INSTANCE.getChildButtons(this, o)))
      .map(list -> {
        ListButton button = new ListButton(list);
        button.setKeepCurrentIndex(keepCurrentIndex);
        return button;
      }).orElse(null);
  }

  @Override
  public void refresh(UUID uuid) {
    if (this.button instanceof ListButton) {
      if (!((ListButton) this.button).isKeepCurrentIndex()) {
        ((ListButton) this.button).removeCurrentIndex(uuid);
      }
      ((ListButton) this.button).getButtons().stream().filter(WrappedButton.class::isInstance).forEach(button -> ((WrappedButton) button).refresh(uuid));
    }
  }
}
