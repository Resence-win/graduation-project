package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.dto.StudentImportResult;
import com.qms.campuscard.dto.StudentRequest;
import com.qms.campuscard.entity.AdminUser;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.mapper.AdminUserMapper;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.mapper.TeacherMapper;
import com.qms.campuscard.service.AttendanceApplicationService;
import com.qms.campuscard.service.StudentService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentMapper studentMapper;
    
    @Resource
    private AdminUserMapper adminUserMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    @Lazy
    private AttendanceApplicationService attendanceApplicationService;

    @Override
    public boolean addStudent(Student student) {
        normalizeStudent(student);
        validateStudentBasicInfo(student);
        ensureStudentNoUnique(student.getStudentNo(), null);
        student.setStatus(1);
        student.setIsDeleted(0);
        student.setCreateTime(LocalDateTime.now());
        return studentMapper.insert(student) > 0;
    }
    
    @Override
    public boolean addStudent(StudentRequest studentRequest) {
        if (studentRequest == null) {
            throw new RuntimeException("学生信息不能为空");
        }
        if (studentRequest.getPassword() == null || studentRequest.getPassword().trim().length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        // 创建并保存学生信息
        Student student = new Student();
        student.setStudentNo(studentRequest.getStudentNo());
        student.setName(studentRequest.getName());
        student.setGender(studentRequest.getGender());
        student.setCollege(studentRequest.getCollege());
        student.setMajor(studentRequest.getMajor());
        student.setClassName(studentRequest.getClassName());
        student.setPhone(studentRequest.getPhone());
        student.setTeacherId(studentRequest.getTeacherId());
        student.setAttendanceMode(normalizeAttendanceMode(studentRequest.getAttendanceMode()));
        student.setAttendanceStatus(normalizeAttendanceStatus(studentRequest.getAttendanceStatus()));
        student.setInternshipCompany(studentRequest.getInternshipCompany());
        student.setStatus(1);
        student.setIsDeleted(0);
        student.setCreateTime(LocalDateTime.now());

        normalizeStudent(student);
        validateStudentBasicInfo(student);
        ensureStudentNoUnique(student.getStudentNo(), null);
        ensureLoginUsernameAvailable(student.getStudentNo());
        
        if (studentMapper.insert(student) <= 0) {
            return false;
        }

        // 创建并保存对应的登录用户
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(student.getStudentNo());
        adminUser.setPassword(getMD5(studentRequest.getPassword().trim()));
        adminUser.setRole("student");
        adminUser.setStatus(1);
        adminUser.setIsDeleted(0);
        adminUser.setCreateTime(LocalDateTime.now());

        return adminUserMapper.insert(adminUser) > 0;
    }
    
    // 添加MD5加密方法
    private String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IPage<Student> getStudentList(Page<Student> page, String studentNo, String name) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        
        if (studentNo != null && !studentNo.isEmpty()) {
            queryWrapper.like("student_no", studentNo);
        }
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        queryWrapper.orderByDesc("create_time");
        IPage<Student> result = studentMapper.selectPage(page, queryWrapper);
        refreshLeaveStatus(result.getRecords());
        fillTeacherName(result.getRecords());
        return result;
    }

    @Override
    public Student getStudentById(Long id) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("is_deleted", 0);
        Student student = studentMapper.selectOne(queryWrapper);
        refreshLeaveStatus(student);
        fillTeacherName(student);
        return student;
    }
    
    @Override
    public Student getStudentByStudentNo(String studentNo) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_no", studentNo);
        queryWrapper.eq("is_deleted", 0);
        Student student = studentMapper.selectOne(queryWrapper);
        refreshLeaveStatus(student);
        fillTeacherName(student);
        return student;
    }

    @Override
    public boolean updateStudent(Student student) {
        if (student == null || student.getId() == null) {
            throw new RuntimeException("学生ID不能为空");
        }
        normalizeStudent(student);
        validateStudentBasicInfo(student);
        ensureStudentNoUnique(student.getStudentNo(), student.getId());
        student.setAttendanceMode(normalizeAttendanceMode(student.getAttendanceMode()));
        student.setAttendanceStatus(normalizeAttendanceStatus(student.getAttendanceStatus()));
        student.setUpdateTime(LocalDateTime.now());
        return studentMapper.updateById(student) > 0;
    }

    private String normalizeAttendanceMode(String attendanceMode) {
        if (attendanceMode == null || attendanceMode.trim().isEmpty()) {
            return "CAMPUS";
        }
        return attendanceMode.trim().toUpperCase();
    }

    private String normalizeAttendanceStatus(String attendanceStatus) {
        if (attendanceStatus == null || attendanceStatus.trim().isEmpty()) {
            return "ON_CAMPUS";
        }
        return attendanceStatus.trim().toUpperCase();
    }

    private void fillTeacherName(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return;
        }
        for (Student student : students) {
            fillTeacherName(student);
        }
    }

    private void refreshLeaveStatus(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return;
        }
        for (Student student : students) {
            refreshLeaveStatus(student);
        }
    }

    private void refreshLeaveStatus(Student student) {
        if (student != null && student.getId() != null) {
            attendanceApplicationService.refreshLeaveStatusByStudentId(student.getId());
            Student refreshedStudent = studentMapper.selectById(student.getId());
            if (refreshedStudent != null) {
                student.setAttendanceStatus(refreshedStudent.getAttendanceStatus());
                student.setUpdateTime(refreshedStudent.getUpdateTime());
            }
        }
    }

    private void fillTeacherName(Student student) {
        if (student == null || student.getTeacherId() == null) {
            return;
        }
        Teacher teacher = teacherMapper.selectById(student.getTeacherId());
        if (teacher != null && teacher.getIsDeleted() != null && teacher.getIsDeleted() == 0) {
            student.setTeacherName(teacher.getName());
        }
    }

    @Override
    public boolean deleteStudent(Long id) {
        Student student = new Student();
        student.setId(id);
        student.setIsDeleted(1);
        student.setUpdateTime(LocalDateTime.now());
        return studentMapper.updateById(student) > 0;
    }

    @Override
    public StudentImportResult batchImportStudents(List<Student> students) {
        StudentImportResult result = new StudentImportResult();
        if (students == null || students.isEmpty()) {
            return result;
        }

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            int rowNumber = i + 2;
            result.increaseTotal();
            try {
                normalizeStudent(student);
                validateStudentImportInfo(student);

                Student existingStudent = getStudentByStudentNo(student.getStudentNo());
                if (existingStudent != null) {
                    result.increaseSkipped();
                    continue;
                }
                ensureImportLoginAccountAvailable(student.getStudentNo());

                student.setAttendanceMode(normalizeAttendanceMode(student.getAttendanceMode()));
                student.setAttendanceStatus(normalizeAttendanceStatus(student.getAttendanceStatus()));
                student.setStatus(1);
                student.setIsDeleted(0);
                student.setCreateTime(LocalDateTime.now());

                if (studentMapper.insert(student) <= 0) {
                    throw new RuntimeException("学生信息写入失败");
                }

                ensureStudentLoginAccount(student.getStudentNo());
                result.increaseSuccess();
            } catch (Exception e) {
                result.addFailure("第" + rowNumber + "行：" + e.getMessage());
            }
        }

        return result;
    }

    @Override
    public List<Student> getAllStudents() {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return studentMapper.selectList(queryWrapper);
    }

    @Override
    public void ensureStudentProfileComplete(Student student) {
        if (student == null || student.getIsDeleted() == null || student.getIsDeleted() != 0) {
            throw new RuntimeException("学生不存在");
        }
        if (isBlank(student.getName())
                || isBlank(student.getGender())
                || isBlank(student.getCollege())
                || isBlank(student.getMajor())
                || isBlank(student.getClassName())
                || isBlank(student.getPhone())) {
            throw new RuntimeException("请先完善姓名、性别、学院、专业、班级、手机号后再进行该操作");
        }
        if (!student.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("请先填写正确的手机号后再进行该操作");
        }
    }

    @Override
    public void ensureStudentProfileCompleteByCard(CampusCard campusCard) {
        if (campusCard == null || !"student".equalsIgnoreCase(campusCard.getUserType())) {
            return;
        }
        Student student = studentMapper.selectById(campusCard.getUserId());
        ensureStudentProfileComplete(student);
    }

    private void validateStudentBasicInfo(Student student) {
        if (student == null) {
            throw new RuntimeException("学生信息不能为空");
        }
        if (isBlank(student.getStudentNo())) {
            throw new RuntimeException("学号不能为空");
        }
        if (isBlank(student.getName())) {
            throw new RuntimeException("姓名不能为空");
        }
        if (isBlank(student.getGender())) {
            throw new RuntimeException("性别不能为空");
        }
        if (isBlank(student.getCollege())) {
            throw new RuntimeException("学院不能为空");
        }
        if (isBlank(student.getMajor())) {
            throw new RuntimeException("专业不能为空");
        }
        if (isBlank(student.getClassName())) {
            throw new RuntimeException("班级不能为空");
        }
        if (isBlank(student.getPhone())) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!student.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
    }

    private void validateStudentImportInfo(Student student) {
        if (student == null) {
            throw new RuntimeException("学生信息不能为空");
        }
        if (isBlank(student.getStudentNo())) {
            throw new RuntimeException("学号不能为空");
        }
        if (isBlank(student.getName())) {
            throw new RuntimeException("姓名不能为空");
        }
        if (isBlank(student.getCollege())) {
            throw new RuntimeException("学院不能为空");
        }
        if (isBlank(student.getMajor())) {
            throw new RuntimeException("专业不能为空");
        }
        if (isBlank(student.getClassName())) {
            throw new RuntimeException("班级不能为空");
        }
    }

    private void normalizeStudent(Student student) {
        if (student == null) {
            return;
        }
        student.setStudentNo(trimToNull(student.getStudentNo()));
        student.setName(trimToNull(student.getName()));
        student.setGender(trimToNull(student.getGender()));
        student.setCollege(trimToNull(student.getCollege()));
        student.setMajor(trimToNull(student.getMajor()));
        student.setClassName(trimToNull(student.getClassName()));
        student.setPhone(trimToNull(student.getPhone()));
        student.setInternshipCompany(trimToNull(student.getInternshipCompany()));
    }

    private void ensureStudentNoUnique(String studentNo, Long currentId) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_no", studentNo);
        queryWrapper.eq("is_deleted", 0);
        if (currentId != null) {
            queryWrapper.ne("id", currentId);
        }
        if (studentMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("学号已存在");
        }
    }

    private void ensureStudentLoginAccount(String studentNo) {
        AdminUser existingAdminUser = getActiveAdminUser(studentNo);

        if (existingAdminUser != null) {
            if (!"student".equals(existingAdminUser.getRole())) {
                throw new RuntimeException("登录账号已存在且不是学生角色");
            }
            return;
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(studentNo);
        adminUser.setPassword(getMD5("123456"));
        adminUser.setRole("student");
        adminUser.setStatus(1);
        adminUser.setIsDeleted(0);
        adminUser.setCreateTime(LocalDateTime.now());

        if (adminUserMapper.insert(adminUser) <= 0) {
            throw new RuntimeException("登录账号创建失败");
        }
    }

    private void ensureLoginUsernameAvailable(String username) {
        if (getActiveAdminUser(username) != null) {
            throw new RuntimeException("登录账号已存在");
        }
    }

    private void ensureImportLoginAccountAvailable(String username) {
        AdminUser existingAdminUser = getActiveAdminUser(username);
        if (existingAdminUser != null && !"student".equals(existingAdminUser.getRole())) {
            throw new RuntimeException("登录账号已存在且不是学生角色");
        }
    }

    private AdminUser getActiveAdminUser(String username) {
        QueryWrapper<AdminUser> adminUserQueryWrapper = new QueryWrapper<>();
        adminUserQueryWrapper.eq("username", username);
        adminUserQueryWrapper.eq("is_deleted", 0);
        return adminUserMapper.selectOne(adminUserQueryWrapper);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
