import mysql.connector
import bcrypt

# 数据库连接配置
config = {
    'host': '47.103.77.247',
    'user': 'root',
    'password': 'mysql_CfcTzc',
    'database': 'sales_management'
}

def hash_password(password):
    """生成BCrypt密码哈希"""
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

def init_users():
    """初始化用户数据"""
    conn = mysql.connector.connect(**config)
    cursor = conn.cursor()
    
    # 清空现有用户
    cursor.execute("DELETE FROM sys_user")
    
    # 用户数据
    users = [
        ('admin', 'admin123', 'ADMIN', '系统管理员', '13800138000', 'admin@company.com'),
        ('sales001', 'sales123', 'SALES', '张三', '13800138001', 'zhangsan@company.com'),
        ('sales002', 'sales123', 'SALES', '李四', '13800138002', 'lisi@company.com'),
        ('sales003', 'sales123', 'SALES', '王五', '13800138003', 'wangwu@company.com'),
    ]
    
    for username, password, role, name, phone, email in users:
        hashed_password = hash_password(password)
        cursor.execute("""
            INSERT INTO sys_user (username, password, role, name, phone, email, status)
            VALUES (%s, %s, %s, %s, %s, %s, 1)
        """, (username, hashed_password, role, name, phone, email))
        print(f"创建用户: {username} ({role})")
    
    conn.commit()
    cursor.close()
    conn.close()
    print("用户初始化完成!")

if __name__ == '__main__':
    init_users()