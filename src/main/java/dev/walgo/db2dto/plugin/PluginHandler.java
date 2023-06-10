package dev.walgo.db2dto.plugin;

import dev.walgo.db2dto.config.Config;
import dev.walgo.walib.ResourceUtils;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginHandler {

    private static List<IPlugin> plugins;

    private PluginHandler() {
        // do nothing
    }

    private static void initPlugins() {
        plugins = new ArrayList<>();

        for (String pluginPackage : Config.getCONFIG().pluginPackages) {
            List<Class<? extends IPlugin>> pluginClasses = ResourceUtils.findClassesFromResources(pluginPackage,
                    IPlugin.class);
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
    }

    public static List<IPlugin> getPlugins() {
        if (plugins == null) {
            initPlugins();
        }
        return plugins;
    }
}
