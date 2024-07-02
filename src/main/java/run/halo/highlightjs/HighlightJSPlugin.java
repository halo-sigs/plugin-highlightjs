package run.halo.highlightjs;

import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * @author ryanwang
 * @since 2.0.0
 */
@Component
public class HighlightJSPlugin extends BasePlugin {

    public HighlightJSPlugin(PluginContext pluginContext) {
        super(pluginContext);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
