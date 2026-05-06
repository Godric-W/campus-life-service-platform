package com.god.gateway.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.god.common.result.Result;
import com.god.common.result.ResultCode;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public final class GatewayResponseUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GatewayResponseUtil() {
    }

    public static Mono<Void> writeError(ServerHttpResponse response, HttpStatus httpStatus, ResultCode resultCode) {
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = toJsonBytes(Result.fail(resultCode));
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private static byte[] toJsonBytes(Result<Void> result) {
        try {
            return OBJECT_MAPPER.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException ex) {
            return "{\"code\":500,\"message\":\"系统异常，请稍后再试\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        }
    }
}
