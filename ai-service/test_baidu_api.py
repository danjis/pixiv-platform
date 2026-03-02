"""测试百度翻译 API 配置"""
import os
import requests
import hashlib
import random

def test_baidu_api():
    """测试百度翻译 API"""
    print("=" * 60)
    print("百度翻译 API 诊断工具")
    print("=" * 60)
    
    # 加载配置
    try:
        from dotenv import load_dotenv
        load_dotenv()
    except:
        pass
    
    appid = os.getenv('BAIDU_APPID')
    secret = os.getenv('BAIDU_SECRET')
    
    if not appid or not secret:
        print("\n❌ 错误：未找到 API 配置")
        print("请检查 .env 文件")
        return
    
    print(f"\n✅ 配置已加载")
    print(f"   APP ID: {appid[:8]}...")
    print(f"   Secret: {secret[:8]}...")
    
    # 测试翻译
    print("\n正在测试翻译 API...")
    test_text = "hello"
    
    url = 'https://fanyi-api.baidu.com/api/trans/vip/translate'
    salt = random.randint(32768, 65536)
    sign_str = f"{appid}{test_text}{salt}{secret}"
    sign = hashlib.md5(sign_str.encode('utf-8')).hexdigest()
    
    params = {
        'q': test_text,
        'from': 'en',
        'to': 'zh',
        'appid': appid,
        'salt': salt,
        'sign': sign
    }
    
    try:
        response = requests.get(url, params=params, timeout=5)
        result = response.json()
        
        print(f"\nAPI 响应: {result}")
        
        if 'error_code' in result:
            error_code = result['error_code']
            error_msg = result['error_msg']
            
            print(f"\n❌ 翻译失败")
            print(f"   错误代码: {error_code}")
            print(f"   错误信息: {error_msg}")
            
            # 错误代码说明
            if error_code == '54003':
                print("\n📋 错误 54003 说明：")
                print("   - 标准版 QPS 限制为 1（每秒 1 次请求）")
                print("   - 建议：使用智能批量翻译（自动限速）")
                print("   - 或升级到高级版（QPS 10）")
            elif error_code == '52001':
                print("\n📋 错误 52001 说明：")
                print("   - APP ID 或密钥错误")
                print("   - 请检查 .env 文件配置")
            elif error_code == '54004':
                print("\n📋 错误 54004 说明：")
                print("   - 账户余额不足")
                print("   - 请充值或等待免费额度恢复")
        else:
            print(f"\n✅ 翻译成功！")
            if 'trans_result' in result:
                translation = result['trans_result'][0]['dst']
                print(f"   {test_text} -> {translation}")
            
            print("\n💡 建议：")
            print("   使用智能批量翻译脚本：")
            print("   python smart_batch_translate.py")
            
    except Exception as e:
        print(f"\n❌ 请求失败: {str(e)}")

if __name__ == "__main__":
    test_baidu_api()
