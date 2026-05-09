package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qms.campuscard.entity.AccessRecord;
import com.qms.campuscard.entity.AttendanceRecord;
import com.qms.campuscard.entity.Book;
import com.qms.campuscard.entity.BorrowRecord;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.CommuteRecord;
import com.qms.campuscard.entity.ConsumeRecord;
import com.qms.campuscard.entity.Merchant;
import com.qms.campuscard.entity.Product;
import com.qms.campuscard.entity.RechargeRecord;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.mapper.AccessRecordMapper;
import com.qms.campuscard.mapper.AttendanceRecordMapper;
import com.qms.campuscard.mapper.BookMapper;
import com.qms.campuscard.mapper.BorrowRecordMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.CommuteRecordMapper;
import com.qms.campuscard.mapper.ConsumeRecordMapper;
import com.qms.campuscard.mapper.MerchantMapper;
import com.qms.campuscard.mapper.ProductMapper;
import com.qms.campuscard.mapper.RechargeRecordMapper;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.mapper.TeacherMapper;
import com.qms.campuscard.service.StatisticsService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private ConsumeRecordMapper consumeRecordMapper;

    @Resource
    private RechargeRecordMapper rechargeRecordMapper;

    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private BookMapper bookMapper;

    @Resource
    private BorrowRecordMapper borrowRecordMapper;

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

    @Resource
    private AccessRecordMapper accessRecordMapper;

    @Resource
    private CommuteRecordMapper commuteRecordMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> getOverview(String startDate, String endDate) {
        LocalDateTime startTime = parseStartTime(startDate);
        LocalDateTime endTime = parseEndTime(endDate);

        Map<String, Object> overview = new HashMap<>();
        overview.put("startDate", startDate);
        overview.put("endDate", endDate);
        overview.put("studentCount", countNotDeleted(studentMapper, Student.class));
        overview.put("teacherCount", countNotDeleted(teacherMapper, Teacher.class));
        overview.put("merchantCount", countNotDeleted(merchantMapper, Merchant.class));
        overview.put("productCount", countNotDeleted(productMapper, Product.class));
        overview.put("bookCount", countNotDeleted(bookMapper, Book.class));

        overview.put("cardTotal", countNotDeleted(campusCardMapper, CampusCard.class));
        overview.put("activeCardCount", campusCardMapper.selectCount(baseQuery(CampusCard.class).eq("status", 1)));
        overview.put("lostCardCount", campusCardMapper.selectCount(baseQuery(CampusCard.class).eq("status", 2)));
        overview.put("cancelledCardCount", campusCardMapper.selectCount(baseQuery(CampusCard.class).eq("status", 0)));

        QueryWrapper<ConsumeRecord> consumeQuery = baseQuery(ConsumeRecord.class);
        applyDateRange(consumeQuery, "consume_time", startTime, endTime);
        overview.put("consumeCount", consumeRecordMapper.selectCount(consumeQuery));
        overview.put("consumeAmount", sumAmount("consume_record", "amount", "consume_time", startTime, endTime));

        QueryWrapper<RechargeRecord> rechargeQuery = baseQuery(RechargeRecord.class);
        applyDateRange(rechargeQuery, "create_time", startTime, endTime);
        overview.put("rechargeCount", rechargeRecordMapper.selectCount(rechargeQuery));
        overview.put("rechargeAmount", sumAmount("recharge_record", "amount", "create_time", startTime, endTime));

        QueryWrapper<BorrowRecord> borrowQuery = baseQuery(BorrowRecord.class);
        applyDateRange(borrowQuery, "borrow_time", startTime, endTime);
        overview.put("borrowCount", borrowRecordMapper.selectCount(borrowQuery));
        overview.put("borrowingCount", borrowRecordMapper.selectCount(baseQuery(BorrowRecord.class).eq("status", 1)));
        overview.put("overdueBorrowCount", borrowRecordMapper.selectCount(baseQuery(BorrowRecord.class).in("status", 1, 3).lt("due_time", LocalDateTime.now())));

        QueryWrapper<AttendanceRecord> attendanceQuery = baseQuery(AttendanceRecord.class);
        applyDateRange(attendanceQuery, "record_time", startTime, endTime);
        overview.put("attendanceCount", attendanceRecordMapper.selectCount(attendanceQuery));
        overview.put("normalAttendanceCount", attendanceRecordMapper.selectCount(cloneDateQuery(AttendanceRecord.class, "record_time", startTime, endTime).eq("status", "正常")));
        overview.put("abnormalAttendanceCount", attendanceRecordMapper.selectCount(cloneDateQuery(AttendanceRecord.class, "record_time", startTime, endTime).ne("status", "正常")));

        QueryWrapper<AccessRecord> accessQuery = baseQuery(AccessRecord.class);
        applyDateRange(accessQuery, "access_time", startTime, endTime);
        overview.put("accessCount", accessRecordMapper.selectCount(accessQuery));
        overview.put("accessSuccessCount", accessRecordMapper.selectCount(cloneDateQuery(AccessRecord.class, "access_time", startTime, endTime).eq("status", "成功")));

        QueryWrapper<CommuteRecord> commuteQuery = baseQuery(CommuteRecord.class);
        applyDateRange(commuteQuery, "ride_time", startTime, endTime);
        overview.put("commuteRideCount", commuteRecordMapper.selectCount(commuteQuery));

        return overview;
    }

    @Override
    public List<Map<String, Object>> getConsumeStatistics(String startDate, String endDate) {
        return consumeRecordMapper.getConsumeStatistics(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getUserRank(String startDate, String endDate, Integer limit) {
        return consumeRecordMapper.getUserRank(startDate, endDate, limit);
    }

    @Override
    public List<Map<String, Object>> getMerchantRank(String startDate, String endDate, Integer limit) {
        return consumeRecordMapper.getMerchantRank(startDate, endDate, limit);
    }

    private LocalDateTime parseStartTime(String date) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        return LocalDate.parse(date).atStartOfDay();
    }

    private LocalDateTime parseEndTime(String date) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        return LocalDate.parse(date).atTime(LocalTime.MAX);
    }

    private <T> QueryWrapper<T> baseQuery(Class<T> entityClass) {
        return new QueryWrapper<T>().eq("is_deleted", 0);
    }

    private <T> Long countNotDeleted(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper, Class<T> entityClass) {
        return mapper.selectCount(baseQuery(entityClass));
    }

    private <T> QueryWrapper<T> cloneDateQuery(Class<T> entityClass, String timeColumn, LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<T> query = baseQuery(entityClass);
        applyDateRange(query, timeColumn, startTime, endTime);
        return query;
    }

    private <T> void applyDateRange(QueryWrapper<T> query, String timeColumn, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null) {
            query.ge(timeColumn, startTime);
        }
        if (endTime != null) {
            query.le(timeColumn, endTime);
        }
    }

    private BigDecimal sumAmount(String tableName, String amountColumn, String timeColumn, LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(")
                .append(amountColumn)
                .append("), 0) FROM ")
                .append(tableName)
                .append(" WHERE is_deleted = 0");
        List<Object> params = new ArrayList<>();
        if (startTime != null) {
            sql.append(" AND ").append(timeColumn).append(" >= ?");
            params.add(startTime);
        }
        if (endTime != null) {
            sql.append(" AND ").append(timeColumn).append(" <= ?");
            params.add(endTime);
        }
        BigDecimal result = jdbcTemplate.queryForObject(sql.toString(), BigDecimal.class, params.toArray());
        return result == null ? BigDecimal.ZERO : result;
    }
}
