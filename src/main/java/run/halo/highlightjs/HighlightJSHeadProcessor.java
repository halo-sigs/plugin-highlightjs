package run.halo.highlightjs;

import com.google.common.base.Throwables;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.RouteMatcher;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import org.springframework.web.util.pattern.PatternParseException;
import org.thymeleaf.context.Contexts;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.web.IWebRequest;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

import java.util.List;

/**
 * @author ryanwang
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HighlightJSHeadProcessor implements TemplateHeadProcessor {

    private static final String TEMPLATE_ID_VARIABLE = "_templateId";

    private final ReactiveSettingFetcher reactiveSettingFetcher;

    private final RouteMatcher routeMatcher = createRouteMatcher();

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
        return reactiveSettingFetcher.fetch("basic", BasicConfig.class)
                .doOnNext(basicConfig -> {
                    if (mismatchedRoute(context, basicConfig) && notContentTemplate(context)) {
                        return;
                    }

                    final IModelFactory modelFactory = context.getModelFactory();
                    String scriptString = highlightJsScript(basicConfig);
                    model.add(modelFactory.createText(scriptString));
                })
                .onErrorResume(e -> {
                    log.error("HighlightJSHeadProcessor process failed", Throwables.getRootCause(e));
                    return Mono.empty();
                })
                .then();
    }

    private String highlightJsScript(BasicConfig basicConfig) {
        StringBuilder injectCode = new StringBuilder();
        injectCode.append("""
                    <!-- PluginHighlightJS start -->
                    <link href="/plugins/PluginHighlightJS/assets/static/styles/%s" rel="stylesheet"/>
                    <script defer src="/plugins/PluginHighlightJS/assets/static/highlight.min.js"></script>
                """.formatted(basicConfig.getStyle()));

        if (basicConfig.isShowCopyButton()) {
            injectCode.append("""
                        <link href="/plugins/PluginHighlightJS/assets/static/plugins/highlightjs-copy.min.css" rel="stylesheet"/>
                        <script defer src="/plugins/PluginHighlightJS/assets/static/plugins/highlightjs-copy.min.js"></script>
                    """);
        }

        injectCode.append("""
                <link href="/plugins/PluginHighlightJS/assets/static/plugins/override.css" rel="stylesheet"/>
                <script>
                """);

        injectCode.append("""
                document.addEventListener("DOMContentLoaded", async function () {
                """);

        if (basicConfig.isShowCopyButton()) {
            injectCode.append("""
                        hljs.addPlugin(new CopyButtonPlugin({ lang: "zh"}));
                    """);
        }

        var extraLanguages = basicConfig.getExtra_languages();

        injectCode.append("""
                      const extraLanguages = "%s".split(",").filter((x) => x);
                      for (let i = 0; i < extraLanguages.length; i++) {
                        const lang = extraLanguages[i];
                        if (lang) {
                          await loadScript(`/plugins/PluginHighlightJS/assets/static/languages/${lang}.min.js`);
                        }
                      }
                    
                      document.querySelectorAll("pre code").forEach((el) => {
                        hljs.highlightElement(el);
                      });
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
                """.formatted(extraLanguages != null ? extraLanguages : ""));
        return injectCode.toString();
    }

    public boolean notContentTemplate(ITemplateContext context) {
        return !"post".equals(context.getVariable(TEMPLATE_ID_VARIABLE))
                && !"page".equals(context.getVariable(TEMPLATE_ID_VARIABLE));
    }

    public boolean mismatchedRoute(ITemplateContext context, BasicConfig basicConfig) {
        if (!Contexts.isWebContext(context)) {
            return false;
        }
        IWebRequest request = Contexts.asWebContext(context).getExchange().getRequest();
        String requestPath = request.getRequestPath();
        RouteMatcher.Route requestRoute = routeMatcher.parseRoute(requestPath);

        return basicConfig.nullSafeRules()
                .stream()
                .noneMatch(rule -> isMatchedRoute(requestRoute, rule));
    }

    private boolean isMatchedRoute(RouteMatcher.Route requestRoute, PathMatchRule rule) {
        try {
            return routeMatcher.match(rule.getPathPattern(), requestRoute);
        } catch (PatternParseException e) {
            // ignore
            log.warn("Parse route pattern [{}] failed", rule.getPathPattern(), Throwables.getRootCause(e));
        }
        return false;
    }

    RouteMatcher createRouteMatcher() {
        var parser = new PathPatternParser();
        parser.setPathOptions(PathContainer.Options.HTTP_PATH);
        return new PathPatternRouteMatcher(parser);
    }

    @Data
    public static class PathMatchRule {
        private String pathPattern;
    }

    @Data
    public static class BasicConfig {
        String extra_languages;
        String style;
        List<PathMatchRule> rules;
        boolean showCopyButton;

        public List<PathMatchRule> nullSafeRules() {
            return ObjectUtils.defaultIfNull(rules, List.of());
        }
    }
}
