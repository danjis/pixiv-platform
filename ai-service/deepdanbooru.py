"""
DeepDanbooru 模型包装类

封装 DeepDanbooru 模型的加载和预测功能
"""

import os
import logging
from typing import List, Tuple
import numpy as np
from PIL import Image

logger = logging.getLogger(__name__)


class DeepDanbooruModel:
    """
    DeepDanbooru 模型包装类
    
    负责加载模型和执行预测
    """
    
    def __init__(self, model_path: str = "models/deepdanbooru"):
        """
        初始化模型
        
        Args:
            model_path: 模型文件路径
        """
        self.model_path = model_path
        self.model = None
        self.tags = []
        self.input_size = 512  # DeepDanbooru 默认输入尺寸
        
        # 加载模型
        self._load_model()
    
    def _load_model(self):
        """
        加载 DeepDanbooru 模型
        
        加载步骤：
        1. 检查模型目录是否存在
        2. 导入 TensorFlow
        3. 加载模型文件（.h5 格式）
        4. 加载标签列表（tags.txt）
        
        Raises:
            FileNotFoundError: 模型文件或标签文件不存在
            ImportError: TensorFlow 未安装
            Exception: 其他加载错误
        """
        try:
            # 1. 检查模型目录是否存在
            if not os.path.exists(self.model_path):
                raise FileNotFoundError(
                    f"模型目录不存在: {self.model_path}\n"
                    f"请从 https://github.com/KichangKim/DeepDanbooru/releases "
                    f"下载预训练模型并解压到 {self.model_path} 目录"
                )
            
            logger.info(f"模型目录存在: {self.model_path}")
            
            # 2. 导入 TensorFlow（延迟导入，避免启动时加载）
            try:
                import tensorflow as tf
                logger.info(f"TensorFlow 版本: {tf.__version__}")
                
                # 配置 TensorFlow（可选）
                # 禁用 GPU（如果不需要）
                # tf.config.set_visible_devices([], 'GPU')
                
                # 设置日志级别（减少 TensorFlow 的输出）
                tf.get_logger().setLevel('ERROR')
                
            except ImportError as e:
                logger.error(f"导入 TensorFlow 失败: {str(e)}")
                logger.error("请安装 TensorFlow: pip install tensorflow==2.14.0")
                raise
            
            # 3. 加载模型文件
            logger.info(f"正在从 {self.model_path} 加载模型...")
            
            # 支持多种模型文件名
            possible_model_files = [
                "model-resnet_custom_v3.h5",
                "model-resnet_custom_v4.h5",
                "model.h5"
            ]
            
            model_file = None
            for filename in possible_model_files:
                candidate = os.path.join(self.model_path, filename)
                if os.path.exists(candidate):
                    model_file = candidate
                    logger.info(f"找到模型文件: {filename}")
                    break
            
            if model_file is None:
                raise FileNotFoundError(
                    f"模型文件不存在！\n"
                    f"请确保以下任一文件存在于 {self.model_path} 目录中:\n"
                    f"  - model-resnet_custom_v3.h5\n"
                    f"  - model-resnet_custom_v4.h5\n"
                    f"  - model.h5"
                )
            
            # 加载模型（不编译，因为只用于推理）
            logger.info(f"正在加载模型文件: {model_file}")
            self.model = tf.keras.models.load_model(
                model_file,
                compile=False
            )
            
            logger.info("模型加载成功！")
            logger.info(f"模型输入形状: {self.model.input_shape}")
            logger.info(f"模型输出形状: {self.model.output_shape}")
            
            # 4. 加载标签列表
            self._load_tags()
            
            logger.info(f"标签列表加载成功！共 {len(self.tags)} 个标签")
            
            # 验证模型输出维度与标签数量是否匹配
            output_dim = self.model.output_shape[-1]
            if output_dim != len(self.tags):
                logger.warning(
                    f"警告：模型输出维度 ({output_dim}) 与标签数量 ({len(self.tags)}) 不匹配！"
                )
            
        except ImportError:
            # 重新抛出 ImportError
            raise
        except FileNotFoundError:
            # 重新抛出 FileNotFoundError
            raise
        except Exception as e:
            logger.error(f"加载模型失败: {str(e)}", exc_info=True)
            raise RuntimeError(f"加载模型失败: {str(e)}") from e
    
    def _load_tags(self):
        """
        加载标签列表
        
        从 tags.txt 文件中加载所有标签名称
        
        Raises:
            FileNotFoundError: 标签文件不存在
            Exception: 读取文件失败
        """
        tags_file = os.path.join(self.model_path, "tags.txt")
        
        if not os.path.exists(tags_file):
            raise FileNotFoundError(
                f"标签文件不存在: {tags_file}\n"
                f"请确保 tags.txt 文件存在于模型目录中"
            )
        
        logger.info(f"正在加载标签列表: {tags_file}")
        
        try:
            with open(tags_file, "r", encoding="utf-8") as f:
                # 读取所有行，去除空白字符
                self.tags = [line.strip() for line in f.readlines() if line.strip()]
            
            logger.info(f"成功加载 {len(self.tags)} 个标签")
            
            # 打印前 10 个标签作为示例
            if len(self.tags) > 0:
                sample_tags = self.tags[:10]
                logger.info(f"标签示例: {', '.join(sample_tags)}")
            
        except Exception as e:
            logger.error(f"读取标签文件失败: {str(e)}", exc_info=True)
            raise RuntimeError(f"读取标签文件失败: {str(e)}") from e
    
    def _preprocess_image(self, image: Image.Image) -> np.ndarray:
        """
        预处理图片
        
        Args:
            image: PIL Image 对象
            
        Returns:
            预处理后的图片数组
        """
        # 转换为 RGB 模式
        if image.mode != "RGB":
            image = image.convert("RGB")
        
        # 调整大小
        image = image.resize((self.input_size, self.input_size), Image.LANCZOS)
        
        # 转换为 numpy 数组
        image_array = np.array(image, dtype=np.float32)
        
        # 归一化到 [0, 1]
        image_array = image_array / 255.0
        
        # 添加 batch 维度
        image_array = np.expand_dims(image_array, axis=0)
        
        return image_array
    
    def predict(self, image: Image.Image) -> List[Tuple[str, float]]:
        """
        预测图片标签
        
        Args:
            image: PIL Image 对象
            
        Returns:
            标签和置信度的列表，按置信度降序排列
            例如: [("1girl", 0.95), ("long_hair", 0.88), ...]
        """
        if self.model is None:
            raise RuntimeError("模型未加载")
        
        try:
            # 预处理图片
            image_array = self._preprocess_image(image)
            
            # 执行预测
            predictions = self.model.predict(image_array, verbose=0)[0]
            
            # 组合标签和置信度
            results = [
                (tag, float(confidence))
                for tag, confidence in zip(self.tags, predictions)
            ]
            
            # 按置信度降序排序
            results.sort(key=lambda x: x[1], reverse=True)
            
            return results
            
        except Exception as e:
            logger.error(f"预测失败: {str(e)}", exc_info=True)
            raise
    
    def predict_top_k(
        self,
        image: Image.Image,
        k: int = 15,
        threshold: float = 0.3
    ) -> List[Tuple[str, float]]:
        """
        预测图片的 Top-K 标签
        
        Args:
            image: PIL Image 对象
            k: 返回的标签数量
            threshold: 置信度阈值
            
        Returns:
            Top-K 标签和置信度的列表
        """
        predictions = self.predict(image)
        
        # 过滤低置信度的标签
        filtered = [
            (tag, confidence)
            for tag, confidence in predictions
            if confidence >= threshold
        ]
        
        # 返回 Top-K
        return filtered[:k]


# 测试代码
if __name__ == "__main__":
    # 配置日志
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    try:
        # 加载模型
        print("正在加载模型...")
        model = DeepDanbooruModel()
        print(f"模型加载成功！支持 {len(model.tags)} 个标签")
        
        # 测试预测（需要提供测试图片）
        test_image_path = "test.jpg"
        if os.path.exists(test_image_path):
            print(f"\n正在测试图片: {test_image_path}")
            image = Image.open(test_image_path)
            
            predictions = model.predict_top_k(image, k=10, threshold=0.3)
            
            print("\n预测结果:")
            for i, (tag, confidence) in enumerate(predictions, 1):
                print(f"{i}. {tag}: {confidence:.4f}")
        else:
            print(f"\n测试图片不存在: {test_image_path}")
            print("请将测试图片命名为 test.jpg 并放在当前目录")
        
    except Exception as e:
        print(f"错误: {str(e)}")
