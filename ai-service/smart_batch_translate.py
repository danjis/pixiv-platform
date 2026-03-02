"""
智能批量翻译工具

特点：
1. 自动检测 QPS 限制
2. 动态调整请求速度
3. 支持断点续传
4. 错误重试机制
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

def smart_batch_translate(translator, tags):
    """
    智能批量翻译
    
    自动适应 API 限制，动态调整速度
    """
    print(f"开始智能批量翻译 {len(tags)} 个标签...")
    print("=" * 60)
    
    translated_count = 0
    skipped_count = 0
    failed_count = 0
    
    # 初始延迟（秒）
    delay = 1.0
    consecutive_failures = 0
    
    # 使用 tqdm 显示进度条
    with tqdm(total=len(tags), desc="翻译进度") as pbar:
        for i, tag in enumerate(tags):
            try:
                # 检查是否已翻译
                if tag in translator.translations or tag in translator.cache:
                    skipped_count += 1
                    pbar.update(1)
                    continue
                
                # 翻译标签
                translation = translator.translate(tag)
                
                if translation != tag:
                    translated_count += 1
                    consecutive_failures = 0  # 重置失败计数
                    
                    # 成功后可以稍微加快速度
                    if delay > 1.0:
                        delay = max(1.0, delay * 0.95)
                else:
                    failed_count += 1
                    consecutive_failures += 1
                    
                    # 连续失败，增加延迟
                    if consecutive_failures >= 3:
                        delay = min(5.0, delay * 1.5)
                        print(f"\n⚠️  检测到连续失败，增加延迟到 {delay:.1f} 秒")
                        consecutive_failures = 0
                
                pbar.update(1)
                
                # 每次请求后延迟
                time.sleep(delay)
                
                # 每 100 个标签保存一次（断点续传）
                if (i + 1) % 100 == 0:
                    translator.save_cache()
                    pbar.set_postfix({
                        '已翻译': translated_count,
                        '延迟': f'{delay:.1f}s'
                    })
                    
            except KeyboardInterrupt:
                print("\n\n用户中断，正在保存进度...")
                translator.save_cache()
                raise
            except Exception as e:
                print(f"\n翻译失败: {tag}, 错误: {str(e)}")
                failed_count += 1
                pbar.update(1)
    
    # 最后保存一次
    translator.save_cache()
    
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
    print("智能批量翻译工具")
    print("=" * 60)
    
    # 检查环境变量
    baidu_appid = os.getenv('BAIDU_APPID')
    baidu_secret = os.getenv('BAIDU_SECRET')
    
    if not baidu_appid or not baidu_secret:
        print("\n⚠️  警告：未配置百度翻译 API")
        print("请设置环境变量或创建 .env 文件")
        print("\n将只使用预设翻译和默认规则")
        print("=" * 60)
        
        response = input("\n是否继续？(y/n): ")
        if response.lower() != 'y':
            print("已取消")
            return
    else:
        print("\n✅ 检测到百度翻译 API 配置")
        print(f"   APP ID: {baidu_appid[:8]}...")
    
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
    
    if stats['total_translations'] >= len(tags) * 0.95:
        print("\n✅ 翻译已基本完成（覆盖率 > 95%）")
        response = input("是否继续补充剩余标签？(y/n): ")
        if response.lower() != 'y':
            print("已取消")
            return
    
    # 确认开始
    print("\n" + "=" * 60)
    print("智能翻译特点：")
    print("  - 自动适应 API 限制")
    print("  - 动态调整请求速度")
    print("  - 支持断点续传（Ctrl+C 可随时中断）")
    print("  - 每 100 个标签自动保存")
    print("=" * 60)
    
    response = input("\n开始智能批量翻译？(y/n): ")
    if response.lower() != 'y':
        print("已取消")
        return
    
    # 智能批量翻译
    try:
        smart_batch_translate(translator=translator, tags=tags)
    except KeyboardInterrupt:
        print("\n\n已中断，进度已保存")
        print("下次运行将从中断处继续")
    
    print("\n✅ 完成！翻译结果已保存到缓存文件")
    print(f"缓存文件: {translator.cache_file}")
    
    # 显示建议
    final_stats = translator.get_translation_stats()
    coverage = final_stats['total_translations'] / len(tags) * 100
    
    if coverage < 90:
        print("\n💡 提示：")
        print(f"   当前覆盖率 {coverage:.1f}%")
        print("   如果遇到 API 限制，可以：")
        print("   1. 等待一段时间后重新运行（免费额度每天重置）")
        print("   2. 升级到百度翻译高级版（QPS 更高）")
        print("   3. 手动编辑 tag_cache.json 补充重要标签")

if __name__ == "__main__":
    # 尝试加载 .env 文件
    try:
        from dotenv import load_dotenv
        load_dotenv()
    except ImportError:
        pass
    
    main()
