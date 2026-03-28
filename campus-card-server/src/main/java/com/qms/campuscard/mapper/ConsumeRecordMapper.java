package com.qms.campuscard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qms.campuscard.entity.ConsumeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ConsumeRecordMapper extends BaseMapper<ConsumeRecord> {

    @Select("SELECT TO_CHAR(consume_time, 'YYYY-MM-DD') as date, SUM(amount) as total_amount " +
            "FROM consume_record " +
            "WHERE consume_time BETWEEN #{startDate} AND #{endDate} AND is_deleted=0 " +
            "GROUP BY TO_CHAR(consume_time, 'YYYY-MM-DD') " +
            "ORDER BY date")
    List<Map<String, Object>> getConsumeStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select("SELECT a.card_id, SUM(cr.amount) as total_amount " +
            "FROM consume_record cr " +
            "JOIN account a ON cr.account_id = a.id " +
            "WHERE cr.consume_time BETWEEN #{startDate} AND #{endDate} AND cr.is_deleted=0 AND a.is_deleted=0 " +
            "GROUP BY a.card_id " +
            "ORDER BY total_amount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getUserRank(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("limit") Integer limit);

    @Select("SELECT merchant_id, SUM(amount) as total_amount " +
            "FROM consume_record " +
            "WHERE consume_time BETWEEN #{startDate} AND #{endDate} AND is_deleted=0 " +
            "GROUP BY merchant_id " +
            "ORDER BY total_amount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getMerchantRank(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("limit") Integer limit);
}
