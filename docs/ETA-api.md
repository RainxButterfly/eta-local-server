---
title: ETA-api
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# ETA-api

Base URLs:

# Authentication

# ETA · API

## POST 触发数据同步

POST /sync

## 功能说明
触发一次云同步，按勾选内容推送本地数据并拉取远端数据。

## 状态码
- 200 成功
- 401 未登录
- 100001 同步冲突

> Body 请求参数

```json
{
}
```

### 请求参数


> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "syncedAt": "string"
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||状态码，200 表示成功|
|» message|string|true|none||提示信息|
|» data|object|true|none||数据负载|
|»» syncedAt|string|true|none||同步时间（ISO 8601）|

## GET 获取设备列表

GET /sync/devices

## 功能说明
获取已连接设备列表。

## 状态码
- 200 成功
- 401 未登录

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |Bearer {token}|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "id": "string",
      "name": "string",
      "platform": "string",
      "online": true,
      "lastSyncAt": "string"
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||状态码，200 表示成功|
|» message|string|true|none||提示信息|
|» data|[object]|true|none||数据负载|
|»» id|string|true|none||资源 ID（UUID）|
|»» name|string|true|none||名称|
|»» platform|string|true|none||平台（Windows/macOS/Linux）|
|»» online|boolean|true|none||是否在线|
|»» lastSyncAt|string|true|none||最后同步时间（ISO 8601）|

## DELETE 解绑设备

DELETE /sync/devices/{id}

## 功能说明
解绑设备。
## 路径参数
- id：设备 ID

## 状态码
- 200 成功
- 404 设备不存在

> Body 请求参数

```json
{}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |Bearer {token}|
|body|body|object| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": null
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||状态码，200 表示成功|
|» message|string|true|none||提示信息|
|» data|null|false|none||数据负载|

## GET 获取同步状态

GET /sync/status

## 功能说明
获取同步状态。

## 状态码
- 200 成功

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |Bearer {token}|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "lastSyncAt": "string",
    "conflicts": 0,
    "online": true
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||状态码，200 表示成功|
|» message|string|true|none||提示信息|
|» data|object|true|none||数据负载|
|»» lastSyncAt|string|true|none||最后同步时间（ISO 8601）|
|»» conflicts|integer|true|none||冲突数|
|»» online|boolean|true|none||是否在线|

## PUT 设置冲突处理策略

PUT /sync/conflict

## 功能说明
设置同步冲突处理策略。strategy 支持 last-wins（以时间较晚的修改为准）/ manual（手动合并）。

## 状态码
- 200 成功

> Body 请求参数

```json
{
  "strategy": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |Bearer {token}|
|body|body|object| 否 |none|
|» strategy|body|string| 是 |冲突解决策略（last-wins 等）|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "strategy": "string"
  }
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» code|integer|true|none||状态码，200 表示成功|
|» message|string|true|none||提示信息|
|» data|object|true|none||数据负载|
|»» strategy|string|true|none||冲突解决策略（last-wins 等）|

# 数据模型

