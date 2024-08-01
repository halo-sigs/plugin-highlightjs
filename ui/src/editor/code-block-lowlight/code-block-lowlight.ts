import {
  ExtensionCodeBlock,
  type ExtensionCodeBlockOptions,
} from "@halo-dev/richtext-editor";
import lowlight from "./lowlight";
import { LowlightPlugin } from "./lowlight-plugin";
import "highlight.js/styles/github.min.css";

export interface CodeBlockLowlightOptions {
  /**
   * The lowlight instance.
   */
  lowlight: any;

  /**
   * The default language.
   * @default null
   * @example 'javascript'
   */
  defaultLanguage?: string | null | undefined;
}

export default ExtensionCodeBlock.extend<
  CodeBlockLowlightOptions & ExtensionCodeBlockOptions
>({
  addOptions() {
    return {
      ...this.parent?.(),
      lowlight: lowlight,
      defaultLanguage: null,
      languages: lowlight.listLanguages(),
    };
  },

  addProseMirrorPlugins() {
    return [
      ...(this.parent?.() || []),
      LowlightPlugin({
        name: this.name,
        lowlight: this.options.lowlight,
        defaultLanguage: this.options.defaultLanguage,
      }),
    ];
  },
});
