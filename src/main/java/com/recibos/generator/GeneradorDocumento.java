package com.recibos.generator;

import com.recibos.model.Recibo;
import com.recibos.model.TipoRecibo;
import com.recibos.util.FormatoBolivar;
import com.recibos.util.NumerosEnLetras;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;

/**
 * Generador de documentos Word (.docx) para recibos de pago.
 *
 * <p>
 * Replica con exactitud el formato del documento modelo:
 * <ul>
 * <li>Encabezado: Bookman Old Style Bold 14pt (empresa + RIF)</li>
 * <li>Título: Times New Roman 16pt centrado</li>
 * <li>Monto total: Times New Roman 14pt centrado</li>
 * <li>Cuerpo: Times New Roman 11pt con negritas específicas</li>
 * <li>Tabla 2 columnas: Concepto / Monto (Bs.)</li>
 * <li>Pie: fecha, firma</li>
 * </ul>
 * </p>
 */
public class GeneradorDocumento {

    // Fuentes del documento
    private static final String FUENTE_ENCABEZADO = "Bookman Old Style";
    private static final String FUENTE_CUERPO = "Times New Roman";

    // Tamaños en half-points (OOXML: sz = puntos × 2)
    private static final int SZ_ENCABEZADO = 28; // 14pt
    private static final int SZ_TITULO = 32; // 16pt
    private static final int SZ_MONTO_HDR = 28; // 14pt
    private static final int SZ_CUERPO = 24; // 12pt → usamos 24 para mayor legibilidad
    private static final int SZ_TABLA = 22; // 11pt

    /**
     * Genera el archivo .docx basado en los datos del recibo.
     *
     * @param recibo     Datos del recibo ya calculados
     * @param directorio Carpeta donde se guardará el archivo
     * @return Ruta absoluta del archivo generado
     * @throws IOException Si ocurre un error al escribir el archivo
     */
    public Path generarDocumento(Recibo recibo, Path directorio) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {

            // ── Configurar márgenes de página (igual al modelo) ──────────────
            configurarPagina(doc);

            // ── 1. Encabezado con nombre de empresa y RIF ────────────────────
            crearEncabezado(doc, recibo);

            // ── 2. Párrafo vacío inicial ─────────────────────────────────────
            doc.createParagraph();

            // ── 3. Título principal: RECIBO DE PAGO ──────────────────────────
            crearTitulo(doc, "RECIBO DE PAGO");
            crearTitulo(doc, "");
            if (recibo.getTipo() == TipoRecibo.ORDINARIO) {
                crearTitulo(doc, "Salario y Cestaticket");
            } else {
                crearTitulo(doc, "Salario Especial por Día Feriado");
            }
            crearTitulo(doc, "");

            // ── 4. Monto total en Bs. (subtítulo grande) ──────────────────────
            crearMontoEncabezado(doc, recibo);
            crearTitulo(doc, "");

            // ── 5. Párrafo declaratorio ──────────────────────────────────────
            crearParrafoDeclaratorio(doc, recibo);

            // ── 6. Párrafo de Cestaticket Socialista ─────────────────────────
            crearParrafoCestaticket(doc, recibo);

            // ── 7. Párrafo beneficio social ──────────────────────────────────
            crearParrafoBeneficio(doc, recibo);

            // ── 8. Espacio antes de tabla ────────────────────────────────────
            XWPFParagraph espacioTabla = doc.createParagraph();
            espacioTabla.setAlignment(ParagraphAlignment.CENTER);

            // ── 9. Tabla de conceptos ────────────────────────────────────────
            crearTabla(doc, recibo);

            // ── 10. Espacio y pie de firma ───────────────────────────────────
            doc.createParagraph();
            crearPie(doc, recibo);

            // ── Guardar archivo ──────────────────────────────────────────────
            Path rutaArchivo = directorio.resolve(recibo.getNombreArchivo());
            try (FileOutputStream fos = new FileOutputStream(rutaArchivo.toFile())) {
                doc.write(fos);
            }
            return rutaArchivo;
        }
    }

    // ======================================================================
    // MÉTODOS PRIVADOS
    // ======================================================================

    private void configurarPagina(XWPFDocument doc) {
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        CTPageSz pgSz = sectPr.addNewPgSz();
        pgSz.setW(BigInteger.valueOf(12240)); // 8.5 pulgadas en twips
        pgSz.setH(BigInteger.valueOf(15840)); // 11 pulgadas en twips

        CTPageMar pgMar = sectPr.addNewPgMar();
        pgMar.setTop(BigInteger.valueOf(1417));
        pgMar.setRight(BigInteger.valueOf(1701));
        pgMar.setBottom(BigInteger.valueOf(1417));
        pgMar.setLeft(BigInteger.valueOf(1701));
        pgMar.setHeader(BigInteger.valueOf(720));
        pgMar.setFooter(BigInteger.valueOf(720));
    }

    private void crearEncabezado(XWPFDocument doc, Recibo recibo) {
        XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);

        // Línea 1: Nombre de la empresa
        XWPFParagraph p1 = header.createParagraph();
        p1.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun r1 = p1.createRun();
        aplicarFuenteEncabezado(r1, FUENTE_ENCABEZADO, SZ_ENCABEZADO, true);
        r1.setText(recibo.getEmpresa().getNombre() + " ");

        // Línea 2: RIF
        XWPFParagraph p2 = header.createParagraph();
        p2.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun r2 = p2.createRun();
        aplicarFuenteEncabezado(r2, FUENTE_ENCABEZADO, SZ_ENCABEZADO, true);
        r2.setText("RIF: " + recibo.getEmpresa().getRif());

        // Línea 3: vacía (separación)
        header.createParagraph();
    }

    private void crearTitulo(XWPFDocument doc, String texto) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        if (!texto.isEmpty()) {
            XWPFRun r = p.createRun();
            aplicarFuente(r, FUENTE_CUERPO, SZ_TITULO, false);
            r.setText(texto);
        }
    }

    private void crearMontoEncabezado(XWPFDocument doc, Recibo recibo) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        aplicarFuente(r, FUENTE_CUERPO, SZ_MONTO_HDR, false);
        // El total del encabezado es el total a pagar (70 USD × tasa)
        r.setText(FormatoBolivar.formatearConPrefijo(recibo.getTotalBs()));
    }

    /**
     * Párrafo principal que declara el pago:
     * "Yo, [NOMBRE EN NEGRITA], titular de la Cédula de Identidad N° [CI EN
     * NEGRITA],
     * trabajador de la empresa [EMPRESA EN NEGRITA], hago constar que he recibido
     * de mi patrono la cantidad de [MONTO EN LETRAS EN NEGRITA] (Bs.[MONTO,XX]),
     * monto equivalente a [X] Dólares Americanos ($[XX]) a la tasa oficial del BCV
     * vigente el día de hoy [FECHA] por concepto de: [CONCEPTO EN NEGRITA],
     * otorgado por la empresa..."
     */
    private void crearParrafoDeclaratorio(XWPFDocument doc, Recibo recibo) {
        String totalLetras = NumerosEnLetras.soloLetras(recibo.getTotalBs());
        String totalFormateado = FormatoBolivar.formatear(recibo.getTotalBs());

        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.BOTH);

        run(p, "Yo, ", false);
        run(p, recibo.getEmpleado().getNombreCompleto(), true);
        run(p, ", titular de la Cédula de Identidad N° ", false);
        run(p, recibo.getEmpleado().getCedula(), true);
        run(p, ", trabajador de la empresa ", false);
        run(p, recibo.getEmpresa().getNombre(), true);
        run(p, ", hago constar que he recibido de mi patrono la cantidad de ", false);
        run(p, totalLetras.toUpperCase() + " BOLIVARES (Bs." + totalFormateado + ")", true);

        if (recibo.getTipo() == TipoRecibo.ORDINARIO) {
            run(p, ", monto equivalente a Setenta Dólares Americanos ($70) a la tasa oficial del BCV" +
                    " vigente el día de hoy " + recibo.getFechaCorta() + " por concepto de: ", false);
            run(p, "SALARIO BÁSICO Y CESTATICKET SOCIALISTA", true);
            run(p, ", otorgado por la empresa de manera voluntaria con la finalidad de ayudar al" +
                    " trabajador a cubrir los gastos de asistencia y facilitar su movilización hasta" +
                    " su lugar de trabajo.", false);
        } else {
            run(p, ", monto equivalente a un (01) día feriado laborado, calculado con base en la " +
                    "tasa del dólar oficial del BCV vigente para el día hábil inmediato anterior, conforme " +
                    "a los lineamientos establecidos por la empresa.", false);
        }
    }

    private void crearParrafoCestaticket(XWPFDocument doc, Recibo recibo) {
        doc.createParagraph(); // Párrafo vacío de separación

        if (recibo.getTipo() == TipoRecibo.ORDINARIO) {
            XWPFParagraph p1 = doc.createParagraph();
            p1.setAlignment(ParagraphAlignment.BOTH);
            run(p1, "Este bono comprende un ", false);
            XWPFRun rSubr = p1.createRun();
            aplicarFuente(rSubr, FUENTE_CUERPO, SZ_CUERPO, true);
            rSubr.setUnderline(UnderlinePatterns.SINGLE);
            rSubr.setText("beneficio social de carácter no remunerativo, ni salarial");
            run(p1, ", en virtud de lo convenido en la Cláusula Sexta del Contrato " +
                    "Individual de Trabajo, conforme a lo establecido en el artículo " +
                    "105 de la Ley Orgánica del Trabajo, los Trabajadores y las " +
                    "Trabajadoras (LOTTT), y según lo dispuesto en la Ley del " +
                    "Cestaticket Socialista para los Trabajadores y las Trabajadoras, " +
                    "y la Gaceta Oficial Extraordinaria de la República Bolivariana " +
                    "de Venezuela N° 6.746 de fecha 01/05/2023, en las cuales se regula " +
                    "este beneficio.", false);
        } else {
            XWPFParagraph p1 = doc.createParagraph();
            p1.setAlignment(ParagraphAlignment.BOTH);
            run(p1, "Dicho pago corresponde al día feriado del ", false);
            run(p1, recibo.getFechaFormateada(), true);
            run(p1, ", y comprende el salario correspondiente al día más un recargo del " +
                    "cincuenta por ciento (50%) sobre el valor del día ordinario, de " +
                    "conformidad con lo establecido en el artículo 119 de la Ley Orgánica " +
                    "del Trabajo, los Trabajadores y las Trabajadoras (LOTTT), que regula el " +
                    "pago de días feriados laborados.", false);
        }
    }

    private void crearParrafoBeneficio(XWPFDocument doc, Recibo recibo) {
        // Doble salto de línea antes del párrafo final de beneficio
        XWPFParagraph pSep = doc.createParagraph();
        pSep.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun rBr1 = pSep.createRun();
        rBr1.addBreak();

        if (recibo.getTipo() == TipoRecibo.ORDINARIO) {
            run(pSep, "En tal sentido, el monto recibido ", false);
            XWPFRun rNeg = pSep.createRun();
            aplicarFuente(rNeg, FUENTE_CUERPO, SZ_CUERPO, true);
            rNeg.setText("no genera incidencia salarial alguna");
            run(pSep, ", ni constituye base de cálculo para prestaciones sociales, vacaciones, " +
                    "utilidades ni ningún otro beneficio derivado de la relación laboral, por " +
                    "tratarse de un ", false);
            run(pSep, "beneficio de carácter social", true);
            run(pSep, ".", false);
        } else {
            run(pSep, "En tal sentido, el monto recibido ", false);
            XWPFRun rNeg = pSep.createRun();
            aplicarFuente(rNeg, FUENTE_CUERPO, SZ_CUERPO, true);
            rNeg.setText("no constituye salario base");
            run(pSep, " para el cálculo de prestaciones sociales, vacaciones, utilidades ni " +
                    "ningún otro beneficio derivado de la relación laboral, salvo en los casos " +
                    "expresamente previstos por la ley.", false);
        }
    }

    private void crearTabla(XWPFDocument doc, Recibo recibo) {
        XWPFTable tabla = doc.createTable(4, 2);

        // Ancho total de la tabla (igual al modelo: 8314 dxa)
        CTTblPr tblPr = tabla.getCTTbl().getTblPr();
        CTTblWidth tblW = tblPr.addNewTblW();
        tblW.setW(BigInteger.valueOf(8314));
        tblW.setType(STTblWidth.DXA);

        // Espaciado entre celdas (CTTblWidth es el tipo correcto para tblCellSpacing)
        CTTblWidth spacing = tblPr.addNewTblCellSpacing();
        spacing.setW(BigInteger.valueOf(15));
        spacing.setType(STTblWidth.DXA);

        // ── Fila 0: Encabezado ────────────────────────────────────────────
        setCeldaTabla(tabla, 0, 0, "Concepto", true, SZ_TABLA);
        setCeldaTabla(tabla, 0, 1, "Monto (Bs.)", true, SZ_TABLA);

        // ── Filas 1 y 2: Conceptos ────────────────────────────────────────
        if (recibo.getTipo() == TipoRecibo.ORDINARIO) {
            setCeldaTabla(tabla, 1, 0, "Salario Básico", false, SZ_TABLA);
            setCeldaTabla(tabla, 1, 1, FormatoBolivar.formatear(recibo.getSalarioBs()), false, SZ_TABLA);
            setCeldaTabla(tabla, 2, 0, "Cestaticket", false, SZ_TABLA);
            setCeldaTabla(tabla, 2, 1, FormatoBolivar.formatear(recibo.getCestaticketBs()), false, SZ_TABLA);
        } else {
            setCeldaTabla(tabla, 1, 0, "Valor día ordinario", false, SZ_TABLA);
            setCeldaTabla(tabla, 1, 1, FormatoBolivar.formatear(recibo.getSalarioBs()), false, SZ_TABLA);
            setCeldaTabla(tabla, 2, 0, "Recargo 50% sobre día feriado", false, SZ_TABLA);
            setCeldaTabla(tabla, 2, 1, FormatoBolivar.formatear(recibo.getCestaticketBs()), false, SZ_TABLA);
        }

        // ── Fila 3: Total a Pagar ─────────────────────────────────────────
        String totalTexto = FormatoBolivar.formatear(recibo.getTotalBs())
                + " (" + NumerosEnLetras.soloLetras(recibo.getTotalBs()).toLowerCase() + ")";
        setCeldaTabla(tabla, 3, 0, "Total a Pagar", true, SZ_TABLA);
        setCeldaTabla(tabla, 3, 1, totalTexto, false, SZ_TABLA);
    }

    private void setCeldaTabla(XWPFTable tabla, int fila, int col, String texto, boolean negrita, int sz) {
        XWPFTableCell celda = tabla.getRow(fila).getCell(col);
        // Limpiar los párrafos existentes y agregar uno nuevo limpio
        celda.removeParagraph(0);
        XWPFParagraph p = celda.addParagraph();
        XWPFRun r = p.createRun();
        aplicarFuente(r, FUENTE_CUERPO, sz, negrita);
        r.setText(texto);
    }

    private void crearPie(XWPFDocument doc, Recibo recibo) {
        // "Lugar y fecha: Caracas, DD de Mes de AAAA"
        XWPFParagraph pFecha = doc.createParagraph();
        XWPFRun rFecha = pFecha.createRun();
        aplicarFuente(rFecha, FUENTE_CUERPO, SZ_TABLA, false);
        rFecha.setText("Lugar y fecha: Caracas, " + recibo.getFechaFormateada());

        // "Recibe conforme:"
        XWPFParagraph pRecibe = doc.createParagraph();
        XWPFRun rRecibe = pRecibe.createRun();
        aplicarFuente(rRecibe, FUENTE_CUERPO, SZ_TABLA, false);
        rRecibe.setText("Recibe conforme:");

        // Nombre del empleado
        XWPFParagraph pNombre = doc.createParagraph();
        XWPFRun rNombre = pNombre.createRun();
        aplicarFuente(rNombre, FUENTE_CUERPO, SZ_TABLA, false);
        rNombre.setText(recibo.getEmpleado().getNombreCompleto());

        // Cédula
        XWPFParagraph pCI = doc.createParagraph();
        XWPFRun rCI = pCI.createRun();
        aplicarFuente(rCI, FUENTE_CUERPO, SZ_TABLA, false);
        rCI.setText("C.I. " + recibo.getEmpleado().getCedula());

        // Espacio
        doc.createParagraph();

        // Firma
        XWPFParagraph pFirma = doc.createParagraph();
        XWPFRun rFirma = pFirma.createRun();
        aplicarFuente(rFirma, FUENTE_CUERPO, SZ_TABLA, false);
        rFirma.setText("Firma: ___________________________");
    }

    // ──────────────────────────────────────────────────────────────────────
    // HELPERS DE FORMATO
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Crea un run con texto normal o en negrita en el párrafo dado.
     */
    private XWPFRun run(XWPFParagraph p, String texto, boolean negrita) {
        XWPFRun r = p.createRun();
        aplicarFuente(r, FUENTE_CUERPO, SZ_CUERPO, negrita);
        r.setText(texto);
        return r;
    }

    /**
     * Aplica fuente, tamaño y negrita a un run del cuerpo del documento.
     */
    private void aplicarFuente(XWPFRun r, String fuente, int sz, boolean negrita) {
        r.setFontFamily(fuente);
        r.setFontSize(sz / 2); // POI usa puntos enteros
        r.setBold(negrita);
        r.setColor("000000");
    }

    /**
     * Aplica fuente especial para el encabezado (Bookman Old Style).
     */
    private void aplicarFuenteEncabezado(XWPFRun r, String fuente, int sz, boolean negrita) {
        r.setFontFamily(fuente);
        r.setFontSize(sz / 2);
        r.setBold(negrita);
        r.setColor("000000");
    }
}
