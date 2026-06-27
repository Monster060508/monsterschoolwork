@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo    企业销售管理系统 - 启动脚本
echo ========================================
echo.

:menu
echo 请选择启动方式:
echo.
echo 1. 启动完整系统（后端 + 前端）
echo 2. 仅启动后端
echo 3. 仅启动前端
echo 4. 初始化数据库
echo 5. 运行测试
echo 6. 退出
echo.
set /p choice=请输入选项 (1-6): 

if "%choice%"=="1" goto start_all
if "%choice%"=="2" goto start_backend
if "%choice%"=="3" goto start_frontend
if "%choice%"=="4" goto init_database
if "%choice%"=="5" goto run_tests
if "%choice%"=="6" goto exit
echo 无效选项，请重新选择
echo.
goto menu

:start_all
echo.
echo [1/2] 启动后端服务...
echo ========================================
cd /d "%~dp0backend"

:: 检查Maven是否安装
where mvn >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Maven，请先安装Maven
    echo 下载地址: https://maven.apache.org/download.cgi
    pause
    goto menu
)

:: 启动后端（新窗口）
echo 正在启动后端服务...
start "后端服务" cmd /c "mvn spring-boot:run"

:: 等待后端启动
echo 等待后端服务启动（约5秒）...
timeout /t 5 /nobreak >nul

echo.
echo [2/2] 启动前端服务...
echo ========================================
cd /d "%~dp0frontend"

:: 检查Node.js是否安装
where node >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Node.js，请先安装Node.js
    echo 下载地址: https://nodejs.org/
    pause
    goto menu
)

:: 检查依赖是否安装
if not exist "node_modules" (
    echo 首次运行，正在安装前端依赖...
    call npm install
)

:: 启动前端
echo 正在启动前端服务...
start "前端服务" cmd /c "npm run dev"

echo.
echo ========================================
echo    系统启动完成！
echo ========================================
echo.
echo 前端地址: http://localhost:5173
echo 后端地址: http://localhost:8080
echo.
echo 测试账号:
echo   管理员: admin / admin123
echo   销售:   sales001 / sales123
echo.
echo 按任意键返回主菜单...
pause >nul
goto menu

:start_backend
echo.
echo 启动后端服务...
echo ========================================
cd /d "%~dp0backend"

:: 检查Maven是否安装
where mvn >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Maven，请先安装Maven
    echo 下载地址: https://maven.apache.org/download.cgi
    pause
    goto menu
)

:: 检查端口是否被占用
netstat -ano | findstr :8080 >nul
if not errorlevel 1 (
    echo 警告: 端口8080已被占用
    set /p kill_process=是否终止占用端口的进程? (Y/N): 
    if /i "!kill_process!"=="Y" (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
            taskkill /PID %%a /F >nul 2>&1
        )
        echo 已终止占用端口的进程
    )
)

echo 正在启动后端服务...
echo 启动完成后请访问: http://localhost:8080
echo.
echo 按 Ctrl+C 停止服务
echo.
call mvn spring-boot:run

echo.
echo 后端服务已停止
pause
goto menu

:start_frontend
echo.
echo 启动前端服务...
echo ========================================
cd /d "%~dp0frontend"

:: 检查Node.js是否安装
where node >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Node.js，请先安装Node.js
    echo 下载地址: https://nodejs.org/
    pause
    goto menu
)

:: 检查依赖是否安装
if not exist "node_modules" (
    echo 首次运行，正在安装前端依赖...
    call npm install
)

:: 检查端口是否被占用
netstat -ano | findstr :5173 >nul
if not errorlevel 1 (
    echo 警告: 端口5173已被占用
    set /p kill_process=是否终止占用端口的进程? (Y/N): 
    if /i "!kill_process!"=="Y" (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173') do (
            taskkill /PID %%a /F >nul 2>&1
        )
        echo 已终止占用端口的进程
    )
)

echo 正在启动前端服务...
echo 启动完成后请访问: http://localhost:5173
echo.
echo 按 Ctrl+C 停止服务
echo.
call npm run dev

echo.
echo 前端服务已停止
pause
goto menu

:init_database
echo.
echo 初始化数据库...
echo ========================================

:: 检查Python是否安装
where python >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到Python，请先安装Python
    echo 下载地址: https://www.python.org/downloads/
    pause
    goto menu
)

cd /d "%~dp0"

echo 数据库初始化说明:
echo.
echo 1. 确保MySQL服务已启动
echo 2. 确保已创建数据库: sales_management
echo 3. 执行 sql/init.sql 创建表结构
echo 4. 执行 sql/data.sql 插入初始数据
echo.
echo 是否使用Python脚本初始化? (需要配置数据库连接)
set /p init_choice=运行初始化脚本? (Y/N): 

if /i "!init_choice!"=="Y" (
    echo 正在运行初始化脚本...
    python init_db.py
) else (
    echo 请手动执行SQL脚本:
    echo   mysql -u root -p ^< sql/init.sql
    echo   mysql -u root -p ^< sql/data.sql
)

echo.
pause
goto menu

:run_tests
echo.
echo 运行测试...
echo ========================================
cd /d "%~dp0"

echo 可用的测试:
echo.
echo 1. 运行后端单元测试
echo 2. 运行API集成测试
echo 3. 返回主菜单
echo.
set /p test_choice=请选择测试 (1-3): 

if "%test_choice%"=="1" (
    cd /d "%~dp0backend"
    echo 运行后端单元测试...
    call mvn test
)
if "%test_choice%"=="2" (
    echo 运行API集成测试...
    python test_all_api.py
)
if "%test_choice%"=="3" goto menu

echo.
pause
goto menu

:exit
echo.
echo 感谢使用企业销售管理系统！
echo.
exit /b 0