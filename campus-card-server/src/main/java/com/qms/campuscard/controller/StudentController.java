package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.service.StudentService;
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
    public Result<Boolean> addStudent(@RequestBody Student student) {
        boolean success = studentService.addStudent(student);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "student", student.getId(), "新增学生：" + student.getName());
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
}
