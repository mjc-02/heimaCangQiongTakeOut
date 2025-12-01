package com.sky.service.impl;

import com.fasterxml.jackson.databind.JsonSerializable;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list!=null && list.size()>0){
            //当前菜品或套餐已在购物车中，数量加一
            ShoppingCart existingCart = list.get(0);
            existingCart.setNumber(existingCart.getNumber()+1);
            shoppingCartMapper.update(existingCart);
        }else {
            Long dishId = shoppingCart.getDishId();
            Long setmealId = shoppingCart.getSetmealId();
            if (dishId != null) {
                //添加到购物车的是菜品，查询菜品名称
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                //添加到购物车的是套餐，查询套餐名称
                Setmeal setmeal = setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            //当前菜品或套餐不在购物车中，添加一条新数据，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }


    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long id = BaseContext.getCurrentId();
        ShoppingCart shoppingCart=ShoppingCart.builder().userId(id).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }
    @Override
    public void cleanShoppingCart() {
        Long id = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(id);
    }
}
