export type TipoRecibo = "ORDINARIO" | "FERIADO" | "HORA_EXTRA";

export interface ReciboModel {
    tipo: TipoRecibo;
    nombre: string;
    cedula: string;
    fecha: Date;
    tasaBCV: number;
}

export interface ReciboCalcs {
    salarioBs: number;
    cestaticketBs: number;
    totalBs: number;
}

export const ValoresUSD = {
    ORDINARIO: { base: 30.0, recargo: 40.0 },
    FERIADO: { base: 1.0, recargo: 0.5 },
    HORA_EXTRA: { base: 0.125, recargo: 0.0625 },
};

export function calcularRecibo(tipo: TipoRecibo, tasaBCV: number): ReciboCalcs {
    const vals = ValoresUSD[tipo];
    return {
        salarioBs: Number((vals.base * tasaBCV).toFixed(2)),
        cestaticketBs: Number((vals.recargo * tasaBCV).toFixed(2)),
        totalBs: Number(((vals.base + vals.recargo) * tasaBCV).toFixed(2)),
    };
}

const MESES = [
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
];

export function getMesEnEspanol(fecha: Date): string {
    return MESES[fecha.getMonth()];
}

export function getFechaCorta(fecha: Date): string {
    // EJ: 14/02/2024
    const d = String(fecha.getDate()).padStart(2, '0');
    const m = String(fecha.getMonth() + 1).padStart(2, '0');
    const y = fecha.getFullYear();
    return `${d}/${m}/${y}`;
}

export function getFechaFormateada(fecha: Date): string {
    return `${fecha.getDate()} de ${MESES[fecha.getMonth()].toLowerCase()} de ${fecha.getFullYear()}`;
}

export function getNombreArchivo(tipo: TipoRecibo, fecha: Date): string {
    let prefijo = "";
    if (tipo === "ORDINARIO") prefijo = "RECIBO_PAGO_";
    else if (tipo === "FERIADO") prefijo = "RECIBO_FERIADO_";
    else prefijo = "RECIBO_HORAS_EXTRAS_";

    const d = String(fecha.getDate()).padStart(2, '0');
    const mes = getMesEnEspanol(fecha).toUpperCase();
    return `${prefijo}${d}_${mes}_${fecha.getFullYear()}.docx`;
}
