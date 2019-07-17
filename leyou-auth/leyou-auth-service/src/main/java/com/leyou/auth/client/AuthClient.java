package com.leyou.auth.client;


import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("user-service")
public interface AuthClient extends UserApi {
}
