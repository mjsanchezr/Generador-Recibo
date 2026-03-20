# Generador de Recibos de Pago 📄

Aplicación de consola en **Java 22** que genera recibos de pago en formato `.docx` editable para **YSAURA ALEJANDRA FAGUNDEZ COA** en **CARDAMOMO Y CELERY, C.A.**

## Requisitos

- Java 22+
- Maven 3.8+

## Uso

### Primera vez (compilar y ejecutar):
```bash
bash compilar.sh
```

### Ejecuciones posteriores:
```bash
java -jar recibos-app.jar
```

El programa solicitará:
1. **Día** del período de pago
2. **Mes** (1–12)
3. **Año**
4. **Tasa BCV del día** (Bs. por 1 USD)

Y generará automáticamente el archivo `RECIBO_PAGO_[MES]_[AÑO].docx` en la carpeta del programa.

## Cálculos

| Concepto       | USD | Bs. (calculado) |
|----------------|-----|-----------------|
| Salario Básico | $30 | $30 × tasa BCV  |
| Cestaticket    | $40 | $40 × tasa BCV  |
| **Total**      | **$70** | **$70 × tasa BCV** |

## Estructura del proyecto

```
src/main/java/com/recibos/
├── model/           → Empleado, DatosEmpresa, Recibo
├── util/            → NumerosEnLetras, FormatoBolivar
├── generator/       → GeneradorDocumento (Apache POI)
└── app/             → Main (menú interactivo)
```
