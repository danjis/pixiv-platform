"""
测试完整翻译功能

验证翻译器的所有功能
"""

from tag_translator import TagTranslator
import os

def test_full_translation():
    """测试完整翻译功能"""
    
    print("=" * 60)
    print("测试完整标签翻译功能")
    print("=" * 60)
    
    # 检查环境变量
    baidu_appid = os.getenv('BAIDU_APPID')
    baidu_secret = os.getenv('BAIDU_SECRET')
    
    if baidu_appid and baidu_secret:
        print("\n✅ 检测到百度翻译 API 配置")
        print(f"   APP ID: {baidu_appid[:8]}...")
        use_online = True
    else:
        print("\n⚠️  未检测到百度翻译 API 配置")
        print("   将只使用预设翻译和默认规则")
        use_online = False
    
    # 创建翻译器
    print("\n正在初始化翻译器...")
    translator = TagTranslator(
        use_online_api=use_online,
        baidu_appid=baidu_appid,
        baidu_secret=baidu_secret
    )
    
    # 获取统计信息
    stats = translator.get_translation_stats()
    print("\n翻译器统计信息：")
    print(f"  - 预设翻译: {stats['preset_translations']} 个")
    print(f"  - 缓存翻译: {stats['cached_translations']} 个")
    print(f"  - 总翻译数: {stats['total_translations']} 个")
    print(f"  - 在线 API: {'已启用' if stats['online_api_enabled'] else '未启用'}")
    
    # 测试预设翻译
    print("\n" + "=" * 60)
    print("测试 1: 预设翻译（高质量）")
    print("-" * 60)
    
    preset_tags = [
        "1girl", "long_hair", "blue_eyes", "school_uniform", "smile",
        "sitting", "looking_at_viewer", "outdoors", "sky", "cherry_blossoms"
    ]
    
    for tag in preset_tags:
        tag_zh = translator.translate(tag)
        status = "✅" if tag != tag_zh else "⚠️"
        print(f"{status} {tag:30s} -> {tag_zh}")
    
    # 测试默认规则
    print("\n" + "=" * 60)
    print("测试 2: 默认规则翻译")
    print("-" * 60)
    
    rule_tags = [
        "2girls", "3boys", "red_hair", "green_eyes", "blue_dress"
    ]
    
    for tag in rule_tags:
        tag_zh = translator.translate(tag)
        status = "✅" if tag != tag_zh else "⚠️"
        print(f"{status} {tag:30s} -> {tag_zh}")
    
    # 测试在线翻译（如果启用）
    if use_online:
        print("\n" + "=" * 60)
        print("测试 3: 在线翻译（百度 API）")
        print("-" * 60)
        
        online_tags = [
            "absurdres", "highres", "masterpiece"
        ]
        
        print("正在测试在线翻译...")
        for tag in online_tags:
            tag_zh = translator.translate(tag)
            status = "✅" if tag != tag_zh else "❌"
            print(f"{status} {tag:30s} -> {tag_zh}")
    
    # 测试批量翻译
    print("\n" + "=" * 60)
    print("测试 4: 批量翻译")
    print("-" * 60)
    
    batch_tags = ["1girl", "long_hair", "blue_eyes", "smile", "school_uniform"]
    translated = translator.translate_batch(batch_tags)
    
    for en, zh in zip(batch_tags, translated):
        print(f"  {en:20s} -> {zh}")
    
    # 最终统计
    print("\n" + "=" * 60)
    print("最终统计信息")
    print("-" * 60)
    
    stats = translator.get_translation_stats()
    print(f"  - 预设翻译: {stats['preset_translations']} 个")
    print(f"  - 缓存翻译: {stats['cached_translations']} 个")
    print(f"  - 总翻译数: {stats['total_translations']} 个")
    
    # 计算覆盖率（假设总共 9176 个标签）
    total_tags = 9176
    coverage = stats['total_translations'] / total_tags * 100
    print(f"  - 覆盖率: {coverage:.2f}%")
    
    print("\n" + "=" * 60)
    print("✅ 翻译功能测试完成！")
    print("=" * 60)
    
    # 提示
    if not use_online:
        print("\n💡 提示：")
        print("   配置百度翻译 API 可以实现 100% 覆盖率")
        print("   参考文档：FULL-TRANSLATION-GUIDE.md")
    elif stats['total_translations'] < 9000:
        print("\n💡 提示：")
        print("   运行批量翻译脚本可以翻译所有标签：")
        print("   python batch_translate_tags.py")

if __name__ == "__main__":
    # 尝试加载 .env 文件
    try:
        from dotenv import load_dotenv
        load_dotenv()
    except ImportError:
        pass
    
    test_full_translation()

