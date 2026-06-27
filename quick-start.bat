@echo off
chcp 65001 >nul
title 企业销售管理系统 - 快速启动

echo ========================================
echo    企业销售管理系统 - 快速启动
echo ========================================
echo.
echo 正在启动系统，请稍候...
echo.

:: 启动后端
echo [1/2] 启动后端服务...
cd /d "%~dp0backend"
start "后端服务" cmd /c "mvn spring-boot:run"

:: 等待后端启动
echo 等待后端服务启动（约5秒）...
timeout /t 5 /nobreak >nul

:: 启动前端
echo [2/2] 启动前端服务...
cd /d "%~dp0frontend"
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
echo 按任意键打开浏览器访问系统...
pause >nul

:: 打开浏览器
start http://localhost:5173

echo.
echo 提示: 关闭此窗口不会停止服务
echo 如需停止服务，请关闭"后端服务"和"前端服务"窗口
echo.
pause