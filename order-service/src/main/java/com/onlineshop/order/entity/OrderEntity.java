package com.onlineshop.order.entity;


import com.onlineshop.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLineEntity> orderLineList = new ArrayList<>();

    public OrderEntity(ClientEntity client, OrderStatus status) {
        this.client = client;
        this.orderDate = LocalDateTime.now();
        this.status = status;
    }

    public void addOrderLine(OrderLineEntity orderLine) {
        orderLine.setOrder(this);
        orderLineList.add(orderLine);
    }


    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof OrderEntity)) return false;

        OrderEntity other = (OrderEntity) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OrderEntity{" + "id=" + id + ", orderDate=" + orderDate +
                ", status=" + status + ", totalAmount=" + totalAmount + '}';
    }
}
