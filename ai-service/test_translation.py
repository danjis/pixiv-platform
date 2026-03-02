"""
测试标签翻译功能

验证英文标签能够正确翻译成中文
"""

from tag_translator import TagTranslator

def test_translation():
    """测试标签翻译"""
    
    print("=" * 60)
    print("测试标签翻译功能")
    print("=" * 60)
    
    # 创建翻译器
    translator = TagTranslator()
    
    # 测试常见标签
    test_tags = [
        "1girl",
        "long_hair",
        "blue_eyes",
        "school_uniform",
        "smile",
        "sitting",
        "looking_at_viewer",
        "outdoors",
        "sky",
        "cherry_blossoms",
        "blue_hair",
        "red_eyes",
        "white_dress",
        "cat_ears",
        "wings"
    ]
    
    print("\n测试标签翻译：")
    print("-" * 60)
    
    for tag in test_tags:
        tag_zh = translator.translate(tag)
        status = "✅" if tag != tag_zh else "⚠️"
        print(f"{status} {tag:30s} -> {tag_zh}")
    
    # 获取统计信息
    stats = translator.get_translation_stats()
    print("\n" + "=" * 60)
    print("翻译器统计信息：")
    print(f"  - 翻译字典文件: {stats['translation_file']}")
    print(f"  - 文件是否存在: {stats['file_exists']}")
    print(f"  - 支持翻译数量: {stats['total_translations']}")
    print("=" * 60)
    
    # 测试批量翻译
    print("\n测试批量翻译：")
    print("-" * 60)
    
    batch_tags = ["1girl", "long_hair", "blue_eyes", "smile", "school_uniform"]
    translated = translator.translate_batch(batch_tags)
    
    for en, zh in zip(batch_tags, translated):
        print(f"  {en:20s} -> {zh}")
    
    print("\n✅ 翻译功能测试完成！")


if __name__ == "__main__":
    test_translation()

