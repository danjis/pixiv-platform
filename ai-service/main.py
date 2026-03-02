"""
AI 智能打标服务 - FastAPI 应用入口

基于 DeepDanbooru 模型的二次元图片智能打标服务
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, HttpUrl
from typing import List, Optional
import logging
import time
import requests
from io import BytesIO
from PIL import Image

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
    description="基于 DeepDanbooru 模型的二次元图片智能打标服务",
    version="1.0.0",
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
    global model, translator
    
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
