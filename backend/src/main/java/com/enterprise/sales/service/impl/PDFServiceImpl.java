package com.enterprise.sales.service.impl;

import com.enterprise.sales.entity.Order;
import com.enterprise.sales.enums.OrderStatus;
import com.enterprise.sales.service.PDFService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PDFServiceImpl implements PDFService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 表格布局常量
    private static final float MARGIN_LEFT = 50;
    private static final float MARGIN_TOP = 50;
    private static final float TABLE_WIDTH = 500;
    private static final float ROW_HEIGHT = 25;
    private static final float COL1_WIDTH = 150;
    private static final float COL2_WIDTH = 350;
    
    @Override
    public byte[] generateOrderPdf(Order order) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            // 加载中文字体
            PDType0Font font = loadChineseFont(document);
            
            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN_TOP;
                
                // 标题
                y = drawTitle(cs, font, "企业销售管理系统 - 订单详情", y, 18);
                y -= 20;
                
                // 分隔线
                y = drawLine(cs, y);
                y -= 20;
                
                // 订单信息表格
                y = drawSectionTitle(cs, font, "订单信息", y, 14);
                y -= 10;
                
                String[][] tableData = {
                    {"订单编号", order.getOrderNo() != null ? order.getOrderNo() : "N/A"},
                    {"客户名称", order.getCustomerName() != null ? order.getCustomerName() : "N/A"},
                    {"订单金额", "CNY " + (order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)},
                    {"订单状态", getStatusText(order.getStatus())},
                    {"下单时间", order.getCreateTime() != null ? order.getCreateTime().format(DATE_FORMATTER) : "N/A"},
                    {"更新时间", order.getUpdateTime() != null ? order.getUpdateTime().format(DATE_FORMATTER) : "N/A"}
                };
                
                y = drawTable(cs, font, tableData, y);
                
                // 页脚
                drawFooter(cs, font, page);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("生成订单PDF失败", e);
            throw new RuntimeException("生成订单PDF失败: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] generateOverviewPdf(Map<String, Object> statistics, List<Map<String, Object>> salesRanking, List<Map<String, Object>> hotProducts) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDType0Font font = loadChineseFont(document);
            
            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - MARGIN_TOP;
                
                // 标题
                y = drawTitle(cs, font, "企业销售管理系统 - 销售总览报表", y, 18);
                y -= 5;
                
                // 生成时间
                y = drawSubtitle(cs, font, "生成时间: " + LocalDateTime.now().format(DATE_FORMATTER), y, 10);
                y -= 20;
                
                // 分隔线
                y = drawLine(cs, y);
                y -= 20;
                
                // 销售统计
                y = drawSectionTitle(cs, font, "销售统计", y, 14);
                y -= 10;
                
                String[][] statsData = {
                    {"总销售额", "CNY " + formatNumber(statistics.get("totalSales"))},
                    {"本月销售额", "CNY " + formatNumber(statistics.get("monthlySales"))},
                    {"日均销售额", "CNY " + formatNumber(statistics.get("dailySales"))},
                    {"总订单数", formatNumber(statistics.get("totalOrders"))}
                };
                y = drawTable(cs, font, statsData, y);
                y -= 20;
                
                // 销售排行榜
                if (salesRanking != null && !salesRanking.isEmpty()) {
                    y = drawSectionTitle(cs, font, "销售排行榜", y, 14);
                    y -= 10;
                    
                    String[][] rankingData = new String[salesRanking.size()][3];
                    for (int i = 0; i < salesRanking.size(); i++) {
                        Map<String, Object> item = salesRanking.get(i);
                        rankingData[i][0] = String.valueOf(i + 1);
                        rankingData[i][1] = item.get("salesperson_name") != null ? item.get("salesperson_name").toString() : "N/A";
                        rankingData[i][2] = "CNY " + formatNumber(item.get("total_sales"));
                    }
                    y = drawRankingTable(cs, font, new String[]{"排名", "销售人员", "销售额"}, rankingData, y);
                    y -= 20;
                }
                
                // 热销商品
                if (hotProducts != null && !hotProducts.isEmpty()) {
                    // 检查是否需要换页
                    if (y < 200) {
                        drawFooter(cs, font, page);
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        // 不在这里重新打开cs，直接使用新页面的逻辑
                        // 因为我们在try-with-resources中，需要特殊处理
                    }
                    
                    y = drawSectionTitle(cs, font, "热销商品", y, 14);
                    y -= 10;
                    
                    String[][] productData = new String[hotProducts.size()][3];
                    for (int i = 0; i < hotProducts.size(); i++) {
                        Map<String, Object> item = hotProducts.get(i);
                        productData[i][0] = String.valueOf(i + 1);
                        
                        String productName = null;
                        if (item.get("product_name") != null) {
                            productName = item.get("product_name").toString();
                        } else if (item.get("name") != null) {
                            productName = item.get("name").toString();
                        } else if (item.get("productname") != null) {
                            productName = item.get("productname").toString();
                        }
                        productData[i][1] = productName != null ? productName : "N/A";
                        productData[i][2] = formatNumber(item.get("sales_quantity"));
                    }
                    drawRankingTable(cs, font, new String[]{"排名", "商品名称", "销售数量"}, productData, y);
                }
                
                // 页脚
                drawFooter(cs, font, page);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("生成总览PDF失败", e);
            throw new RuntimeException("生成总览PDF失败: " + e.getMessage());
        }
    }
    
    /**
     * 加载中文字体 - 使用系统内置CJK字体
     */
    private PDType0Font loadChineseFont(PDDocument document) throws Exception {
        // 尝试加载系统中文字体，优先使用TTF文件（加载更简单可靠）
        String[][] fontConfigs = {
            {"C:/Windows/Fonts/simhei.ttf", "黑体"},       // 黑体 - TTF文件
            {"C:/Windows/Fonts/simkai.ttf", "楷体"},       // 楷体 - TTF文件  
            {"C:/Windows/Fonts/STSONG.TTF", "华文宋体"},   // 华文宋体
            {"C:/Windows/Fonts/STKAITI.TTF", "华文楷体"},  // 华文楷体
            {"/usr/share/fonts/truetype/wqy/wqy-microhei.ttc", "文泉驿微米黑"},
            {"/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf", "Droid"},
            {"/System/Library/Fonts/PingFang.ttc", "苹方"}
        };
        
        for (String[] config : fontConfigs) {
            try {
                java.io.File fontFile = new java.io.File(config[0]);
                if (fontFile.exists()) {
                    log.info("使用字体: {} ({})", config[1], config[0]);
                    // PDFBox 2.x 使用InputStream加载字体
                    try (InputStream fontStream = new java.io.FileInputStream(fontFile)) {
                        return PDType0Font.load(document, fontStream, true);
                    }
                }
            } catch (Exception e) {
                log.debug("字体加载失败: {} - {}", config[0], e.getMessage());
            }
        }
        
        // 如果系统字体都不可用，尝试从classpath加载
        String[] classpathFonts = {"fonts/simsun.ttc", "fonts/simhei.ttf", "fonts/msyh.ttc"};
        for (String fontPath : classpathFonts) {
            try {
                ClassPathResource resource = new ClassPathResource(fontPath);
                if (resource.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        // PDFBox 2.x 从InputStream加载字体
                        return PDType0Font.load(document, is);
                    }
                }
            } catch (Exception e) {
                log.debug("classpath字体加载失败: {}", fontPath);
            }
        }
        
        // 抛出明确异常
        throw new RuntimeException("未找到中文字体文件，请确保系统安装了中文字体（如微软雅黑、宋体等）");
    }
    
    private float drawTitle(PDPageContentStream cs, PDType0Font font, String text, float y, float fontSize) throws Exception {
        cs.beginText();
        cs.setFont(font, fontSize);
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (pageWidth() - textWidth) / 2;
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - fontSize - 5;
    }
    
    private float drawSubtitle(PDPageContentStream cs, PDType0Font font, String text, float y, float fontSize) throws Exception {
        cs.beginText();
        cs.setFont(font, fontSize);
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (pageWidth() - textWidth) / 2;
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - fontSize;
    }
    
    private float drawLine(PDPageContentStream cs, float y) throws Exception {
        cs.setLineWidth(1);
        cs.moveTo(MARGIN_LEFT, y);
        cs.lineTo(MARGIN_LEFT + TABLE_WIDTH, y);
        cs.stroke();
        return y;
    }
    
    private float drawSectionTitle(PDPageContentStream cs, PDType0Font font, String text, float y, float fontSize) throws Exception {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(MARGIN_LEFT, y);
        cs.showText(text);
        cs.endText();
        return y - fontSize - 5;
    }
    
    private float drawTable(PDPageContentStream cs, PDType0Font font, String[][] data, float y) throws Exception {
        float currentY = y;
        
        for (String[] row : data) {
            // 绘制单元格边框
            cs.setLineWidth(0.5f);
            cs.moveTo(MARGIN_LEFT, currentY);
            cs.lineTo(MARGIN_LEFT + TABLE_WIDTH, currentY);
            cs.stroke();
            
            // 第一列（标签）
            cs.beginText();
            cs.setFont(font, 11);
            cs.newLineAtOffset(MARGIN_LEFT + 10, currentY - 18);
            cs.showText(row[0]);
            cs.endText();
            
            // 第二列（值）
            cs.beginText();
            cs.setFont(font, 11);
            cs.newLineAtOffset(MARGIN_LEFT + COL1_WIDTH + 10, currentY - 18);
            cs.showText(row[1]);
            cs.endText();
            
            currentY -= ROW_HEIGHT;
        }
        
        // 底部边框
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN_LEFT, currentY);
        cs.lineTo(MARGIN_LEFT + TABLE_WIDTH, currentY);
        cs.stroke();
        
        // 左右边框
        cs.moveTo(MARGIN_LEFT, y);
        cs.lineTo(MARGIN_LEFT, currentY);
        cs.stroke();
        cs.moveTo(MARGIN_LEFT + TABLE_WIDTH, y);
        cs.lineTo(MARGIN_LEFT + TABLE_WIDTH, currentY);
        cs.stroke();
        
        // 中间分隔线
        cs.moveTo(MARGIN_LEFT + COL1_WIDTH, y);
        cs.lineTo(MARGIN_LEFT + COL1_WIDTH, currentY);
        cs.stroke();
        
        return currentY - 10;
    }
    
    private float drawRankingTable(PDPageContentStream cs, PDType0Font font, String[] headers, String[][] data, float y) throws Exception {
        float currentY = y;
        float col1W = 60;   // 排名列宽
        float col2W = 240;  // 名称列宽
        float col3W = 200;  // 数值列宽
        
        // 表头
        cs.setLineWidth(1);
        cs.moveTo(MARGIN_LEFT, currentY);
        cs.lineTo(MARGIN_LEFT + TABLE_WIDTH, currentY);
        cs.stroke();
        
        cs.beginText();
        cs.setFont(font, 12);
        cs.newLineAtOffset(MARGIN_LEFT + 10, currentY - 18);
        cs.showText(headers[0]);
        cs.endText();
        
        cs.beginText();
        cs.newLineAtOffset(MARGIN_LEFT + col1W + 10, currentY - 18);
        cs.showText(headers[1]);
        cs.endText();
        
        cs.beginText();
        cs.newLineAtOffset(MARGIN_LEFT + col1W + col2W + 10, currentY - 18);
        cs.showText(headers[2]);
        cs.endText();
        
        currentY -= ROW_HEIGHT;
        
        // 数据行
        for (String[] row : data) {
            cs.setLineWidth(0.5f);
            cs.moveTo(MARGIN_LEFT, currentY);
            cs.lineTo(MARGIN_LEFT + TABLE_WIDTH, currentY);
            cs.stroke();
            
            cs.beginText();
            cs.setFont(font, 11);
            cs.newLineAtOffset(MARGIN_LEFT + 10, currentY - 18);
            cs.showText(row[0]);
            cs.endText();
            
            cs.beginText();
            cs.newLineAtOffset(MARGIN_LEFT + col1W + 10, currentY - 18);
            cs.showText(row[1]);
            cs.endText();
            
            cs.beginText();
            cs.newLineAtOffset(MARGIN_LEFT + col1W + col2W + 10, currentY - 18);
            cs.showText(row[2]);
            cs.endText();
            
            currentY -= ROW_HEIGHT;
        }
        
        // 底部边框
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN_LEFT, currentY);
        cs.lineTo(MARGIN_LEFT + TABLE_WIDTH, currentY);
        cs.stroke();
        
        // 左右边框
        cs.moveTo(MARGIN_LEFT, y);
        cs.lineTo(MARGIN_LEFT, currentY);
        cs.stroke();
        cs.moveTo(MARGIN_LEFT + TABLE_WIDTH, y);
        cs.lineTo(MARGIN_LEFT + TABLE_WIDTH, currentY);
        cs.stroke();
        
        // 列分隔线
        cs.moveTo(MARGIN_LEFT + col1W, y);
        cs.lineTo(MARGIN_LEFT + col1W, currentY);
        cs.stroke();
        cs.moveTo(MARGIN_LEFT + col1W + col2W, y);
        cs.lineTo(MARGIN_LEFT + col1W + col2W, currentY);
        cs.stroke();
        
        return currentY - 10;
    }
    
    private void drawFooter(PDPageContentStream cs, PDType0Font font, PDPage page) throws Exception {
        float footerY = 30;
        cs.beginText();
        cs.setFont(font, 9);
        cs.newLineAtOffset(MARGIN_LEFT, footerY);
        cs.showText("企业销售管理系统 - " + LocalDateTime.now().format(DATE_FORMATTER));
        cs.endText();
    }
    
    private float pageWidth() {
        return PDRectangle.A4.getWidth();
    }
    
    private String getStatusText(Object status) {
        if (status == null) {
            return "N/A";
        }
        if (status instanceof OrderStatus) {
            return ((OrderStatus) status).getDescription();
        }
        return status.toString();
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
