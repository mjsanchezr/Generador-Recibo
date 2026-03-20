package com.recibos.model;

/**
 * Datos del empleado.
 * Representa a la trabajadora YSAURA ALEJANDRA FAGUNDEZ COA.
 */
public class Empleado {

    private final String nombreCompleto;
    private final String cedula;

    /**
     * Constructor con datos del empleado.
     *
     * @param nombreCompleto Nombre completo en mayúsculas
     * @param cedula         Cédula de identidad (ej: V-30.370.579)
     */
    public Empleado(String nombreCompleto, String cedula) {
        this.nombreCompleto = nombreCompleto;
        this.cedula = cedula;
    }

    /**
     * Crea el empleado por defecto (YSAURA ALEJANDRA FAGUNDEZ COA).
     *
     * @return Instancia con los datos de la empleada
     */
    public static Empleado porDefecto() {
        return new Empleado("YSAURA ALEJANDRA FAGUNDEZ COA", "V-30.370.579");
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getCedula() {
        return cedula;
    }

    @Override
    public String toString() {
        return nombreCompleto + " | C.I. " + cedula;
    }
}
