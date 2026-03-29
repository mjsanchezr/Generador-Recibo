import { Document, Paragraph, TextRun, Table, TableRow, TableCell, AlignmentType, WidthType, Packer, BorderStyle } from "docx";
import { saveAs } from "file-saver";
import { type ReciboModel, calcularRecibo, getFechaCorta, getFechaFormateada, getNombreArchivo } from "./calculadora";
import { soloLetras, formatearBolivar } from "./numerosEnLetras";

export async function generarDocumento(recibo: ReciboModel) {
    const calcs = calcularRecibo(recibo.tipo, recibo.tasaBCV);
    const totalBsStr = formatearBolivar(calcs.totalBs);
    const totalLetras = soloLetras(calcs.totalBs).toUpperCase() + " BOLIVARES (Bs." + totalBsStr + ")";

    const runNormal = (text: string) => new TextRun({ text, font: "Times New Roman", size: 22 });
    const runBold = (text: string) => new TextRun({ text, font: "Times New Roman", size: 22, bold: true });
    const runUnderline = (text: string) => new TextRun({ text, font: "Times New Roman", size: 22, underline: {} });

    const declarativoRuns = [
        runNormal("Yo, "),
        runBold(recibo.nombre),
        runNormal(", titular de la Cédula de Identidad N° "),
        runBold(recibo.cedula),
        runNormal(", trabajador de la empresa "),
        runBold("CARDAMOMO Y CELERY, C.A."),
        runNormal(", hago constar que he recibido de mi patrono la cantidad de "),
        runBold(totalLetras)
    ];

    if (recibo.tipo === "ORDINARIO") {
        declarativoRuns.push(
            runNormal(`, monto equivalente a Setenta Dólares Americanos ($70) a la tasa oficial del BCV vigente el día de hoy ${getFechaCorta(recibo.fecha)} por concepto de: `),
            runBold("SALARIO BÁSICO Y CESTATICKET SOCIALISTA"),
            runNormal(", otorgado por la empresa de manera voluntaria con la finalidad de ayudar al trabajador a cubrir los gastos de asistencia y facilitar su movilización hasta su lugar de trabajo.")
        );
    } else if (recibo.tipo === "FERIADO") {
        declarativoRuns.push(
            runNormal(`, monto equivalente a un (01) día feriado laborado, calculado con base en la tasa del dólar oficial del BCV vigente para el día hábil inmediato anterior, conforme a los lineamientos establecidos por la empresa.`)
        );
    } else {
        declarativoRuns.push(
            runNormal(`, por concepto del pago de una (01) hora extraordinaria laborada (en el horario comprendido de 7:00 pm a 8:00 pm), calculada con base en la tasa del dólar oficial del BCV vigente para el día hábil inmediato anterior, conforme a los lineamientos establecidos por la empresa.`)
        );
    }

    let cestaticketRuns: TextRun[] = [];
    if (recibo.tipo === "ORDINARIO") {
        cestaticketRuns = [
            runNormal("Este bono comprende un "),
            runUnderline("beneficio social de carácter no remunerativo, ni salarial"),
            runNormal(", en virtud de lo convenido en la Cláusula Sexta del Contrato Individual de Trabajo, conforme a lo establecido en el artículo 105 de la Ley Orgánica del Trabajo, los Trabajadores y las Trabajadoras (LOTTT), y según lo dispuesto en la Ley del Cestaticket Socialista para los Trabajadores y las Trabajadoras, y la Gaceta Oficial Extraordinaria de la República Bolivariana de Venezuela N° 6.746 de fecha 01/05/2023, en las cuales se regula este beneficio.")
        ];
    } else if (recibo.tipo === "FERIADO") {
        cestaticketRuns = [
            runNormal("Dicho pago corresponde al día feriado del "),
            runBold(getFechaFormateada(recibo.fecha)),
            runNormal(", y comprende el salario correspondiente al día más un recargo del cincuenta por ciento (50%) sobre el valor del día ordinario, de conformidad con lo establecido en el artículo 119 de la Ley Orgánica del Trabajo, los Trabajadores y las Trabajadoras (LOTTT), que regula el pago de días feriados laborados.")
        ];
    } else {
        cestaticketRuns = [
            runNormal("Dicho pago corresponde a la labor extraordinaria realizada el día "),
            runBold(getFechaFormateada(recibo.fecha)),
            runNormal(", y comprende el valor de una (01) hora de labor calculada sobre el salario ordinario más un recargo del cincuenta por ciento (50%), de conformidad con lo establecido en el artículo 118 de la Ley Orgánica del Trabajo, los Trabajadores y las Trabajadoras (LOTTT), que regula el pago de las horas extraordinarias.")
        ];
    }

    let finalRuns: TextRun[] = [];
    if (recibo.tipo === "ORDINARIO") {
        finalRuns = [
            runNormal("En tal sentido, el monto recibido "),
            runBold("no genera incidencia salarial alguna"),
            runNormal(", ni constituye base de cálculo para prestaciones sociales, vacaciones, utilidades ni ningún otro beneficio derivado de la relación laboral, por tratarse de un "),
            runBold("beneficio de carácter social"),
            runNormal(".")
        ];
    } else {
        finalRuns = [
            runNormal("En tal sentido, el monto recibido "),
            runBold("no constituye salario base"),
            runNormal(" para el cálculo de prestaciones sociales, vacaciones, utilidades ni ningún otro beneficio derivado de la relación laboral, salvo en los casos expresamente previstos por la ley.")
        ];
    }

    let docTitle = "";
    if (recibo.tipo === "ORDINARIO") docTitle = "Salario y Cestaticket";
    else if (recibo.tipo === "FERIADO") docTitle = "Salario Especial por Día Feriado";
    else docTitle = "Pago de Horas Extraordinarias";

    let row1Label = "", row2Label = "";
    if (recibo.tipo === "ORDINARIO") { row1Label = "Salario Básico"; row2Label = "Cestaticket"; }
    else if (recibo.tipo === "FERIADO") { row1Label = "Valor día ordinario"; row2Label = "Recargo 50% sobre día feriado"; }
    else { row1Label = "Valor 1 Hora Ordinaria"; row2Label = "Recargo 50% por Hora Extra"; }

    const border = { style: BorderStyle.SINGLE, size: 2, color: "000000" };
    const cell = (text: string, isBold = false) => new TableCell({
        children: [new Paragraph({ children: [new TextRun({ text, font: "Times New Roman", size: 22, bold: isBold })], alignment: AlignmentType.CENTER })],
        width: { size: 50, type: WidthType.PERCENTAGE },
        margins: { top: 100, bottom: 100 },
        borders: { top: border, bottom: border, left: border, right: border }
    });

    const doc = new Document({
        sections: [{
            properties: {
                page: { margin: { top: 1417, bottom: 1417, left: 1701, right: 1701 } }
            },
            children: [
                new Paragraph({
                    alignment: AlignmentType.CENTER,
                    children: [new TextRun({ text: "CARDAMOMO Y CELERY, C.A.", font: "Bookman Old Style", size: 28, bold: true })]
                }),
                new Paragraph({
                    alignment: AlignmentType.CENTER,
                    children: [new TextRun({ text: "RIF: J-407755994", font: "Bookman Old Style", size: 28, bold: true })]
                }),
                new Paragraph({ text: "" }),
                new Paragraph({ text: "" }),
                new Paragraph({
                    alignment: AlignmentType.CENTER,
                    children: [new TextRun({ text: "RECIBO DE PAGO", font: "Times New Roman", size: 32, bold: true })]
                }),
                new Paragraph({
                    alignment: AlignmentType.CENTER,
                    children: [new TextRun({ text: docTitle, font: "Times New Roman", size: 32, bold: true })]
                }),
                new Paragraph({ text: "" }),
                new Paragraph({
                    alignment: AlignmentType.CENTER,
                    children: [new TextRun({ text: `Bs. ${totalBsStr}`, font: "Times New Roman", size: 28, bold: true })]
                }),
                new Paragraph({ text: "" }),
                new Paragraph({ alignment: AlignmentType.BOTH, children: declarativoRuns }),
                new Paragraph({ text: "" }),
                new Paragraph({ alignment: AlignmentType.BOTH, children: cestaticketRuns }),
                new Paragraph({ text: "" }),
                new Paragraph({ text: "" }),
                new Paragraph({ alignment: AlignmentType.BOTH, children: finalRuns }),
                new Paragraph({ text: "" }),
                new Paragraph({ text: "" }),
                new Table({
                    width: { size: 100, type: WidthType.PERCENTAGE },
                    borders: { top: border, bottom: border, left: border, right: border, insideHorizontal: border, insideVertical: border },
                    rows: [
                        new TableRow({ children: [cell("Concepto", true), cell("Monto (Bs.)", true)] }),
                        new TableRow({ children: [cell(row1Label), cell(formatearBolivar(calcs.salarioBs))] }),
                        new TableRow({ children: [cell(row2Label), cell(formatearBolivar(calcs.cestaticketBs))] }),
                        new TableRow({ children: [cell("Total a Pagar", true), cell(totalBsStr, true)] })
                    ]
                }),
                new Paragraph({ text: "" }),
                new Paragraph({ text: "" }),
                new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: `Caracas, ${getFechaFormateada(new Date())}`, font: "Times New Roman", size: 22 })] }),
                new Paragraph({ text: "" }),
                new Paragraph({ text: "" }),
                new Paragraph({ text: "" }),
                new Paragraph({ text: "" }),
                new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "____________________________________", font: "Times New Roman", size: 22 })] }),
                new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: recibo.nombre, font: "Times New Roman", size: 22, bold: true })] }),
                new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: recibo.cedula, font: "Times New Roman", size: 22 })] })
            ]
        }]
    });

    const blob = await Packer.toBlob(doc);
    saveAs(blob, getNombreArchivo(recibo.tipo, recibo.fecha));
}
