package run.halo.highlightjs;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.SettingFetcher;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.dialect.TemplateHeadProcessor;
import run.halo.app.theme.router.strategy.ModelConst;

/**
 * @author ryanwang
 */
@Component
public class HighlightJSHeadProcessor implements TemplateHeadProcessor {

    private final SettingFetcher settingFetcher;

    public HighlightJSHeadProcessor(SettingFetcher settingFetcher) {
        this.settingFetcher = settingFetcher;
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
        if (!isContentTemplate(context)) {
            return Mono.empty();
        }
        return settingFetcher.fetch("basic", BasicConfig.class).map(basicConfig -> {
            final IModelFactory modelFactory = context.getModelFactory();
            model.add(modelFactory.createText(highlightJsScript(basicConfig.getExtra_languages(), basicConfig.getStyle())));
            return Mono.empty();
        }).orElse(Mono.empty()).then();
    }

    private String highlightJsScript(String extraLanguages, String style) {
        return """
                <!-- PluginHighlightJS start -->
                <link href="/plugins/PluginHighlightJS/assets/static/styles/%s" rel="stylesheet"/>
                <script src="/plugins/PluginHighlightJS/assets/static/highlight.min.js"></script>
                <script>
                    document.addEventListener("DOMContentLoaded", async function () {
                      const extraLanguages = "%s".split(",").filter((x) => x);
                    
                      for (let i = 0; i < extraLanguages.length; i++) {
                        const lang = extraLanguages[i];
                        if (lang) {
                          await loadScript(`/plugins/PluginHighlightJS/assets/static/languages/${lang}.min.js`);
                        }
                      }
                    
                      console.log("Extra languages: ", extraLanguages);
                    
                      document.querySelectorAll("pre code").forEach((el) => {
                        hljs.highlightElement(el);
                      });
                      console.log("Loaded languages: ", hljs.listLanguages());
                    });
                    
                    function loadScript(url) {
                      return new Promise(function (resolve, reject) {
                        const script = document.createElement("script");
                        script.type = "text/javascript";
                        script.src = url;
                        script.onload = resolve;
                        script.onerror = reject;
                        document.head.appendChild(script);
                      });
                    }
                </script>
                <!-- PluginHighlightJS end -->
                """.formatted(style, extraLanguages != null ? extraLanguages : "");
    }

    public boolean isContentTemplate(ITemplateContext context) {
        return DefaultTemplateEnum.POST.getValue().equals(context.getVariable(ModelConst.TEMPLATE_ID)) || DefaultTemplateEnum.SINGLE_PAGE.getValue().equals(context.getVariable(ModelConst.TEMPLATE_ID));
    }

    @Data
    public static class BasicConfig {
        String extra_languages;
        String style;
    }
}
