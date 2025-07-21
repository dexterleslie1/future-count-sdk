package com.future.count;

import com.fasterxml.jackson.core.type.TypeReference;
import com.future.common.exception.BusinessException;
import com.future.common.http.ObjectResponse;
import com.future.common.json.JSONUtil;
import feign.*;
import feign.codec.ErrorDecoder;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
                .errorDecoder(new ErrorDecoder() {
                    @Override
                    public Exception decode(String methodKey, Response response) {
                        try {
                            String json = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8);
                            ObjectResponse<String> responseError = JSONUtil.ObjectMapperInstance.readValue(json, new TypeReference<ObjectResponse<String>>() {
                            });
                            return new BusinessException(responseError.getErrorCode(), responseError.getErrorMessage());
                        } catch (IOException e) {
                            return e;
                        }
                    }
                })
                .target(Api.class, "http://" + host + ":" + port);

        // 调用计数器服务 init 方法以初始化对应的 flag
        boolean exit = false;
        int failCount = 0;
        while (!exit) {
            boolean exception = false;
            for (String flag : flagList) {
                try {
                    api.init(flag);
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
        return api.getCountByFlag(flag).getData();
    }

    /**
     * 递增计数器
     *
     * @param increaseCountDTOList
     * @throws BusinessException
     */
    public void updateIncreaseCount(List<IncreaseCountDTO> increaseCountDTOList) throws BusinessException {
        api.updateIncreaseCount(increaseCountDTOList);
    }
}
