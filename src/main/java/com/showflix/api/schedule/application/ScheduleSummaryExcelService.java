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
        Map<String, String> dateRemarks = result.dateRemarks();

        int totalCol = users.size() + 1;
        int remarksCol = users.size() + 2;

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(year + "년 " + month + "월 출근시간");
            sheet.createFreezePane(1, 2); // 날짜 열 + 헤더 행 고정

            Styles styles = createStyles(workbook);

            int rowNum = 0;

            // ── 타이틀 행 ────────────────────────────────────────────
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(22);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(year + "년 " + month + "월 출근시간 현황");
            titleCell.setCellStyle(styles.title);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, remarksCol));

            // ── 헤더 행 (날짜 | 출근자1 | 출근자2 | ... | 합계 | 특이사항) ──
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(20);

            Cell dateHeader = headerRow.createCell(0);
            dateHeader.setCellValue("날짜");
            dateHeader.setCellStyle(styles.colHeader);

            for (int u = 0; u < users.size(); u++) {
                Cell cell = headerRow.createCell(u + 1);
                cell.setCellValue(users.get(u).userName());
                cell.setCellStyle(styles.colHeader);
            }

            Cell totalHeader = headerRow.createCell(totalCol);
            totalHeader.setCellValue("합계");
            totalHeader.setCellStyle(styles.totalHeader);

            Cell remarksHeader = headerRow.createCell(remarksCol);
            remarksHeader.setCellValue("특이사항");
            remarksHeader.setCellStyle(styles.totalHeader);

            // ── 데이터 행 (날짜별) ────────────────────────────────────
            LocalDate firstDay = LocalDate.of(year, month, 1);
            double[] userTotals = new double[users.size()];
            double grandTotal = 0;

            for (int d = 1; d <= daysInMonth; d++) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.setHeightInPoints(18);

                LocalDate date = firstDay.withDayOfMonth(d);
                DayOfWeek dow = date.getDayOfWeek();
                int dowIdx = (dow == DayOfWeek.SUNDAY) ? 0 : dow.getValue();
                String label = d + "일(" + DAY_OF_WEEK_KR[dowIdx] + ")";
                String dateStr = String.format("%04d-%02d-%02d", year, month, d);

                // 날짜 셀 (일/토 색상 적용)
                Cell dateCell = dataRow.createCell(0);
                dateCell.setCellValue(label);
                if (dowIdx == 0) {
                    dateCell.setCellStyle(styles.sunDateCell);
                } else if (dowIdx == 6) {
                    dateCell.setCellStyle(styles.satDateCell);
                } else {
                    dateCell.setCellStyle(styles.dateCell);
                }

                double dayTotal = 0;
                for (int u = 0; u < users.size(); u++) {
                    Map<String, String> userMap = data.getOrDefault(users.get(u).userId(), Map.of());
                    String hoursStr = userMap.get(dateStr);
                    Cell cell = dataRow.createCell(u + 1);
                    if (hoursStr != null && !hoursStr.isBlank()) {
                        try {
                            double hours = Double.parseDouble(hoursStr);
                            if (hours > 0) {
                                cell.setCellValue(hours);
                                cell.setCellStyle(styles.dataCell);
                                dayTotal += hours;
                                userTotals[u] += hours;
                                continue;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    cell.setCellValue("-");
                    cell.setCellStyle(styles.emptyCell);
                }

                grandTotal += dayTotal;

                // 합계 셀
                Cell totalCell = dataRow.createCell(totalCol);
                totalCell.setCellValue(dayTotal > 0 ? dayTotal : 0);
                totalCell.setCellStyle(styles.totalCell);

                // 특이사항 셀 (저장된 값 표시)
                Cell remarksCell = dataRow.createCell(remarksCol);
                String remarksValue = dateRemarks.getOrDefault(dateStr, "");
                remarksCell.setCellValue(remarksValue);
                remarksCell.setCellStyle(styles.remarksCell);
            }

            // ── 합계 행 ─────────────────────────────────────────────
            Row totalRow = sheet.createRow(rowNum++);
            totalRow.setHeightInPoints(18);

            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("합계");
            totalLabelCell.setCellStyle(styles.totalHeader);

            for (int u = 0; u < users.size(); u++) {
                Cell cell = totalRow.createCell(u + 1);
                cell.setCellValue(userTotals[u] > 0 ? userTotals[u] : 0);
                cell.setCellStyle(styles.totalCell);
            }

            Cell grandTotalCell = totalRow.createCell(totalCol);
            grandTotalCell.setCellValue(grandTotal);
            grandTotalCell.setCellStyle(styles.totalCell);

            // 특이사항 열 빈 칸
            Cell totalRemarksCell = totalRow.createCell(remarksCol);
            totalRemarksCell.setCellValue("");
            totalRemarksCell.setCellStyle(styles.remarksCell);

            // ── 열 너비 설정 ─────────────────────────────────────────
            sheet.setColumnWidth(0, 3200); // 날짜
            for (int c = 1; c <= users.size(); c++) {
                sheet.setColumnWidth(c, 2400); // 출근자
            }
            sheet.setColumnWidth(totalCol, 2400);    // 합계
            sheet.setColumnWidth(remarksCol, 6000);  // 특이사항

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
        CellStyle totalHeader;
        CellStyle dateCell;
        CellStyle sunDateCell;
        CellStyle satDateCell;
        CellStyle dataCell;
        CellStyle emptyCell;
        CellStyle totalCell;
        CellStyle remarksCell;
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

        // 컬럼 헤더
        s.colHeader = workbook.createCellStyle();
        s.colHeader.setFont(boldDark);
        s.colHeader.setAlignment(HorizontalAlignment.CENTER);
        s.colHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.colHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.colHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.colHeader);

        // 합계 헤더
        s.totalHeader = workbook.createCellStyle();
        s.totalHeader.setFont(boldDark);
        s.totalHeader.setAlignment(HorizontalAlignment.CENTER);
        s.totalHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.totalHeader.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        s.totalHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.totalHeader);

        // 날짜 셀 (평일)
        s.dateCell = workbook.createCellStyle();
        s.dateCell.setFont(boldDark);
        s.dateCell.setAlignment(HorizontalAlignment.LEFT);
        s.dateCell.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s.dateCell);

        // 날짜 셀 (일요일)
        s.sunDateCell = workbook.createCellStyle();
        s.sunDateCell.setFont(sunFont);
        s.sunDateCell.setAlignment(HorizontalAlignment.LEFT);
        s.sunDateCell.setVerticalAlignment(VerticalAlignment.CENTER);
        s.sunDateCell.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        s.sunDateCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.sunDateCell);

        // 날짜 셀 (토요일)
        s.satDateCell = workbook.createCellStyle();
        s.satDateCell.setFont(satFont);
        s.satDateCell.setAlignment(HorizontalAlignment.LEFT);
        s.satDateCell.setVerticalAlignment(VerticalAlignment.CENTER);
        s.satDateCell.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        s.satDateCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.satDateCell);

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

        // 특이사항 셀 (빈 칸)
        s.remarksCell = workbook.createCellStyle();
        s.remarksCell.setFont(normalFont);
        s.remarksCell.setAlignment(HorizontalAlignment.LEFT);
        s.remarksCell.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s.remarksCell);

        return s;
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
