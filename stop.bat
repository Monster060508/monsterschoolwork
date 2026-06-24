@echo off
chcp 65001 >nul
title 企业销售管理系统 - 停止服务

echo ========================================
echo    企业销售管理系统 - 停止服务
echo ========================================
echo.

echo 正在停止所有服务...
echo.

:: 停止Java进程（后端）
echo [1/2] 停止后端服务...
tasklist /FI "IMAGENAME eq java.exe" 2>nul | find /I "java.exe" >nul
if not errorlevel 1 (
    echo 找到Java进程，正在终止...
    taskkill /F /IM java.exe >nul 2>&1
    echo 后端服务已停止
) else (
    echo 未找到运行中的后端服务
)

:: 停止Node进程（前端）
echo [2/2] 停止前端服务...
tasklist /FI "IMAGENAME eq node.exe" 2>nul | find /I "node.exe" >nul
if not errorlevel 1 (
    echo 找到Node进程，正在终止...
    taskkill /F /IM node.exe >nul 2>&1
    echo 前端服务已停止
) else (
    echo 未找到运行中的前端服务
)

echo.
echo ========================================
echo    所有服务已停止
echo ========================================
echo.
pause