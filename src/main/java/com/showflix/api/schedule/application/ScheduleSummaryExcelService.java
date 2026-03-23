package com.showflix.api.schedule.application;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Application Layer - 월별 출근시간 Excel 생성 서비스
 */
@Service
public class ScheduleSummaryExcelService {

    private static final String[] DAY_OF_WEEK_KR = {"일", "월", "화", "수", "목", "금", "토"};

    /**
     * 월별 출근시간 현황 Excel 생성
     */
    public byte[] generate(int year, int month, ScheduleSummaryService.MonthResult result) {
        int daysInMonth = result.daysInMonth();
        List<ScheduleSummaryService.UserInfo> users = result.users();
        Map<String, Map<String, String>> data = result.data();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(year + "년 " + month + "월 출근시간");
            sheet.createFreezePane(1, 2); // 이름 열 + 헤더 행 고정

            Styles styles = createStyles(workbook);

            int rowNum = 0;

            // ── 타이틀 행 ────────────────────────────────────────────
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(22);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(year + "년 " + month + "월 출근시간 현황");
            titleCell.setCellStyle(styles.title);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, daysInMonth + 1));

            // ── 헤더 행 (이름 | 1일(월) | ... | N일 | 합계) ─────────
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(20);

            Cell nameHeader = headerRow.createCell(0);
            nameHeader.setCellValue("이름");
            nameHeader.setCellStyle(styles.colHeader);

            LocalDate firstDay = LocalDate.of(year, month, 1);
            for (int d = 1; d <= daysInMonth; d++) {
                LocalDate date = firstDay.withDayOfMonth(d);
                DayOfWeek dow = date.getDayOfWeek();
                int dowIdx = (dow == DayOfWeek.SUNDAY) ? 0 : dow.getValue(); // MON=1..SAT=6, SUN=0
                String label = d + "일(" + DAY_OF_WEEK_KR[dowIdx] + ")";

                Cell cell = headerRow.createCell(d);
                cell.setCellValue(label);
                if (dowIdx == 0) {
                    cell.setCellStyle(styles.sunHeader);
                } else if (dowIdx == 6) {
                    cell.setCellStyle(styles.satHeader);
                } else {
                    cell.setCellStyle(styles.colHeader);
                }
            }

            Cell totalHeader = headerRow.createCell(daysInMonth + 1);
            totalHeader.setCellValue("합계");
            totalHeader.setCellStyle(styles.totalHeader);

            // ── 데이터 행 (직원별) ────────────────────────────────────
            for (ScheduleSummaryService.UserInfo user : users) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.setHeightInPoints(18);

                Cell nameCell = dataRow.createCell(0);
                nameCell.setCellValue(user.userName());
                nameCell.setCellStyle(styles.nameCell);

                double monthTotal = 0;
                Map<String, String> userMap = data.getOrDefault(user.userId(), Map.of());

                for (int d = 1; d <= daysInMonth; d++) {
                    String dateStr = String.format("%04d-%02d-%02d", year, month, d);
                    String hoursStr = userMap.get(dateStr);
                    Cell cell = dataRow.createCell(d);
                    if (hoursStr != null && !hoursStr.isBlank()) {
                        try {
                            double hours = Double.parseDouble(hoursStr);
                            if (hours > 0) {
                                cell.setCellValue(hours);
                                cell.setCellStyle(styles.dataCell);
                                monthTotal += hours;
                                continue;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    cell.setCellValue("-");
                    cell.setCellStyle(styles.emptyCell);
                }

                Cell totalCell = dataRow.createCell(daysInMonth + 1);
                if (monthTotal > 0) {
                    totalCell.setCellValue(monthTotal);
                } else {
                    totalCell.setCellValue(0);
                }
                totalCell.setCellStyle(styles.totalCell);
            }

            // ── 열 너비 설정 ─────────────────────────────────────────
            sheet.setColumnWidth(0, 3200); // 이름
            for (int c = 1; c <= daysInMonth; c++) {
                sheet.setColumnWidth(c, 2000);
            }
            sheet.setColumnWidth(daysInMonth + 1, 2400); // 합계

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Excel 생성 중 오류 발생", e);
        }
    }

    // ── 스타일 ──────────────────────────────────────────────────────────

    private static class Styles {
        CellStyle title;
        CellStyle colHeader;
        CellStyle sunHeader;
        CellStyle satHeader;
        CellStyle totalHeader;
        CellStyle nameCell;
        CellStyle dataCell;
        CellStyle emptyCell;
        CellStyle totalCell;
    }

    private Styles createStyles(Workbook workbook) {
        Styles s = new Styles();

        Font boldWhite = workbook.createFont();
        boldWhite.setBold(true);
        boldWhite.setColor(IndexedColors.WHITE.getIndex());
        boldWhite.setFontHeightInPoints((short) 13);

        Font boldDark = workbook.createFont();
        boldDark.setBold(true);
        boldDark.setFontHeightInPoints((short) 10);

        Font normalFont = workbook.createFont();
        normalFont.setFontHeightInPoints((short) 10);

        Font sunFont = workbook.createFont();
        sunFont.setBold(true);
        sunFont.setColor(IndexedColors.RED.getIndex());
        sunFont.setFontHeightInPoints((short) 10);

        Font satFont = workbook.createFont();
        satFont.setBold(true);
        satFont.setColor(IndexedColors.BLUE.getIndex());
        satFont.setFontHeightInPoints((short) 10);

        // 타이틀
        s.title = workbook.createCellStyle();
        s.title.setFont(boldWhite);
        s.title.setAlignment(HorizontalAlignment.CENTER);
        s.title.setVerticalAlignment(VerticalAlignment.CENTER);
        s.title.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
        s.title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.title);

        // 컬럼 헤더 (평일)
        s.colHeader = workbook.createCellStyle();
        s.colHeader.setFont(boldDark);
        s.colHeader.setAlignment(HorizontalAlignment.CENTER);
        s.colHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.colHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.colHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.colHeader);

        // 일요일 헤더
        s.sunHeader = workbook.createCellStyle();
        s.sunHeader.setFont(sunFont);
        s.sunHeader.setAlignment(HorizontalAlignment.CENTER);
        s.sunHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.sunHeader.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        s.sunHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.sunHeader);

        // 토요일 헤더
        s.satHeader = workbook.createCellStyle();
        s.satHeader.setFont(satFont);
        s.satHeader.setAlignment(HorizontalAlignment.CENTER);
        s.satHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.satHeader.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        s.satHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.satHeader);

        // 합계 헤더
        s.totalHeader = workbook.createCellStyle();
        s.totalHeader.setFont(boldDark);
        s.totalHeader.setAlignment(HorizontalAlignment.CENTER);
        s.totalHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.totalHeader.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        s.totalHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.totalHeader);

        // 이름 셀
        s.nameCell = workbook.createCellStyle();
        s.nameCell.setFont(boldDark);
        s.nameCell.setAlignment(HorizontalAlignment.LEFT);
        s.nameCell.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s.nameCell);

        // 데이터 셀 (시간 값 있음)
        s.dataCell = workbook.createCellStyle();
        s.dataCell.setFont(normalFont);
        s.dataCell.setAlignment(HorizontalAlignment.CENTER);
        s.dataCell.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s.dataCell);

        // 빈 셀 (출근 없음)
        s.emptyCell = workbook.createCellStyle();
        s.emptyCell.setFont(normalFont);
        s.emptyCell.setAlignment(HorizontalAlignment.CENTER);
        s.emptyCell.setVerticalAlignment(VerticalAlignment.CENTER);
        s.emptyCell.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.emptyCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.emptyCell);

        // 합계 셀
        s.totalCell = workbook.createCellStyle();
        s.totalCell.setFont(boldDark);
        s.totalCell.setAlignment(HorizontalAlignment.CENTER);
        s.totalCell.setVerticalAlignment(VerticalAlignment.CENTER);
        s.totalCell.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        s.totalCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.totalCell);

        return s;
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
