package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qms.campuscard.entity.AccessRecord;
import com.qms.campuscard.entity.AttendanceLocation;
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
import com.qms.campuscard.mapper.AttendanceLocationMapper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final String STATISTIC_LOCATION_EXPIRED_MESSAGE = "该考勤点已过期超过1个月，暂不支持统计查看";

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
    private AttendanceLocationMapper attendanceLocationMapper;

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

    @Override
    public Map<String, Object> getLibraryOverdueStatistics(String startDate, String endDate, Integer limit) {
        int safeLimit = sanitizeLimit(limit);

        Map<String, Object> data = new HashMap<>();
        data.put("currentOverdueCount", queryForLong(
                "SELECT COUNT(*) FROM borrow_record " +
                        "WHERE is_deleted = 0 AND status IN (1, 3) AND due_time IS NOT NULL AND due_time < CURRENT_TIMESTAMP"));
        data.put("borrowingCount", queryForLong(
                "SELECT COUNT(*) FROM borrow_record " +
                        "WHERE is_deleted = 0 AND status IN (1, 3)"));
        data.put("availableBookCount", queryForLong(
                "SELECT COUNT(*) FROM book WHERE is_deleted = 0 AND status = 1"));
        data.put("borrowRestrictionRank", queryBorrowRestrictionRank(safeLimit));
        return data;
    }

    @Override
    public Map<String, Object> getLibraryBookDetails(String type, Integer page, Integer size) {
        String safeType = StringUtils.hasText(type) ? type : "overdue";
        int safePage = sanitizePage(page);
        int safeSize = sanitizePageSize(size);
        int offset = (safePage - 1) * safeSize;

        Map<String, Object> data = new HashMap<>();
        data.put("type", safeType);
        data.put("page", safePage);
        data.put("size", safeSize);

        List<Object> params = new ArrayList<>();
        String fromSql;
        String selectSql;
        String orderSql;
        if ("available".equals(safeType)) {
            selectSql = "SELECT b.id AS book_id, b.book_name, b.author, b.collection_location, b.status, b.create_time ";
            fromSql = "FROM book b WHERE b.is_deleted = 0 AND b.status = 1";
            orderSql = " ORDER BY b.create_time DESC, b.id DESC";
        } else {
            String userNameExpr = buildUserNameExpr();
            selectSql = "SELECT br.id AS record_id, b.id AS book_id, b.book_name, b.author, b.collection_location, " +
                    "cc.card_no, cc.user_type, " + userNameExpr + " AS user_name, br.borrow_time, br.due_time, br.status, " +
                    "CASE WHEN br.due_time IS NOT NULL AND br.due_time < CURRENT_TIMESTAMP " +
                    "THEN GREATEST(1, CEIL(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - br.due_time)) / 86400.0)) ELSE 0 END AS overdue_days ";
            fromSql = "FROM borrow_record br " +
                    "LEFT JOIN campus_card cc ON br.card_id = cc.id AND cc.is_deleted = 0 " +
                    "LEFT JOIN student s ON cc.user_id = s.id AND cc.user_type = 'student' AND s.is_deleted = 0 " +
                    "LEFT JOIN teacher t ON cc.user_id = t.id AND cc.user_type = 'teacher' AND t.is_deleted = 0 " +
                    "LEFT JOIN book b ON br.book_id = b.id AND b.is_deleted = 0 " +
                    "WHERE br.is_deleted = 0 AND br.status IN (1, 3)";
            if ("overdue".equals(safeType)) {
                fromSql += " AND br.due_time IS NOT NULL AND br.due_time < CURRENT_TIMESTAMP";
            } else {
                safeType = "borrowing";
            }
            orderSql = " ORDER BY overdue_days DESC, br.borrow_time DESC, br.id DESC";
        }

        Long total = queryForLong("SELECT COUNT(*) " + fromSql, params);
        params.add(safeSize);
        params.add(offset);
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                selectSql + fromSql + orderSql + " LIMIT ? OFFSET ?",
                params.toArray());

        data.put("type", safeType);
        data.put("total", total);
        data.put("records", records);
        return data;
    }

    @Override
    public Map<String, Object> getCardCancelStatistics(String startDate, String endDate) {
        LocalDateTime startTime = parseStartTime(startDate);
        LocalDateTime endTime = parseEndTime(endDate);

        String cancelBase = " FROM card_change_record ccr " +
                "LEFT JOIN campus_card cc ON ccr.card_id = cc.id AND cc.is_deleted = 0 " +
                "WHERE ccr.is_deleted = 0 AND ccr.operation_type = '注销'";

        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(cancelBase);
        appendDateRange(where, params, "ccr.create_time", startTime, endTime);

        Map<String, Object> data = new HashMap<>();
        data.put("total", queryForLong("SELECT COUNT(*)" + where, params));

        String reasonExpr = "CASE " +
                "WHEN ccr.remark IS NULL OR TRIM(ccr.remark) = '' THEN '未填写' " +
                "WHEN ccr.remark LIKE '%新值:%' THEN TRIM(SPLIT_PART(ccr.remark, '新值:', 2)) " +
                "ELSE ccr.remark END";

        data.put("byReason", jdbcTemplate.queryForList(
                "SELECT " + reasonExpr + " AS reason, COUNT(*) AS count" + where +
                        " GROUP BY reason ORDER BY count DESC",
                params.toArray()));
        data.put("byDate", jdbcTemplate.queryForList(
                "SELECT TO_CHAR(ccr.create_time, 'YYYY-MM-DD') AS date, COUNT(*) AS count" + where +
                        " GROUP BY TO_CHAR(ccr.create_time, 'YYYY-MM-DD') ORDER BY date",
                params.toArray()));
        data.put("byUserType", jdbcTemplate.queryForList(
                "SELECT CASE cc.user_type WHEN 'student' THEN '学生' WHEN 'teacher' THEN '教师' ELSE '未知' END AS user_type, " +
                        "COUNT(*) AS count" + where + " GROUP BY user_type ORDER BY count DESC",
                params.toArray()));
        return data;
    }

    @Override
    public Map<String, Object> getCardCancelDetails(String startDate, String endDate, Integer page, Integer size) {
        LocalDateTime startTime = parseStartTime(startDate);
        LocalDateTime endTime = parseEndTime(endDate);
        int safePage = sanitizePage(page);
        int safeSize = sanitizePageSize(size);
        int offset = (safePage - 1) * safeSize;

        List<Object> params = new ArrayList<>();
        StringBuilder fromSql = new StringBuilder("FROM card_change_record ccr " +
                "LEFT JOIN campus_card cc ON ccr.card_id = cc.id AND cc.is_deleted = 0 " +
                "LEFT JOIN student s ON cc.user_id = s.id AND cc.user_type = 'student' AND s.is_deleted = 0 " +
                "LEFT JOIN teacher t ON cc.user_id = t.id AND cc.user_type = 'teacher' AND t.is_deleted = 0 " +
                "WHERE ccr.is_deleted = 0 AND ccr.operation_type = '注销'");
        appendDateRange(fromSql, params, "ccr.create_time", startTime, endTime);

        String reasonExpr = buildCancelReasonExpr();
        String userNoExpr = "CASE WHEN cc.user_type = 'student' THEN s.student_no " +
                "WHEN cc.user_type = 'teacher' THEN t.teacher_no ELSE '' END";
        String userNameExpr = "CASE WHEN cc.user_type = 'student' THEN COALESCE(s.name, '未知学生') " +
                "WHEN cc.user_type = 'teacher' THEN COALESCE(t.name, '未知教师') ELSE '未知用户' END";

        Long total = queryForLong("SELECT COUNT(*) " + fromSql, params);
        List<Object> listParams = new ArrayList<>(params);
        listParams.add(safeSize);
        listParams.add(offset);
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT ccr.id, cc.card_no, cc.user_type, " +
                        "CASE cc.user_type WHEN 'student' THEN '学生' WHEN 'teacher' THEN '教师' ELSE '未知' END AS user_type_label, " +
                        userNoExpr + " AS user_no, " + userNameExpr + " AS user_name, " +
                        reasonExpr + " AS cancel_reason, ccr.create_time AS cancel_time " +
                        fromSql + " ORDER BY ccr.create_time DESC, ccr.id DESC LIMIT ? OFFSET ?",
                listParams.toArray());

        Map<String, Object> data = new HashMap<>();
        data.put("page", safePage);
        data.put("size", safeSize);
        data.put("total", total);
        data.put("records", records);
        return data;
    }

    @Override
    public Map<String, Object> getWeeklyAttendanceStatistics(String startDateText, String endDateText, Long locationId) {
        validateStatisticLocationWithinRange(locationId);

        LocalDate endDate = StringUtils.hasText(endDateText) ? LocalDate.parse(endDateText) : LocalDate.now();
        LocalDate startDate = StringUtils.hasText(startDateText) ? LocalDate.parse(startDateText) : endDate.minusDays(6);
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        List<Object> params = new ArrayList<>();
        params.add(startTime);
        params.add(endTime);
        String locationCondition = "";
        if (locationId != null) {
            locationCondition = "AND location_id = ? ";
            params.add(locationId);
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT TO_CHAR(record_time, 'YYYY-MM-DD') AS date, status, COUNT(*) AS count " +
                        "FROM attendance_record " +
                        "WHERE is_deleted = 0 AND record_time >= ? AND record_time <= ? " +
                        locationCondition +
                        "AND status IN ('正常', '迟到', '早退', '缺勤') " +
                        "GROUP BY TO_CHAR(record_time, 'YYYY-MM-DD'), status " +
                        "ORDER BY date",
                params.toArray());

        Map<String, Map<String, Object>> dailyMap = new LinkedHashMap<>();
        for (LocalDate dateItem = startDate; !dateItem.isAfter(endDate); dateItem = dateItem.plusDays(1)) {
            String date = dateItem.toString();
            Map<String, Object> item = new HashMap<>();
            item.put("date", date);
            item.put("normal", 0L);
            item.put("late", 0L);
            item.put("early", 0L);
            item.put("absent", 0L);
            dailyMap.put(date, item);
        }

        for (Map<String, Object> row : rows) {
            String date = String.valueOf(row.get("date"));
            Map<String, Object> item = dailyMap.get(date);
            if (item == null) {
                continue;
            }
            String status = String.valueOf(row.get("status"));
            Object count = row.get("count");
            if ("正常".equals(status)) {
                item.put("normal", count);
            } else if ("迟到".equals(status)) {
                item.put("late", count);
            } else if ("早退".equals(status)) {
                item.put("early", count);
            } else if ("缺勤".equals(status)) {
                item.put("absent", count);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("startDate", startDate.toString());
        data.put("endDate", endDate.toString());
        data.put("daily", new ArrayList<>(dailyMap.values()));
        return data;
    }

    private void validateStatisticLocationWithinRange(Long locationId) {
        if (locationId == null) {
            return;
        }
        AttendanceLocation location = attendanceLocationMapper.selectById(locationId);
        if (location == null) {
            throw new RuntimeException("打卡位置不存在");
        }
        if (location.getEndTime() == null) {
            throw new RuntimeException("打卡位置未配置结束时间，暂不支持统计查看");
        }
        if (location.getEndTime().isBefore(LocalDateTime.now().minusMonths(1))) {
            throw new RuntimeException(STATISTIC_LOCATION_EXPIRED_MESSAGE);
        }
    }

    @Override
    public Map<String, Object> getCommuteStatistics(String startDate, String endDate) {
        LocalDateTime startTime = parseStartTime(startDate);
        LocalDateTime endTime = parseEndTime(endDate);

        List<Object> params = new ArrayList<>();
        StringBuilder instanceSql = new StringBuilder(
                "SELECT TO_CHAR(cr.ride_time, 'YYYY-MM-DD') AS ride_date, cr.route_id, cr.vehicle_id, cr.schedule_id, " +
                        "COUNT(*) AS ride_count, COALESCE(MAX(cv.seat_count), 0) AS seat_count " +
                        "FROM commute_record cr " +
                        "LEFT JOIN commute_vehicle cv ON cr.vehicle_id = cv.id AND cv.is_deleted = 0 " +
                        "WHERE cr.is_deleted = 0");
        appendDateRange(instanceSql, params, "cr.ride_time", startTime, endTime);
        instanceSql.append(" GROUP BY TO_CHAR(cr.ride_time, 'YYYY-MM-DD'), cr.route_id, cr.vehicle_id, cr.schedule_id");

        String instanceSubQuery = instanceSql.toString();

        Map<String, Object> data = new HashMap<>();
        data.put("totalRideCount", queryForLong("SELECT COALESCE(SUM(ride_count), 0) FROM (" + instanceSubQuery + ") x", params));
        data.put("byRoute", jdbcTemplate.queryForList(
                "SELECT cr.route_id, COALESCE(route.route_name, '未知线路') AS route_name, " +
                        "SUM(cr.ride_count) AS ride_count, SUM(cr.seat_count) AS seat_count, " +
                        "GREATEST(SUM(cr.seat_count) - SUM(cr.ride_count), 0) AS empty_seat_count, " +
                        "CASE WHEN SUM(cr.seat_count) > 0 THEN ROUND(GREATEST(SUM(cr.seat_count) - SUM(cr.ride_count), 0) * 100.0 / SUM(cr.seat_count), 2) ELSE 0 END AS vacancy_rate " +
                        "FROM (" + instanceSubQuery + ") cr " +
                        "LEFT JOIN commute_route route ON cr.route_id = route.id AND route.is_deleted = 0 " +
                        "GROUP BY cr.route_id, route.route_name ORDER BY ride_count DESC",
                params.toArray()));
        return data;
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

    private List<Map<String, Object>> queryBorrowRestrictionRank(Integer limit) {
        String userNameExpr = buildUserNameExpr();
        String userNoExpr = "CASE WHEN cc.user_type = 'student' THEN s.student_no " +
                "WHEN cc.user_type = 'teacher' THEN t.teacher_no ELSE '' END";

        String sql = "WITH active_restriction AS (" +
                "SELECT cc.id AS card_id, cc.card_no, cc.user_type, " + userNoExpr + " AS user_no, " +
                userNameExpr + " AS user_name, " +
                "GREATEST(1, CEIL(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - br.due_time)) / 86400.0)) AS remaining_restricted_days " +
                "FROM borrow_record br " +
                "LEFT JOIN campus_card cc ON br.card_id = cc.id AND cc.is_deleted = 0 " +
                "LEFT JOIN student s ON cc.user_id = s.id AND cc.user_type = 'student' AND s.is_deleted = 0 " +
                "LEFT JOIN teacher t ON cc.user_id = t.id AND cc.user_type = 'teacher' AND t.is_deleted = 0 " +
                "WHERE br.is_deleted = 0 AND br.status IN (1, 3) AND br.due_time IS NOT NULL AND br.due_time < CURRENT_TIMESTAMP" +
                "), returned_base AS (" +
                "SELECT cc.id AS card_id, cc.card_no, cc.user_type, " + userNoExpr + " AS user_no, " +
                userNameExpr + " AS user_name, br.return_time, " +
                "GREATEST(1, CEIL(EXTRACT(EPOCH FROM (br.return_time - br.due_time)) / 86400.0)) AS overdue_days " +
                "FROM borrow_record br " +
                "LEFT JOIN campus_card cc ON br.card_id = cc.id AND cc.is_deleted = 0 " +
                "LEFT JOIN student s ON cc.user_id = s.id AND cc.user_type = 'student' AND s.is_deleted = 0 " +
                "LEFT JOIN teacher t ON cc.user_id = t.id AND cc.user_type = 'teacher' AND t.is_deleted = 0 " +
                "WHERE br.is_deleted = 0 AND br.status = 2 AND br.due_time IS NOT NULL " +
                "AND br.return_time IS NOT NULL AND br.return_time > br.due_time" +
                "), returned_restriction AS (" +
                "SELECT card_id, card_no, user_type, user_no, user_name, " +
                "GREATEST(1, CEIL(EXTRACT(EPOCH FROM ((return_time + CAST(overdue_days AS INTEGER) * INTERVAL '1 day') - CURRENT_TIMESTAMP)) / 86400.0)) AS remaining_restricted_days " +
                "FROM returned_base WHERE return_time + CAST(overdue_days AS INTEGER) * INTERVAL '1 day' > CURRENT_TIMESTAMP" +
                ") SELECT card_id, card_no, user_type, user_no, user_name, MAX(remaining_restricted_days) AS remaining_restricted_days " +
                "FROM (SELECT * FROM active_restriction UNION ALL SELECT * FROM returned_restriction) restricted " +
                "GROUP BY card_id, card_no, user_type, user_no, user_name " +
                "ORDER BY remaining_restricted_days DESC, user_name ASC LIMIT ?";
        return jdbcTemplate.queryForList(sql, limit);
    }

    private String buildUserNameExpr() {
        return "CASE " +
                "WHEN cc.user_type = 'student' THEN COALESCE(s.student_no || ' - ' || s.name, s.name, '未知学生') " +
                "WHEN cc.user_type = 'teacher' THEN COALESCE(t.teacher_no || ' - ' || t.name, t.name, '未知教师') " +
                "ELSE '未知用户' END";
    }

    private String buildCancelReasonExpr() {
        return "CASE " +
                "WHEN ccr.remark IS NULL OR TRIM(ccr.remark) = '' THEN '未填写' " +
                "WHEN ccr.remark LIKE '%新值:%' THEN TRIM(SPLIT_PART(ccr.remark, '新值:', 2)) " +
                "ELSE ccr.remark END";
    }

    private void appendDateRange(StringBuilder sql, List<Object> params, String timeColumn, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null) {
            sql.append(" AND ").append(timeColumn).append(" >= ?");
            params.add(startTime);
        }
        if (endTime != null) {
            sql.append(" AND ").append(timeColumn).append(" <= ?");
            params.add(endTime);
        }
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 10;
        }
        return Math.min(limit, 50);
    }

    private int sanitizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private int sanitizePageSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private Long queryForLong(String sql) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class);
        return result == null ? 0L : result;
    }

    private Long queryForLong(String sql, List<Object> params) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class, params.toArray());
        return result == null ? 0L : result;
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
