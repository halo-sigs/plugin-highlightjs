/* eslint-env node */
require("@rushstack/eslint-patch/modern-module-resolution");
module.exports = {
  env: {
    es2021: true,
    browser: true,
  },
  extends: "eslint:recommended",
  overrides: [],
  parserOptions: {
    ecmaVersion: "latest",
    sourceType: "module",
  },
  rules: {
    "@typescript-eslint/ban-ts-comment": "off",
    "@typescript-eslint/no-explicit-any": "off",
  },
};
