package com.recibos.app;

import com.recibos.generator.GeneradorDocumento;
import com.recibos.model.Recibo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Punto de entrada principal de la aplicación Generador de Recibos de Pago.
 *
 * <p>
 * Solicita por consola:
 * <ol>
 * <li>El día del período de pago</li>
 * <li>El mes del período de pago</li>
 * <li>El año del período de pago</li>
 * <li>La tasa oficial BCV del día (Bs. por USD)</li>
 * </ol>
 *
 * <p>
 * Luego genera automáticamente el documento Word (.docx) en la misma
 * carpeta donde se encuentra el programa.
 * </p>
 */
public class Main {

    private static final String BANNER = """
            ╔══════════════════════════════════════════════════════╗
            ║        GENERADOR DE RECIBOS DE PAGO                 ║
            ║        CARDAMOMO Y CELERY, C.A.                     ║
            ║        RIF: J-407755994                             ║
            ╠══════════════════════════════════════════════════════╣
            ║  Empleada: YSAURA ALEJANDRA FAGUNDEZ COA            ║
            ║  C.I. V-30.370.579                                  ║
            ╠══════════════════════════════════════════════════════╣
            ║  Salario: $30 | Cestaticket: $40 | Total: $70       ║
            ╚══════════════════════════════════════════════════════╝
            """;

    private static final String[] MESES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    public static void main(String[] args) {
        System.out.println(BANNER);

        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        while (continuar) {
            try {
                // ── Solicitar datos al usuario ────────────────────────────
                int dia = solicitarEntero(scanner, "Ingrese el día del período de pago (1-31): ", 1, 31);
                int mes = solicitarEntero(scanner, "Ingrese el mes         (1=Enero ... 12=Diciembre): ", 1, 12);
                int anio = solicitarEntero(scanner, "Ingrese el año         (ej: 2023): ", 2000, 2100);

                System.out.printf("Ingrese la tasa BCV del día (Bs. por 1 USD, ej: 36.50): ");
                double tasaBCV = solicitarDecimal(scanner);

                // ── Validar fecha ─────────────────────────────────────────
                LocalDate fecha;
                try {
                    fecha = LocalDate.of(anio, mes, dia);
                } catch (Exception e) {
                    System.out.println("⚠  Fecha inválida. Verifique que el día exista en el mes indicado.\n");
                    continue;
                }

                // ── Mostrar resumen para confirmar ─────────────────────────
                System.out.println();
                System.out.println("─────────────────────────────────────────────────");
                System.out.printf("  Período : %s de %s de %d%n", dia, MESES[mes - 1], anio);
                System.out.printf("  Tasa BCV: Bs. %.2f / USD%n", tasaBCV);
                System.out.printf("  Salario : Bs. %.2f  ($30 × %.2f)%n", 30 * tasaBCV, tasaBCV);
                System.out.printf("  Cesta   : Bs. %.2f  ($40 × %.2f)%n", 40 * tasaBCV, tasaBCV);
                System.out.printf("  TOTAL   : Bs. %.2f  ($70 × %.2f)%n", 70 * tasaBCV, tasaBCV);
                System.out.println("─────────────────────────────────────────────────");
                System.out.print("¿Confirmar y generar el documento? (S/N): ");

                String confirmacion = scanner.nextLine().trim().toUpperCase();
                if (!confirmacion.equals("S") && !confirmacion.equals("SI") && !confirmacion.equals("SÍ")) {
                    System.out.println("Operación cancelada.\n");
                    continuar = preguntarSiContinuar(scanner);
                    continue;
                }

                // ── Generar el documento ──────────────────────────────────
                Recibo recibo = Recibo.crear(fecha, tasaBCV);
                GeneradorDocumento generador = new GeneradorDocumento();

                // Guardar en la carpeta RECIBOS (junto al programa)
                Path directorio = Paths.get(System.getProperty("user.dir"), "RECIBOS");
                Files.createDirectories(directorio);
                Path archivoGenerado = generador.generarDocumento(recibo, directorio);

                System.out.println();
                System.out.println("✅ ¡Documento generado exitosamente!");
                System.out.println("   📄 Archivo: " + archivoGenerado.getFileName());
                System.out.println("   📁 Ubicación: " + archivoGenerado.getParent());
                System.out.println();

            } catch (IOException e) {
                System.err.println("❌ Error al generar el documento: " + e.getMessage());
                e.printStackTrace();
            } catch (InputMismatchException e) {
                System.out.println("⚠  Entrada inválida. Por favor ingrese un número.\n");
                scanner.nextLine(); // Limpiar buffer
                continue;
            }

            continuar = preguntarSiContinuar(scanner);
        }

        System.out.println("\n¡Hasta luego! Los recibos han sido guardados en la carpeta RECIBOS.");
        scanner.close();
    }

    // ──────────────────────────────────────────────────────────────────────
    // MÉTODOS AUXILIARES
    // ──────────────────────────────────────────────────────────────────────

    private static int solicitarEntero(Scanner scanner, String mensaje, int min, int max) {
        int valor;
        while (true) {
            System.out.print(mensaje);
            try {
                valor = Integer.parseInt(scanner.nextLine().trim());
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("⚠  Debe ser un número entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("⚠  Por favor ingrese un número entero válido.");
            }
        }
    }

    private static double solicitarDecimal(Scanner scanner) {
        while (true) {
            try {
                String entrada = scanner.nextLine().trim().replace(",", ".");
                double valor = Double.parseDouble(entrada);
                if (valor > 0) {
                    return valor;
                }
                System.out.print("⚠  La tasa debe ser mayor que 0. Intente de nuevo: ");
            } catch (NumberFormatException e) {
                System.out.print("⚠  Por favor ingrese un número decimal válido (ej: 36.50): ");
            }
        }
    }

    private static boolean preguntarSiContinuar(Scanner scanner) {
        System.out.print("¿Desea generar otro recibo? (S/N): ");
        String respuesta = scanner.nextLine().trim().toUpperCase();
        System.out.println();
        return respuesta.equals("S") || respuesta.equals("SI") || respuesta.equals("SÍ");
    }
}
