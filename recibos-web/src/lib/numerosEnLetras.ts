export const UNIDADES = [
    "", "UN", "DOS", "TRES", "CUATRO", "CINCO",
    "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ",
    "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE",
    "DIECISÉIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE",
    "VEINTIÚN", "VEINTIDÓS", "VEINTITRÉS", "VEINTICUATRO", "VEINTICINCO",
    "VEINTISÉIS", "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE"
];

export const DECENAS = [
    "", "DIEZ", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA",
    "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"
];

export const CENTENAS = [
    "", "CIEN", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
    "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
];

export function convertirEntero(numero: number): string {
    if (numero === 0) return "CERO";
    if (numero < 0) return "MENOS " + convertirEntero(-Math.floor(numero));

    let resultado = "";
    let n = Math.floor(numero);

    if (n >= 1_000_000_000) {
        const miles = Math.floor(n / 1_000_000_000);
        resultado += convertirEntero(miles) + " MIL MILLONES";
        n %= 1_000_000_000;
        if (n > 0) resultado += " ";
    }

    if (n >= 1_000_000) {
        const millones = Math.floor(n / 1_000_000);
        if (millones === 1) {
            resultado += "UN MILLÓN";
        } else {
            resultado += convertirEntero(millones) + " MILLONES";
        }
        n %= 1_000_000;
        if (n > 0) resultado += " ";
    }

    if (n >= 1_000) {
        const miles = Math.floor(n / 1_000);
        if (miles === 1) {
            resultado += "MIL";
        } else {
            resultado += convertirEntero(miles) + " MIL";
        }
        n %= 1_000;
        if (n > 0) resultado += " ";
    }

    if (n >= 100) {
        const centenaIdx = Math.floor(n / 100);
        if (n === 100) {
            resultado += "CIEN";
        } else {
            resultado += CENTENAS[centenaIdx];
        }
        n %= 100;
        if (n > 0) resultado += " ";
    }

    if (n >= 30) {
        const decenaIdx = Math.floor(n / 10);
        resultado += DECENAS[decenaIdx];
        n %= 10;
        if (n > 0) resultado += " Y ";
    }

    if (n > 0 && n < 30) {
        resultado += UNIDADES[n];
    }

    return resultado.trim();
}

export function soloLetras(monto: number): string {
    const parteEntera = Math.floor(monto);
    const centimos = Math.round((monto - parteEntera) * 100);

    const textoEntero = convertirEntero(parteEntera);

    if (centimos === 0) {
        return textoEntero;
    } else {
        const prefijo = textoEntero === "CERO" ? "" : textoEntero + " CON ";
        return prefijo + convertirEntero(centimos) + " CENTIMOS";
    }
}

export function formatearBolivar(monto: number): string {
    return new Intl.NumberFormat("es-VE", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(monto);
}
