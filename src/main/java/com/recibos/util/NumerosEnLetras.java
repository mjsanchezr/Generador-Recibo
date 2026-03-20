package com.recibos.util;

/**
 * Convierte números decimales a texto en español venezolano (mayúsculas).
 *
 * <p>
 * Ejemplos:
 * <ul>
 * <li>1016.10 → "MIL DIECISEIS CON 10 CENTIMOS"</li>
 * <li>2370.90 → "DOS MIL TRESCIENTOS SETENTA CON 90 CENTIMOS"</li>
 * <li>300.00 → "TRESCIENTOS BOLIVARES EXACTOS"</li>
 * </ul>
 * </p>
 */
public class NumerosEnLetras {

    private static final String[] UNIDADES = {
            "", "UN", "DOS", "TRES", "CUATRO", "CINCO",
            "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ",
            "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE",
            "DIECISÉIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE",
            "VEINTIÚN", "VEINTIDÓS", "VEINTITRÉS", "VEINTICUATRO", "VEINTICINCO",
            "VEINTISÉIS", "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE"
    };

    private static final String[] DECENAS = {
            "", "DIEZ", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA",
            "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"
    };

    private static final String[] CENTENAS = {
            "", "CIEN", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
            "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
    };

    /**
     * Convierte un valor en bolívares a su representación textual completa.
     * Formato: "MONTO BOLIVARES [EXACTOS|CON xx CENTIMOS]"
     *
     * @param monto Monto en bolívares (máximo 999.999.999,99)
     * @return Texto en mayúsculas representando el monto
     */
    public static String convertir(double monto) {
        long parteEntera = (long) monto;
        int centimos = (int) Math.round((monto - parteEntera) * 100);

        String textoEntero = convertirEntero(parteEntera);

        if (centimos == 0) {
            return textoEntero + " BOLIVARES EXACTOS";
        } else {
            return textoEntero + " CON " + centimos + " CENTIMOS BOLIVARES";
        }
    }

    /**
     * Convierte solo la parte entera de un número a letras.
     *
     * @param numero Número entero positivo
     * @return Texto en mayúsculas
     */
    public static String convertirEntero(long numero) {
        if (numero == 0)
            return "CERO";
        if (numero < 0)
            return "MENOS " + convertirEntero(-numero);

        StringBuilder resultado = new StringBuilder();

        if (numero >= 1_000_000_000L) {
            long miles = numero / 1_000_000_000L;
            resultado.append(convertirEntero(miles)).append(" MIL MILLONES");
            numero %= 1_000_000_000L;
            if (numero > 0)
                resultado.append(" ");
        }

        if (numero >= 1_000_000L) {
            long millones = numero / 1_000_000L;
            if (millones == 1) {
                resultado.append("UN MILLÓN");
            } else {
                resultado.append(convertirEntero(millones)).append(" MILLONES");
            }
            numero %= 1_000_000L;
            if (numero > 0)
                resultado.append(" ");
        }

        if (numero >= 1_000L) {
            long miles = numero / 1_000L;
            if (miles == 1) {
                resultado.append("MIL");
            } else {
                resultado.append(convertirEntero(miles)).append("MIL");
            }
            numero %= 1_000L;
            if (numero > 0)
                resultado.append(" ");
        }

        if (numero >= 100L) {
            int centenaIdx = (int) (numero / 100);
            if (numero == 100) {
                resultado.append("CIEN");
            } else {
                resultado.append(CENTENAS[centenaIdx]);
            }
            numero %= 100L;
            if (numero > 0)
                resultado.append(" ");
        }

        if (numero >= 30L) {
            int decenaIdx = (int) (numero / 10);
            resultado.append(DECENAS[decenaIdx]);
            numero %= 10L;
            if (numero > 0)
                resultado.append(" Y ");
        }

        if (numero > 0 && numero < 30) {
            resultado.append(UNIDADES[(int) numero]);
        }

        return resultado.toString().trim();
    }

    /**
     * Versión que devuelve solo la parte en letras sin la palabra BOLIVARES,
     * útil para construir el texto del recibo con formato personalizado.
     *
     * @param monto Monto en bolívares
     * @return Texto representando el monto (ej: "MIL DIECISEIS CON 10 CENTIMOS")
     */
    public static String soloLetras(double monto) {
        long parteEntera = (long) monto;
        int centimos = (int) Math.round((monto - parteEntera) * 100);

        String textoEntero = convertirEntero(parteEntera);

        if (centimos == 0) {
            return textoEntero;
        } else {
            return textoEntero + " CON " + centimos + " CENTIMOS";
        }
    }
}
