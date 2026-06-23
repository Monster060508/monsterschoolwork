package com.enterprise.sales.service.impl;

import com.enterprise.sales.entity.Order;
import com.enterprise.sales.service.PDFService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PDFServiceImpl implements PDFService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public byte[] generateOrderPdf(Order order) {
        try (XWPFDocument document = new XWPFDocument()) {
            // 创建标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("企业销售管理系统 - 订单详情");
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            
            // 创建空行
            document.createParagraph();
            
            // 订单基本信息
            createOrderInfoSection(document, order);
            
            // 输出PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("生成订单PDF失败", e);
            throw new RuntimeException("生成订单PDF失败: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] generateOverviewPdf(Map<String, Object> statistics, List<Map<String, Object>> salesRanking, List<Map<String, Object>> hotProducts) {
        try (XWPFDocument document = new XWPFDocument()) {
            // 创建标题
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("企业销售管理系统 - 销售总览报表");
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            
            // 生成时间
            XWPFParagraph timeParagraph = document.createParagraph();
            timeParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun timeRun = timeParagraph.createRun();
            timeRun.setText("生成时间: " + LocalDateTime.now().format(DATE_FORMATTER));
            timeRun.setFontSize(10);
            timeRun.setColor("666666");
            
            // 创建空行
            document.createParagraph();
            
            // 销售统计
            createStatisticsSection(document, statistics);
            
            // 销售排行榜
            createSalesRankingSection(document, salesRanking);
            
            // 热销商品
            createHotProductsSection(document, hotProducts);
            
            // 输出PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("生成总览PDF失败", e);
            throw new RuntimeException("生成总览PDF失败: " + e.getMessage());
        }
    }
    
    private void createOrderInfoSection(XWPFDocument document, Order order) {
        // 订单信息标题
        addSectionTitle(document, "订单信息");
        
        // 订单详情表格
        XWPFTable table = document.createTable(6, 2);
        
        // 设置表格内容
        setTableCell(table, 0, 0, "订单编号");
        setTableCell(table, 0, 1, order.getOrderNo() != null ? order.getOrderNo() : "N/A");
        
        setTableCell(table, 1, 0, "客户名称");
        setTableCell(table, 1, 1, order.getCustomerName() != null ? order.getCustomerName() : "N/A");
        
        setTableCell(table, 2, 0, "订单金额");
        setTableCell(table, 2, 1, "¥" + (order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO));
        
        setTableCell(table, 3, 0, "订单状态");
        setTableCell(table, 3, 1, order.getStatus() != null ? order.getStatus().getDescription() : "N/A");
        
        setTableCell(table, 4, 0, "下单时间");
        setTableCell(table, 4, 1, order.getCreateTime() != null ? order.getCreateTime().format(DATE_FORMATTER) : "N/A");
        
        setTableCell(table, 5, 0, "更新时间");
        setTableCell(table, 5, 1, order.getUpdateTime() != null ? order.getUpdateTime().format(DATE_FORMATTER) : "N/A");
    }
    
    private void createStatisticsSection(XWPFDocument document, Map<String, Object> statistics) {
        addSectionTitle(document, "销售统计");
        
        XWPFTable table = document.createTable(4, 2);
        
        setTableCell(table, 0, 0, "总销售额");
        setTableCell(table, 0, 1, "¥" + formatNumber(statistics.get("totalSales")));
        
        setTableCell(table, 1, 0, "本月销售额");
        setTableCell(table, 1, 1, "¥" + formatNumber(statistics.get("monthlySales")));
        
        setTableCell(table, 2, 0, "日均销售额");
        setTableCell(table, 2, 1, "¥" + formatNumber(statistics.get("dailySales")));
        
        setTableCell(table, 3, 0, "总订单数");
        setTableCell(table, 3, 1, formatNumber(statistics.get("totalOrders")));
    }
    
    private void createSalesRankingSection(XWPFDocument document, List<Map<String, Object>> salesRanking) {
        if (salesRanking == null || salesRanking.isEmpty()) {
            return;
        }
        
        addSectionTitle(document, "销售排行榜");
        
        XWPFTable table = document.createTable(salesRanking.size() + 1, 3);
        
        // 表头
        setTableCell(table, 0, 0, "排名");
        setTableCell(table, 0, 1, "销售人员");
        setTableCell(table, 0, 2, "销售额");
        
        // 数据
        for (int i = 0; i < salesRanking.size(); i++) {
            Map<String, Object> item = salesRanking.get(i);
            setTableCell(table, i + 1, 0, String.valueOf(i + 1));
            setTableCell(table, i + 1, 1, item.get("salesperson_name") != null ? item.get("salesperson_name").toString() : "N/A");
            setTableCell(table, i + 1, 2, "¥" + formatNumber(item.get("total_sales")));
        }
    }
    
    private void createHotProductsSection(XWPFDocument document, List<Map<String, Object>> hotProducts) {
        if (hotProducts == null || hotProducts.isEmpty()) {
            return;
        }
        
        addSectionTitle(document, "热销商品");
        
        XWPFTable table = document.createTable(hotProducts.size() + 1, 3);
        
        // 表头
        setTableCell(table, 0, 0, "排名");
        setTableCell(table, 0, 1, "商品名称");
        setTableCell(table, 0, 2, "销售数量");
        
        // 数据
        for (int i = 0; i < hotProducts.size(); i++) {
            Map<String, Object> item = hotProducts.get(i);
            setTableCell(table, i + 1, 0, String.valueOf(i + 1));
            setTableCell(table, i + 1, 1, item.get("name") != null ? item.get("name").toString() : "N/A");
            setTableCell(table, i + 1, 2, formatNumber(item.get("sales_quantity")));
        }
    }
    
    private void addSectionTitle(XWPFDocument document, String title) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(200);
        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(14);
    }
    
    private void setTableCell(XWPFTable table, int row, int col, String text) {
        XWPFTableRow tableRow = table.getRow(row);
        XWPFTableCell cell = tableRow.getCell(col);
        cell.setText(text != null ? text : "");
    }
    
    private String formatNumber(Object value) {
        if (value == null) {
            return "0";
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        return value.toString();
    }
}
