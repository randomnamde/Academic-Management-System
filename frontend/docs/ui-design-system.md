# 学生管理系统 UI 设计规范

## 1. 视觉基调
- 风格：教育管理后台，清晰、专业、低饱和蓝绿。
- 关键词：秩序、可信、可读、轻量动效。

## 2. 设计令牌
- 字体：`IBM Plex Sans`（正文），`IBM Plex Serif`（展示）。
- 主色：`brand-600 #24736d`。
- 文本：`ink-900 #2e3947`。
- 圆角：`12px / 16px / 22px`。
- 阴影：`soft` 与 `panel` 两级。

## 3. 组件层
- 基础组件：
  - `AppButton`
  - `AppInput`
  - `AppSelect`
  - `AppCard`
  - `AppBadge`
  - `AppModal`
  - `AppTabs`
  - `AppTable`
  - `AppPagination`
- 页面壳层：
  - `CrudPageShell`
  - `AnalyticsPageShell`
  - `ProfilePageShell`

## 4. 页面结构规范
- 顶层统一使用 `.app-page`。
- 业务内容放入 `.app-panel`，头部使用 `.app-panel-header`。
- 查询/筛选区域使用 `.app-toolbar`。
- 列表统一 `AppTable` 视觉规范与 hover 行反馈。

## 5. 文案规范
- 统一词表：序号、查询、重置、启用、停用、发布、下线。
- 所有前端源码文件使用 UTF-8。
- 提交前执行：`npm run check:utf8`。

## 6. 测试与验收
- E2E：`npm run test:e2e -- --list` 查看用例，联调环境执行完整回归。
- 构建：`npm run build`。
