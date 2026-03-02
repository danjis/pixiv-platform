# AI 智能打标服务

基于 DeepDanbooru 模型的二次元图片智能打标服务，使用 FastAPI 构建的高性能 Python 微服务。

## 功能特性

- 🎨 **智能识别**: 使用 DeepDanbooru 深度学习模型识别二次元图片内容
- 🏷️ **自动打标**: 自动生成 6000+ 个二次元相关标签
- 🌏 **中文翻译**: 自动将英文标签翻译成中文，支持 300+ 常用标签
- ⚡ **高性能**: 基于 FastAPI 的异步处理，支持高并发
- 🔌 **易集成**: RESTful API 接口，方便与 Java 后端集成
- 📊 **置信度评分**: 每个标签都包含置信度分数

## 技术栈

- **Web 框架**: FastAPI 0.104.1
- **ASGI 服务器**: Uvicorn 0.24.0
- **深度学习**: TensorFlow 2.14.0
- **图像处理**: Pillow 10.1.0
- **HTTP 客户端**: Requests 2.31.0

## 快速开始

### 1. 安装依赖

```bash
cd pixiv-platform/ai-service
pip install -r requirements.txt
```

### 2. 下载模型

从 [DeepDanbooru Releases](https://github.com/KichangKim/DeepDanbooru/releases) 下载预训练模型，解压到 `models/deepdanbooru/` 目录。

详细说明请参考 `models/README.md`。

### 3. 启动服务

```bash
python main.py
```

服务将在 `http://localhost:8000` 启动。

### 4. 访问文档

- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

## API 接口

### 健康检查

```http
GET /health
```

**响应示例**:
```json
{
  "status": "healthy",
  "model_loaded": true,
  "tags_count": 6000
}
```

### 预测标签

```http
POST /api/predict
Content-Type: application/json

{
  "image_url": "https://example.com/image.jpg"
}
```

**响应示例**:
```json
{
  "tags": [
    {
      "tag": "1girl",
      "tag_zh": "1个女孩",
      "confidence": 0.95
    },
    {
      "tag": "long_hair",
      "tag_zh": "长发",
      "confidence": 0.88
    },
    {
      "tag": "school_uniform",
      "tag_zh": "校服",
      "confidence": 0.76
    }
  ],
  "processing_time": 0.85
}
```

## 标签翻译

### 翻译字典

标签翻译字典位于 `models/deepdanbooru/tag_translations.json`，包含 300+ 个常用标签的中英文对照。

### 测试翻译功能

```bash
python test_translation.py
```

### 添加自定义翻译

编辑 `models/deepdanbooru/tag_translations.json` 文件，添加新的翻译：

```json
{
  "your_tag": "你的标签",
  "another_tag": "另一个标签"
}
```

重启服务后生效。

### 翻译规则

1. **优先使用翻译字典**: 如果标签在字典中，直接返回中文翻译
2. **应用默认规则**: 对于常见模式（如颜色+特征），自动生成翻译
3. **保留原标签**: 如果没有匹配的翻译，返回英文原标签

## 配置说明

### CORS 配置

默认允许所有来源访问，生产环境建议修改 `main.py` 中的 CORS 配置：

```python
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080"],  # 只允许 Java 后端
    allow_credentials=True,
    allow_methods=["POST", "GET"],
    allow_headers=["*"],
)
```

### 置信度阈值

默认置信度阈值为 0.3，可以在 `main.py` 中调整：

```python
if confidence >= 0.3:  # 修改此值
```

### 返回标签数量

默认最多返回 15 个标签，可以在 `main.py` 中调整：

```python
tag_predictions = tag_predictions[:15]  # 修改此值
```

## 性能优化

### 1. GPU 加速

如果有 NVIDIA GPU，安装 TensorFlow GPU 版本：

```bash
pip install tensorflow-gpu==2.14.0
```

### 2. 批量处理

对于大量图片，可以实现批量预测接口：

```python
@app.post("/api/predict-batch")
async def predict_batch(requests: List[TaggingRequest]):
    # 批量处理逻辑
    pass
```

### 3. 模型缓存

模型在服务启动时加载一次，后续请求复用同一个模型实例。

## 与 Java 后端集成

Java 后端通过 HTTP 调用此服务：

```java
@Service
public class AutoTaggingService {
    
    private final RestTemplate restTemplate;
    private final String aiServiceUrl = "http://localhost:8000";
    
    public List<TagPrediction> predictTags(String imageUrl) {
        String url = aiServiceUrl + "/api/predict";
        
        TaggingRequest request = new TaggingRequest(imageUrl);
        TaggingResponse response = restTemplate.postForObject(
            url, request, TaggingResponse.class
        );
        
        return response.getTags();
    }
}
```

## 异步处理（推荐）

使用 RabbitMQ 消息队列实现异步打标：

1. Java 后端发送打标请求到 RabbitMQ
2. Python 服务监听队列并处理请求
3. 处理完成后将结果写入数据库

这样可以避免阻塞主流程，提高系统响应速度。

## 错误处理

服务会返回标准的 HTTP 错误码：

- `400`: 请求参数错误（如图片 URL 无效）
- `500`: 服务器内部错误（如模型预测失败）
- `503`: 服务不可用（如模型未加载）

## 日志

服务使用 Python logging 模块记录日志：

- **INFO**: 正常操作日志（模型加载、预测请求等）
- **ERROR**: 错误日志（下载失败、预测失败等）

## 部署

### Docker 部署

创建 `Dockerfile`:

```dockerfile
FROM python:3.10-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

构建和运行：

```bash
docker build -t ai-service .
docker run -p 8000:8000 ai-service
```

### 生产环境建议

1. 使用 Gunicorn + Uvicorn workers 提高并发能力
2. 配置 Nginx 反向代理
3. 启用 HTTPS
4. 限制请求频率（防止滥用）
5. 监控服务健康状态

## 许可证

本项目使用 MIT 许可证。

DeepDanbooru 模型也遵循 MIT 许可证。

## 参考资料

- [DeepDanbooru GitHub](https://github.com/KichangKim/DeepDanbooru)
- [FastAPI 文档](https://fastapi.tiangolo.com/)
- [TensorFlow 文档](https://www.tensorflow.org/)
