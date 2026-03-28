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

    // Valores fijos en USD para ORDINARIO
    public static final double SALARIO_ORDINARIO_USD = 30.0;
    public static final double CESTATICKET_ORDINARIO_USD = 40.0;

    // Valores fijos en USD para FERIADO
    public static final double SALARIO_FERIADO_USD = 1.0;
    public static final double RECARGO_FERIADO_USD = 0.5;

    // Valores fijos en USD para HORA_EXTRA
    public static final double SALARIO_HORA_EXTRA_USD = 0.125;
    public static final double RECARGO_HORA_EXTRA_USD = 0.0625;

    private final LocalDate fecha;
    private final double tasaBCV;
    private final TipoRecibo tipo;
    private final double salarioBs;
    private final double cestaticketBs;
    private final double totalBs;
    private final Empleado empleado;
    private final DatosEmpresa empresa;

    /**
     * Constructor principal. Calcula automáticamente los montos en bolívares.
     *
     * @param tipo     Tipo de recibo a generar (ORDINARIO o FERIADO)
     * @param fecha    Fecha del recibo
     * @param tasaBCV  Tasa oficial del BCV (Bs. por 1 USD)
     * @param empleado Datos del empleado
     * @param empresa  Datos de la empresa
     */
    public Recibo(TipoRecibo tipo, LocalDate fecha, double tasaBCV, Empleado empleado, DatosEmpresa empresa) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.tasaBCV = tasaBCV;
        this.empleado = empleado;
        this.empresa = empresa;

        // Cálculo automático según el tipo
        if (tipo == TipoRecibo.ORDINARIO) {
            this.salarioBs = SALARIO_ORDINARIO_USD * tasaBCV;
            this.cestaticketBs = CESTATICKET_ORDINARIO_USD * tasaBCV;
            this.totalBs = (SALARIO_ORDINARIO_USD + CESTATICKET_ORDINARIO_USD) * tasaBCV;
        } else if (tipo == TipoRecibo.FERIADO) {
            // FERIADO: salarioBs es el valor día, cestaticketBs es el recargo
            this.salarioBs = SALARIO_FERIADO_USD * tasaBCV;
            this.cestaticketBs = RECARGO_FERIADO_USD * tasaBCV;
            this.totalBs = (SALARIO_FERIADO_USD + RECARGO_FERIADO_USD) * tasaBCV;
        } else {
            // HORA_EXTRA: salarioBs es el valor de la hora, cestaticketBs es el recargo
            this.salarioBs = SALARIO_HORA_EXTRA_USD * tasaBCV;
            this.cestaticketBs = RECARGO_HORA_EXTRA_USD * tasaBCV;
            this.totalBs = (SALARIO_HORA_EXTRA_USD + RECARGO_HORA_EXTRA_USD) * tasaBCV;
        }
    }

    /**
     * Crea un recibo con los datos por defecto de empresa y empleada.
     *
     * @param tipo    Tipo de recibo
     * @param fecha   Fecha del recibo
     * @param tasaBCV Tasa BCV del día
     * @return Recibo con los datos por defecto
     */
    public static Recibo crear(TipoRecibo tipo, LocalDate fecha, double tasaBCV) {
        return new Recibo(tipo, fecha, tasaBCV, Empleado.porDefecto(), DatosEmpresa.porDefecto());
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public TipoRecibo getTipo() {
        return tipo;
    }

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
                .getDisplayName(TextStyle.FULL, Locale.of("es", "VE"))
                .toUpperCase();
    }

    /**
     * Devuelve la fecha formateada para el pie del documento.
     * Ejemplo: "30 de Octubre de 2023"
     */
    public String getFechaFormateada() {
        String mes = fecha.getMonth()
                .getDisplayName(TextStyle.FULL, Locale.of("es", "VE"));
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
        String prefijo;
        if (tipo == TipoRecibo.ORDINARIO) {
            prefijo = "RECIBO_PAGO_";
        } else if (tipo == TipoRecibo.FERIADO) {
            prefijo = "RECIBO_FERIADO_";
        } else {
            prefijo = "RECIBO_HORAS_EXTRAS_";
        }
        return prefijo + getMesEnEspanol() + "_" + fecha.getYear() + ".docx";
    }

    @Override
    public String toString() {
        return String.format(
                "Recibo[tipo=%s, fecha=%s, tasa=%.2f, bs1=%.2f, bs2=%.2f, total=%.2f]",
                tipo, fecha, tasaBCV, salarioBs, cestaticketBs, totalBs);
    }
}
