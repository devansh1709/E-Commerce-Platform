package com.cfs.Ecomm.repo;

import com.cfs.Ecomm.model.Orders;
import com.cfs.Ecomm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    @Query("select o from Orders o join fetch o.user")
    List<Orders> findAllOrdersWithUsers();

    List<Orders> findByUser(User user);

}
