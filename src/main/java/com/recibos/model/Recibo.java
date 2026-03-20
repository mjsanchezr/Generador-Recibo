package com.recibos.model;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Modelo de un Recibo de Pago.
 *
 * <p>
 * Encapsula todos los datos necesarios para generar el recibo:
 * fecha de pago, tasa BCV y los montos calculados automáticamente
 * en base a los valores fijos de salario ($30) y cestaticket ($40).
 * </p>
 */
public class Recibo {

    // Valores fijos en USD (según solicitud del usuario)
    public static final double SALARIO_USD = 30.0;
    public static final double CESTATICKET_USD = 40.0;
    public static final double TOTAL_USD = SALARIO_USD + CESTATICKET_USD;

    private final LocalDate fecha;
    private final double tasaBCV;
    private final double salarioBs;
    private final double cestaticketBs;
    private final double totalBs;
    private final Empleado empleado;
    private final DatosEmpresa empresa;

    /**
     * Constructor principal. Calcula automáticamente los montos en bolívares.
     *
     * @param fecha    Fecha del recibo
     * @param tasaBCV  Tasa oficial del BCV (Bs. por 1 USD)
     * @param empleado Datos del empleado
     * @param empresa  Datos de la empresa
     */
    public Recibo(LocalDate fecha, double tasaBCV, Empleado empleado, DatosEmpresa empresa) {
        this.fecha = fecha;
        this.tasaBCV = tasaBCV;
        this.empleado = empleado;
        this.empresa = empresa;
        // Cálculo automático
        this.salarioBs = SALARIO_USD * tasaBCV;
        this.cestaticketBs = CESTATICKET_USD * tasaBCV;
        this.totalBs = TOTAL_USD * tasaBCV;
    }

    /**
     * Crea un recibo con los datos por defecto de empresa y empleada.
     *
     * @param fecha   Fecha del recibo
     * @param tasaBCV Tasa BCV del día
     * @return Recibo con los datos de CARDAMOMO Y CELERY / YSAURA FAGUNDEZ
     */
    public static Recibo crear(LocalDate fecha, double tasaBCV) {
        return new Recibo(fecha, tasaBCV, Empleado.porDefecto(), DatosEmpresa.porDefecto());
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public LocalDate getFecha() {
        return fecha;
    }

    public double getTasaBCV() {
        return tasaBCV;
    }

    public double getSalarioBs() {
        return salarioBs;
    }

    public double getCestaticketBs() {
        return cestaticketBs;
    }

    public double getTotalBs() {
        return totalBs;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public DatosEmpresa getEmpresa() {
        return empresa;
    }

    /**
     * Devuelve el nombre del mes en español (mayúsculas).
     * Ejemplo: "OCTUBRE"
     */
    public String getMesEnEspanol() {
        return fecha.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("es", "VE"))
                .toUpperCase();
    }

    /**
     * Devuelve la fecha formateada para el pie del documento.
     * Ejemplo: "30 de Octubre de 2023"
     */
    public String getFechaFormateada() {
        String mes = fecha.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("es", "VE"));
        // Capitaliza primera letra
        mes = Character.toUpperCase(mes.charAt(0)) + mes.substring(1);
        return fecha.getDayOfMonth() + " de " + mes + " de " + fecha.getYear();
    }

    /**
     * Devuelve la fecha corta en formato dd-MM-yyyy para el texto del cuerpo.
     * Ejemplo: "31-10-2023"
     */
    public String getFechaCorta() {
        return String.format("%02d-%02d-%04d",
                fecha.getDayOfMonth(), fecha.getMonthValue(), fecha.getYear());
    }

    /**
     * Genera el nombre sugerido para el archivo de salida.
     * Ejemplo: "RECIBO_PAGO_OCTUBRE_2023.docx"
     */
    public String getNombreArchivo() {
        return "RECIBO_PAGO_" + getMesEnEspanol() + "_" + fecha.getYear() + ".docx";
    }

    @Override
    public String toString() {
        return String.format(
                "Recibo[fecha=%s, tasa=%.2f, salario=%.2f, cestaticket=%.2f, total=%.2f]",
                fecha, tasaBCV, salarioBs, cestaticketBs, totalBs);
    }
}
