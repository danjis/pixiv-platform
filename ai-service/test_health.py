"""
测试健康检查接口

验证 /health 接口是否正常工作
"""

import requests
import sys

def test_health_check():
    """测试健康检查接口"""
    
    # AI 服务地址
    base_url = "http://localhost:8000"
    health_url = f"{base_url}/health"
    
    print("=" * 60)
    print("测试 AI 服务健康检查接口")
    print("=" * 60)
    
    try:
        # 发送 GET 请求
        print(f"\n正在请求: {health_url}")
        response = requests.get(health_url, timeout=5)
        
        # 检查状态码
        print(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            # 解析响应
            data = response.json()
            
            print("\n响应内容:")
            print(f"  - status: {data.get('status')}")
            print(f"  - model_loaded: {data.get('model_loaded')}")
            print(f"  - tags_count: {data.get('tags_count')}")
            
            # 验证响应格式
            assert 'status' in data, "缺少 status 字段"
            assert 'model_loaded' in data, "缺少 model_loaded 字段"
            assert 'tags_count' in data, "缺少 tags_count 字段"
            
            assert data['status'] in ['healthy', 'degraded'], "status 值不正确"
            assert isinstance(data['model_loaded'], bool), "model_loaded 应该是布尔值"
            assert isinstance(data['tags_count'], int), "tags_count 应该是整数"
            
            print("\n✅ 健康检查接口测试通过！")
            
            # 检查服务状态
            if data['status'] == 'healthy' and data['model_loaded']:
                print(f"\n🎉 服务状态良好！模型已加载，支持 {data['tags_count']} 个标签")
            elif data['status'] == 'degraded':
                print("\n⚠️  服务处于降级状态（模型未加载）")
            
            return True
            
        else:
            print(f"\n❌ 请求失败，状态码: {response.status_code}")
            print(f"响应内容: {response.text}")
            return False
            
    except requests.exceptions.ConnectionError:
        print("\n❌ 无法连接到 AI 服务")
        print("请确保服务已启动: python main.py")
        return False
        
    except requests.exceptions.Timeout:
        print("\n❌ 请求超时")
        return False
        
    except Exception as e:
        print(f"\n❌ 测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


if __name__ == "__main__":
    success = test_health_check()
    sys.exit(0 if success else 1)

