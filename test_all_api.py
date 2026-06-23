import requests
import json
import time

BASE_URL = "http://localhost:8080/api"
token = None
results = []

def test_api(name, method, url, body=None):
    global token
    print(f"\n{'='*50}")
    print(f"测试: {name}")
    print(f"方法: {method}  URL: {url}")
    
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    
    try:
        if method == "GET":
            resp = requests.get(url, headers=headers, timeout=30)
        elif method == "POST":
            resp = requests.post(url, headers=headers, json=body, timeout=30)
        elif method == "PUT":
            resp = requests.put(url, headers=headers, json=body, timeout=30)
        elif method == "DELETE":
            resp = requests.delete(url, headers=headers, timeout=30)
        else:
            print(f"不支持的方法: {method}")
            results.append({"name": name, "status": "失败", "code": "N/A"})
            return None
        
        status = resp.status_code
        color = "\033[92m" if 200 <= status < 300 else "\033[91m"
        reset = "\033[0m"
        
        print(f"状态码: {color}{status}{reset}")
        
        try:
            data = resp.json()
            resp_str = json.dumps(data, ensure_ascii=False, indent=2)
            if len(resp_str) > 500:
                resp_str = resp_str[:500] + "..."
            print(f"响应:\n{resp_str}")
        except:
            print(f"响应(前200字符): {resp.text[:200]}")
        
        success = 200 <= status < 300
        results.append({"name": name, "status": "成功" if success else "失败", "code": status})
        return resp.text
    except Exception as e:
        print(f"\033[91m错误: {e}\033[0m")
        results.append({"name": name, "status": "失败", "code": "ERR"})
        return None

# ==========================================
# 1. 认证接口测试
# ==========================================
print("\n" + "="*60)
print("       一、认证接口测试")
print("="*60)

# 1.1 管理员登录
login_result = test_api("管理员登录", "POST", f"{BASE_URL}/auth/login", 
    {"username": "admin", "password": "admin123"})
if login_result:
    try:
        data = json.loads(login_result)
        token = data.get("data", {}).get("token")
        print(f"获取到Token: {token}")
    except:
        pass

# 1.2 销售员登录
test_api("销售员登录(张三)", "POST", f"{BASE_URL}/auth/login", 
    {"username": "sales001", "password": "sales123"})

# 1.3 错误密码登录
test_api("错误密码登录", "POST", f"{BASE_URL}/auth/login", 
    {"username": "admin", "password": "wrongpassword"})

# 1.4 获取当前用户
test_api("获取当前用户信息", "GET", f"{BASE_URL}/auth/me")

# ==========================================
# 2. 用户管理接口测试
# ==========================================
print("\n" + "="*60)
print("       二、用户管理接口测试")
print("="*60)

test_api("获取用户列表", "GET", f"{BASE_URL}/users")
test_api("获取单个用户(ID=1)", "GET", f"{BASE_URL}/users/1")

# 创建新用户并获取ID
user_result = test_api("创建新用户", "POST", f"{BASE_URL}/users", 
    {"username": "testuser", "password": "test123", "role": "销售", "name": "测试用户", "phone": "13900139000", "email": "test@company.com"})

# 获取创建的用户ID
new_user_id = None
if user_result:
    try:
        data = json.loads(user_result)
        new_user_id = data.get("data", {}).get("id")
        print(f"创建的用户ID: {new_user_id}")
    except:
        pass

if new_user_id:
    test_api("更新用户信息", "PUT", f"{BASE_URL}/users/{new_user_id}", 
        {"name": "测试用户-已更新", "phone": "13900139001"})

# ==========================================
# 3. 商品管理接口测试
# ==========================================
print("\n" + "="*60)
print("       三、商品管理接口测试")
print("="*60)

test_api("获取商品列表(分页)", "GET", f"{BASE_URL}/products?page=1&size=5")
test_api("获取单个商品(ID=1)", "GET", f"{BASE_URL}/products/1")
test_api("搜索商品(关键词:iPhone)", "GET", f"{BASE_URL}/products/search?keyword=iPhone")
test_api("获取热销商品TOP5", "GET", f"{BASE_URL}/products/hot?limit=5")
test_api("获取低库存商品", "GET", f"{BASE_URL}/products/low-stock?threshold=20")
test_api("价格区间查询(5000-10000)", "GET", f"{BASE_URL}/products/price-range?minPrice=5000&maxPrice=10000")
test_api("创建新商品", "POST", f"{BASE_URL}/products", 
    {"name": "测试商品", "description": "这是一个测试商品", "price": 99.99, "stockQuantity": 100})
test_api("更新商品信息", "PUT", f"{BASE_URL}/products/13", 
    {"name": "测试商品-已更新", "price": 199.99})

# ==========================================
# 4. 订单管理接口测试
# ==========================================
print("\n" + "="*60)
print("       四、订单管理接口测试")
print("="*60)

test_api("获取订单列表(分页)", "GET", f"{BASE_URL}/orders?page=1&size=5")
test_api("获取单个订单(ID=1)", "GET", f"{BASE_URL}/orders/1")
test_api("按状态筛选(COMPLETED)", "GET", f"{BASE_URL}/orders?status=COMPLETED")
test_api("按销售人员筛选(ID=2)", "GET", f"{BASE_URL}/orders?salespersonId=2")
test_api("获取订单状态统计", "GET", f"{BASE_URL}/orders/status-count")

# 创建新订单并获取ID
order_result = test_api("创建新订单", "POST", f"{BASE_URL}/orders", 
    {"customerName": "测试客户有限公司", "salespersonId": 2, 
     "items": [{"productId": 1, "quantity": 2, "unitPrice": 8999.00}, 
               {"productId": 3, "quantity": 5, "unitPrice": 1899.00}]})

# 获取创建的订单ID
new_order_id = None
if order_result:
    try:
        data = json.loads(order_result)
        new_order_id = data.get("data", {}).get("id")
        print(f"创建的订单ID: {new_order_id}")
    except:
        pass

if new_order_id:
    test_api("更新订单状态为进行中", "PUT", f"{BASE_URL}/orders/{new_order_id}/status", 
        {"status": "进行中"})
    test_api("完成订单", "POST", f"{BASE_URL}/orders/{new_order_id}/complete")

# 创建待取消订单并获取ID
cancel_order_result = test_api("创建待取消订单", "POST", f"{BASE_URL}/orders", 
    {"customerName": "取消测试客户", "salespersonId": 3, 
     "items": [{"productId": 5, "quantity": 1, "unitPrice": 3299.00}]})

# 获取取消订单ID
cancel_order_id = None
if cancel_order_result:
    try:
        data = json.loads(cancel_order_result)
        cancel_order_id = data.get("data", {}).get("id")
        print(f"取消订单ID: {cancel_order_id}")
    except:
        pass

if cancel_order_id:
    test_api("取消订单", "POST", f"{BASE_URL}/orders/{cancel_order_id}/cancel", 
        {"reason": "客户要求取消"})

# ==========================================
# 5. 总览/统计接口测试
# ==========================================
print("\n" + "="*60)
print("       五、总览/统计接口测试")
print("="*60)

test_api("获取总览统计", "GET", f"{BASE_URL}/overview/statistics")
test_api("获取销售排行榜", "GET", f"{BASE_URL}/overview/sales-ranking")
test_api("获取热销商品排行", "GET", f"{BASE_URL}/overview/hot-products?limit=5")
test_api("获取订单趋势(近30天)", "GET", f"{BASE_URL}/overview/order-trend?days=30")

# ==========================================
# 6. AI智能问答接口测试
# ==========================================
print("\n" + "="*60)
print("       六、AI智能问答接口测试")
print("="*60)

test_api("获取支持的意图列表", "GET", f"{BASE_URL}/ai/intents")
test_api("AI聊天-销售查询", "POST", f"{BASE_URL}/ai/chat", 
    {"question": "本月销售情况如何？", "conversationId": "test-conv-001"})
test_api("AI聊天-商品查询", "POST", f"{BASE_URL}/ai/chat", 
    {"question": "哪些商品卖得最好？", "conversationId": "test-conv-002"})
test_api("AI聊天-订单查询", "POST", f"{BASE_URL}/ai/chat", 
    {"question": "最近的订单情况怎么样？", "conversationId": "test-conv-003"})
test_api("AI聊天-员工查询", "POST", f"{BASE_URL}/ai/chat", 
    {"question": "销售团队有哪些人？", "conversationId": "test-conv-004"})
test_api("AI聊天-统计分析", "POST", f"{BASE_URL}/ai/chat", 
    {"question": "帮我统计一下总销售额", "conversationId": "test-conv-005"})

test_api("获取对话历史列表", "GET", f"{BASE_URL}/ai/conversations")
test_api("获取对话详情(test-conv-001)", "GET", f"{BASE_URL}/ai/conversation/test-conv-001")

# SSE流式聊天测试
print(f"\n{'='*50}")
print("测试: AI流式聊天(SSE)")
print(f"方法: POST  URL: {BASE_URL}/ai/chat/stream")
try:
    headers = {"Content-Type": "application/json", "Accept": "text/event-stream"}
    body = {"question": "分析一下热销商品的趋势", "conversationId": "test-stream-001"}
    resp = requests.post(f"{BASE_URL}/ai/chat/stream", headers=headers, json=body, stream=True, timeout=30)
    print(f"状态码: {resp.status_code}")
    content = resp.text[:500]
    print(f"SSE响应(前500字符):\n{content}")
    results.append({"name": "AI流式聊天(SSE)", "status": "成功", "code": resp.status_code})
except Exception as e:
    print(f"\033[91m错误: {e}\033[0m")
    results.append({"name": "AI流式聊天(SSE)", "status": "失败", "code": "ERR"})

test_api("数据分析接口", "POST", f"{BASE_URL}/ai/analyze", 
    {"question": "分析销售数据", "data": [{"product": "iPhone", "sales": 100}, {"product": "MacBook", "sales": 50}]})

test_api("清除对话历史(test-conv-001)", "DELETE", f"{BASE_URL}/ai/conversation/test-conv-001")

# ==========================================
# 7. 文档管理接口测试
# ==========================================
print("\n" + "="*60)
print("       七、文档管理接口测试")
print("="*60)

test_api("获取Markdown文档列表", "GET", f"{BASE_URL}/markdown-documents")

# ==========================================
# 8. PDF导出接口测试
# ==========================================
print("\n" + "="*60)
print("       八、PDF导出接口测试")
print("="*60)

print(f"\n{'='*50}")
print("测试: 导出订单PDF")
try:
    headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    resp = requests.get(f"{BASE_URL}/orders/1/export-pdf", headers=headers, timeout=30)
    print(f"状态码: {resp.status_code}")
    print(f"文件大小: {len(resp.content)} bytes")
    ct = resp.headers.get("Content-Type", "")
    print(f"Content-Type: {ct}")
    results.append({"name": "导出订单PDF", "status": "成功", "code": resp.status_code})
except Exception as e:
    print(f"\033[91m错误: {e}\033[0m")
    results.append({"name": "导出订单PDF", "status": "失败", "code": "ERR"})

# ==========================================
# 9. 删除测试
# ==========================================
print("\n" + "="*60)
print("       九、删除测试")
print("="*60)

test_api("删除测试商品", "DELETE", f"{BASE_URL}/products/13")
if new_user_id:
    test_api("删除测试用户", "DELETE", f"{BASE_URL}/users/{new_user_id}")

# ==========================================
# 测试结果汇总
# ==========================================
print("\n\n" + "="*60)
print("                    测试结果汇总")
print("="*60)

print("\n")
for r in results:
    status_icon = "[PASS]" if r["status"] == "成功" else "[FAIL]"
    color = "\033[92m" if r["status"] == "成功" else "\033[91m"
    reset = "\033[0m"
    print(f"  {color}{r['name']:<40} {status_icon} {r['code']}{reset}")

success_count = sum(1 for r in results if r["status"] == "成功")
fail_count = sum(1 for r in results if r["status"] == "失败")
total_count = len(results)

print(f"\n  总计: {total_count}  |  成功: {success_count}  |  失败: {fail_count}")
print("="*60)
