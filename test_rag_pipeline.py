#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
RAG（检索增强生成）完整流程测试
测试：文档上传 → 分块 → 向量化 → 向量存储 → 语义检索 → 大模型回答
"""

import requests
import json
import time
import sys

BASE_URL = "http://localhost:8080/api"

def print_header(title):
    print("\n" + "=" * 60)
    print(f"       {title}")
    print("=" * 60)

def print_step(step, description):
    print(f"\n  步骤 {step}: {description}")
    print("  " + "-" * 40)

def test_api(name, method, url, data=None, timeout=60):
    """测试API接口"""
    print(f"\n  测试: {name}")
    print(f"  方法: {method}  URL: {url}")
    
    try:
        if method == "GET":
            response = requests.get(url, timeout=timeout)
        elif method == "POST":
            response = requests.post(url, json=data, timeout=timeout)
        elif method == "DELETE":
            response = requests.delete(url, timeout=timeout)
        else:
            print(f"  错误: 不支持的方法 {method}")
            return None
        
        print(f"  状态码: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print(f"  成功: {result.get('message', '操作成功')}")
            return result
        else:
            print(f"  失败: {response.text[:200]}")
            return None
            
    except requests.exceptions.Timeout:
        print(f"  错误: 请求超时")
        return None
    except Exception as e:
        print(f"  错误: {e}")
        return None

def main():
    print_header("RAG 完整流程测试")
    print("\n测试项目：")
    print("  1. 文档上传（Markdown格式）")
    print("  2. 文档分块（智能分块）")
    print("  3. 向量嵌入生成（DashScope text-embedding-v1）")
    print("  4. 向量存储（MySQL）")
    print("  5. 语义检索（余弦相似度）")
    print("  6. 大模型回答生成（mimo-v2.5-pro）")
    
    # 测试文档内容
    test_documents = [
        {
            "title": "销售管理系统使用指南",
            "content": """# 销售管理系统使用指南

## 一、系统概述
本系统是一个企业级销售管理系统，包含以下主要模块：
1. 总览模块：提供销售数据可视化，包括销售额统计、订单趋势、热销商品排行等
2. 商品管理模块：支持商品信息维护、图片上传、库存管理
3. 订单管理模块：处理订单全生命周期，从创建到完成
4. 员工管理模块：负责用户权限管理、绩效统计
5. 智能问答模块：提供AI驱动的数据分析和知识检索

## 二、商品管理
### 2.1 添加商品
在商品管理页面，点击"添加商品"按钮，填写以下信息：
- 商品名称：必填，商品的显示名称
- 价格：必填，商品的销售价格
- 库存数量：必填，当前库存数量
- 商品描述：选填，商品的详细描述
- 商品图片：选填，支持上传商品图片到阿里云OSS

### 2.2 库存管理
系统会自动监控库存水平：
- 当库存低于10件时，系统会显示低库存预警
- 管理员可以手动调整库存数量
- 支持批量导入库存数据

## 三、订单管理
### 3.1 创建订单
创建订单时需要填写：
- 客户名称：必填
- 负责销售员：必填，从员工列表中选择
- 订单项：必填，至少选择一个商品并指定数量

### 3.2 订单状态流转
订单状态包括：
- 待处理（PENDING）：新创建的订单
- 进行中（IN_PROGRESS）：已确认正在处理
- 已完成（COMPLETED）：订单完成
- 已取消（CANCELLED）：订单取消

## 四、智能问答
系统提供三种问答模式：
1. RAG模式：基于知识库文档的回答
2. SQL模式：基于数据库查询的数据分析
3. 自动模式：根据问题自动选择合适的回答方式""",
            "metadata": '{"category": "documentation", "type": "guide", "version": "1.0"}'
        },
        {
            "title": "常见问题解答FAQ",
            "content": """# 常见问题解答

## 账号相关
### Q: 如何重置密码？
A: 管理员可以在员工管理页面找到对应用户，点击"重置密码"按钮。重置后密码将变为默认密码"123456"，建议用户登录后立即修改。

### Q: 忘记用户名怎么办？
A: 请联系管理员查询您的用户名。管理员可以在员工管理页面查看所有用户信息。

## 订单相关
### Q: 如何导出订单PDF？
A: 在订单详情页面，点击右上角的"导出PDF"按钮即可生成并下载订单PDF文件。PDF包含订单基本信息、商品明细和合计金额。

### Q: 订单状态可以修改吗？
A: 订单状态按照流程自动流转：待处理→进行中→已完成。只有待处理状态的订单可以取消。如需特殊处理，请联系管理员。

## 商品相关
### Q: 库存不足怎么办？
A: 系统会自动预警低库存商品（低于10件）。管理员可以：
1. 在商品详情页面手动调整库存
2. 联系采购部门补货
3. 暂时下架商品

### Q: 如何上传商品图片？
A: 在商品编辑页面，点击图片上传区域，选择本地图片文件。系统会自动上传到阿里云OSS并生成访问链接。

## 数据相关
### Q: 如何查看销售统计？
A: 在总览模块可以查看：
- 今日/本月/本年销售额
- 订单数量统计
- 热销商品排行榜
- 销售趋势图表

### Q: 数据可以导出吗？
A: 目前系统支持订单PDF导出。如需批量导出数据，请联系管理员使用后台导出功能。""",
            "metadata": '{"category": "faq", "type": "help"}'
        },
        {
            "title": "销售技巧与策略",
            "content": """# 销售技巧与策略

## 一、客户沟通技巧
### 1.1 建立信任
成功的销售始于建立信任关系：
- 保持专业形象，准时赴约
- 认真倾听客户需求，不急于推销
- 提供真实可靠的产品信息
- 遵守承诺，及时跟进

### 1.2 需求挖掘
通过提问了解客户真实需求：
- 开放式问题："您目前面临什么挑战？"
- 探索式问题："这个问题对您的业务有什么影响？"
- 确认式问题："如果我没理解错，您最关心的是...对吗？"

## 二、产品推荐策略
### 2.1 FAB法则
- Feature（特点）：产品有什么特点
- Advantage（优势）：相比竞品有什么优势
- Benefit（利益）：能为客户带来什么价值

### 2.2 高利润商品推荐
优先推荐以下类型商品：
1. 高单价商品：提升单笔订单金额
2. 高毛利商品：增加公司利润
3. 组合套餐：提高客单价

## 三、订单促成技巧
### 3.1 识别购买信号
当客户出现以下行为时，表示有购买意向：
- 询问价格和付款方式
- 讨论交货时间和售后保障
- 频繁点头或做笔记

### 3.2 促成交易方法
- 限时优惠法："这个价格只到本月底"
- 假设成交法："您希望什么时候送货？"
- 选择成交法："您要A款还是B款？"

## 四、客户维护
### 4.1 售后跟进
订单完成后及时跟进：
- 3天内回访确认收货
- 1周内了解使用情况
- 1个月后询问是否需要补货

### 4.2 客户关系管理
- 定期发送节日问候
- 分享有价值的行业资讯
- 记录客户偏好和特殊需求""",
            "metadata": '{"category": "knowledge", "type": "sales_tips"}'
        }
    ]
    
    # 测试问答问题
    test_questions = [
        "如何添加商品？",
        "订单状态有哪些？",
        "如何重置密码？",
        "推荐商品有什么技巧？",
        "库存不足应该怎么办？"
    ]
    
    # 存储上传的文档ID
    uploaded_doc_ids = []
    
    # 测试1: 文档上传和处理
    print_header("一、文档上传和处理")
    print_step(1, "上传测试文档并进行分块、向量化、存储")
    
    for i, doc in enumerate(test_documents, 1):
        print(f"\n  上传文档 {i}/{len(test_documents)}: {doc['title']}")
        
        result = test_api(
            f"上传文档: {doc['title']}",
            "POST",
            f"{BASE_URL}/rag/documents",
            data=doc,
            timeout=120  # 向量化可能需要较长时间
        )
        
        if result and result.get('code') == 200:
            data = result.get('data', {})
            source_doc_id = data.get('sourceDocId')
            uploaded_doc_ids.append(source_doc_id)
            
            print(f"  文档ID: {source_doc_id}")
            print(f"  分块数量: {data.get('totalChunks', 0)}")
            print(f"  向量维度: {data.get('embeddingDimension', 0)}")
            print(f"  存储文档数: {data.get('storedDocuments', 0)}")
            
            # 显示分块预览
            chunks = data.get('chunks', [])
            if chunks:
                print(f"  分块预览:")
                for j, chunk in enumerate(chunks[:2], 1):
                    print(f"    [{j}] {chunk[:80]}...")
        else:
            print(f"  文档上传失败!")
    
    # 测试2: 查看文档块
    if uploaded_doc_ids:
        print_header("二、查看文档块信息")
        print_step(2, "查看已上传文档的分块信息")
        
        source_doc_id = uploaded_doc_ids[0]
        test_api(
            f"获取文档块 (ID={source_doc_id})",
            "GET",
            f"{BASE_URL}/rag/documents/{source_doc_id}/chunks"
        )
    
    # 测试3: 语义问答
    print_header("三、语义问答测试")
    print_step(3, "使用语义检索回答用户问题")
    
    for i, question in enumerate(test_questions, 1):
        print(f"\n  问题 {i}/{len(test_questions)}: {question}")
        
        result = test_api(
            f"语义问答: {question[:20]}...",
            "POST",
            f"{BASE_URL}/rag/query",
            data={"question": question, "topK": 3},
            timeout=120  # 大模型生成可能需要较长时间
        )
        
        if result and result.get('code') == 200:
            data = result.get('data', {})
            
            print(f"\n  回答:")
            answer = data.get('answer', '')
            # 格式化输出回答
            for line in answer.split('\n'):
                if line.strip():
                    print(f"    {line}")
            
            # 显示检索到的文档
            retrieved_docs = data.get('retrievedDocuments', [])
            if retrieved_docs:
                print(f"\n  检索到的相关文档:")
                for j, doc in enumerate(retrieved_docs[:3], 1):
                    print(f"    [{j}] {doc.get('title', '')} (相似度: {doc.get('similarity', 0)})")
                    print(f"        {doc.get('content', '')[:100]}...")
        else:
            print(f"  问答失败!")
        
        # 添加延迟避免API限制
        if i < len(test_questions):
            time.sleep(2)
    
    # 测试4: 清理测试数据
    print_header("四、清理测试数据")
    print_step(4, "删除测试文档")
    
    for doc_id in uploaded_doc_ids:
        test_api(
            f"删除文档 (ID={doc_id})",
            "DELETE",
            f"{BASE_URL}/rag/documents/{doc_id}"
        )
    
    # 测试总结
    print_header("RAG 流程测试完成")
    print("\n测试总结：")
    print(f"  1. 文档上传: 成功上传 {len(uploaded_doc_ids)} 个文档")
    print(f"  2. 文档分块: 智能分块，按段落和句子分割")
    print(f"  3. 向量嵌入: 使用 DashScope text-embedding-v1 模型")
    print(f"  4. 向量存储: 存储到 MySQL vector_document 表")
    print(f"  5. 语义检索: 基于余弦相似度的向量检索")
    print(f"  6. 大模型回答: 使用 mimo-v2.5-pro 生成回答")
    print("\nRAG完整流程测试成功！")

if __name__ == "__main__":
    main()