package dev.walgo.db2dto.plugin;

import dev.walgo.db2dto.config.Config;
import dev.walgo.walib.ResourceUtils;
import dev.walgo.walib.db.DBInfo;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PluginHandler.class);

    private static List<IPlugin> plugins;

    private PluginHandler() {
        // do nothing
    }

    private static void initPlugins(String pluginPackage, DBInfo dbInfo) {
        List<Class<? extends IPlugin>> pluginClasses = ResourceUtils.findClassesFromResources(pluginPackage,
                IPlugin.class);
        for (Class<? extends IPlugin> pluginClass : pluginClasses) {
            if (pluginClass.isInterface() || Modifier.isAbstract(pluginClass.getModifiers())) {
                continue;
            }
            try {
                IPlugin plugin = pluginClass.getConstructor().newInstance();
                if (plugin.usePlugin(dbInfo)) {
                    plugins.add(plugin);
                }
            } catch (Exception ex) {
                LOG.error("Error on instantiate: " + pluginClass, ex);
            }
        }
    }

    private static void initPlugins(DBInfo dbInfo) {
        plugins = new ArrayList<>();

        initPlugins(IPlugin.class.getPackageName() + ".dflt", dbInfo);
        for (String pluginPackage : Config.getCONFIG().pluginPackages) {
            initPlugins(pluginPackage, dbInfo);
        }
    }

    public static List<IPlugin> getPlugins(DBInfo dbInfo) {
        if (plugins == null) {
            initPlugins(dbInfo);
        }
        return plugins;
    }
}
