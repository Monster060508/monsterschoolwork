package com.enterprise.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.sales.entity.Order;
import com.enterprise.sales.entity.OrderItem;
import com.enterprise.sales.entity.Product;
import com.enterprise.sales.entity.User;
import com.enterprise.sales.enums.OrderStatus;
import com.enterprise.sales.mapper.OrderMapper;
import com.enterprise.sales.service.OrderItemService;
import com.enterprise.sales.service.OrderService;
import com.enterprise.sales.service.ProductService;
import com.enterprise.sales.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    private final OrderMapper orderMapper;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final UserService userService;
    
    @Override
    @Transactional
    public Order createOrder(Order order, List<Map<String, Object>> items) {
        // 校验订单项
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("订单商品不能为空");
        }
        
        // 生成订单号
        order.setOrderNo(generateOrderNo());
        
        // 设置默认值
        order.setStatus(OrderStatus.PENDING);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);
        
        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            
            // 获取商品信息
            Product product = productService.getById(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在: " + productId);
            }
            
            // 检查库存
            if (!productService.checkStock(productId, quantity)) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }
            
            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(productId);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());
            
            orderItems.add(orderItem);
            
            // 计算小计
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(subtotal);
        }
        
        // 设置订单总金额
        order.setTotalAmount(totalAmount);
        
        // 保存订单
        orderMapper.insert(order);
        
        // 保存订单项
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemService.createOrderItem(orderItem);
        }
        
        return order;
    }
    
    @Override
    @Transactional
    public Order updateOrder(Long id, Order order) {
        Order existingOrder = getById(id);
        if (existingOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 已完成或已取消的订单不能修改
        if (existingOrder.getStatus() == OrderStatus.COMPLETED || existingOrder.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("已完成或已取消的订单不能修改");
        }
        
        // 更新订单信息
        if (order.getCustomerName() != null) {
            existingOrder.setCustomerName(order.getCustomerName());
        }
        if (order.getSalespersonId() != null) {
            existingOrder.setSalespersonId(order.getSalespersonId());
        }
        if (order.getStatus() != null) {
            existingOrder.setStatus(order.getStatus());
        }
        
        existingOrder.setUpdateTime(LocalDateTime.now());
        
        // 保存更新
        updateById(existingOrder);
        
        return existingOrder;
    }
    
    @Override
    @Transactional
    public boolean deleteOrder(Long id) {
        Order order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 只有待处理状态的订单才能删除
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("只有待处理状态的订单才能删除");
        }
        
        // 逻辑删除
        order.setDeleted(1);
        order.setUpdateTime(LocalDateTime.now());
        
        return updateById(order);
    }
    
    @Override
    @Transactional
    public boolean updateOrderStatus(Long id, OrderStatus status) {
        Order order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 状态流转验证
        if (!isValidStatusTransition(order.getStatus(), status)) {
            throw new RuntimeException("无效的状态变更");
        }
        
        // 如果是完成订单，扣减库存
        if (status == OrderStatus.COMPLETED) {
            completeOrder(id);
        }
        
        // 如果是取消订单，回滚库存
        if (status == OrderStatus.CANCELLED) {
            cancelOrder(id, "手动取消");
        }
        
        // 更新状态
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        
        return updateById(order);
    }
    
    @Override
    public Order findByOrderNo(String orderNo) {
        return orderMapper.findByOrderNo(orderNo);
    }
    
    @Override
    public List<Order> findBySalespersonId(Long salespersonId) {
        return orderMapper.findBySalespersonId(salespersonId);
    }
    
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return orderMapper.findByStatus(status.getCode());
    }
    
    @Override
    public List<Order> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return orderMapper.findByTimeRange(startTime, endTime);
    }
    
    @Override
    public List<Order> getOrders(int page, int size, OrderStatus status, Long salespersonId, String keyword) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        
        if (status != null) {
            queryWrapper.eq("status", status.getCode());
        }
        
        if (salespersonId != null) {
            queryWrapper.eq("salesperson_id", salespersonId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("order_no", keyword)
                .or()
                .like("customer_name", keyword)
            );
        }
        
        queryWrapper.orderByDesc("create_time");
        
        // 分页
        int offset = (page - 1) * size;
        queryWrapper.last("LIMIT " + offset + ", " + size);
        
        List<Order> orders = list(queryWrapper);
        
        // 填充关联字段（销售人员名称和商品名称）
        populateOrderExtraFields(orders);
        
        return orders;
    }
    
    @Override
    public long getOrderCount(OrderStatus status, Long salespersonId, String keyword) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        
        if (status != null) {
            queryWrapper.eq("status", status.getCode());
        }
        
        if (salespersonId != null) {
            queryWrapper.eq("salesperson_id", salespersonId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("order_no", keyword)
                .or()
                .like("customer_name", keyword)
            );
        }
        
        return count(queryWrapper);
    }
    
    /**
     * 填充订单的关联字段（销售人员名称和商品名称）
     */
    private void populateOrderExtraFields(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        
        for (Order order : orders) {
            // 填充销售人员名称
            if (order.getSalespersonId() != null) {
                try {
                    User salesperson = userService.getById(order.getSalespersonId());
                    if (salesperson != null) {
                        order.setSalespersonName(salesperson.getName());
                    }
                } catch (Exception e) {
                    // 忽略查询失败
                }
            }
            
            // 填充商品名称
            try {
                List<OrderItem> items = orderItemService.findByOrderId(order.getId());
                if (items != null && !items.isEmpty()) {
                    StringBuilder productNames = new StringBuilder();
                    for (OrderItem item : items) {
                        Product product = productService.getById(item.getProductId());
                        if (product != null) {
                            if (productNames.length() > 0) {
                                productNames.append(", ");
                            }
                            productNames.append(product.getName());
                        }
                    }
                    order.setProductName(productNames.toString());
                }
            } catch (Exception e) {
                // 忽略查询失败
            }
        }
    }
    
    @Override
    public Map<String, Long> countByStatus() {
        List<Map<String, Object>> result = orderMapper.countByStatus();
        Map<String, Long> statusCount = new HashMap<>();
        
        for (Map<String, Object> map : result) {
            String status = map.get("status").toString();
            Long count = Long.valueOf(map.get("count").toString());
            statusCount.put(status, count);
        }
        
        return statusCount;
    }
    
    @Override
    public List<Map<String, Object>> getSalesRanking(int limit) {
        return orderMapper.getSalesRanking(limit);
    }
    
    @Override
    public List<Map<String, Object>> getOrderTrend(int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        return orderMapper.getOrderTrend(startTime, endTime);
    }
    
    @Override
    public List<Map<String, Object>> getRecentOrders(int limit) {
        return orderMapper.getRecentOrders(limit);
    }
    
    @Override
    public double getTotalSales() {
        return orderMapper.getTotalSales();
    }
    
    @Override
    public double getMonthlySales() {
        return orderMapper.getMonthlySales();
    }
    
    @Override
    public double getDailyAverageSales() {
        return orderMapper.getDailyAverageSales();
    }
    
    @Override
    public byte[] generateOrderPdf(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 获取订单项
        List<Map<String, Object>> orderItems = orderItemService.findOrderItemDetails(orderId);
        
        // 创建Excel工作簿（用于生成PDF）
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("订单详情");
            
            // 创建标题样式
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // 创建数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            // 创建标题行
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("订单详情");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));
            
            // 创建订单信息
            Row orderInfoRow1 = sheet.createRow(2);
            orderInfoRow1.createCell(0).setCellValue("订单号:");
            orderInfoRow1.createCell(1).setCellValue(order.getOrderNo());
            orderInfoRow1.createCell(3).setCellValue("客户名称:");
            orderInfoRow1.createCell(4).setCellValue(order.getCustomerName());
            
            Row orderInfoRow2 = sheet.createRow(3);
            orderInfoRow2.createCell(0).setCellValue("订单状态:");
            orderInfoRow2.createCell(1).setCellValue(order.getStatus().getDescription());
            orderInfoRow2.createCell(3).setCellValue("订单金额:");
            orderInfoRow2.createCell(4).setCellValue(order.getTotalAmount().toString());
            
            Row orderInfoRow3 = sheet.createRow(4);
            orderInfoRow3.createCell(0).setCellValue("创建时间:");
            orderInfoRow3.createCell(1).setCellValue(order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 创建表头
            Row headerRow = sheet.createRow(6);
            String[] headers = {"序号", "商品名称", "商品图片", "数量", "单价", "小计"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 7;
            for (Map<String, Object> item : orderItems) {
                Row dataRow = sheet.createRow(rowNum++);
                
                Cell cell0 = dataRow.createCell(0);
                cell0.setCellValue(rowNum - 7);
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = dataRow.createCell(1);
                cell1.setCellValue(item.get("product_name").toString());
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = dataRow.createCell(2);
                cell2.setCellValue(item.get("product_image") != null ? item.get("product_image").toString() : "");
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = dataRow.createCell(3);
                cell3.setCellValue(Integer.parseInt(item.get("quantity").toString()));
                cell3.setCellStyle(dataStyle);
                
                Cell cell4 = dataRow.createCell(4);
                cell4.setCellValue(Double.parseDouble(item.get("unit_price").toString()));
                cell4.setCellStyle(dataStyle);
                
                Cell cell5 = dataRow.createCell(5);
                cell5.setCellValue(Double.parseDouble(item.get("subtotal").toString()));
                cell5.setCellStyle(dataStyle);
            }
            
            // 设置列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 5000);
            }
            
            // 写入字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("生成PDF失败", e);
        }
    }
    
    @Override
    @Transactional
    public boolean completeOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 只有进行中状态的订单才能完成
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new RuntimeException("只有进行中状态的订单才能完成");
        }
        
        // 获取订单项
        List<OrderItem> orderItems = orderItemService.findByOrderId(orderId);
        
        // 扣减库存
        for (OrderItem orderItem : orderItems) {
            productService.deductStock(orderItem.getProductId(), orderItem.getQuantity());
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdateTime(LocalDateTime.now());
        
        return updateById(order);
    }
    
    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, String reason) {
        Order order = getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 如果订单已完成，需要回滚库存
        if (order.getStatus() == OrderStatus.COMPLETED) {
            List<OrderItem> orderItems = orderItemService.findByOrderId(orderId);
            
            // 回滚库存
            for (OrderItem orderItem : orderItems) {
                productService.addStock(orderItem.getProductId(), orderItem.getQuantity());
            }
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdateTime(LocalDateTime.now());
        
        return updateById(order);
    }
    
    @Override
    public String generateOrderNo() {
        // 生成订单号：ORD + 日期 + 4位随机数
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        
        return "ORD" + dateStr + random;
    }
    
    /**
     * 验证状态流转是否有效
     */
    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.IN_PROGRESS || newStatus == OrderStatus.CANCELLED;
            case IN_PROGRESS:
                return newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED;
            case COMPLETED:
                return false; // 已完成的订单不能变更状态
            case CANCELLED:
                return false; // 已取消的订单不能变更状态
            default:
                return false;
        }
    }
}