package com.future.count;

import com.future.common.exception.BusinessException;
import com.future.common.feign.CustomizeErrorDecoder;
import com.future.common.feign.FeignUtil;
import com.future.common.http.ObjectResponse;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CountService {

    private Api api;

    /**
     * @param host
     * @param port
     * @param flagList 服务支持的flag列表
     */
    public CountService(String host, int port, List<String> flagList) throws BusinessException {
        Assert.isTrue(flagList != null && !flagList.isEmpty(), "请指定服务支持的flag列表");

        api = Feign.builder()
                // https://stackoverflow.com/questions/56987701/feign-client-retry-on-exception
                .retryer(Retryer.NEVER_RETRY)
                // https://qsli.github.io/2020/04/28/feign-method-timeout/
                .options(new Request.Options(15, TimeUnit.SECONDS, 15, TimeUnit.SECONDS, false))
                .encoder(new FormEncoder(new JacksonEncoder()))
                .decoder(new JacksonDecoder())
                // feign logger
                // https://cloud.tencent.com/developer/article/1588501
                .logger(new Logger.ErrorLogger()).logLevel(Logger.Level.NONE)
                // ErrorDecoder
                // https://cloud.tencent.com/developer/article/1588501
                .errorDecoder(new CustomizeErrorDecoder())
                .target(Api.class, "http://" + host + ":" + port);

        // 调用计数器服务 init 方法以初始化对应的 flag
        boolean exit = false;
        int failCount = 0;
        while (!exit) {
            boolean exception = false;
            for (String flag : flagList) {
                try {
                    ObjectResponse<String> response = api.init(flag);
                    if (response.getErrorCode() > 0) {
                        BusinessException ex = new BusinessException(response.getErrorCode(), response.getErrorMessage());
                        ex.setData(response.getData());
                        throw ex;
                    }
                    if (log.isDebugEnabled())
                        log.debug("成功调用计数器服务 init 方法，flag {}", flag);
                } catch (Exception ex) {
                    log.error("调用计数器服务 init 方法失败，flag {}，原因：{}", flag, ex.getMessage());

                    failCount = failCount + 1;
                    if (failCount >= 10) {
                        throw new BusinessException("尝试调用计数器服务的 init 方法共失败 " + failCount + " 次，可能是因为计数器服务没有启动并运行");
                    }

                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException ignored) {

                    }

                    exception = true;
                    break;
                }
            }

            if (!exception)
                exit = true;
        }
        if (log.isDebugEnabled())
            log.debug("成功调用计数器服务 init 方法，flagList {}", flagList);

    }

    /**
     * 根据 flag 查询计数器当前计数
     *
     * @param flag
     * @return
     * @throws BusinessException
     */
    public long getCountByFlag(String flag) throws BusinessException {
        ObjectResponse<Long> response = api.getCountByFlag(flag);
        FeignUtil.throwBizExceptionIfResponseFailed(response);
        return response.getData();
    }

    /**
     * 递增计数器
     *
     * @param increaseCountDTOList
     * @throws BusinessException
     */
    public void updateIncreaseCount(List<IncreaseCountDTO> increaseCountDTOList) throws BusinessException {
        ObjectResponse<String> response = api.updateIncreaseCount(increaseCountDTOList);
        FeignUtil.throwBizExceptionIfResponseFailed(response);
    }
}
