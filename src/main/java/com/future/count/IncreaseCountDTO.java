package com.future.count;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class IncreaseCountDTO {
    /**
     * 幂等标识，防止重复递增计数器
     */
    @Getter
    /*private String idempotentUuid;*/
    private Long idempotentId;
    @Getter
    private String flag;

//    /**
//     * 幂等标识通过 idempotentUuidPrefix+":"+flag 生成，因为同一个订单建立两种 Cassandra 索引需要区分开幂等标识
//     *
//     * @param idempotentUuidPrefix
//     * @param flag
//     */
//    public IncreaseCountDTO(String idempotentUuidPrefix, String flag) {
//        this.idempotentUuid = idempotentUuidPrefix + ":" + flag;
//        this.flag = flag;
//    }
}
