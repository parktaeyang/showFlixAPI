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
        List<ScheduleSummaryService.UserInfo> staffUsers = result.staffUsers();
        List<ScheduleSummaryService.UserInfo> actorUsers = result.actorUsers();
        Map<String, Map<String, String>> data = result.data();
        Map<String, String> staffRemarks = result.staffRemarks();
        Map<String, String> actorRemarks = result.actorRemarks();

        // 컬럼 배치: 날짜 | 스탭들 | 특이사항(스탭) | 배우들 | 특이사항(배우) | 합계
        int staffRemarksCol = staffUsers.size() + 1;
        int actorStartCol = staffRemarksCol + 1;
        int actorRemarksCol = actorStartCol + actorUsers.size();
        int totalCol = actorRemarksCol + 1;
        int lastCol = totalCol;

        double[] staffTotals = new double[staffUsers.size()];
        double[] actorTotals = new double[actorUsers.size()];

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
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastCol));

            // ── 헤더 행 ──────────────────────────────────────────────
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(20);

            Cell dateHeader = headerRow.createCell(0);
            dateHeader.setCellValue("날짜");
            dateHeader.setCellStyle(styles.colHeader);

            // 스탭 헤더
            for (int u = 0; u < staffUsers.size(); u++) {
                Cell cell = headerRow.createCell(u + 1);
                cell.setCellValue(staffUsers.get(u).userName());
                cell.setCellStyle(styles.colHeader);
            }
            Cell staffRemarksHeader = headerRow.createCell(staffRemarksCol);
            staffRemarksHeader.setCellValue("특이사항(스탭)");
            staffRemarksHeader.setCellStyle(styles.totalHeader);

            // 배우 헤더
            for (int u = 0; u < actorUsers.size(); u++) {
                Cell cell = headerRow.createCell(actorStartCol + u);
                cell.setCellValue(actorUsers.get(u).userName());
                cell.setCellStyle(styles.colHeader);
            }
            Cell actorRemarksHeader = headerRow.createCell(actorRemarksCol);
            actorRemarksHeader.setCellValue("특이사항(배우)");
            actorRemarksHeader.setCellStyle(styles.totalHeader);

            Cell totalHeader = headerRow.createCell(totalCol);
            totalHeader.setCellValue("합계");
            totalHeader.setCellStyle(styles.totalHeader);

            // ── 데이터 행 (날짜별) ────────────────────────────────────
            LocalDate firstDay = LocalDate.of(year, month, 1);
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

                // 스탭 데이터
                for (int u = 0; u < staffUsers.size(); u++) {
                    Map<String, String> userMap = data.getOrDefault(staffUsers.get(u).userId(), Map.of());
                    String hoursStr = userMap.get(dateStr);
                    Cell cell = dataRow.createCell(u + 1);
                    if (hoursStr != null && !hoursStr.isBlank()) {
                        try {
                            double hours = Double.parseDouble(hoursStr);
                            if (hours > 0) {
                                cell.setCellValue(hours);
                                cell.setCellStyle(styles.dataCell);
                                dayTotal += hours;
                                staffTotals[u] += hours;
                                continue;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    cell.setCellValue("-");
                    cell.setCellStyle(styles.emptyCell);
                }

                // 스탭 특이사항
                Cell staffRemarksCell = dataRow.createCell(staffRemarksCol);
                staffRemarksCell.setCellValue(staffRemarks.getOrDefault(dateStr, ""));
                staffRemarksCell.setCellStyle(styles.remarksCell);

                // 배우 데이터
                for (int u = 0; u < actorUsers.size(); u++) {
                    Map<String, String> userMap = data.getOrDefault(actorUsers.get(u).userId(), Map.of());
                    String hoursStr = userMap.get(dateStr);
                    Cell cell = dataRow.createCell(actorStartCol + u);
                    if (hoursStr != null && !hoursStr.isBlank()) {
                        try {
                            double hours = Double.parseDouble(hoursStr);
                            if (hours > 0) {
                                cell.setCellValue(hours);
                                cell.setCellStyle(styles.dataCell);
                                dayTotal += hours;
                                actorTotals[u] += hours;
                                continue;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    cell.setCellValue("-");
                    cell.setCellStyle(styles.emptyCell);
                }

                // 배우 특이사항
                Cell actorRemarksCell = dataRow.createCell(actorRemarksCol);
                actorRemarksCell.setCellValue(actorRemarks.getOrDefault(dateStr, ""));
                actorRemarksCell.setCellStyle(styles.remarksCell);

                grandTotal += dayTotal;

                // 합계 셀
                Cell totalCell = dataRow.createCell(totalCol);
                totalCell.setCellValue(dayTotal > 0 ? dayTotal : 0);
                totalCell.setCellStyle(styles.totalCell);
            }

            // ── 합계 행 ─────────────────────────────────────────────
            Row totalRow = sheet.createRow(rowNum++);
            totalRow.setHeightInPoints(18);

            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("합계");
            totalLabelCell.setCellStyle(styles.totalHeader);

            for (int u = 0; u < staffUsers.size(); u++) {
                Cell cell = totalRow.createCell(u + 1);
                cell.setCellValue(staffTotals[u] > 0 ? staffTotals[u] : 0);
                cell.setCellStyle(styles.totalCell);
            }
            // 스탭 특이사항 빈 칸
            Cell totalStaffRemarksCell = totalRow.createCell(staffRemarksCol);
            totalStaffRemarksCell.setCellValue("");
            totalStaffRemarksCell.setCellStyle(styles.remarksCell);

            for (int u = 0; u < actorUsers.size(); u++) {
                Cell cell = totalRow.createCell(actorStartCol + u);
                cell.setCellValue(actorTotals[u] > 0 ? actorTotals[u] : 0);
                cell.setCellStyle(styles.totalCell);
            }
            // 배우 특이사항 빈 칸
            Cell totalActorRemarksCell = totalRow.createCell(actorRemarksCol);
            totalActorRemarksCell.setCellValue("");
            totalActorRemarksCell.setCellStyle(styles.remarksCell);

            Cell grandTotalCell = totalRow.createCell(totalCol);
            grandTotalCell.setCellValue(grandTotal);
            grandTotalCell.setCellStyle(styles.totalCell);

            // ── 열 너비 설정 ─────────────────────────────────────────
            sheet.setColumnWidth(0, 3200); // 날짜
            for (int c = 1; c <= staffUsers.size(); c++) {
                sheet.setColumnWidth(c, 2400);
            }
            sheet.setColumnWidth(staffRemarksCol, 6000);  // 스탭 특이사항
            for (int c = 0; c < actorUsers.size(); c++) {
                sheet.setColumnWidth(actorStartCol + c, 2400);
            }
            sheet.setColumnWidth(actorRemarksCol, 6000);  // 배우 특이사항
            sheet.setColumnWidth(totalCol, 2400);          // 합계

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
