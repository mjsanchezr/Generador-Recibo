package com.recibos.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utilidad para formatear cantidades en bolívares venezolanos.
 *
 * <p>
 * Usa el formato venezolano:
 * <ul>
 * <li>Separador de miles: punto (.)</li>
 * <li>Separador decimal: coma (,)</li>
 * <li>Dos decimales</li>
 * </ul>
 *
 * <p>
 * Ejemplos:
 * <ul>
 * <li>1016.10 → "1.016,10"</li>
 * <li>2370.90 → "2.370,90"</li>
 * <li>900.00 → "900,00"</li>
 * </ul>
 * </p>
 */
public class FormatoBolivar {

    private static final DecimalFormat FORMATO;

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        FORMATO = new DecimalFormat("#,##0.00", simbolos);
    }

    /**
     * Formatea un valor con separador de miles (.) y decimales (,).
     *
     * @param monto Valor numérico
     * @return Cadena formateada (ej: "1.016,10")
     */
    public static String formatear(double monto) {
        return FORMATO.format(monto);
    }

    /**
     * Formatea un valor redondeado a entero, con separador de miles.
     * Útil para mostrar en tabla cuando los centimos son 0.
     *
     * @param monto Valor numérico
     * @return Cadena formateada sin decimales (ej: "1.016")
     */
    public static String formatearEntero(double monto) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        DecimalFormat fmt = new DecimalFormat("#,##0", simbolos);
        return fmt.format(Math.round(monto));
    }

    /**
     * Formatea con el prefijo "Bs. " listo para usar en el documento.
     *
     * @param monto Valor numérico
     * @return Cadena prefijada (ej: "Bs. 1.016,10")
     */
    public static String formatearConPrefijo(double monto) {
        return "Bs. " + formatear(monto);
    }
}
