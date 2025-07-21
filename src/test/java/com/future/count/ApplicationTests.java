package com.future.count;

import com.future.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = Application.class)
@Slf4j
@EnableFutureCount
public class ApplicationTests {

    @Resource
    CountService countService;

    /**
     * @throws BusinessException
     */
    @Test
    public void contextLoads() throws BusinessException {
        String flag = "order";

        long countOrigin = countService.getCountByFlag(flag);
        String idemponentIdPrefix1 = UUID.randomUUID().toString();
        String idemponentIdPrefix2 = UUID.randomUUID().toString();
        List<IncreaseCountDTO> increaseCountDTOList = new ArrayList<IncreaseCountDTO>() {{
            IncreaseCountDTO increaseCountDTO = new IncreaseCountDTO(idemponentIdPrefix1, flag);
            this.add(increaseCountDTO);
            increaseCountDTO = new IncreaseCountDTO(idemponentIdPrefix2, flag);
            this.add(increaseCountDTO);
        }};
        countService.updateIncreaseCount(increaseCountDTOList);
        long count = countService.getCountByFlag(flag);
        Assertions.assertEquals(countOrigin + 2, count);

        // 测试使用相同的 idempotentId 再递增一次，结果不会重复递增
        countService.updateIncreaseCount(increaseCountDTOList);
        count = countService.getCountByFlag(flag);
        Assertions.assertEquals(countOrigin + 2, count);

        // 测试有部分 idempotentId 已经递增过情况
        increaseCountDTOList = new ArrayList<IncreaseCountDTO>() {{
            this.add(new IncreaseCountDTO(UUID.randomUUID().toString(), flag));
            this.add(new IncreaseCountDTO(UUID.randomUUID().toString(), flag));
            this.add(new IncreaseCountDTO(UUID.randomUUID().toString(), flag));
            this.add(new IncreaseCountDTO(idemponentIdPrefix1, flag));
            this.add(new IncreaseCountDTO(idemponentIdPrefix2, flag));
        }};
        countService.updateIncreaseCount(increaseCountDTOList);
        count = countService.getCountByFlag(flag);
        Assertions.assertEquals(countOrigin + 5, count);


        // region 测试未初始化 flag

        String flagNotExists = "flagNotExists";
        increaseCountDTOList = new ArrayList<IncreaseCountDTO>() {{
            this.add(new IncreaseCountDTO(UUID.randomUUID().toString(), flagNotExists));
            this.add(new IncreaseCountDTO(UUID.randomUUID().toString(), flagNotExists));
            this.add(new IncreaseCountDTO(UUID.randomUUID().toString(), flagNotExists));
        }};
        try {
            countService.updateIncreaseCount(increaseCountDTOList);
            Assertions.fail();
        } catch (BusinessException ex) {
            Assertions.assertEquals("未初始化 flag flagNotExists 计数器，请调用 /api/v1/count/init 接口先初始化", ex.getMessage());
        }

        // endregion
    }
}
