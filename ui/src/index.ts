import { definePlugin } from "@halo-dev/ui-shared";

export default definePlugin({
  extensionPoints: {
    "default:editor:extension:create": async () => {
      const { ExtensionCodeBlockLow } =
        await import("./editor/code-block-lowlight");
      return [ExtensionCodeBlockLow];
    },
  },
});
