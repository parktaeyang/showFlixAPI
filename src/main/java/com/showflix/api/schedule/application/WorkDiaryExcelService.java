package com.showflix.api.schedule.application;

import com.showflix.api.schedule.domain.WorkDiary;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Application Layer - 업무일지 Excel 생성 서비스
 */
@Service
public class WorkDiaryExcelService {

    private static final String[] HEADERS = {
            "날짜", "담당자", "현금 결제", "지정석/특수예약/멤버십", "이벤트", "가게 관련", "특이사항"
    };

    public byte[] generate(int year, int month, List<WorkDiary> workDiaries) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(year + "년 " + month + "월 업무일지");
            sheet.createFreezePane(0, 2);

            Styles styles = createStyles(workbook);

            int rowNum = 0;

            // 타이틀
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(22);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(year + "년 " + month + "월 업무일지");
            titleCell.setCellStyle(styles.title);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            // 헤더
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(styles.colHeader);
            }

            // 데이터
            for (WorkDiary diary : workDiaries) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.setHeightInPoints(18);

                createCell(dataRow, 0, diary.getDate(), styles.dataCell);
                createCell(dataRow, 1, diary.getManager(), styles.dataCell);
                createCell(dataRow, 2, diary.getCashPayment(), styles.dataCell);
                createCell(dataRow, 3, diary.getReservations(), styles.dataCell);
                createCell(dataRow, 4, diary.getEvent(), styles.dataCell);
                createCell(dataRow, 5, diary.getStoreRelated(), styles.dataCell);
                createCell(dataRow, 6, diary.getNotes(), styles.dataCell);
            }

            // 열 너비
            sheet.setColumnWidth(0, 3200);  // 날짜
            sheet.setColumnWidth(1, 2400);  // 담당자
            sheet.setColumnWidth(2, 5000);  // 현금 결제
            sheet.setColumnWidth(3, 8000);  // 지정석/특수예약/멤버십
            sheet.setColumnWidth(4, 5000);  // 이벤트
            sheet.setColumnWidth(5, 8000);  // 가게 관련
            sheet.setColumnWidth(6, 8000);  // 특이사항

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Excel 생성 중 오류 발생", e);
        }
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    // ── 스타일 ────────────────────────────────────────────────────────

    private static class Styles {
        CellStyle title;
        CellStyle colHeader;
        CellStyle dataCell;
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

        s.title = workbook.createCellStyle();
        s.title.setFont(boldWhite);
        s.title.setAlignment(HorizontalAlignment.CENTER);
        s.title.setVerticalAlignment(VerticalAlignment.CENTER);
        s.title.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
        s.title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.title);

        s.colHeader = workbook.createCellStyle();
        s.colHeader.setFont(boldDark);
        s.colHeader.setAlignment(HorizontalAlignment.CENTER);
        s.colHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        s.colHeader.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.colHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(s.colHeader);

        s.dataCell = workbook.createCellStyle();
        s.dataCell.setFont(normalFont);
        s.dataCell.setVerticalAlignment(VerticalAlignment.CENTER);
        s.dataCell.setWrapText(true);
        setBorder(s.dataCell);

        return s;
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
