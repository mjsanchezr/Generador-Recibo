package com.recibos.model;

/**
 * Datos de la empresa empleadora.
 * Constantes de la empresa CARDAMOMO Y CELERY, C.A.
 */
public class DatosEmpresa {

    private final String nombre;
    private final String rif;

    /**
     * Constructor con todos los datos de la empresa.
     *
     * @param nombre Nombre de la empresa
     * @param rif    Registro de Información Fiscal (RIF)
     */
    public DatosEmpresa(String nombre, String rif) {
        this.nombre = nombre;
        this.rif = rif;
    }

    /**
     * Crea la instancia de empresa por defecto (CARDAMOMO Y CELERY, C.A.)
     *
     * @return Instancia con los datos de la empresa
     */
    public static DatosEmpresa porDefecto() {
        return new DatosEmpresa("CARDAMOMO Y CELERY, C.A.", "J-407755994");
    }

    public String getNombre() {
        return nombre;
    }

    public String getRif() {
        return rif;
    }

    @Override
    public String toString() {
        return nombre + " | RIF: " + rif;
    }
}
