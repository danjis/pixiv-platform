"""
生成简单翻译

不使用在线 API，通过规则和模式生成翻译
适用于无法使用百度 API 的情况
"""

import json
import re

def load_all_tags(tags_file='models/deepdanbooru/tags.txt'):
    """加载所有标签"""
    with open(tags_file, 'r', encoding='utf-8') as f:
        tags = [line.strip() for line in f if line.strip()]
    return tags

def translate_by_rules(tag):
    """
    使用规则翻译标签
    
    这是一个简化版本，不使用在线 API
    """
    # 1. 人物数量
    if tag == '1girl':
        return '1个女孩'
    elif tag == '2girls':
        return '2个女孩'
    elif tag == '3girls':
        return '3个女孩'
    elif tag == '4girls':
        return '4个女孩'
    elif tag == '5girls':
        return '5个女孩'
    elif tag == '6+girls':
        return '6个以上女孩'
    elif tag == 'multiple_girls':
        return '多个女孩'
    
    elif tag == '1boy':
        return '1个男孩'
    elif tag == '2boys':
        return '2个男孩'
    elif tag == '3boys':
        return '3个男孩'
    elif tag == '4boys':
        return '4个男孩'
    elif tag == '5boys':
        return '5个男孩'
    elif tag == '6+boys':
        return '6个以上男孩'
    elif tag == 'multiple_boys':
        return '多个男孩'
    
    # 2. 颜色 + 特征
    color_map = {
        'blue': '蓝色',
        'red': '红色',
        'green': '绿色',
        'yellow': '黄色',
        'purple': '紫色',
        'pink': '粉色',
        'orange': '橙色',
        'brown': '棕色',
        'black': '黑色',
        'white': '白色',
        'grey': '灰色',
        'gray': '灰色',
        'blonde': '金色',
        'silver': '银色'
    }
    
    feature_map = {
        'hair': '头发',
        'eyes': '眼睛',
        'dress': '连衣裙',
        'shirt': '衬衫',
        'skirt': '裙子',
        'bow': '蝴蝶结',
        'ribbon': '丝带'
    }
    
    for color_en, color_zh in color_map.items():
        for feature_en, feature_zh in feature_map.items():
            if tag == f'{color_en}_{feature_en}':
                return f'{color_zh}{feature_zh}'
    
    # 3. 简单的词汇替换
    simple_translations = {
        'solo': '单人',
        'smile': '微笑',
        'open_mouth': '张嘴',
        'blush': '脸红',
        'looking_at_viewer': '看向观众',
        'sitting': '坐着',
        'standing': '站着',
        'outdoors': '室外',
        'indoors': '室内',
        'sky': '天空',
        'cloud': '云',
        'tree': '树',
        'flower': '花',
        'water': '水',
        'day': '白天',
        'night': '夜晚',
    }
    
    if tag in simple_translations:
        return simple_translations[tag]
    
    # 4. 下划线转空格（作为最后的回退）
    # 例如：long_hair -> long hair
    return tag.replace('_', ' ')

def generate_translations():
    """生成所有标签的翻译"""
    print("=" * 60)
    print("生成简单翻译（无需 API）")
    print("=" * 60)
    
    # 加载所有标签
    print("\n正在加载标签列表...")
    tags = load_all_tags()
    print(f"共加载 {len(tags)} 个标签")
    
    # 加载现有翻译
    print("\n正在加载现有翻译...")
    try:
        with open('models/deepdanbooru/tag_translations.json', 'r', encoding='utf-8') as f:
            existing_translations = json.load(f)
        print(f"已有 {len(existing_translations)} 个预设翻译")
    except:
        existing_translations = {}
        print("未找到预设翻译文件")
    
    # 生成翻译
    print("\n正在生成翻译...")
    translations = {}
    translated_count = 0
    kept_english_count = 0
    
    for tag in tags:
        if tag in existing_translations:
            # 使用现有翻译
            translations[tag] = existing_translations[tag]
            translated_count += 1
        else:
            # 使用规则生成翻译
            translation = translate_by_rules(tag)
            translations[tag] = translation
            
            if translation != tag and '_' not in translation:
                translated_count += 1
            else:
                kept_english_count += 1
    
    # 保存到缓存文件
    print("\n正在保存翻译...")
    with open('models/deepdanbooru/tag_cache.json', 'w', encoding='utf-8') as f:
        json.dump(translations, f, ensure_ascii=False, indent=2)
    
    print("\n" + "=" * 60)
    print("翻译生成完成！")
    print(f"  - 总标签数: {len(tags)}")
    print(f"  - 已翻译: {translated_count} 个")
    print(f"  - 保留英文: {kept_english_count} 个")
    print(f"  - 覆盖率: {translated_count / len(tags) * 100:.2f}%")
    print("=" * 60)
    
    print("\n翻译结果已保存到: models/deepdanbooru/tag_cache.json")
    print("\n注意：")
    print("  - 部分标签使用了简单规则翻译")
    print("  - 未翻译的标签保留了英文（用空格替换下划线）")
    print("  - 您可以手动编辑 tag_cache.json 优化翻译质量")

if __name__ == "__main__":
    generate_translations()
