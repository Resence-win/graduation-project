package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.StudentRequest;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.service.StudentService;
import com.qms.campuscard.util.ExcelUtil;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    @Resource
    private LogUtil logUtil;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public Result<Boolean> addStudent(@RequestBody StudentRequest studentRequest) {
        boolean success = studentService.addStudent(studentRequest);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "student", null, "新增学生：" + studentRequest.getName());
            return Result.success("添加成功", true);
        } else {
            return Result.error("添加失败");
        }
    }

    @GetMapping("/list")
    public Result<IPage<Student>> getStudentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String name) {
        Page<Student> pageParam = new Page<>(page, size);
        IPage<Student> studentPage = studentService.getStudentList(pageParam, studentNo, name);
        // 记录日志
        logUtil.recordLog(1L, "查询", "student", null, "查询学生列表");
        return Result.success(studentPage);
    }

    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        if (student != null) {
            // 记录日志
            logUtil.recordLog(1L, "查询", "student", id, "查询学生详情：" + student.getName());
            return Result.success(student);
        } else {
            return Result.error("学生不存在");
        }
    }
    
    @GetMapping("/by-no/{studentNo}")
    public Result<Student> getStudentByStudentNo(@PathVariable String studentNo) {
        Student student = studentService.getStudentByStudentNo(studentNo);
        if (student != null) {
            // 记录日志
            logUtil.recordLog(1L, "查询", "student", null, "根据学号查询学生详情：" + student.getName());
            return Result.success(student);
        } else {
            return Result.error("学生不存在");
        }
    }

    @PutMapping
    public Result<Boolean> updateStudent(@RequestBody Student student) {
        boolean success = studentService.updateStudent(student);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "student", student.getId(), "修改学生：" + student.getName());
            return Result.success("更新成功", true);
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteStudent(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        boolean success = studentService.deleteStudent(id);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "删除", "student", id, "删除学生：" + (student != null ? student.getName() : "未知"));
            return Result.success("删除成功", true);
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 导出所有学生信息
     */
    @GetMapping("/export")
    public void exportStudents(jakarta.servlet.http.HttpServletResponse response) {
        try {
            List<Student> students = studentService.getAllStudents();
            ExcelUtil.exportStudents(response, students);
            // 记录日志
            logUtil.recordLog(1L, "导出", "student", null, "导出学生信息，共" + students.size() + "条");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量导入学生信息
     */
    @PostMapping("/import")
    public Result<Boolean> importStudents(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            List<Student> students = ExcelUtil.importStudentsSimple(file.getInputStream());
            boolean success = studentService.batchImportStudents(students);
            if (success) {
                logUtil.recordLog(1L, "导入", "student", null, "导入学生信息，共" + students.size() + "条");
                return Result.success("导入成功", true);
            } else {
                return Result.error("导入失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/template/download")
    public void downloadImportTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try {
            ExcelUtil.downloadImportTemplate(response);
            logUtil.recordLog(1L, "下载", "student", null, "下载学生导入模板");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
