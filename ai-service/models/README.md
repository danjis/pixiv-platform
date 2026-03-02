# DeepDanbooru 模型文件目录

## 说明

此目录用于存放 DeepDanbooru 预训练模型文件。

## 模型下载

DeepDanbooru 是一个开源的二次元图片标签识别模型，专门为动漫/插画图片训练。

### 下载地址

1. **GitHub 官方仓库**: https://github.com/KichangKim/DeepDanbooru
2. **预训练模型**: https://github.com/KichangKim/DeepDanbooru/releases

### 推荐模型

- **模型名称**: DeepDanbooru v3-20211112-sgd-e28
- **下载链接**: https://github.com/KichangKim/DeepDanbooru/releases/tag/v3-20211112-sgd-e28
- **模型大小**: 约 500MB
- **支持标签**: 6000+ 个二次元相关标签

### 安装步骤

#### 方式 1：下载 .h5 模型文件（推荐）

1. 访问 [DeepDanbooru Releases](https://github.com/KichangKim/DeepDanbooru/releases)
2. 下载 `model-resnet_custom_v3.h5` 文件
3. 下载 `tags.txt` 文件
4. 将文件放到此目录下：
   ```
   models/
   └── deepdanbooru/
       ├── model-resnet_custom_v3.h5  # 模型文件
       └── tags.txt                    # 标签列表
   ```

#### 方式 2：下载 SavedModel 格式

1. 下载完整的模型压缩包
2. 解压到此目录下，目录结构应为：
   ```
   models/
   └── deepdanbooru/
       ├── saved_model.pb
       ├── variables/
       │   ├── variables.data-00000-of-00001
       │   └── variables.index
       └── tags.txt
   ```

#### 方式 3：使用 Git LFS（适合开发者）

```bash
# 安装 Git LFS
git lfs install

# 克隆模型仓库
cd models
git clone https://github.com/KichangKim/DeepDanbooru.git deepdanbooru
```

### 验证安装

安装完成后，目录结构应该是：

```
models/
└── deepdanbooru/
    ├── model-resnet_custom_v3.h5  # 或 model.h5
    └── tags.txt                    # 必需
```

运行测试脚本验证：

```bash
cd ../../  # 回到 ai-service 目录
python deepdanbooru.py
```

如果看到 "模型加载成功！支持 XXXX 个标签"，说明安装成功。

## 模型特性

- **输入尺寸**: 512x512 RGB 图片
- **输出**: 6000+ 个标签的置信度分数（0.0 - 1.0）
- **识别内容**: 
  - 角色特征（性别、发型、发色、眼睛颜色等）
  - 服装（校服、和服、泳装等）
  - 场景（室内、室外、海滩等）
  - 风格（写实、Q版、水彩等）
  - 动作（站立、坐着、跑步等）
  - 其他（表情、配饰、背景等）
- **适用场景**: 二次元/动漫/插画图片

## 标签示例

常见的标签包括：

- **角色**: `1girl`, `1boy`, `multiple_girls`, `solo`
- **发型**: `long_hair`, `short_hair`, `twintails`, `ponytail`
- **发色**: `blonde_hair`, `black_hair`, `blue_hair`, `pink_hair`
- **眼睛**: `blue_eyes`, `red_eyes`, `green_eyes`
- **服装**: `school_uniform`, `dress`, `kimono`, `swimsuit`
- **场景**: `outdoors`, `indoors`, `beach`, `classroom`
- **风格**: `anime_style`, `chibi`, `realistic`

完整的标签列表请查看 `tags.txt` 文件。

## 使用示例

```python
from deepdanbooru import DeepDanbooruModel
from PIL import Image

# 加载模型
model = DeepDanbooruModel()

# 打开图片
image = Image.open('test.jpg')

# 预测标签（Top 10，置信度 > 0.3）
predictions = model.predict_top_k(image, k=10, threshold=0.3)

# 打印结果
for tag, confidence in predictions:
    print(f"{tag}: {confidence:.4f}")
```

## 性能说明

### CPU 推理

- **首次加载**: 约 5-10 秒
- **单张预测**: 约 1-3 秒
- **内存占用**: 约 1-2 GB

### GPU 推理（NVIDIA GPU + CUDA）

- **首次加载**: 约 3-5 秒
- **单张预测**: 约 0.1-0.5 秒
- **显存占用**: 约 1-2 GB

### 优化建议

1. **批量处理**: 一次处理多张图片可以提高吞吐量
2. **模型量化**: 使用 TensorFlow Lite 或 ONNX 减小模型大小
3. **缓存结果**: 对相同图片缓存预测结果
4. **异步处理**: 使用消息队列异步处理，避免阻塞主流程

## 常见问题

### Q: 模型文件太大，下载很慢怎么办？

A: 可以使用国内镜像或者使用下载工具（如 wget、aria2）断点续传。

### Q: 加载模型时报错 "No module named 'tensorflow'"

A: 请安装 TensorFlow：`pip install tensorflow==2.14.0`

### Q: 预测结果不准确怎么办？

A: 
1. 确保输入图片是二次元/动漫风格
2. 调整置信度阈值（默认 0.3）
3. 图片质量要清晰，尺寸不要太小

### Q: 可以识别真人照片吗？

A: DeepDanbooru 专门为二次元图片训练，对真人照片效果不佳。如需识别真人照片，建议使用其他模型（如 CLIP、ResNet 等）。

### Q: 如何添加自定义标签？

A: DeepDanbooru 是预训练模型，不支持直接添加标签。如需自定义标签，需要重新训练模型。

## 注意事项

1. 模型文件较大（约 500MB），首次下载需要时间
2. 模型加载需要一定内存（建议至少 2GB 可用内存）
3. GPU 加速可以显著提升推理速度（可选）
4. 生产环境建议使用 TensorFlow Serving 或 ONNX Runtime 优化性能
5. 模型输出的标签是英文，如需中文标签需要额外的翻译映射

## 许可证

DeepDanbooru 模型遵循 MIT 许可证，可以免费用于商业和非商业用途。

## 参考资料

- [DeepDanbooru GitHub](https://github.com/KichangKim/DeepDanbooru)
- [DeepDanbooru 论文](https://arxiv.org/abs/1907.09384)
- [Danbooru 标签系统](https://danbooru.donmai.us/wiki_pages/tag_groups)
