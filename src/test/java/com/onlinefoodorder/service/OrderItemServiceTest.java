package com.onlinefoodorder.service;

import com.onlinefoodorder.entity.OrderItem;
import com.onlinefoodorder.repository.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemService orderItemService;

    @Test
    void saveAll_Success() {
        // Arrange
        OrderItem item1 = new OrderItem();
        OrderItem item2 = new OrderItem();
        List<OrderItem> orderItems = List.of(item1, item2);
        
        when(orderItemRepository.saveAll(anyList())).thenReturn(orderItems);

        // Act
        orderItemService.saveAll(orderItems);

        // Assert
        verify(orderItemRepository).saveAll(orderItems);
    }

    @Test
    void saveAll_EmptyList_Success() {
        // Arrange
        List<OrderItem> emptyList = List.of();
        when(orderItemRepository.saveAll(anyList())).thenReturn(emptyList);

        // Act
        orderItemService.saveAll(emptyList);

        // Assert
        verify(orderItemRepository).saveAll(emptyList);
    }
}