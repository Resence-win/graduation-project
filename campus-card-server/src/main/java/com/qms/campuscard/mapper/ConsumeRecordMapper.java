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

    @Select("SELECT a.card_id, c.user_id, c.user_type, " +
            "CASE " +
            "WHEN c.user_type = 'student' THEN s.name " +
            "WHEN c.user_type = 'teacher' THEN t.name " +
            "ELSE '未知用户' " +
            "END as user_name, " +
            "SUM(cr.amount) as total_amount " +
            "FROM consume_record cr " +
            "JOIN account a ON cr.account_id = a.id " +
            "JOIN campus_card c ON a.card_id = c.id " +
            "LEFT JOIN student s ON c.user_id = s.id AND c.user_type = 'student' " +
            "LEFT JOIN teacher t ON c.user_id = t.id AND c.user_type = 'teacher' " +
            "WHERE cr.consume_time BETWEEN #{startDate} AND #{endDate} AND cr.is_deleted=0 AND a.is_deleted=0 AND c.is_deleted=0 " +
            "GROUP BY a.card_id, c.user_id, c.user_type, user_name " +
            "ORDER BY total_amount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getUserRank(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("limit") Integer limit);

    @Select("SELECT cr.merchant_id, m.merchant_name, SUM(cr.amount) as total_amount " +
            "FROM consume_record cr " +
            "JOIN merchant m ON cr.merchant_id = m.id " +
            "WHERE cr.consume_time BETWEEN #{startDate} AND #{endDate} AND cr.is_deleted=0 AND m.is_deleted=0 " +
            "GROUP BY cr.merchant_id, m.merchant_name " +
            "ORDER BY total_amount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getMerchantRank(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("limit") Integer limit);
}
