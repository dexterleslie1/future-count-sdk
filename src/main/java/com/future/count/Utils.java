package com.future.count;

import com.future.common.exception.BusinessException;
import com.future.common.http.BaseResponse;
import com.future.common.http.ListResponse;
import com.future.common.http.ObjectResponse;
import com.future.common.http.PageResponse;

public class Utils {
    /**
     * 如果响应错误码大于0则抛出BusinessException
     *
     * @param response
     * @throws BusinessException
     */
    public static void throwBusinessExceptionIfFailed(BaseResponse response) throws BusinessException {
        if (response.getErrorCode() > 0) {
            // 获取响应中的数据
            Object data = null;
            if (response instanceof ObjectResponse) {
                data = ((ObjectResponse) response).getData();
            } else if (response instanceof ListResponse) {
                data = ((ListResponse) response).getData();
            } else if (response instanceof PageResponse) {
                data = ((PageResponse) response).getData();
            }

            BusinessException ex = new BusinessException(response.getErrorCode(), response.getErrorMessage());
            ex.setData(data);
            throw ex;
        }
    }
}
