package com.qms.campuscard.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.qms.campuscard.entity.OperationLog;
import com.qms.campuscard.entity.Student;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
     * 导出系统操作日志到Excel
     */
    public static void exportOperationLogs(HttpServletResponse response, List<OperationLog> logs) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("系统操作日志", StandardCharsets.UTF_8).replaceAll("\\+", "%");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        List<OperationLogExportDto> exportDtos = new ArrayList<>();
        for (OperationLog log : logs) {
            exportDtos.add(new OperationLogExportDto(log));
        }

        EasyExcel.write(response.getOutputStream(), OperationLogExportDto.class)
                .sheet("系统操作日志")
                .doWrite(exportDtos);
    }

    /**
     * 下载学生导入模板
     */
    public static void downloadImportTemplate(HttpServletResponse response) throws IOException {
        downloadImportTemplate(response, new LinkedHashMap<>());
    }

    /**
     * 下载学生导入模板，包含学院下拉和专业联动下拉。
     */
    public static void downloadImportTemplate(HttpServletResponse response, Map<String, List<String>> collegeMajorOptions) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("学生导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生导入");
            Sheet dictSheet = workbook.createSheet("学院专业字典");
            int hiddenSheetIndex = workbook.getSheetIndex(dictSheet);
            workbook.setSheetHidden(hiddenSheetIndex, true);

            CellStyle headerStyle = createTemplateHeaderStyle(workbook);
            CellStyle bodyStyle = createTemplateBodyStyle(workbook);

            String[] headers = {"学号", "姓名", "学院", "专业", "班级"};
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(28);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            List<String[]> examples = getStudentTemplateExamples();
            for (int rowIndex = 0; rowIndex < examples.size(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                row.setHeightInPoints(24);
                String[] values = examples.get(rowIndex);
                for (int colIndex = 0; colIndex < headers.length; colIndex++) {
                    Cell cell = row.createCell(colIndex);
                    cell.setCellValue(values[colIndex]);
                    cell.setCellStyle(bodyStyle);
                }
            }
            for (int rowIndex = examples.size() + 1; rowIndex <= 1000; rowIndex++) {
                Row row = sheet.createRow(rowIndex);
                row.setHeightInPoints(24);
                for (int colIndex = 0; colIndex < headers.length; colIndex++) {
                    row.createCell(colIndex).setCellStyle(bodyStyle);
                }
            }

            sheet.setColumnWidth(0, 14 * 256);
            sheet.setColumnWidth(1, 12 * 256);
            sheet.setColumnWidth(2, 28 * 256);
            sheet.setColumnWidth(3, 28 * 256);
            sheet.setColumnWidth(4, 14 * 256);
            sheet.createFreezePane(0, 1);

            buildCollegeMajorDictionary(workbook, dictSheet, collegeMajorOptions);
            addCollegeMajorValidation(sheet, collegeMajorOptions);

            workbook.write(response.getOutputStream());
        }
    }

    private static CellStyle createTemplateHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private static CellStyle createTemplateBodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    private static List<String[]> getStudentTemplateExamples() {
        List<String[]> examples = new ArrayList<>();
        examples.add(new String[]{"20260011", "林雨辰", "计算机与人工智能学院", "软件工程", "软工2301"});
        examples.add(new String[]{"20260012", "沈嘉宁", "计算机与人工智能学院", "数据科学与大数据技术", "大数据2301"});
        examples.add(new String[]{"20260013", "唐思远", "经济管理学院", "会计学", "会计2301"});
        examples.add(new String[]{"20260014", "许晨曦", "机电工程学院", "机械设计制造及其自动化", "机设2301"});
        examples.add(new String[]{"20260015", "顾安然", "外国语学院", "英语", "英语2302"});
        examples.add(new String[]{"20260016", "周明哲", "土木与水利工程学院", "工程管理", "工管2301"});
        return examples;
    }

    private static void buildCollegeMajorDictionary(Workbook workbook, Sheet dictSheet, Map<String, List<String>> collegeMajorOptions) {
        Row titleRow = dictSheet.createRow(0);
        titleRow.createCell(0).setCellValue("学院");
        titleRow.createCell(1).setCellValue("专业命名区域");
        titleRow.createCell(3).setCellValue("学院列表");

        int collegeIndex = 0;
        for (Map.Entry<String, List<String>> entry : collegeMajorOptions.entrySet()) {
            String rangeName = "MajorList_" + (collegeIndex + 1);
            int rowIndex = collegeIndex + 1;
            Row row = dictSheet.createRow(rowIndex);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(rangeName);
            row.createCell(3).setCellValue(entry.getKey());

            List<String> majors = entry.getValue() == null ? new ArrayList<>() : entry.getValue();
            int majorColumn = 5 + collegeIndex;
            for (int majorIndex = 0; majorIndex < majors.size(); majorIndex++) {
                Row majorRow = dictSheet.getRow(majorIndex + 1);
                if (majorRow == null) {
                    majorRow = dictSheet.createRow(majorIndex + 1);
                }
                majorRow.createCell(majorColumn).setCellValue(majors.get(majorIndex));
            }

            if (!majors.isEmpty()) {
                Name majorName = workbook.createName();
                majorName.setNameName(rangeName);
                majorName.setRefersToFormula("'学院专业字典'!" + toExcelColumn(majorColumn) + "$2:" + toExcelColumn(majorColumn) + "$" + (majors.size() + 1));
            }
            collegeIndex++;
        }

        if (!collegeMajorOptions.isEmpty()) {
            Name collegeListName = workbook.createName();
            collegeListName.setNameName("CollegeList");
            collegeListName.setRefersToFormula("'学院专业字典'!$D$2:$D$" + (collegeMajorOptions.size() + 1));

            Name collegeMajorMapName = workbook.createName();
            collegeMajorMapName.setNameName("CollegeMajorMap");
            collegeMajorMapName.setRefersToFormula("'学院专业字典'!$A$2:$B$" + (collegeMajorOptions.size() + 1));
        }
    }

    private static void addCollegeMajorValidation(Sheet sheet, Map<String, List<String>> collegeMajorOptions) {
        if (collegeMajorOptions == null || collegeMajorOptions.isEmpty()) {
            return;
        }
        DataValidationHelper helper = sheet.getDataValidationHelper();

        DataValidationConstraint collegeConstraint = helper.createFormulaListConstraint("CollegeList");
        CellRangeAddressList collegeRange = new CellRangeAddressList(1, 1000, 2, 2);
        DataValidation collegeValidation = helper.createValidation(collegeConstraint, collegeRange);
        collegeValidation.setSuppressDropDownArrow(true);
        collegeValidation.setShowErrorBox(true);
        collegeValidation.createErrorBox("学院选择错误", "请从下拉列表中选择系统已启用的学院");
        sheet.addValidationData(collegeValidation);

        DataValidationConstraint majorConstraint = helper.createFormulaListConstraint("INDIRECT(VLOOKUP($C2,CollegeMajorMap,2,FALSE))");
        CellRangeAddressList majorRange = new CellRangeAddressList(1, 1000, 3, 3);
        DataValidation majorValidation = helper.createValidation(majorConstraint, majorRange);
        majorValidation.setSuppressDropDownArrow(true);
        majorValidation.setShowErrorBox(true);
        majorValidation.createErrorBox("专业选择错误", "请先选择学院，再从下拉列表中选择该学院下的专业");
        sheet.addValidationData(majorValidation);
    }

    private static String toExcelColumn(int zeroBasedColumnIndex) {
        StringBuilder columnName = new StringBuilder();
        int dividend = zeroBasedColumnIndex + 1;
        while (dividend > 0) {
            int modulo = (dividend - 1) % 26;
            columnName.insert(0, (char) ('A' + modulo));
            dividend = (dividend - modulo) / 26;
        }
        return "$" + columnName;
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
     * 操作日志导出DTO类，控制导出字段和格式
     */
    public static class OperationLogExportDto {
        @ExcelProperty("ID")
        private Long id;

        @ExcelProperty("操作人ID")
        private Long operatorId;

        @ExcelProperty("操作人")
        private String operatorName;

        @ExcelProperty("操作类型")
        private String operationType;

        @ExcelProperty("操作表")
        private String targetTable;

        @ExcelProperty("目标ID")
        private Long targetId;

        @ExcelProperty("目标")
        private String targetName;

        @ExcelProperty("操作内容")
        private String content;

        @ExcelProperty("操作时间")
        @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        public OperationLogExportDto(OperationLog log) {
            this.id = log.getId();
            this.operatorId = log.getOperatorId();
            this.operatorName = log.getOperatorName();
            this.operationType = log.getOperationType();
            this.targetTable = log.getTargetTable();
            this.targetId = log.getTargetId();
            this.targetName = log.getTargetName();
            this.content = log.getContent();
            this.createTime = log.getCreateTime();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Long operatorId) {
            this.operatorId = operatorId;
        }

        public String getOperatorName() {
            return operatorName;
        }

        public void setOperatorName(String operatorName) {
            this.operatorName = operatorName;
        }

        public String getOperationType() {
            return operationType;
        }

        public void setOperationType(String operationType) {
            this.operationType = operationType;
        }

        public String getTargetTable() {
            return targetTable;
        }

        public void setTargetTable(String targetTable) {
            this.targetTable = targetTable;
        }

        public Long getTargetId() {
            return targetId;
        }

        public void setTargetId(Long targetId) {
            this.targetId = targetId;
        }

        public String getTargetName() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
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
