"""
批量翻译所有标签

读取 tags.txt 文件，使用百度翻译 API 批量翻译所有标签
翻译结果保存到 tag_cache.json
"""

import json
import time
import os
from tag_translator import TagTranslator
from tqdm import tqdm

def load_all_tags(tags_file='models/deepdanbooru/tags.txt'):
    """加载所有标签"""
    with open(tags_file, 'r', encoding='utf-8') as f:
        tags = [line.strip() for line in f if line.strip()]
    return tags

def batch_translate(translator, tags, batch_size=10, delay=1.0):
    """
    批量翻译标签
    
    Args:
        translator: 翻译器实例
        tags: 标签列表
        batch_size: 每批翻译数量
        delay: 每批之间的延迟（秒），避免 API 限流
    """
    print(f"开始批量翻译 {len(tags)} 个标签...")
    print(f"批次大小: {batch_size}, 延迟: {delay} 秒")
    print("=" * 60)
    
    translated_count = 0
    skipped_count = 0
    failed_count = 0
    
    # 使用 tqdm 显示进度条
    for i in tqdm(range(0, len(tags), batch_size), desc="翻译进度"):
        batch = tags[i:i+batch_size]
        
        for tag in batch:
            try:
                # 检查是否已翻译
                if tag in translator.translations or tag in translator.cache:
                    skipped_count += 1
                    continue
                
                # 翻译标签
                translation = translator.translate(tag)
                
                if translation != tag:
                    translated_count += 1
                else:
                    failed_count += 1
                    
            except Exception as e:
                print(f"\n翻译失败: {tag}, 错误: {str(e)}")
                failed_count += 1
        
        # 延迟，避免 API 限流
        if i + batch_size < len(tags):
            time.sleep(delay)
    
    print("\n" + "=" * 60)
    print("批量翻译完成！")
    print(f"  - 新翻译: {translated_count} 个")
    print(f"  - 已跳过: {skipped_count} 个（已有翻译）")
    print(f"  - 失败: {failed_count} 个")
    print(f"  - 总计: {len(tags)} 个")
    print("=" * 60)
    
    # 显示统计信息
    stats = translator.get_translation_stats()
    print("\n翻译器统计信息：")
    print(f"  - 预设翻译: {stats['preset_translations']} 个")
    print(f"  - 缓存翻译: {stats['cached_translations']} 个")
    print(f"  - 总翻译数: {stats['total_translations']} 个")
    print(f"  - 覆盖率: {stats['total_translations'] / len(tags) * 100:.2f}%")

def main():
    """主函数"""
    print("=" * 60)
    print("批量翻译标签工具")
    print("=" * 60)
    
    # 检查环境变量
    baidu_appid = os.getenv('BAIDU_APPID')
    baidu_secret = os.getenv('BAIDU_SECRET')
    
    if not baidu_appid or not baidu_secret:
        print("\n⚠️  警告：未配置百度翻译 API")
        print("请设置环境变量：")
        print("  - BAIDU_APPID")
        print("  - BAIDU_SECRET")
        print("\n或者创建 .env 文件并配置")
        print("\n将只使用预设翻译和默认规则")
        print("=" * 60)
        
        response = input("\n是否继续？(y/n): ")
        if response.lower() != 'y':
            print("已取消")
            return
    
    # 创建翻译器
    print("\n正在初始化翻译器...")
    translator = TagTranslator(
        use_online_api=True,
        baidu_appid=baidu_appid,
        baidu_secret=baidu_secret
    )
    
    # 加载所有标签
    print("正在加载标签列表...")
    tags = load_all_tags()
    print(f"共加载 {len(tags)} 个标签")
    
    # 显示当前统计
    stats = translator.get_translation_stats()
    print(f"\n当前翻译状态：")
    print(f"  - 预设翻译: {stats['preset_translations']} 个")
    print(f"  - 缓存翻译: {stats['cached_translations']} 个")
    print(f"  - 待翻译: {len(tags) - stats['total_translations']} 个")
    
    # 确认开始
    print("\n" + "=" * 60)
    response = input("开始批量翻译？(y/n): ")
    if response.lower() != 'y':
        print("已取消")
        return
    
    # 批量翻译
    # 注意：如果遇到 54003 错误（访问频率限制），可以：
    # 1. 降低 batch_size（如改为 5）
    # 2. 增加 delay（如改为 2.0 秒）
    batch_translate(
        translator=translator,
        tags=tags,
        batch_size=5,   # 每批 5 个标签（降低频率）
        delay=2.0       # 每批延迟 2 秒（增加间隔）
    )
    
    print("\n✅ 完成！翻译结果已保存到缓存文件")
    print(f"缓存文件: {translator.cache_file}")

if __name__ == "__main__":
    # 尝试加载 .env 文件
    try:
        from dotenv import load_dotenv
        load_dotenv()
    except ImportError:
        pass
    
    main()
