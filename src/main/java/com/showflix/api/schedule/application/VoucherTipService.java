package com.showflix.api.schedule.application;

import com.showflix.api.auth.domain.User;
import com.showflix.api.auth.domain.UserRepository;
import com.showflix.api.schedule.domain.SelectedDate;
import com.showflix.api.schedule.domain.SelectedDateRepository;
import com.showflix.api.schedule.domain.VoucherTip;
import com.showflix.api.schedule.domain.VoucherTipRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application Layer - 바우처/팁 유스케이스 서비스
 */
@Service
public class VoucherTipService {

    private final SelectedDateRepository selectedDateRepository;
    private final VoucherTipRepository voucherTipRepository;
    private final UserRepository userRepository;

    public VoucherTipService(SelectedDateRepository selectedDateRepository,
                             VoucherTipRepository voucherTipRepository,
                             UserRepository userRepository) {
        this.selectedDateRepository = selectedDateRepository;
        this.voucherTipRepository = voucherTipRepository;
        this.userRepository = userRepository;
    }

    /**
     * 월별 전체 배우 목록 + 일별 바우처/팁 그리드 데이터 조회
     * ScheduleSummaryService.getMonthData() 패턴 참고
     */
    @Transactional(readOnly = true)
    public MonthResult getMonthData(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        // 전체 사용자 조회 후 ACTOR 유형만 필터링
        List<ActorInfo> actors = userRepository.findAll().stream()
                .filter(u -> "ACTOR".equals(u.getAccountType()))
                .sorted(Comparator.comparing(User::getUsername))
                .map(u -> new ActorInfo(u.getUserid(), u.getUsername(), u.getRole()))
                .collect(Collectors.toList());

        // 해당 월 날짜 목록 생성
        List<String> dates = new ArrayList<>();
        for (int d = 1; d <= end.getDayOfMonth(); d++) {
            dates.add(String.format("%04d-%02d-%02d", year, month, d));
        }

        // 해당 월 전체 바우처/팁 데이터 조회
        List<VoucherTip> allData = voucherTipRepository.findByMonth(start.toString(), end.toString());

        // date → (userId → VoucherTipCell) 맵 구성
        Map<String, Map<String, VoucherTipCell>> data = new LinkedHashMap<>();
        for (VoucherTip vt : allData) {
            if (vt.getVoucher() != 0 || vt.getTip() != 0) {
                data.computeIfAbsent(vt.getDate(), k -> new LinkedHashMap<>())
                    .put(vt.getUserId(), new VoucherTipCell(vt.getVoucher(), vt.getTip()));
            }
        }

        return new MonthResult(year, month, end.getDayOfMonth(), actors, dates, data);
    }

    /**
     * 특정 날짜 출근자 목록 + 기존 바우처/팁 값 병합 조회
     */
    @Transactional(readOnly = true)
    public List<VoucherTipEntry> getByDate(String date) {
        // 해당 날짜 출근자 목록 (selected_date)
        List<SelectedDate> actors = selectedDateRepository.findByDateBetween(date, date);

        // 기존 저장된 바우처/팁 데이터 (user_id → VoucherTip)
        Map<String, VoucherTip> savedMap = voucherTipRepository.findByDate(date)
                .stream()
                .collect(Collectors.toMap(VoucherTip::getUserId, vt -> vt));

        return actors.stream()
                .map(actor -> {
                    VoucherTip saved = savedMap.get(actor.getUserId());
                    VoucherTipEntry entry = new VoucherTipEntry();
                    entry.setUserId(actor.getUserId());
                    entry.setUserName(actor.getUserName());
                    entry.setRole(actor.getRole());
                    entry.setVoucher(saved != null ? saved.getVoucher() : 0);
                    entry.setTip(saved != null ? saved.getTip() : 0);
                    return entry;
                })
                .collect(Collectors.toList());
    }

    /**
     * 바우처/팁 일괄 저장 (upsert) - 일별 데이터
     */
    @Transactional
    public void saveAll(String date, List<SaveEntry> entries) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("날짜를 입력해주세요.");
        }
        for (SaveEntry entry : entries) {
            VoucherTip vt = new VoucherTip();
            vt.setDate(date);
            vt.setUserId(entry.userId());
            vt.setUserName(entry.userName());
            vt.setVoucher(entry.voucher());
            vt.setTip(entry.tip());
            voucherTipRepository.upsert(vt);
        }
    }

    /**
     * 월별 그리드 일괄 저장 (bulk upsert) - 각 엔트리에 date 포함
     */
    @Transactional
    public void saveBulk(List<DailySaveEntry> entries) {
        for (DailySaveEntry entry : entries) {
            VoucherTip vt = new VoucherTip();
            vt.setDate(entry.date());
            vt.setUserId(entry.userId());
            vt.setUserName(entry.userName());
            vt.setVoucher(entry.voucher());
            vt.setTip(entry.tip());
            voucherTipRepository.upsert(vt);
        }
    }

    /**
     * 월별 바우처/팁 엑셀 생성 (그리드 형식: 날짜 × 배우)
     * ScheduleSummaryExcelService 패턴 참고
     */
    public byte[] exportToExcel(int year, int month, MonthResult result) {
        List<ActorInfo> actors = result.actors();
        Map<String, Map<String, VoucherTipCell>> data = result.data();
        int daysInMonth = result.daysInMonth();

        // 컬럼 배치: 날짜 | 배우1(V) | 배우1(T) | 배우2(V) | 배우2(T) | ... | 합계(V) | 합계(T)
        int actorColCount = actors.size() * 2; // 바우처+팁 각각
        int totalVCol = 1 + actorColCount;
        int totalTCol = totalVCol + 1;
        int lastCol = totalTCol;

        int[] actorVoucherTotals = new int[actors.size()];
        int[] actorTipTotals = new int[actors.size()];

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(year + "년 " + month + "월 바우처/팁");
            sheet.createFreezePane(1, 2);

            // ── 스타일 ─────────────────────────────────
            Font boldWhite = workbook.createFont();
            boldWhite.setBold(true);
            boldWhite.setColor(IndexedColors.WHITE.getIndex());
            boldWhite.setFontHeightInPoints((short) 13);

            Font boldDark = workbook.createFont();
            boldDark.setBold(true);
            boldDark.setFontHeightInPoints((short) 10);

            Font normalFont = workbook.createFont();
            normalFont.setFontHeightInPoints((short) 10);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(boldWhite);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFillForegroundColor(IndexedColors.VIOLET.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(titleStyle);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldDark);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(headerStyle);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setFont(normalFont);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(dataStyle);

            CellStyle emptyStyle = workbook.createCellStyle();
            emptyStyle.setFont(normalFont);
            emptyStyle.setAlignment(HorizontalAlignment.CENTER);
            emptyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            emptyStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            emptyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(emptyStyle);

            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.setFont(boldDark);
            totalStyle.setAlignment(HorizontalAlignment.CENTER);
            totalStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            totalStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(totalStyle);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setFont(boldDark);
            dateStyle.setAlignment(HorizontalAlignment.LEFT);
            dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(dateStyle);

            int rowNum = 0;

            // ── 타이틀 행 ─────────────────────────────
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(22);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(year + "년 " + month + "월 바우처/팁");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, lastCol));

            // ── 헤더 행 ──────────────────────────────
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(20);

            Cell dateHeader = headerRow.createCell(0);
            dateHeader.setCellValue("날짜");
            dateHeader.setCellStyle(headerStyle);

            for (int i = 0; i < actors.size(); i++) {
                Cell vCell = headerRow.createCell(1 + i * 2);
                vCell.setCellValue(actors.get(i).userName() + "(V)");
                vCell.setCellStyle(headerStyle);

                Cell tCell = headerRow.createCell(1 + i * 2 + 1);
                tCell.setCellValue(actors.get(i).userName() + "(T)");
                tCell.setCellStyle(headerStyle);
            }

            Cell totalVHeader = headerRow.createCell(totalVCol);
            totalVHeader.setCellValue("합계(V)");
            totalVHeader.setCellStyle(totalStyle);

            Cell totalTHeader = headerRow.createCell(totalTCol);
            totalTHeader.setCellValue("합계(T)");
            totalTHeader.setCellStyle(totalStyle);

            // ── 데이터 행 (날짜별) ───────────────────
            LocalDate firstDay = LocalDate.of(year, month, 1);
            int grandVoucher = 0;
            int grandTip = 0;

            String[] DOW_KR = {"일", "월", "화", "수", "목", "금", "토"};

            for (int d = 1; d <= daysInMonth; d++) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.setHeightInPoints(18);

                LocalDate date = firstDay.withDayOfMonth(d);
                int dowIdx = date.getDayOfWeek().getValue() % 7; // 일=0
                String label = d + "일(" + DOW_KR[dowIdx] + ")";
                String dateStr = String.format("%04d-%02d-%02d", year, month, d);

                Cell dateCell = dataRow.createCell(0);
                dateCell.setCellValue(label);
                dateCell.setCellStyle(dateStyle);

                int dayVoucher = 0;
                int dayTip = 0;

                for (int i = 0; i < actors.size(); i++) {
                    Map<String, VoucherTipCell> dateMap = data.getOrDefault(dateStr, Map.of());
                    VoucherTipCell cell = dateMap.get(actors.get(i).userId());

                    Cell vCell = dataRow.createCell(1 + i * 2);
                    Cell tCell = dataRow.createCell(1 + i * 2 + 1);

                    if (cell != null && (cell.voucher() != 0 || cell.tip() != 0)) {
                        vCell.setCellValue(cell.voucher());
                        vCell.setCellStyle(dataStyle);
                        tCell.setCellValue(cell.tip());
                        tCell.setCellStyle(dataStyle);
                        dayVoucher += cell.voucher();
                        dayTip += cell.tip();
                        actorVoucherTotals[i] += cell.voucher();
                        actorTipTotals[i] += cell.tip();
                    } else {
                        vCell.setCellValue("-");
                        vCell.setCellStyle(emptyStyle);
                        tCell.setCellValue("-");
                        tCell.setCellStyle(emptyStyle);
                    }
                }

                grandVoucher += dayVoucher;
                grandTip += dayTip;

                Cell totalVCell = dataRow.createCell(totalVCol);
                totalVCell.setCellValue(dayVoucher);
                totalVCell.setCellStyle(totalStyle);

                Cell totalTCell = dataRow.createCell(totalTCol);
                totalTCell.setCellValue(dayTip);
                totalTCell.setCellStyle(totalStyle);
            }

            // ── 합계 행 ──────────────────────────────
            Row totalRow = sheet.createRow(rowNum);
            totalRow.setHeightInPoints(18);

            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("합계");
            totalLabel.setCellStyle(totalStyle);

            for (int i = 0; i < actors.size(); i++) {
                Cell vCell = totalRow.createCell(1 + i * 2);
                vCell.setCellValue(actorVoucherTotals[i]);
                vCell.setCellStyle(totalStyle);

                Cell tCell = totalRow.createCell(1 + i * 2 + 1);
                tCell.setCellValue(actorTipTotals[i]);
                tCell.setCellStyle(totalStyle);
            }

            Cell grandVCell = totalRow.createCell(totalVCol);
            grandVCell.setCellValue(grandVoucher);
            grandVCell.setCellStyle(totalStyle);

            Cell grandTCell = totalRow.createCell(totalTCol);
            grandTCell.setCellValue(grandTip);
            grandTCell.setCellStyle(totalStyle);

            // ── 열 너비 설정 ─────────────────────────
            sheet.setColumnWidth(0, 3200); // 날짜
            for (int c = 1; c <= actorColCount; c++) {
                sheet.setColumnWidth(c, 2400);
            }
            sheet.setColumnWidth(totalVCol, 2400);
            sheet.setColumnWidth(totalTCol, 2400);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Excel 생성 중 오류 발생", e);
        }
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    // ─── Result / Command DTO ───────────────────────────────────────

    /** 월별 그리드 조회 결과 */
    public record MonthResult(
            int year,
            int month,
            int daysInMonth,
            List<ActorInfo> actors,
            List<String> dates,                                   // ["2026-04-01", ...]
            Map<String, Map<String, VoucherTipCell>> data  // date → (userId → cell)
    ) {}

    public record ActorInfo(String userId, String userName, String role) {}

    public record VoucherTipCell(int voucher, int tip) {}

    /**
     * 조회 결과 DTO (기존 일별 API용 — deprecated)
     */
    public static class VoucherTipEntry {
        private String userId;
        private String userName;
        private String role;
        private int voucher;
        private int tip;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public int getVoucher() { return voucher; }
        public void setVoucher(int voucher) { this.voucher = voucher; }

        public int getTip() { return tip; }
        public void setTip(int tip) { this.tip = tip; }
    }

    /**
     * 저장 요청 Command (기존 일별 API용)
     */
    public record SaveEntry(String userId, String userName, int voucher, int tip) {}

    /**
     * 월별 그리드 저장 요청 Command (각 엔트리에 date 포함)
     */
    public record DailySaveEntry(String userId, String userName, String date, int voucher, int tip) {}
}
