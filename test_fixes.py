#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试修复的三个问题：
1. JWT认证 - /api/auth/me端点
2. SSE流式聊天
3. 用户创建请求体验证
"""

import requests
import json
import time
import sys

BASE_URL = "http://localhost:8080"

def test_jwt_auth():
    """测试JWT认证"""
    print("=" * 60)
    print("测试1: JWT认证 - /api/auth/me端点")
    print("=" * 60)
    
    # 1. 先登录获取token
    login_url = f"{BASE_URL}/api/auth/login"
    login_data = {
        "username": "admin",
        "password": "admin123"
    }
    
    try:
        print("1. 尝试登录...")
        response = requests.post(login_url, json=login_data, timeout=10)
        print(f"   状态码: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("code") == 200:
                token = data["data"]["token"]
                print(f"   ✅ 登录成功，获取到token: {token[:20]}...")
                
                # 2. 使用token访问/api/auth/me
                me_url = f"{BASE_URL}/api/auth/me"
                headers = {"Authorization": f"Bearer {token}"}
                
                print("2. 使用token访问/api/auth/me...")
                me_response = requests.get(me_url, headers=headers, timeout=10)
                print(f"   状态码: {me_response.status_code}")
                
                if me_response.status_code == 200:
                    me_data = me_response.json()
                    if me_data.get("code") == 200:
                        print(f"   ✅ 获取用户信息成功: {me_data['data']}")
                        return True
                    else:
                        print(f"   ❌ 获取用户信息失败: {me_data.get('message')}")
                else:
                    print(f"   ❌ 请求失败: {me_response.text}")
            else:
                print(f"   ❌ 登录失败: {data.get('message')}")
        else:
            print(f"   ❌ 登录请求失败: {response.text}")
            
    except Exception as e:
        print(f"   ❌ 测试异常: {e}")
    
    return False

def test_sse_stream():
    """测试SSE流式聊天"""
    print("\n" + "=" * 60)
    print("测试2: SSE流式聊天")
    print("=" * 60)
    
    stream_url = f"{BASE_URL}/api/ai/chat/stream"
    data = {
        "question": "你好，请介绍一下你自己",
        "conversationId": "test-stream-" + str(int(time.time()))
    }
    
    try:
        print("1. 发送流式聊天请求...")
        response = requests.post(stream_url, json=data, stream=True, timeout=120)
        print(f"   状态码: {response.status_code}")
        
        if response.status_code == 200:
            print("2. 接收流式响应...")
            token_count = 0
            full_response = ""
            
            for line in response.iter_lines():
                if line:
                    line_str = line.decode('utf-8')
                    if line_str.startswith('data:'):
                        data_str = line_str[5:].strip()
                        if data_str:
                            try:
                                event_data = json.loads(data_str)
                                if 'token' in event_data:
                                    token_count += 1
                                    full_response += event_data['token']
                                    if token_count <= 5:  # 只显示前5个token
                                        print(f"   Token {token_count}: {event_data['token']}")
                                elif 'fullResponse' in event_data:
                                    print(f"   ✅ 流式响应完成，共接收 {token_count} 个token")
                                    print(f"   完整响应长度: {len(event_data['fullResponse'])} 字符")
                                    return True
                            except json.JSONDecodeError:
                                pass
            
            if token_count > 0:
                print(f"   ✅ 流式响应完成，共接收 {token_count} 个token")
                return True
            else:
                print("   ❌ 未接收到任何token")
        else:
            print(f"   ❌ 请求失败: {response.text}")
            
    except requests.exceptions.Timeout:
        print("   ❌ 请求超时")
    except Exception as e:
        print(f"   ❌ 测试异常: {e}")
    
    return False

def test_user_creation_validation():
    """测试用户创建请求体验证"""
    print("\n" + "=" * 60)
    print("测试3: 用户创建请求体验证")
    print("=" * 60)
    
    create_url = f"{BASE_URL}/api/users"
    
    # 测试1: 无效数据 - 缺少必填字段
    print("1. 测试缺少必填字段...")
    invalid_data_1 = {
        "username": "testuser"
        # 缺少password, name, role
    }
    
    try:
        response = requests.post(create_url, json=invalid_data_1, timeout=10)
        print(f"   状态码: {response.status_code}")
        
        if response.status_code == 400:
            data = response.json()
            if "验证失败" in data.get("message", ""):
                print(f"   ✅ 验证失败正确返回: {data.get('message')}")
                print(f"   错误详情: {data.get('data')}")
            else:
                print(f"   ⚠️ 返回400但不是验证错误: {data.get('message')}")
        else:
            print(f"   ❌ 期望400但得到: {response.status_code}")
            
    except Exception as e:
        print(f"   ❌ 测试异常: {e}")
    
    # 测试2: 无效数据 - 用户名太短
    print("\n2. 测试用户名太短...")
    invalid_data_2 = {
        "username": "ab",  # 少于3个字符
        "password": "password123",
        "name": "测试用户",
        "role": "SALESPERSON"
    }
    
    try:
        response = requests.post(create_url, json=invalid_data_2, timeout=10)
        print(f"   状态码: {response.status_code}")
        
        if response.status_code == 400:
            data = response.json()
            if "验证失败" in data.get("message", ""):
                print(f"   ✅ 验证失败正确返回: {data.get('message')}")
            else:
                print(f"   ⚠️ 返回400但不是验证错误: {data.get('message')}")
        else:
            print(f"   ❌ 期望400但得到: {response.status_code}")
            
    except Exception as e:
        print(f"   ❌ 测试异常: {e}")
    
    # 测试3: 有效数据
    print("\n3. 测试有效数据...")
    valid_data = {
        "username": "testuser" + str(int(time.time())),
        "password": "password123",
        "name": "测试用户",
        "role": "SALESPERSON"
    }
    
    try:
        response = requests.post(create_url, json=valid_data, timeout=10)
        print(f"   状态码: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("code") == 200:
                print(f"   ✅ 用户创建成功: {data.get('data', {}).get('username')}")
                return True
            else:
                print(f"   ❌ 创建失败: {data.get('message')}")
        else:
            print(f"   ❌ 请求失败: {response.text}")
            
    except Exception as e:
        print(f"   ❌ 测试异常: {e}")
    
    return False

def main():
    """主测试函数"""
    print("开始测试修复的三个问题...")
    print(f"目标服务器: {BASE_URL}")
    
    # 等待服务器启动
    print("\n等待服务器启动...")
    time.sleep(2)
    
    # 测试服务器是否可达
    try:
        response = requests.get(f"{BASE_URL}/api/overview/statistics", timeout=5)
        if response.status_code == 200:
            print("✅ 服务器已启动")
        else:
            print(f"⚠️ 服务器响应异常: {response.status_code}")
    except:
        print("❌ 服务器未启动，请先启动后端服务")
        return
    
    # 执行测试
    results = []
    results.append(("JWT认证", test_jwt_auth()))
    results.append(("SSE流式聊天", test_sse_stream()))
    results.append(("用户创建验证", test_user_creation_validation()))
    
    # 输出测试结果
    print("\n" + "=" * 60)
    print("测试结果汇总")
    print("=" * 60)
    
    all_passed = True
    for name, result in results:
        status = "✅ 通过" if result else "❌ 失败"
        print(f"{name}: {status}")
        if not result:
            all_passed = False
    
    print("\n" + "=" * 60)
    if all_passed:
        print("🎉 所有测试通过！")
    else:
        print("⚠️ 部分测试失败，请检查日志")
    print("=" * 60)

if __name__ == "__main__":
    main()
