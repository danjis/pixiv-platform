"""
测试模型加载功能

此脚本用于测试 DeepDanbooruModel 类的模型加载功能
"""

import logging
import sys
import os

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)


def test_model_loading():
    """测试模型加载功能"""
    
    print("=" * 60)
    print("DeepDanbooru 模型加载测试")
    print("=" * 60)
    
    try:
        from deepdanbooru import DeepDanbooruModel
        
        print("\n1. 导入 DeepDanbooruModel 类... ✓")
        
        # 检查模型目录是否存在
        model_path = "models/deepdanbooru"
        if not os.path.exists(model_path):
            print(f"\n⚠️  警告：模型目录不存在: {model_path}")
            print("\n请按照以下步骤下载模型：")
            print("1. 访问 https://github.com/KichangKim/DeepDanbooru/releases")
            print("2. 下载 model-resnet_custom_v3.h5 和 tags.txt")
            print(f"3. 将文件放到 {model_path}/ 目录下")
            print("\n详细说明请查看 models/README.md")
            return False
        
        print(f"\n2. 检查模型目录... ✓ ({model_path})")
        
        # 检查必需的文件
        required_files = ["tags.txt"]
        model_files = [
            "model-resnet_custom_v3.h5",
            "model-resnet_custom_v4.h5",
            "model.h5"
        ]
        
        # 检查标签文件
        tags_file = os.path.join(model_path, "tags.txt")
        if not os.path.exists(tags_file):
            print(f"\n❌ 错误：标签文件不存在: {tags_file}")
            return False
        
        print(f"3. 检查标签文件... ✓ ({tags_file})")
        
        # 检查模型文件
        model_file_found = False
        for model_file in model_files:
            full_path = os.path.join(model_path, model_file)
            if os.path.exists(full_path):
                print(f"4. 检查模型文件... ✓ ({model_file})")
                model_file_found = True
                break
        
        if not model_file_found:
            print(f"\n❌ 错误：模型文件不存在")
            print(f"请确保以下任一文件存在于 {model_path} 目录中:")
            for mf in model_files:
                print(f"  - {mf}")
            return False
        
        # 尝试加载模型
        print("\n5. 正在加载模型...")
        print("   （首次加载可能需要 5-10 秒）")
        
        model = DeepDanbooruModel(model_path=model_path)
        
        print(f"\n✓ 模型加载成功！")
        print(f"   - 支持标签数量: {len(model.tags)}")
        print(f"   - 输入尺寸: {model.input_size}x{model.input_size}")
        print(f"   - 模型输入形状: {model.model.input_shape}")
        print(f"   - 模型输出形状: {model.model.output_shape}")
        
        # 显示标签示例
        if len(model.tags) > 0:
            print(f"\n   标签示例（前 20 个）:")
            for i, tag in enumerate(model.tags[:20], 1):
                print(f"      {i:2d}. {tag}")
        
        print("\n" + "=" * 60)
        print("✓ 所有测试通过！模型已成功加载。")
        print("=" * 60)
        
        return True
        
    except ImportError as e:
        print(f"\n❌ 导入错误: {str(e)}")
        if "tensorflow" in str(e).lower():
            print("\n请安装 TensorFlow:")
            print("  pip install tensorflow==2.14.0")
        return False
        
    except FileNotFoundError as e:
        print(f"\n❌ 文件不存在: {str(e)}")
        return False
        
    except Exception as e:
        print(f"\n❌ 加载失败: {str(e)}")
        logger.error("详细错误信息:", exc_info=True)
        return False


def test_model_prediction():
    """测试模型预测功能（如果有测试图片）"""
    
    print("\n" + "=" * 60)
    print("测试模型预测功能")
    print("=" * 60)
    
    try:
        from deepdanbooru import DeepDanbooruModel
        from PIL import Image
        
        # 查找测试图片
        test_images = ["test.jpg", "test.png", "sample.jpg", "sample.png"]
        test_image_path = None
        
        for img_path in test_images:
            if os.path.exists(img_path):
                test_image_path = img_path
                break
        
        if test_image_path is None:
            print("\n⚠️  未找到测试图片")
            print("如需测试预测功能，请将测试图片命名为以下任一名称：")
            for img in test_images:
                print(f"  - {img}")
            return False
        
        print(f"\n找到测试图片: {test_image_path}")
        
        # 加载模型
        print("正在加载模型...")
        model = DeepDanbooruModel()
        
        # 加载图片
        print(f"正在加载图片: {test_image_path}")
        image = Image.open(test_image_path)
        print(f"图片尺寸: {image.size}, 模式: {image.mode}")
        
        # 预测标签
        print("\n正在预测标签...")
        predictions = model.predict_top_k(image, k=15, threshold=0.3)
        
        print(f"\n预测结果（共 {len(predictions)} 个标签）:")
        print("-" * 60)
        for i, (tag, confidence) in enumerate(predictions, 1):
            bar_length = int(confidence * 40)
            bar = "█" * bar_length + "░" * (40 - bar_length)
            print(f"{i:2d}. {tag:30s} {bar} {confidence:.4f}")
        
        print("\n✓ 预测测试通过！")
        return True
        
    except Exception as e:
        print(f"\n❌ 预测测试失败: {str(e)}")
        logger.error("详细错误信息:", exc_info=True)
        return False


if __name__ == "__main__":
    print("\n")
    print("╔" + "═" * 58 + "╗")
    print("║" + " " * 10 + "DeepDanbooru 模型加载测试工具" + " " * 17 + "║")
    print("╚" + "═" * 58 + "╝")
    print("\n")
    
    # 测试模型加载
    loading_success = test_model_loading()
    
    if loading_success:
        # 如果加载成功，尝试测试预测
        print("\n")
        test_model_prediction()
    
    print("\n")
    
    # 返回退出码
    sys.exit(0 if loading_success else 1)
