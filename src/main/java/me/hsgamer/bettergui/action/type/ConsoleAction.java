package me.hsgamer.bettergui.action.type;

import me.hsgamer.bettergui.BetterGUI;
import me.hsgamer.bettergui.api.action.BaseAction;
import me.hsgamer.bettergui.builder.ActionBuilder;
import me.hsgamer.hscore.task.BatchRunnable;
import org.bukkit.Bukkit;

import java.util.UUID;

public class ConsoleAction extends BaseAction {
  public ConsoleAction(ActionBuilder.Input input) {
    super(input);
  }

  @Override
  public void accept(UUID uuid, BatchRunnable.Process process) {
    Bukkit.getScheduler().runTask(BetterGUI.getInstance(), () -> {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getReplacedString(uuid));
      process.next();
    });
  }

  @Override
  protected boolean shouldBeTrimmed() {
    return true;
  }
}
