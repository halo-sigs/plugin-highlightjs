# plugin-highlightjs

主题端的代码块高亮插件，集成了 [highlight.js](https://github.com/highlightjs/highlight.js)。

## 开发环境

```bash
git clone git@github.com:halo-sigs/plugin-highlightjs.git

# 或者当你 fork 之后

git clone git@github.com:{your_github_id}/plugin-highlightjs.git
```

```bash
cd path/to/plugin-highlightjs
```

```bash
# macOS / Linux
./gradlew build

# Windows
./gradlew.bat build
```

修改 Halo 配置文件：

```yaml
halo:
  plugin:
    runtime-mode: development
    classes-directories:
      - "build/classes"
      - "build/resources"
    lib-directories:
      - "libs"
    fixedPluginPath:
      - "/path/to/plugin-highlightjs"
```

## 使用方式

1. 在 [Releases](https://github.com/halo-sigs/plugin-highlightjs/releases) 下载最新的 JAR 文件。
2. 在 Halo 后台的插件管理上传 JAR 文件进行安装。

> 需要注意的是，首次安装之后，需要进入插件设置页面保存一次设置，才能正常使用。

## 主题适配

此插件无需主题主动适配即可使用，其原理就是将 `highlight.js` 所需的依赖引入和初始化代码都自动插入到了内容页面上。因此，主题开发者无需再针对代码高亮进行适配开发，如果有特殊的需求，建议共同完善此插件。