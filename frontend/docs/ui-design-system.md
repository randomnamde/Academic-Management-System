# 学生管理系统 UI 设计规范

## 1. 视觉基调
- 风格：教育管理后台，清晰、专业、低饱和蓝绿。
- 关键词：秩序、可信、可读、轻量动效。

## 2. 设计令牌
- 字体：`IBM Plex Sans`（正文），`IBM Plex Serif`（展示）。
- 主色：`primary-900 #0F2742`、`primary-800 #163454`、`primary-700 #1E4266`。
- 文本：`slatex-900 #1E2938`、`slatex-700 #334155`、`slatex-600 #475569`。
- 圆角：`6px（control） / 8px（panel）`。
- 阴影：`0 1px 2px rgba(15, 23, 42, 0.06)`（基础）与 `0 1px 2px rgba(15, 23, 42, 0.08)`（弹层）。
- 动效：`180ms ease`，默认使用透明度与位移过渡。

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
