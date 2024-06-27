package com.teachingtool.user.service;

import com.teachingtool.utils.R;
import com.teachingtool.param.AddressParam;

public interface AddressService {

    /**
     * 查询地址列表
     * @param userId
     * @return
     */
    R list(Integer userId);

    /**
     * 保存数据库数据
     * @param address
     * @return
     */
    R save(AddressParam address);

    /**
     * 删除地址数据
     * @param id
     * @return
     */
    R remove(Integer id);
}
