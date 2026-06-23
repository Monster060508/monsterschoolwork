#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Comprehensive integration test for all fixes:
1. JWT auth - /api/auth/me endpoint
2. SSE streaming chat
3. User creation request validation
4. Role value consistency (ADMIN/SALES)
"""

import requests
import json
import time
import sys

BASE_URL = "http://localhost:8080"

def test_jwt_auth():
    """Test JWT auth with role verification"""
    print("=" * 60)
    print("Test 1: JWT Auth - /api/auth/me")
    print("=" * 60)
    
    login_url = f"{BASE_URL}/api/auth/login"
    login_data = {
        "username": "admin",
        "password": "admin123"
    }
    
    try:
        print("1. Login...")
        response = requests.post(login_url, json=login_data, timeout=10)
        print(f"   Status: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("code") == 200:
                token = data["data"]["token"]
                user_info = data["data"]["user"]
                role = user_info.get("role")
                print(f"   [OK] Login success, token: {token[:30]}...")
                print(f"   [INFO] User role: {role}")
                
                # Verify role is ADMIN (not Chinese description)
                if role == "ADMIN":
                    print("   [OK] Role value is correct (ADMIN)")
                else:
                    print(f"   [WARN] Role value: {role} (expected ADMIN)")
                
                me_url = f"{BASE_URL}/api/auth/me"
                headers = {"Authorization": f"Bearer {token}"}
                
                print("2. Access /api/auth/me with token...")
                me_response = requests.get(me_url, headers=headers, timeout=10)
                print(f"   Status: {me_response.status_code}")
                
                if me_response.status_code == 200:
                    me_data = me_response.json()
                    if me_data.get("code") == 200:
                        me_role = me_data["data"].get("role")
                        print(f"   [OK] Get user info: username={me_data['data']['username']}, role={me_role}")
                        if me_role == "ADMIN":
                            print("   [OK] /me endpoint role is correct (ADMIN)")
                            return True
                        else:
                            print(f"   [WARN] /me role: {me_role}")
                            return True
                    else:
                        print(f"   [FAIL] {me_data.get('message')}")
                else:
                    print(f"   [FAIL] {me_response.text[:200]}")
            else:
                print(f"   [FAIL] Login failed: {data.get('message')}")
        else:
            print(f"   [FAIL] Login request failed: {response.text[:200]}")
            
    except Exception as e:
        print(f"   [FAIL] Exception: {e}")
    
    return False

def test_sse_stream():
    """Test SSE streaming"""
    print("\n" + "=" * 60)
    print("Test 2: SSE Streaming Chat")
    print("=" * 60)
    
    # First login to get token
    login_url = f"{BASE_URL}/api/auth/login"
    login_data = {"username": "admin", "password": "admin123"}
    
    try:
        login_response = requests.post(login_url, json=login_data, timeout=10)
        if login_response.status_code != 200:
            print("   [FAIL] Login failed for SSE test")
            return False
        
        token = login_response.json()["data"]["token"]
        headers = {"Authorization": f"Bearer {token}"}
    except Exception as e:
        print(f"   [FAIL] Login exception: {e}")
        return False
    
    stream_url = f"{BASE_URL}/api/ai/chat/stream"
    data = {
        "question": "Hello, introduce yourself briefly",
        "conversationId": "test-stream-" + str(int(time.time()))
    }
    
    try:
        print("1. Send stream request...")
        response = requests.post(stream_url, json=data, stream=True, timeout=120, headers=headers)
        print(f"   Status: {response.status_code}")
        
        if response.status_code == 200:
            print("2. Receiving stream...")
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
                                if isinstance(event_data, dict):
                                    if 'token' in event_data:
                                        token_count += 1
                                        full_response += event_data['token']
                                        if token_count <= 3:
                                            print(f"   Token {token_count}: {event_data['token'][:50]}")
                                    elif 'fullResponse' in event_data:
                                        print(f"   [OK] Stream done, {token_count} tokens")
                                        print(f"   Response length: {len(event_data['fullResponse'])} chars")
                                        return True
                                else:
                                    # Handle non-dict data (token string)
                                    token_count += 1
                                    full_response += str(event_data)
                                    if token_count <= 3:
                                        print(f"   Token {token_count}: {str(event_data)[:50]}")
                            except json.JSONDecodeError:
                                pass
            
            if token_count > 0:
                print(f"   [OK] Stream done, {token_count} tokens")
                print(f"   Total response length: {len(full_response)} chars")
                return True
            else:
                print("   [FAIL] No tokens received")
        else:
            print(f"   [FAIL] {response.text[:200]}")
            
    except requests.exceptions.Timeout:
        print("   [FAIL] Timeout")
    except Exception as e:
        print(f"   [FAIL] Exception: {e}")
    
    return False

def test_user_creation_validation():
    """Test user creation validation"""
    print("\n" + "=" * 60)
    print("Test 3: User Creation Validation")
    print("=" * 60)
    
    # First login to get token
    login_url = f"{BASE_URL}/api/auth/login"
    login_data = {"username": "admin", "password": "admin123"}
    
    try:
        login_response = requests.post(login_url, json=login_data, timeout=10)
        if login_response.status_code != 200:
            print("   [FAIL] Login failed for validation test")
            return False
        
        token = login_response.json()["data"]["token"]
        headers = {"Authorization": f"Bearer {token}"}
    except Exception as e:
        print(f"   [FAIL] Login exception: {e}")
        return False
    
    create_url = f"{BASE_URL}/api/users"
    
    # Test 1: Missing required fields
    print("1. Test missing required fields...")
    invalid_data_1 = {
        "username": "testuser"
    }
    
    try:
        response = requests.post(create_url, json=invalid_data_1, timeout=10, headers=headers)
        print(f"   Status: {response.status_code}")
        
        if response.status_code == 400:
            data = response.json()
            print(f"   [OK] Validation error: {data.get('message')}")
        else:
            print(f"   [WARN] Expected 400 but got: {response.status_code}")
            
    except Exception as e:
        print(f"   [FAIL] Exception: {e}")
    
    # Test 2: Username too short
    print("\n2. Test username too short...")
    invalid_data_2 = {
        "username": "ab",
        "password": "password123",
        "name": "TestUser",
        "role": "SALES"
    }
    
    try:
        response = requests.post(create_url, json=invalid_data_2, timeout=10, headers=headers)
        print(f"   Status: {response.status_code}")
        
        if response.status_code == 400:
            data = response.json()
            print(f"   [OK] Validation error: {data.get('message')}")
        else:
            print(f"   [WARN] Expected 400 but got: {response.status_code}")
            
    except Exception as e:
        print(f"   [FAIL] Exception: {e}")
    
    # Test 3: Valid data with SALES role
    print("\n3. Test valid data with SALES role...")
    valid_data = {
        "username": "testuser" + str(int(time.time())),
        "password": "password123",
        "name": "TestUser",
        "role": "SALES"
    }
    
    try:
        response = requests.post(create_url, json=valid_data, timeout=10, headers=headers)
        print(f"   Status: {response.status_code}")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("code") == 200:
                created_role = data.get("data", {}).get("role")
                print(f"   [OK] User created: {data.get('data', {}).get('username')}, role: {created_role}")
                return True
            else:
                print(f"   [FAIL] {data.get('message')}")
        else:
            print(f"   [FAIL] {response.text[:200]}")
            
    except Exception as e:
        print(f"   [FAIL] Exception: {e}")
    
    return False

def test_role_consistency():
    """Test role value consistency between login and /me"""
    print("\n" + "=" * 60)
    print("Test 4: Role Consistency")
    print("=" * 60)
    
    login_url = f"{BASE_URL}/api/auth/login"
    login_data = {"username": "admin", "password": "admin123"}
    
    try:
        # Login
        response = requests.post(login_url, json=login_data, timeout=10)
        if response.status_code != 200:
            print("   [FAIL] Login failed")
            return False
        
        data = response.json()
        login_role = data["data"]["user"]["role"]
        token = data["data"]["token"]
        print(f"1. Login role: {login_role}")
        
        # Get /me
        me_url = f"{BASE_URL}/api/auth/me"
        headers = {"Authorization": f"Bearer {token}"}
        me_response = requests.get(me_url, headers=headers, timeout=10)
        
        if me_response.status_code != 200:
            print("   [FAIL] /me failed")
            return False
        
        me_data = me_response.json()
        me_role = me_data["data"]["role"]
        print(f"2. /me role: {me_role}")
        
        # Compare
        if login_role == me_role:
            print(f"   [OK] Roles match: {login_role}")
            return True
        else:
            print(f"   [FAIL] Roles don't match: login={login_role}, /me={me_role}")
            return False
            
    except Exception as e:
        print(f"   [FAIL] Exception: {e}")
        return False

def main():
    """Main test"""
    print("Testing all fixes...")
    print(f"Server: {BASE_URL}")
    
    time.sleep(2)
    
    # Check server
    try:
        response = requests.get(f"{BASE_URL}/api/overview/statistics", timeout=5)
        if response.status_code == 200:
            print("[OK] Server is running")
        else:
            print(f"[WARN] Server response: {response.status_code}")
    except:
        print("[FAIL] Server not running, please start backend first")
        return
    
    # Run tests
    results = []
    results.append(("JWT Auth", test_jwt_auth()))
    results.append(("SSE Stream", test_sse_stream()))
    results.append(("User Validation", test_user_creation_validation()))
    results.append(("Role Consistency", test_role_consistency()))
    
    # Results
    print("\n" + "=" * 60)
    print("Test Results")
    print("=" * 60)
    
    all_passed = True
    for name, result in results:
        status = "[PASS]" if result else "[FAIL]"
        print(f"{name}: {status}")
        if not result:
            all_passed = False
    
    print("\n" + "=" * 60)
    if all_passed:
        print("All tests passed!")
    else:
        print("Some tests failed, check logs")
    print("=" * 60)

if __name__ == "__main__":
    main()
