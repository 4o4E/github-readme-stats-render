# github-readme-stats-render

用于生成[github-readme-status](https://github.com/anuraghazra/github-readme-stats)卡片的渲染图

## http服务

`http-server`模块供了一个简单的http server用于根据需求生成皮肤渲染图

### 使用

1. 安装11或更高版本的[java](https://adoptium.net/)
2. 从[release](https://github.com/4o4E/github-readme-stats-render/releases/latest)下载对应操作系统的jar文件
3. 在控制台中使用`java -jar http-server-${plateform}.jar`启动服务

### 示例配置文件

```yaml
# http 服务的地址
host: localhost
# http 服务的端口
port: 2345
# 设置为null不走代理
proxy:
  type: socket # socket/http
  host: localhost
  port: 7890
# wakatime的api token, 在 https://wakatime.com/settings/account 获取
waka_token: waka_xxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
# 布局设置, 使用到的字体请自行放入对应的文件夹, JetBrainsMono可在 https://www.jetbrains.com/lp/mono/ 下载
layout:
  # 标题
  title_font: font/JetBrainsMono-Bold.ttf
  title_size: 26
  # 语言名字
  lang_font: font/JetBrainsMono-Bold.ttf
  lang_size: 20
  # 语言时长
  text_font: font/JetBrainsMono-Medium.ttf
  text_size: 20

  # 背景圆角
  bg_radii: 4.5
  # 描边圆角
  stroke_radii: 4.5

  # 外边距
  margin: 25
  # 上下间距
  spacing: 30
  # 占比矩形左右边距
  bar_padding: 10
  # 占比矩形高度
  bar_height: 10
  # 占比矩形宽度
  bar_width: 280
# 主题
themes:
  default_repocard:
    title_color: "ff2f80ed"
    icon_color: "ff586069"
    text_color: "ff434d58"
    bg_color: "fffffefe"
  # 其他主题...
```

### api接口

**api不会主动缓存渲染结果, 如有需要请自行缓存**

#### 获取wakatime渲染图

url: `/wakatime/{user}/{range}`

| url参数 | 含义          | 示例                            |
|-------|-------------|-------------------------------|
| user  | wakatime用户名 | `404E`                        |
| range | 时间范围        | `7d`/`30d`/`6m`/`30d`/`y`/`y` |

**可用参数**

| 请求参数  | 含义         | 默认值  | 可用值            |
|-------|------------|------|----------------|
| theme | 主题         | null | 参考config       |
| all   | 是否显示所有lang | null | `true`/`false` |

**示例请求**

```http request
GET http://localhost:2345/wakatime/404E/7d?theme=dark
```
