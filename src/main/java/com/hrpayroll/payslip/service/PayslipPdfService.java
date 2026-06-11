package com.hrpayroll.payslip.service;

import com.hrpayroll.payslip.entity.Payslip;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class PayslipPdfService {

    private static Font titleFont;
    private static Font headerFont;
    private static Font normalFont;
    private static Font smallItalicFont;

    static {
        try {
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            titleFont = new Font(bfChinese, 18, Font.BOLD);
            headerFont = new Font(bfChinese, 12, Font.BOLD);
            normalFont = new Font(bfChinese, 10, Font.NORMAL);
            smallItalicFont = new Font(bfChinese, 9, Font.ITALIC);
        } catch (Exception e) {
            titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            smallItalicFont = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC);
        }
    }

    public byte[] generatePdf(Payslip payslip, String password) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            if (password != null && !password.isEmpty()) {
                writer.setEncryption(
                        password.getBytes(),
                        password.getBytes(),
                        PdfWriter.ALLOW_PRINTING,
                        PdfWriter.STANDARD_ENCRYPTION_128
                );
            }

            document.open();

            Paragraph title = new Paragraph("工资条", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Paragraph info = new Paragraph(
                    "姓名: " + payslip.getEmployeeName() + "    工号: " + payslip.getEmployeeNo()
                            + "    月份: " + payslip.getYearMonth(),
                    normalFont
            );
            info.setSpacingAfter(15);
            document.add(info);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            addTableHeader(table, "项目", "金额 (元)", headerFont);

            addTableRow(table, "基本工资", payslip.getBaseSalary(), normalFont);
            addTableRow(table, "绩效工资", payslip.getPerformanceSalary(), normalFont);
            addTableRow(table, "岗位工资", payslip.getPostSalary(), normalFont);
            addTableRow(table, "工龄工资", payslip.getSenioritySalary(), normalFont);
            addTableRow(table, "加班费", payslip.getOvertimePay(), normalFont);
            addTableRow(table, "餐补", payslip.getMealAllowance(), normalFont);
            addTableRow(table, "交通补", payslip.getTransportAllowance(), normalFont);
            addTableRow(table, "通讯补", payslip.getCommunicationAllowance(), normalFont);
            addTableRowNegative(table, "考勤扣款", payslip.getAttendanceDeduction(), normalFont);
            addTableRowNegative(table, "社保(个人)", payslip.getSocialSecurity(), normalFont);
            addTableRowNegative(table, "公积金(个人)", payslip.getHousingFund(), normalFont);
            addTableRowNegative(table, "个人所得税", payslip.getIndividualTax(), normalFont);

            addTableHeader(table, "应发工资", formatAmount(payslip.getGrossPay()), headerFont);
            addTableHeader(table, "实发工资", formatAmount(payslip.getNetPay()), headerFont);

            document.add(table);

            Paragraph note = new Paragraph(
                    "\n\n备注: 本工资条为系统自动生成，如有疑问请联系 HR。",
                    smallItalicFont
            );
            document.add(note);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("生成工资条 PDF 失败: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String key, String value, Font font) {
        PdfPCell keyCell = new PdfPCell(new Phrase(key, font));
        keyCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        keyCell.setPadding(5);
        table.addCell(keyCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addTableRow(PdfPTable table, String label, BigDecimal amount, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(5);
        table.addCell(labelCell);

        String amountStr = formatAmount(amount);
        PdfPCell amountCell = new PdfPCell(new Phrase(amountStr, font));
        amountCell.setPadding(5);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(amountCell);
    }

    private void addTableRowNegative(PdfPTable table, String label, BigDecimal amount, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(5);
        table.addCell(labelCell);

        String amountStr = amount != null ? amount.negate().setScale(2).toString() : "0.00";
        PdfPCell amountCell = new PdfPCell(new Phrase(amountStr, font));
        amountCell.setPadding(5);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(amountCell);
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? amount.setScale(2).toString() : "0.00";
    }
}
