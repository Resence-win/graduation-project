package com.qms.campuscard.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.qms.campuscard.entity.Student;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    /**
     * 导出学生信息到Excel
     */
    public static void exportStudents(HttpServletResponse response, List<Student> students) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("学生信息", StandardCharsets.UTF_8).replaceAll("\\+", "%");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        
        // 转换为导出DTO
        List<StudentExportDto> exportDtos = new ArrayList<>();
        for (Student student : students) {
            exportDtos.add(new StudentExportDto(student));
        }
        
        // 导出Excel
        EasyExcel.write(response.getOutputStream(), StudentExportDto.class)
                .sheet("学生信息")
                .doWrite(exportDtos);
    }

    /**
     * 下载学生导入模板
     */
    public static void downloadImportTemplate(HttpServletResponse response) throws IOException {
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("学生导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        
        // 导出模板（包含表头但无数据）
        EasyExcel.write(response.getOutputStream(), StudentImportDto.class)
                .sheet("学生导入模板")
                .doWrite(new ArrayList<>());
    }

    /**
     * 导入学生信息从Excel
     */
    public static List<Student> importStudents(InputStream inputStream) {
        List<Student> students = new ArrayList<>();
        
        EasyExcel.read(inputStream, Student.class, new AnalysisEventListener<Student>() {
            @Override
            public void invoke(Student student, AnalysisContext context) {
                students.add(student);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 所有数据解析完成
            }
        }).sheet().doRead();
        
        return students;
    }

    /**
     * 导入学生信息从Excel（简化版，只包含必要字段）
     */
    public static List<Student> importStudentsSimple(InputStream inputStream) {
        List<Student> students = new ArrayList<>();
        
        EasyExcel.read(inputStream, StudentImportDto.class, new AnalysisEventListener<StudentImportDto>() {
            @Override
            public void invoke(StudentImportDto dto, AnalysisContext context) {
                Student student = new Student();
                student.setStudentNo(dto.getStudentNo());
                student.setName(dto.getName());
                student.setCollege(dto.getCollege());
                student.setMajor(dto.getMajor());
                student.setClassName(dto.getClassName());
                students.add(student);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 所有数据解析完成
            }
        }).sheet().doRead();
        
        return students;
    }

    /**
     * 学生导入DTO类，只包含必要字段
     */
    public static class StudentImportDto {
        @ExcelProperty("学号")
        private String studentNo; // 学号
        @ExcelProperty("姓名")
        private String name; // 姓名
        @ExcelProperty("学院")
        private String college; // 学院
        @ExcelProperty("专业")
        private String major; // 专业
        @ExcelProperty("班级")
        private String className; // 班级

        public String getStudentNo() {
            return studentNo;
        }

        public void setStudentNo(String studentNo) {
            this.studentNo = studentNo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCollege() {
            return college;
        }

        public void setCollege(String college) {
            this.college = college;
        }

        public String getMajor() {
            return major;
        }

        public void setMajor(String major) {
            this.major = major;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }

    /**
     * 学生导出DTO类，控制导出字段和格式
     */
    public static class StudentExportDto {
        @ExcelProperty("ID")
        private Long id;

        @ExcelProperty("学号")
        private String studentNo;

        @ExcelProperty("姓名")
        private String name;

        @ExcelProperty("性别")
        private String gender;

        @ExcelProperty("学院")
        private String college;

        @ExcelProperty("专业")
        private String major;

        @ExcelProperty("班级")
        private String className;

        @ExcelProperty("手机号")
        private String phone;

        @ExcelProperty("状态")
        private String status;

        @ExcelProperty("创建时间")
        @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        // 构造方法，从Student实体转换
        public StudentExportDto(Student student) {
            this.id = student.getId();
            this.studentNo = student.getStudentNo();
            this.name = student.getName();
            this.gender = student.getGender();
            this.college = student.getCollege();
            this.major = student.getMajor();
            this.className = student.getClassName();
            this.phone = student.getPhone();
            this.status = student.getStatus() == 1 ? "正常" : "异常";
            this.createTime = student.getCreateTime();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getStudentNo() {
            return studentNo;
        }

        public void setStudentNo(String studentNo) {
            this.studentNo = studentNo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getCollege() {
            return college;
        }

        public void setCollege(String college) {
            this.college = college;
        }

        public String getMajor() {
            return major;
        }

        public void setMajor(String major) {
            this.major = major;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }
}
