package com.example.demo.service;

import com.example.demo.dto.SaleDTO;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Generates a complete vehicle sale invoice as a PDF byte array using iText 7.
 *
 * Layout:
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │  DEALER LETTERHEAD                   TAX INVOICE            │
 *   │  Address / Phone / Email             Invoice #  INV-2025-X  │
 *   │                                      Date       dd MMM yyyy │
 *   ├─────────────────────────────────────────────────────────────┤
 *   │  BILL TO                                                     │
 *   │  Customer name / phone / email                               │
 *   ├─────────────────────────────────────────────────────────────┤
 *   │  VEHICLE DETAILS                                             │
 *   │  Model | Variant | Fuel | Transmission | Color | VIN | Mfg  │
 *   ├─────────────────────────────────────────────────────────────┤
 *   │  PRICE SUMMARY                                               │
 *   │  Ex-showroom price        ₹ X,XX,XXX                        │
 *   │  Booking advance (paid)  -₹    XX,XXX                       │
 *   │  Balance due              ₹ X,XX,XXX                        │
 *   │  ─────────────────────────────────────                       │
 *   │  TOTAL SALE PRICE         ₹ X,XX,XXX   (bold)               │
 *   ├─────────────────────────────────────────────────────────────┤
 *   │  PAYMENT DETAILS  (mode / bank / loan / exchange)            │
 *   ├─────────────────────────────────────────────────────────────┤
 *   │  Remarks                                                     │
 *   ├─────────────────────────────────────────────────────────────┤
 *   │  Authorised Signatory ___________    Customer Signature ___  │
 *   └─────────────────────────────────────────────────────────────┘
 */
@Slf4j
@Service
public class InvoicePdfService {

    private static final DeviceRgb BRAND_BLUE  = new DeviceRgb(0x1a, 0x37, 0x6b);
    private static final DeviceRgb LIGHT_GREY  = new DeviceRgb(0xf4, 0xf5, 0xf7);
    private static final DeviceRgb MID_GREY    = new DeviceRgb(0xcc, 0xcc, 0xcc);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final NumberFormat INR = NumberFormat.getNumberInstance(new Locale("en", "IN"));

    static {
        INR.setMinimumFractionDigits(2);
        INR.setMaximumFractionDigits(2);
    }

    // ── Public API ─────────────────────────────────────────────────────────

    public byte[] generate(SaleDTO sale) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer   = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc       = new Document(pdfDoc, PageSize.A4);
            doc.setMargins(36, 48, 36, 48);

            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            addHeader(doc, sale, regular, bold);
            addDivider(doc);
            addBillTo(doc, sale, regular, bold);
            addDivider(doc);
            addVehicleDetails(doc, sale, regular, bold);
            addDivider(doc);
            addPriceSummary(doc, sale, regular, bold);
            addDivider(doc);
            addPaymentDetails(doc, sale, regular, bold);

            if (sale.getRemarks() != null && !sale.getRemarks().isBlank()) {
                addDivider(doc);
                addRemarks(doc, sale, regular, bold);
            }

            addDivider(doc);
            addSignatureBlock(doc, regular, bold);
            addFooter(doc, sale, regular);

            doc.close();
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate invoice PDF for {}", sale.getInvoiceNumber(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    // ── Sections ───────────────────────────────────────────────────────────

    private void addHeader(Document doc, SaleDTO sale, PdfFont regular, PdfFont bold)
            throws IOException {
        Table header = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .setWidth(UnitValue.createPercentValue(100));

        // Left — dealer info
        Cell left = new Cell().setBorder(Border.NO_BORDER).setPaddingBottom(4);
        left.add(new Paragraph(sale.getDealerName())
                .setFont(bold).setFontSize(16).setFontColor(BRAND_BLUE));
        left.add(smallPara(sale.getDealerAddress(), regular));
        left.add(smallPara("Tel: " + nvl(sale.getDealerPhone()), regular));
        left.add(smallPara("Email: " + nvl(sale.getDealerEmail()), regular));

        // Right — invoice meta
        Cell right = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        right.add(new Paragraph("TAX INVOICE")
                .setFont(bold).setFontSize(18).setFontColor(BRAND_BLUE));
        right.add(smallPara("Invoice No:  " + sale.getInvoiceNumber(), bold));
        right.add(smallPara("Date:  " + (sale.getSaleDate() != null
                ? sale.getSaleDate().format(DATE_FMT) : "—"), regular));
        right.add(smallPara("Status:  " + nvl(sale.getPaymentStatus()), regular));

        header.addCell(left);
        header.addCell(right);
        doc.add(header);
    }

    private void addBillTo(Document doc, SaleDTO sale, PdfFont regular, PdfFont bold) {
        doc.add(sectionHeading("BILL TO", bold));
        Table t = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));
        row(t, "Customer",  nvl(sale.getCustomerName()),  regular, bold);
        row(t, "Phone",     nvl(sale.getCustomerPhone()),  regular, bold);
        row(t, "Email",     nvl(sale.getCustomerEmail()),  regular, bold);
        doc.add(t);
    }

    private void addVehicleDetails(Document doc, SaleDTO sale, PdfFont regular, PdfFont bold) {
        doc.add(sectionHeading("VEHICLE DETAILS", bold));
        Table t = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));
        row(t, "Model",           nvl(sale.getModelName()) + " " + nvl(sale.getVariant()), regular, bold);
        row(t, "Fuel Type",       nvl(sale.getFuelType()),         regular, bold);
        row(t, "Transmission",    nvl(sale.getTransmission()),     regular, bold);
        row(t, "Colour",          nvl(sale.getColor()),            regular, bold);
        row(t, "VIN",             nvl(sale.getVin()),              regular, bold);
        row(t, "Mfg. Date",       nvl(sale.getManufactureDate()),  regular, bold);
        doc.add(t);
    }

    private void addPriceSummary(Document doc, SaleDTO sale, PdfFont regular, PdfFont bold) {
        doc.add(sectionHeading("PRICE SUMMARY", bold));

        Table t = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(100));

        BigDecimal exShowroom   = nvlBD(sale.getExShowroomPrice());
        BigDecimal bookingAdv   = nvlBD(sale.getBookingAmount());
        BigDecimal salePrice    = nvlBD(sale.getSalePrice());
        BigDecimal balance      = salePrice.subtract(bookingAdv);

        priceRow(t, "Ex-Showroom Price",        exShowroom, regular, false);
        priceRow(t, "Booking Advance (paid)",   bookingAdv.negate(), regular, false);
        priceRow(t, "Balance Due",              balance,   regular, false);

        // Total row — highlighted
        Cell labelCell = new Cell()
                .add(new Paragraph("TOTAL SALE PRICE")
                        .setFont(bold).setFontSize(11).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(BRAND_BLUE).setPadding(6).setBorder(Border.NO_BORDER);
        Cell valueCell = new Cell()
                .add(new Paragraph(rupee(salePrice))
                        .setFont(bold).setFontSize(11).setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(BRAND_BLUE).setPadding(6).setBorder(Border.NO_BORDER);
        t.addCell(labelCell);
        t.addCell(valueCell);

        doc.add(t);
    }

    private void addPaymentDetails(Document doc, SaleDTO sale, PdfFont regular, PdfFont bold) {
        doc.add(sectionHeading("PAYMENT DETAILS", bold));
        Table t = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));
        row(t, "Payment Mode", nvl(sale.getPaymentMode()), regular, bold);
        if (sale.getLoanAmount() != null && sale.getLoanAmount().compareTo(BigDecimal.ZERO) > 0) {
            row(t, "Loan Amount",  rupee(sale.getLoanAmount()), regular, bold);
            row(t, "Finance Bank", nvl(sale.getFinanceBank()),  regular, bold);
        }
        if (sale.getExchangeVehicle() != null && !sale.getExchangeVehicle().isBlank()) {
            row(t, "Exchange Vehicle", sale.getExchangeVehicle(),              regular, bold);
            row(t, "Exchange Value",   rupee(nvlBD(sale.getExchangeValue())), regular, bold);
        }
        doc.add(t);
    }

    private void addRemarks(Document doc, SaleDTO sale, PdfFont regular, PdfFont bold) {
        doc.add(sectionHeading("REMARKS", bold));
        doc.add(new Paragraph(sale.getRemarks()).setFont(regular).setFontSize(9).setFontColor(ColorConstants.DARK_GRAY));
    }

    private void addSignatureBlock(Document doc, PdfFont regular, PdfFont bold) {
        doc.add(new Paragraph("\n"));
        Table sig = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell authSig = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
        authSig.add(new Paragraph("\n\n\n____________________________")
                .setFont(regular).setFontSize(9));
        authSig.add(new Paragraph("Authorised Signatory").setFont(bold).setFontSize(9));

        Cell custSig = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
        custSig.add(new Paragraph("\n\n\n____________________________")
                .setFont(regular).setFontSize(9));
        custSig.add(new Paragraph("Customer Signature").setFont(bold).setFontSize(9));

        sig.addCell(authSig);
        sig.addCell(custSig);
        doc.add(sig);
    }

    private void addFooter(Document doc, SaleDTO sale, PdfFont regular) {
        doc.add(new Paragraph("\nThis is a computer-generated invoice. "
                + "For queries contact " + nvl(sale.getDealerEmail()) + ".")
                .setFont(regular).setFontSize(7.5f)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

        private void addDivider(Document doc) {
        // Create a solid line of 0.5f thickness
        SolidLine line = new SolidLine(0.5f);
        line.setColor(MID_GREY); // Use your MID_GREY color

        // Add it as a line separator with top and bottom margins
        doc.add(new LineSeparator(line)
                .setMarginTop(8)
                .setMarginBottom(8));
        }

    private Paragraph sectionHeading(String text, PdfFont bold) {
        return new Paragraph(text)
                .setFont(bold).setFontSize(9f)
                .setFontColor(BRAND_BLUE)
                .setMarginBottom(4);
    }

    private Paragraph smallPara(String text, PdfFont font) {
        return new Paragraph(text).setFont(font).setFontSize(9).setMargin(1);
    }

    private void row(Table t, String label, String value, PdfFont regular, PdfFont bold) {
        t.addCell(new Cell().add(new Paragraph(label).setFont(bold).setFontSize(9))
                .setBorder(Border.NO_BORDER).setBackgroundColor(LIGHT_GREY).setPadding(4));
        t.addCell(new Cell().add(new Paragraph(value).setFont(regular).setFontSize(9))
                .setBorder(Border.NO_BORDER).setPadding(4));
    }

    private void priceRow(Table t, String label, BigDecimal amount, PdfFont font, boolean highlight) {
        t.addCell(new Cell().add(new Paragraph(label).setFont(font).setFontSize(9))
                .setBorder(Border.NO_BORDER).setPadding(3));
        t.addCell(new Cell().add(new Paragraph(rupee(amount)).setFont(font).setFontSize(9)
                .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setPadding(3));
    }

    private String rupee(BigDecimal v) {
        if (v == null) return "₹ 0.00";
        return "₹ " + INR.format(v);
    }

    private String nvl(String s) { return s != null ? s : "—"; }

    private BigDecimal nvlBD(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
}
