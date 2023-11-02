package com.example.inventoryservice.repository;

import com.example.inventoryservice.model.ItemInTheInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Repository
@Transactional
public interface ItemInTheInventoryRepository extends JpaRepository<ItemInTheInventory, Integer> {
    List<ItemInTheInventory> findByItemCodeIn(List<String> itemCodes);

    Optional<ItemInTheInventory> findByItemCode(String itemCode);

    @Modifying
    @Query("UPDATE ItemInTheInventory item SET item.quantity = item.quantity - :amount WHERE item.itemCode = :itemCode")
    void reduceQuantityByItemCode(@Param("itemCode") String itemCode, @Param("amount") Integer amount);

    @Modifying
    @Query("UPDATE ItemInTheInventory item SET item.quantity = :amount WHERE item.itemCode = :itemCode")
    void setQuantityByItemCode(@Param("itemCode") String itemCode, @Param("amount") Integer amount);


}
