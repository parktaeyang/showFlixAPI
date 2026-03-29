package com.showflix.api.schedule.application;

import com.showflix.api.schedule.domain.ScheduleRole;
import com.showflix.api.schedule.domain.SelectedDate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application Layer - 월별 달력 Excel 생성 서비스
 */
@Service
public class CalendarExcelService {

    // 역할 표시 순서 (calendar-react.js의 ROLE_ORDER와 동일)
    private static final ScheduleRole[] ROLE_DISPLAY_ORDER = {
        ScheduleRole.MALE1, ScheduleRole.MALE2, ScheduleRole.MALE3,
        ScheduleRole.FEMALE1, ScheduleRole.FEMALE2, ScheduleRole.FEMALE3,
        ScheduleRole.DOOR, ScheduleRole.OPER, ScheduleRole.KITCHEN,
        ScheduleRole.HELPER, ScheduleRole.HOLEMAN
    };

    private static final String[] DAY_OF_WEEK_KR = {"일", "월", "화", "수", "목", "금", "토"};

    public byte[] generateMonthlyCalendar(int year, int month, List<SelectedDate> data) {
        // date -> 출근자 목록
        Map<String, List<SelectedDate>> dateMap = data.stream()
                .collect(Collectors.groupingBy(SelectedDate::getDate));

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(year + "년 " + month + "월");

            // 스타일 생성 (워크북당 1회)
            Styles styles = createStyles(workbook);

            int rowNum = 0;

            // 타이틀 행
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(22);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(year + "년 " + month + "월 달력");
            titleCell.setCellStyle(styles.title);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            // 주 블록 생성
            LocalDate firstDay = LocalDate.of(year, month, 1);
            int daysInMonth = firstDay.lengthOfMonth();
            // 일요일=0 기준 첫 날의 요일 offset
            int firstDow = firstDay.getDayOfWeek() == DayOfWeek.SUNDAY ? 0
                    : firstDay.getDayOfWeek().getValue(); // MON=1..SAT=6, SUN handled above

            // 달력 슬롯 배열 (null = 해당 월 외 padding)
            int totalSlots = (int) Math.ceil((firstDow + daysInMonth) / 7.0) * 7;
            Integer[] slots = new Integer[totalSlots];
            for (int i = 0; i < daysInMonth; i++) {
                slots[firstDow + i] = i + 1;
            }

            int numWeeks = totalSlots / 7;
            for (int w = 0; w < numWeeks; w++) {
                // 요일 헤더 행
                Row headerRow = sheet.createRow(rowNum++);
                headerRow.setHeightInPoints(18);

                Cell roleHeaderCell = headerRow.createCell(0);
                roleHeaderCell.setCellValue("역할");
                roleHeaderCell.setCellStyle(styles.roleHeader);

                for (int d = 0; d < 7; d++) {
                    Integer day = slots[w * 7 + d];
                    Cell cell = headerRow.createCell(d + 1);
                    if (day == null) {
                        cell.setCellStyle(styles.padding);
                    } else {
                        cell.setCellValue(DAY_OF_WEEK_KR[d] + "(" + day + ")");
                        cell.setCellStyle(d == 0 ? styles.sunHeader
                                : d == 6 ? styles.satHeader
                                : styles.weekdayHeader);
                    }
                }

                // 유효한 역할 코드 Set
                Set<String> validRoleNames = Arrays.stream(ScheduleRole.values())
                        .map(Enum::name)
                        .collect(Collectors.toSet());

                // 역할별 데이터 행
                for (ScheduleRole role : ROLE_DISPLAY_ORDER) {
                    Row dataRow = sheet.createRow(rowNum++);
                    dataRow.setHeightInPoints(18);

                    Cell roleLabelCell = dataRow.createCell(0);
                    roleLabelCell.setCellValue(role.getDisplayName());
                    roleLabelCell.setCellStyle(styles.roleLabel);

                    for (int d = 0; d < 7; d++) {
                        Integer day = slots[w * 7 + d];
                        Cell cell = dataRow.createCell(d + 1);
                        if (day == null) {
                            cell.setCellStyle(styles.padding);
                            continue;
                        }
                        String dateStr = String.format("%04d-%02d-%02d", year, month, day);
                        List<SelectedDate> dayData = dateMap.getOrDefault(dateStr, Collections.emptyList());

                        List<SelectedDate> rolePersons = dayData.stream()
                                .filter(sd -> role.name().equals(sd.getRole()))
                                .toList();

                        if (rolePersons.isEmpty()) {
                            cell.setCellStyle(styles.empty);
                        } else {
                            boolean hasConfirmed = rolePersons.stream().anyMatch(sd -> "Y".equalsIgnoreCase(sd.getConfirmed()));
                            boolean hasUnconfirmed = rolePersons.stream().anyMatch(sd -> !"Y".equalsIgnoreCase(sd.getConfirmed()));

                            String names = rolePersons.stream()
                                    .map(SelectedDate::getUserName)
                                    .filter(n -> n != null && !n.isBlank())
                                    .collect(Collectors.joining("\n"));
                            cell.setCellValue(names);

                            if (hasConfirmed && hasUnconfirmed) {
                                cell.setCellStyle(styles.mixed);
                            } else if (hasConfirmed) {
                                cell.setCellStyle(styles.confirmed);
                            } else {
                                cell.setCellStyle(styles.unconfirmed);
                            }
                        }
                    }
                }

                // "예비" 행: Enum에 매칭되지 않는 모든 사용자
                Row reserveRow = sheet.createRow(rowNum++);
                reserveRow.setHeightInPoints(18);

                Cell reserveLabelCell = reserveRow.createCell(0);
                reserveLabelCell.setCellValue("예비");
                reserveLabelCell.setCellStyle(styles.roleLabel);

                for (int d = 0; d < 7; d++) {
                    Integer day = slots[w * 7 + d];
                    Cell cell = reserveRow.createCell(d + 1);
                    if (day == null) {
                        cell.setCellStyle(styles.padding);
                        continue;
                    }
                    String dateStr = String.format("%04d-%02d-%02d", year, month, day);
                    List<SelectedDate> dayData = dateMap.getOrDefault(dateStr, Collections.emptyList());

                    List<SelectedDate> noRolePersons = dayData.stream()
                            .filter(sd -> sd.getRole() == null || sd.getRole().isBlank()
                                    || !validRoleNames.contains(sd.getRole()))
                            .toList();

                    if (noRolePersons.isEmpty()) {
                        cell.setCellStyle(styles.empty);
                    } else {
                        boolean hasConfirmed = noRolePersons.stream().anyMatch(sd -> "Y".equalsIgnoreCase(sd.getConfirmed()));
                        boolean hasUnconfirmed = noRolePersons.stream().anyMatch(sd -> !"Y".equalsIgnoreCase(sd.getConfirmed()));

                        String names = noRolePersons.stream()
                                .map(SelectedDate::getUserName)
                                .filter(n -> n != null && !n.isBlank())
                                .collect(Collectors.joining("\n"));
                        cell.setCellValue(names);

                        if (hasConfirmed && hasUnconfirmed) {
                            cell.setCellStyle(styles.mixed);
                        } else if (hasConfirmed) {
                            cell.setCellStyle(styles.confirmed);
                        } else {
                            cell.setCellStyle(styles.unconfirmed);
                        }
                    }
                }

                // 주 블록 구분 빈 행
                if (w < numWeeks - 1) {
                    Row spacer = sheet.createRow(rowNum++);
                    spacer.setHeightInPoints(6);
                }
            }

            // 열 너비
            sheet.setColumnWidth(0, 2200); // 역할 열
            for (int c = 1; c <= 7; c++) {
                sheet.setColumnWidth(c, 3800);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Excel 생성 중 오류 발생", e);
        }
    }

    private static class Styles {
        CellStyle title;
        CellStyle roleHeader;
        CellStyle weekdayHeader;
        CellStyle sunHeader;
        CellStyle satHeader;
        CellStyle roleLabel;
        CellStyle confirmed;
        CellStyle unconfirmed;
        CellStyle mixed;
        CellStyle empty;
        CellStyle padding;
    }

    private Styles createStyles(Workbook workbook) {
        Styles s = new Styles();

        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldFont.setFontHeightInPoints((short) 12);

        Font normalFont = workbook.createFont();
        normalFont.setFontHeightInPoints((short) 10);

        // 타이틀
        s.title = workbook.createCellStyle();
        s.title.setFont(boldFont);
        s.title.setAlignment(HorizontalAlignment.CENTER);
        s.title.setVerticalAlignment(VerticalAlignment.CENTER);
        s.title.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font whiteFont = workbook.createFont();
        whiteFont.setBold(true);
        whiteFont.setColor(IndexedColors.WHITE.getIndex());
        whiteFont.setFontHeightInPoints((short) 13);
        s.title.setFont(whiteFont);
        setBorder(s.title);

        // 역할 헤더 (요일 헤더 행의 첫 칸)
        s.roleHeader = workbook.createCellStyle();
        s.roleHeader.setFont(boldFont);
        s.roleHeader.setAlignment(HorizontalAlignment.CENTER);
        s.roleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.roleHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.roleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.roleHeader);

        // 평일 요일 헤더
        s.weekdayHeader = workbook.createCellStyle();
        s.weekdayHeader.setFont(boldFont);
        s.weekdayHeader.setAlignment(HorizontalAlignment.CENTER);
        s.weekdayHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.weekdayHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.weekdayHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.weekdayHeader);

        // 일요일 헤더
        s.sunHeader = workbook.createCellStyle();
        s.sunHeader.setFont(boldFont);
        s.sunHeader.setAlignment(HorizontalAlignment.CENTER);
        s.sunHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.sunHeader.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        s.sunHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.sunHeader);

        // 토요일 헤더
        s.satHeader = workbook.createCellStyle();
        s.satHeader.setFont(boldFont);
        s.satHeader.setAlignment(HorizontalAlignment.CENTER);
        s.satHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.satHeader.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        s.satHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.satHeader);

        // 역할 레이블 (데이터 행의 첫 칸)
        s.roleLabel = workbook.createCellStyle();
        s.roleLabel.setFont(boldFont);
        s.roleLabel.setAlignment(HorizontalAlignment.CENTER);
        s.roleLabel.setVerticalAlignment(VerticalAlignment.CENTER);
        s.roleLabel.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        s.roleLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.roleLabel);

        // 확정 인원 (연초록)
        s.confirmed = workbook.createCellStyle();
        s.confirmed.setFont(normalFont);
        s.confirmed.setAlignment(HorizontalAlignment.CENTER);
        s.confirmed.setVerticalAlignment(VerticalAlignment.CENTER);
        s.confirmed.setWrapText(true);
        s.confirmed.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        s.confirmed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.confirmed);

        // 미확정 인원 (연노랑)
        s.unconfirmed = workbook.createCellStyle();
        s.unconfirmed.setFont(normalFont);
        s.unconfirmed.setAlignment(HorizontalAlignment.CENTER);
        s.unconfirmed.setVerticalAlignment(VerticalAlignment.CENTER);
        s.unconfirmed.setWrapText(true);
        s.unconfirmed.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        s.unconfirmed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.unconfirmed);

        // 혼재 (확정+미확정)
        s.mixed = workbook.createCellStyle();
        s.mixed.setFont(normalFont);
        s.mixed.setAlignment(HorizontalAlignment.CENTER);
        s.mixed.setVerticalAlignment(VerticalAlignment.CENTER);
        s.mixed.setWrapText(true);
        s.mixed.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        s.mixed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.mixed);

        // 빈 셀
        s.empty = workbook.createCellStyle();
        s.empty.setFont(normalFont);
        s.empty.setAlignment(HorizontalAlignment.CENTER);
        s.empty.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(s.empty);

        // 패딩 셀 (해당 월 외)
        s.padding = workbook.createCellStyle();
        s.padding.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.padding.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.padding);

        return s;
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
