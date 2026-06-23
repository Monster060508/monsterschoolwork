package com.enterprise.sales.service;

import com.enterprise.sales.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * PDF生成服务接口
 */
public interface PDFService {
    
    /**
     * 生成订单PDF
     * @param order 订单信息
     * @return PDF字节数组
     */
    byte[] generateOrderPdf(Order order);
    
    /**
     * 生成销售总览PDF报表
     * @param statistics 统计数据
     * @param salesRanking 销售排行榜
     * @param hotProducts 热销商品
     * @return PDF字节数组
     */
    byte[] generateOverviewPdf(Map<String, Object> statistics, List<Map<String, Object>> salesRanking, List<Map<String, Object>> hotProducts);
}
