package com.recibos.model;

/**
 * Define los tipos de recibo soportados por la aplicación.
 */
public enum TipoRecibo {
    /**
     * Modelo 1: Salario ($30) y Cestaticket ($40). Total $70.
     * Textos e indemnizaciones por ley.
     */
    ORDINARIO,

    /**
     * Modelo 2: Día Feriado laborado.
     * Día ordinario ($1) + Recargo 50% ($0.5). Total $1.5.
     * Textos declaratorios especiales.
     */
    FERIADO,

    /**
     * Modelo 3: Horas Extraordinarias.
     * 1 hora extra ($0.125) + Recargo 50% ($0.0625). Total $0.1875.
     * Textos sobre pago de 1 hora de 7pm a 8pm.
     */
    HORA_EXTRA
}
