package com.teachingtool.clients;

import com.teachingtool.param.CartParam;
import com.teachingtool.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "cart-service")
public interface CartClient {
    @PostMapping("/cart/remove")
    R remove(@RequestBody CartParam cartParam);
}
