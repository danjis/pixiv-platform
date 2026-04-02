"""
AI 智能打标服务 - FastAPI 应用入口

基于 DeepDanbooru 模型的二次元图片智能打标服务
"""

from fastapi import FastAPI, HTTPException, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, HttpUrl
from typing import List, Optional, Dict
import logging
import time
import requests
import uuid
import threading
from io import BytesIO
from PIL import Image
import numpy as np

from deepdanbooru import DeepDanbooruModel
from tag_translator import get_translator

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 创建 FastAPI 应用
app = FastAPI(
    title="AI 智能打标服务",
    description="基于 DeepDanbooru 模型的二次元图片智能打标与视觉搜索服务",
    version="2.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# 配置 CORS（跨域资源共享）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应该限制为特定域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 全局变量：模型实例和翻译器
model: Optional[DeepDanbooruModel] = None
translator = None
mobilenet_model = None  # MobileNetV2 视觉特征提取模型
# 批量打标任务存储
batch_jobs: Dict[str, dict] = {}


# 请求和响应模型
class TaggingRequest(BaseModel):
    """打标请求"""
    image_url: HttpUrl
    
    class Config:
        json_schema_extra = {
            "example": {
                "image_url": "https://example.com/image.jpg"
            }
        }


class BatchTaggingRequest(BaseModel):
    """批量打标请求"""
    image_urls: List[HttpUrl]
    
    class Config:
        json_schema_extra = {
            "example": {
                "image_urls": [
                    "https://example.com/image1.jpg",
                    "https://example.com/image2.jpg"
                ]
            }
        }


class FeatureRequest(BaseModel):
    """特征提取请求"""
    image_url: HttpUrl
    
    class Config:
        json_schema_extra = {
            "example": {
                "image_url": "https://example.com/image.jpg"
            }
        }


class TagPrediction(BaseModel):
    """标签预测结果"""
    tag: str
    tag_zh: str
    confidence: float
    
    class Config:
        json_schema_extra = {
            "example": {
                "tag": "1girl",
                "tag_zh": "1个女孩",
                "confidence": 0.95
            }
        }


class TaggingResponse(BaseModel):
    """打标响应"""
    tags: List[TagPrediction]
    processing_time: float
    
    class Config:
        json_schema_extra = {
            "example": {
                "tags": [
                    {"tag": "1girl", "tag_zh": "1个女孩", "confidence": 0.95},
                    {"tag": "long_hair", "tag_zh": "长发", "confidence": 0.88},
                    {"tag": "school_uniform", "tag_zh": "校服", "confidence": 0.76}
                ],
                "processing_time": 0.85
            }
        }


class HealthResponse(BaseModel):
    """健康检查响应"""
    status: str
    model_loaded: bool
    tags_count: int
    
    class Config:
        json_schema_extra = {
            "example": {
                "status": "healthy",
                "model_loaded": True,
                "tags_count": 6000
            }
        }


@app.on_event("startup")
async def startup_event():
    """应用启动时加载模型和翻译器"""
    global model, translator, mobilenet_model
    
    logger.info("正在启动 AI 智能打标服务...")
    
    try:
        logger.info("正在加载 DeepDanbooru 模型...")
        model = DeepDanbooruModel()
        logger.info(f"模型加载成功！支持 {len(model.tags)} 个标签")
    except Exception as e:
        logger.error(f"模型加载失败: {str(e)}")
        logger.warning("服务将在没有模型的情况下启动，预测功能将不可用")
        model = None
    
    try:
        logger.info("正在加载 MobileNetV2 视觉特征模型...")
        import tensorflow as tf
        mobilenet_model = tf.keras.applications.MobileNetV2(
            input_shape=(224, 224, 3),
            include_top=False,
            weights='imagenet',
            pooling='avg'
        )
        logger.info(f"MobileNetV2 加载成功！输出维度: {mobilenet_model.output_shape}")
    except Exception as e:
        logger.error(f"MobileNetV2 加载失败: {str(e)}")
        logger.warning("以图搜图功能将不可用")
        mobilenet_model = None
    
    try:
        logger.info("正在加载标签翻译器...")
        translator = get_translator()
        stats = translator.get_translation_stats()
        logger.info(f"翻译器加载成功！支持 {stats['total_translations']} 个标签翻译")
    except Exception as e:
        logger.error(f"翻译器加载失败: {str(e)}")
        logger.warning("将使用默认翻译规则")
        translator = get_translator()
    
    logger.info("AI 智能打标服务启动完成！")


@app.get("/", tags=["根路径"])
async def root():
    """根路径"""
    return {
        "service": "AI 智能打标服务",
        "version": "1.0.0",
        "status": "running",
        "docs": "/docs"
    }


@app.get("/health", response_model=HealthResponse, tags=["健康检查"])
async def health_check():
    """
    健康检查接口
    
    返回服务的健康状态和模型加载情况
    """
    return HealthResponse(
        status="healthy" if model is not None else "degraded",
        model_loaded=model is not None,
        tags_count=len(model.tags) if model is not None else 0
    )


@app.post("/api/predict", response_model=TaggingResponse, tags=["智能打标"])
async def predict_tags(request: TaggingRequest):
    """
    智能打标接口
    
    根据图片 URL 预测标签
    
    - **image_url**: 图片的 URL 地址（必须是可访问的公网地址）
    
    返回：
    - **tags**: 标签列表（按置信度降序排列）
    - **processing_time**: 处理时间（秒）
    
    注意：
    - 置信度阈值为 0.3，低于此值的标签将被过滤
    - 最多返回 15 个标签
    - 处理时间应在 10 秒内完成
    """
    # 检查模型是否已加载
    if model is None:
        logger.error("模型未加载，无法进行预测")
        raise HTTPException(
            status_code=503,
            detail="模型未加载，服务暂时不可用"
        )
    
    start_time = time.time()
    
    try:
        # 1. 下载图片
        logger.info(f"正在下载图片: {request.image_url}")
        response = requests.get(str(request.image_url), timeout=10)
        response.raise_for_status()
        
        # 2. 加载图片
        image = Image.open(BytesIO(response.content))
        logger.info(f"图片下载成功，尺寸: {image.size}")
        
        # 3. 预测标签
        logger.info("正在预测标签...")
        predictions = model.predict(image)
        
        # 4. 过滤、翻译和排序
        # 置信度阈值：0.4
        # 最多返回：8 个标签
        filtered_predictions = []
        for tag, confidence in predictions:
            if confidence >= 0.4:
                # 跳过垃圾标签和内容分级标签
                if _should_skip_tag(tag):
                    continue
                tag_zh = translator.translate(tag) if translator else tag
                filtered_predictions.append(
                    TagPrediction(
                        tag=tag,
                        tag_zh=tag_zh,
                        confidence=float(confidence)
                    )
                )
        
        filtered_predictions = filtered_predictions[:8]
        
        processing_time = time.time() - start_time
        
        logger.info(
            f"预测完成！生成 {len(filtered_predictions)} 个标签，"
            f"处理时间: {processing_time:.2f} 秒"
        )
        
        return TaggingResponse(
            tags=filtered_predictions,
            processing_time=round(processing_time, 2)
        )
        
    except requests.RequestException as e:
        logger.error(f"下载图片失败: {str(e)}")
        raise HTTPException(
            status_code=400,
            detail=f"无法下载图片: {str(e)}"
        )
    except Exception as e:
        logger.error(f"预测失败: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"预测失败: {str(e)}"
        )


def _should_skip_tag(tag: str) -> bool:
    """
    判断标签是否应该跳过（垃圾标签/分级标签）
    """
    # 跳过内容分级标签
    if tag.startswith('rating:'):
        return True
    # 跳过纯特殊字符/数字的垃圾标签
    stripped = tag.strip('_- ')
    if not stripped or all(c in '?!.#@*&^%$~=+|\\/<>[]{}()' for c in stripped):
        return True
    # 跳过太短且无意义的标签
    if len(stripped) <= 1 and not stripped.isalpha():
        return True
    return False


def _download_image(url: str) -> Image.Image:
    """下载图片并返回 PIL Image 对象"""
    response = requests.get(str(url), timeout=15)
    response.raise_for_status()
    return Image.open(BytesIO(response.content))


def _predict_tags_for_image(image: Image.Image) -> List[TagPrediction]:
    """对单张图片进行标签预测"""
    predictions = model.predict(image)
    filtered = []
    for tag, confidence in predictions:
        if confidence >= 0.4:
            if _should_skip_tag(tag):
                continue
            tag_zh = translator.translate(tag) if translator else tag
            filtered.append(TagPrediction(tag=tag, tag_zh=tag_zh, confidence=float(confidence)))
    return filtered[:8]


# ==================== 批量打标接口 ====================

@app.post("/api/tagging/batch", tags=["批量打标"])
async def batch_tagging(request: BatchTaggingRequest):
    """
    批量打标接口（异步）
    
    提交多张图片的打标任务，返回任务 ID，可通过状态查询接口获取结果。
    最多支持 50 张图片。
    """
    if model is None:
        raise HTTPException(status_code=503, detail="模型未加载")
    
    if len(request.image_urls) > 50:
        raise HTTPException(status_code=400, detail="单次最多提交 50 张图片")
    
    job_id = str(uuid.uuid4())
    batch_jobs[job_id] = {
        "status": "processing",
        "total": len(request.image_urls),
        "completed": 0,
        "results": {},
        "created_at": time.time()
    }
    
    def process_batch():
        job = batch_jobs[job_id]
        for i, url in enumerate(request.image_urls):
            try:
                image = _download_image(str(url))
                tags = _predict_tags_for_image(image)
                job["results"][str(url)] = {
                    "status": "success",
                    "tags": [t.model_dump() for t in tags]
                }
            except Exception as e:
                job["results"][str(url)] = {
                    "status": "error",
                    "error": str(e)
                }
            job["completed"] = i + 1
        job["status"] = "completed"
    
    thread = threading.Thread(target=process_batch, daemon=True)
    thread.start()
    
    return {"job_id": job_id, "status": "processing", "total": len(request.image_urls)}


@app.get("/api/tagging/status/{job_id}", tags=["批量打标"])
async def get_batch_status(job_id: str):
    """
    查询批量打标任务状态
    """
    if job_id not in batch_jobs:
        raise HTTPException(status_code=404, detail="任务不存在")
    
    job = batch_jobs[job_id]
    return {
        "job_id": job_id,
        "status": job["status"],
        "total": job["total"],
        "completed": job["completed"],
        "results": job["results"] if job["status"] == "completed" else None
    }


# ==================== 标签翻译接口 ====================

class TranslateTagsRequest(BaseModel):
    tags: List[str]

@app.post("/api/tags/translate", tags=["标签翻译"])
async def translate_tags(request: TranslateTagsRequest):
    """
    批量翻译标签名称（英文→中文）
    """
    if translator is None:
        raise HTTPException(status_code=503, detail="翻译器未加载")
    
    results = {}
    for tag in request.tags:
        zh = translator.translate(tag)
        if zh and zh != tag:
            results[tag] = zh
    
    return {"translations": results, "count": len(results)}


# ==================== 以图搜图 - 特征提取接口 ====================

@app.post("/api/extract-features", tags=["以图搜图"])
async def extract_features(request: FeatureRequest):
    """
    图像特征提取接口
    
    从图片中提取 DeepDanbooru 模型的特征向量（标签概率分布），
    用于以图搜图的向量相似度检索。
    
    返回 256 维归一化特征向量（取 Top-256 标签概率值）。
    """
    if model is None:
        raise HTTPException(status_code=503, detail="模型未加载")
    
    start_time = time.time()
    
    try:
        image = _download_image(str(request.image_url))
        vector = _extract_feature_vector(image)
        
        processing_time = time.time() - start_time
        logger.info(f"特征提取完成，维度: {len(vector)}, 耗时: {processing_time:.2f}s")
        
        return {
            "feature_vector": vector,
            "dimensions": len(vector),
            "processing_time": round(processing_time, 2)
        }
    except requests.RequestException as e:
        raise HTTPException(status_code=400, detail=f"无法下载图片: {str(e)}")
    except Exception as e:
        logger.error(f"特征提取失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"特征提取失败: {str(e)}")


@app.post("/api/extract-features/upload", tags=["以图搜图"])
async def extract_features_upload(file: UploadFile = File(...)):
    """
    图像特征提取接口（文件上传版）
    
    直接上传图片文件提取特征向量，用于用户端以图搜图。
    支持 JPG/PNG/GIF/WEBP 格式，最大 10MB。
    """
    if model is None:
        raise HTTPException(status_code=503, detail="模型未加载")
    
    # 验证文件类型
    allowed_types = {"image/jpeg", "image/png", "image/gif", "image/webp"}
    if file.content_type not in allowed_types:
        raise HTTPException(status_code=400, detail="不支持的文件格式，仅支持 JPG/PNG/GIF/WEBP")
    
    start_time = time.time()
    
    try:
        contents = await file.read()
        if len(contents) > 10 * 1024 * 1024:
            raise HTTPException(status_code=400, detail="文件大小不能超过 10MB")
        
        image = Image.open(BytesIO(contents))
        vector = _extract_feature_vector(image)
        
        # 同时返回标签预测结果
        tags = _predict_tags_for_image(image)
        
        processing_time = time.time() - start_time
        logger.info(f"上传图片特征提取完成，维度: {len(vector)}, 耗时: {processing_time:.2f}s")
        
        return {
            "feature_vector": vector,
            "dimensions": len(vector),
            "tags": [t.model_dump() for t in tags],
            "processing_time": round(processing_time, 2)
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"上传图片特征提取失败: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"特征提取失败: {str(e)}")


def _extract_feature_vector(image: Image.Image) -> List[float]:
    """
    从图片中提取视觉特征向量
    
    使用 MobileNetV2（预训练于 ImageNet）提取通用视觉特征。
    MobileNetV2 训练于百万张多样化图片，能识别图片的视觉内容、
    结构和风格，自然区分不同类型的图片（二次元/图表/截图/照片）。
    
    流程：
    1. 图片预处理为 224×224
    2. MobileNetV2 提取 1280 维特征 (GlobalAveragePooling)
    3. 分桶最大池化压缩为 256 维
    4. L2 归一化
    """
    import tensorflow as tf
    
    VECTOR_DIM = 256
    
    if mobilenet_model is None:
        logger.error("MobileNetV2 未加载")
        return [0.0] * VECTOR_DIM
    
    # 预处理：调整尺寸 + MobileNetV2 标准化
    img = image.copy().resize((224, 224)).convert('RGB')
    arr = np.array(img, dtype=np.float32)
    arr = tf.keras.applications.mobilenet_v2.preprocess_input(arr)
    arr = np.expand_dims(arr, axis=0)
    
    # 提取 1280 维特征
    features = mobilenet_model.predict(arr, verbose=0)[0]  # (1280,)
    
    # 分桶最大池化：1280 → 256 (每桶 5 个)
    bucket_size = len(features) // VECTOR_DIM  # 1280 // 256 = 5
    vector = np.array(
        [np.max(features[i * bucket_size: (i + 1) * bucket_size]) for i in range(VECTOR_DIM)],
        dtype=np.float32
    )
    
    # L2 归一化
    norm = np.linalg.norm(vector)
    if norm > 0:
        vector = vector / norm
    
    return vector.tolist()


if __name__ == "__main__":
    import uvicorn
    
    # 启动服务
    # host: 0.0.0.0 表示监听所有网络接口
    # port: 8000 是默认端口
    # reload: True 表示代码修改后自动重启（开发环境）
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
