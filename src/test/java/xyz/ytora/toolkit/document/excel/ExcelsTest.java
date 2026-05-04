package xyz.ytora.toolkit.document.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelsTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteAndReadXlsxMaps() {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("name", "ytora");
        row.put("age", 18);
        row.put("enabled", true);
        data.add(row);

        byte[] bytes = Excels.write(data);
        List<Map<String, Object>> result = Excels.read(new ByteArrayInputStream(bytes));

        assertEquals(1, result.size());
        assertEquals("ytora", result.get(0).get("name"));
        assertEquals(18, result.get(0).get("age"));
        assertEquals(true, result.get(0).get("enabled"));
    }

    @Test
    void shouldReadXlsWithoutCallerSpecifyingVersion() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("users");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("name");
            header.createCell(1).setCellValue("age");

            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue("ytora");
            data.createCell(1).setCellValue(18);

            workbook.write(outputStream);
        }

        List<Map<String, Object>> result = Excels.read(new ByteArrayInputStream(outputStream.toByteArray()));

        assertEquals(1, result.size());
        assertEquals("ytora", result.get(0).get("name"));
        assertEquals(18, result.get(0).get("age"));
    }

    @Test
    void shouldWriteXlsByFileExtension() {
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("name", "ytora");
        row.put("age", 18);
        data.add(row);

        File file = tempDir.resolve("users.xls").toFile();
        Excels.write(data, file);

        List<Map<String, Object>> result = Excels.read(file);
        assertEquals(1, result.size());
        assertEquals("ytora", result.get(0).get("name"));
        assertEquals(18, result.get(0).get("age"));
    }

    @Test
    void shouldWriteAndReadBeans() {
        User user = new User();
        user.setName("ytora");
        user.setAge(18);
        user.setEnabled(true);
        user.setBirthday(LocalDate.of(2026, 4, 21));

        List<User> users = new ArrayList<User>();
        users.add(user);

        byte[] bytes = Excels.writeBeans(users);
        List<User> result = Excels.readBeans(new ByteArrayInputStream(bytes), User.class);

        assertEquals(1, result.size());
        assertEquals("ytora", result.get(0).getName());
        assertEquals(18, result.get(0).getAge());
        assertTrue(result.get(0).isEnabled());
        assertEquals(LocalDate.of(2026, 4, 21), result.get(0).getBirthday());
    }

    @Test
    void shouldHandleEmptyData() {
        byte[] bytes = Excels.write(new ArrayList<Map<String, Object>>());
        List<Map<String, Object>> result = Excels.read(new ByteArrayInputStream(bytes));

        assertTrue(result.isEmpty());
        assertFalse(bytes.length == 0);
    }

    @Test
    void shouldWriteEmptyMapsWithExplicitColumns() throws Exception {
        List<ExcelColumn> columns = Arrays.asList(
                new ExcelColumn("name", "姓名", 20, ""),
                new ExcelColumn("age", "年龄", 20, "0")
        );

        byte[] bytes = Excels.write(new ArrayList<Map<String, Object>>(), columns);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            assertEquals("姓名", header.getCell(0).getStringCellValue());
            assertEquals("年龄", header.getCell(1).getStringCellValue());
            assertEquals(null, sheet.getRow(1));
        }
    }

    @Test
    void shouldUseExcelAnnotationWhenWritingAndReadingBeans() throws Exception {
        AnnotatedUser user = new AnnotatedUser();
        user.setName("ytora");
        user.setAge(18);
        user.setBirthday(LocalDate.of(2026, 4, 21));
        user.setSecret("skip");

        List<AnnotatedUser> users = new ArrayList<AnnotatedUser>();
        users.add(user);

        byte[] bytes = Excels.writeBeans(users);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(1);
            assertEquals("年龄", header.getCell(0).getStringCellValue());
            assertEquals("姓名", header.getCell(1).getStringCellValue());
            assertEquals("生日", header.getCell(2).getStringCellValue());
            assertEquals(30 * 256, sheet.getColumnWidth(1));

            CellStyle headerStyle = header.getCell(0).getCellStyle();
            assertEquals(HorizontalAlignment.CENTER, headerStyle.getAlignment());
            assertEquals(BorderStyle.THIN, headerStyle.getBorderBottom());
            assertTrue(workbook.getFontAt(headerStyle.getFontIndex()).getBold());

            CellStyle bodyStyle = sheet.getRow(2).getCell(0).getCellStyle();
            assertEquals(HorizontalAlignment.CENTER, bodyStyle.getAlignment());
            assertEquals(BorderStyle.THIN, bodyStyle.getBorderBottom());
        }

        List<AnnotatedUser> result = Excels.readBeans(new ByteArrayInputStream(bytes), AnnotatedUser.class);

        assertEquals(1, result.size());
        assertEquals("ytora", result.get(0).getName());
        assertEquals(18, result.get(0).getAge());
        assertEquals(LocalDate.of(2026, 4, 21), result.get(0).getBirthday());
        assertEquals(null, result.get(0).getSecret());
    }

    @Test
    void shouldWriteEmptyBeansWithExplicitBeanClass() throws Exception {
        byte[] bytes = Excels.writeBeans(new ArrayList<AnnotatedUser>(), AnnotatedUser.class);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(1);
            assertEquals("年龄", header.getCell(0).getStringCellValue());
            assertEquals("姓名", header.getCell(1).getStringCellValue());
            assertEquals("生日", header.getCell(2).getStringCellValue());
            assertEquals(null, sheet.getRow(2));
        }
    }

    @Test
    void shouldUseAnnotatedFileNameWhenWritingBeansToDirectory() throws Exception {
        AnnotatedUser user = new AnnotatedUser();
        user.setName("ytora");

        List<AnnotatedUser> users = new ArrayList<AnnotatedUser>();
        users.add(user);

        Excels.writeBeans(users, tempDir.toFile());

        File file = tempDir.resolve("annotated-users.xlsx").toFile();
        assertTrue(file.exists());

        try (FileInputStream inputStream = new FileInputStream(file)) {
            List<AnnotatedUser> result = Excels.readBeans(inputStream, AnnotatedUser.class);
            assertEquals(1, result.size());
            assertEquals("ytora", result.get(0).getName());
        }
    }

    @Test
    void shouldReadBeansFromAnnotatedStartRow() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("users");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("姓名");
            header.createCell(1).setCellValue("年龄");

            Row skipped = sheet.createRow(1);
            skipped.createCell(0).setCellValue("说明行");
            skipped.createCell(1).setCellValue("不应读取");

            Row data = sheet.createRow(2);
            data.createCell(0).setCellValue("ytora");
            data.createCell(1).setCellValue(18);

            workbook.write(outputStream);
        }

        List<StartRowUser> result = Excels.readBeans(
                new ByteArrayInputStream(outputStream.toByteArray()),
                StartRowUser.class
        );

        assertEquals(1, result.size());
        assertEquals("ytora", result.get(0).getName());
        assertEquals(18, result.get(0).getAge());
    }

    static class User {

        private String name;

        private int age;

        private boolean enabled;

        private LocalDate birthday;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }
    }

    @Excel(value = "annotated-users.xlsx", headerRowIndex = 1)
    static class AnnotatedUser {

        @Excel(value = "姓名", index = 1, width = 30)
        private String name;

        @Excel(value = "年龄", index = 0, format = "0")
        private int age;

        @Excel(value = "生日", index = 2, format = "yyyy-mm-dd")
        private LocalDate birthday;

        @Excel(ignore = true)
        private String secret;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(LocalDate birthday) {
            this.birthday = birthday;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    @Excel(headerRowIndex = 0, startRow = 2)
    static class StartRowUser {

        @Excel("姓名")
        private String name;

        @Excel("年龄")
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
