package org.wwapp.db2dto.plugin;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

/** @author Walery Wysotsky <dev@wysotsky.info> */
@Slf4j
public class PluginHandler {

  private static List<IPlugin> plugins;

  private PluginHandler() {
    // do nothing
  }

  private static void initPlugins() {
    plugins = new ArrayList<>();
    Reflections reflections = new Reflections("", new SubTypesScanner());
    Set<Class<? extends IPlugin>> pluginClasses = reflections.getSubTypesOf(IPlugin.class);
    for (Class<? extends IPlugin> pluginClass : pluginClasses) {
      if (pluginClass.isInterface() || Modifier.isAbstract(pluginClass.getModifiers())) {
        continue;
      }
      try {
        IPlugin plugin = pluginClass.getConstructor().newInstance();
        plugins.add(plugin);
      } catch (Exception ex) {
        LOG.error("Error on instantiate: " + pluginClass, ex);
      }
    }
  }

  public static List<IPlugin> getPlugins() {
    if (plugins == null) {
      initPlugins();
    }
    return plugins;
  }
}
