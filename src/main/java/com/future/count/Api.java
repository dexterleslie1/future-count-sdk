package com.future.count;

import com.future.common.exception.BusinessException;
import com.future.common.http.ObjectResponse;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface Api {
    @RequestLine("POST /api/v1/count/init?flag={flag}")
    ObjectResponse<String> init(@Param("flag") String flag) throws BusinessException;

    @RequestLine("POST /api/v1/count/updateIncreaseCount")
    @Headers(value = {"Content-Type: application/json"})
    ObjectResponse<String> updateIncreaseCount(List<IncreaseCountDTO> increaseCountDTOList) throws BusinessException;

    @RequestLine("GET /api/v1/count/getCountByFlag?flag={flag}")
    ObjectResponse<Long> getCountByFlag(@Param("flag") String flag) throws BusinessException;
}
