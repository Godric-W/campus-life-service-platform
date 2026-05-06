package com.god.common.client;

import com.god.common.dto.UserSimpleDTO;
import com.god.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "campus-user-auth-service", path = "/users")
public interface UserFeignClient {

    @GetMapping("/simple/{id}")
    Result<UserSimpleDTO> getUserSimple(@PathVariable("id") Long id);

    @PostMapping("/simple/batch")
    Result<Map<Long, UserSimpleDTO>> batchGetUserSimple(@RequestBody List<Long> userIds);
}
