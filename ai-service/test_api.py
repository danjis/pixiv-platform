"""
测试智能打标 API

验证 POST /api/predict 接口的功能
"""

import requests
import json
import time


def test_predict_api():
    """测试预测 API"""
    
    # API 地址
    api_url = "http://localhost:8000/api/predict"
    
    # 测试图片 URL（使用一个公开的测试图片）
    test_image_url = "https://raw.githubusercontent.com/KichangKim/DeepDanbooru/master/project/sample/sample.jpg"
    
    # 构建请求
    request_data = {
        "image_url": test_image_url
    }
    
    print("=" * 60)
    print("测试智能打标 API")
    print("=" * 60)
    print(f"\n请求 URL: {api_url}")
    print(f"图片 URL: {test_image_url}")
    print("\n发送请求...")
    
    try:
        # 记录开始时间
        start_time = time.time()
        
        # 发送 POST 请求
        response = requests.post(
            api_url,
            json=request_data,
            headers={"Content-Type": "application/json"},
            timeout=15
        )
        
        # 记录结束时间
        end_time = time.time()
        total_time = end_time - start_time
        
        print(f"\n响应状态码: {response.status_code}")
        print(f"总耗时: {total_time:.2f} 秒")
        
        if response.status_code == 200:
            # 解析响应
            result = response.json()
            
            print("\n✓ 请求成功！")
            print(f"\n生成标签数量: {len(result['tags'])}")
            print(f"服务器处理时间: {result['processing_time']} 秒")
            
            print("\n预测的标签:")
            print("-" * 60)
            for i, tag_info in enumerate(result['tags'], 1):
                tag = tag_info['tag']
                confidence = tag_info['confidence']
                print(f"{i:2d}. {tag:30s} (置信度: {confidence:.4f})")
            
            # 验证需求
            print("\n" + "=" * 60)
            print("需求验证:")
            print("=" * 60)
            
            # 需求 6.1: 分析图片内容
            print("✓ 需求 6.1: 成功分析图片内容")
            
            # 需求 6.2: 生成至少 3 个相关标签
            if len(result['tags']) >= 3:
                print(f"✓ 需求 6.2: 生成了 {len(result['tags'])} 个标签（≥ 3）")
            else:
                print(f"✗ 需求 6.2: 只生成了 {len(result['tags'])} 个标签（< 3）")
            
            # 需求 6.3: 置信度过滤（阈值 0.3）
            min_confidence = min(tag['confidence'] for tag in result['tags']) if result['tags'] else 0
            if min_confidence >= 0.3:
                print(f"✓ 需求 6.3: 所有标签置信度 ≥ 0.3（最低: {min_confidence:.4f}）")
            else:
                print(f"✗ 需求 6.3: 存在低于 0.3 的标签（最低: {min_confidence:.4f}）")
            
            # 验证最多返回 15 个标签
            if len(result['tags']) <= 15:
                print(f"✓ 标签数量限制: {len(result['tags'])} ≤ 15")
            else:
                print(f"✗ 标签数量超限: {len(result['tags'])} > 15")
            
            # 需求 6.5: 10 秒内完成
            if result['processing_time'] <= 10:
                print(f"✓ 需求 6.5: 处理时间 {result['processing_time']} 秒 ≤ 10 秒")
            else:
                print(f"✗ 需求 6.5: 处理时间 {result['processing_time']} 秒 > 10 秒")
            
            print("\n" + "=" * 60)
            print("测试完成！")
            print("=" * 60)
            
        else:
            print(f"\n✗ 请求失败！")
            print(f"错误信息: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("\n✗ 连接失败！")
        print("请确保 AI 服务正在运行：")
        print("  cd pixiv-platform/ai-service")
        print("  python main.py")
        
    except requests.exceptions.Timeout:
        print("\n✗ 请求超时！")
        print("服务响应时间超过 15 秒")
        
    except Exception as e:
        print(f"\n✗ 发生错误: {str(e)}")


def test_health_check():
    """测试健康检查接口"""
    
    health_url = "http://localhost:8000/health"
    
    print("\n" + "=" * 60)
    print("测试健康检查接口")
    print("=" * 60)
    
    try:
        response = requests.get(health_url, timeout=5)
        
        if response.status_code == 200:
            result = response.json()
            print(f"\n✓ 服务状态: {result['status']}")
            print(f"✓ 模型已加载: {result['model_loaded']}")
            print(f"✓ 支持标签数: {result['tags_count']}")
        else:
            print(f"\n✗ 健康检查失败: {response.status_code}")
            
    except Exception as e:
        print(f"\n✗ 健康检查失败: {str(e)}")


if __name__ == "__main__":
    # 先测试健康检查
    test_health_check()
    
    # 再测试预测 API
    test_predict_api()
