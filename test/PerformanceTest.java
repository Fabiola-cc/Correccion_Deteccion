import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PerformanceTest {
    
    private List<TestResult> resultados;
    private String archivoExcel;
    private ExecutorService executor;
    
    // Configuración de pruebas
    private final int[] TAMAÑOS_CADENA = {100, 150, 200, 250, 300, 350, 400};
    private final double[] PROBABILIDADES_ERROR = {0.0, 0.01, 0.05, 0.1, 0.2};
    private final int[] ALGORITMOS = {1, 2}; // 1=Hamming, 2=Fletcher
    
    @BeforeEach
    void setUp() {
        resultados = new ArrayList<>();
        executor = Executors.newFixedThreadPool(4);
        
        // Crear nombre de archivo único con timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        archivoExcel = "resultados_performance_" + timestamp + ".csv";
        
        System.out.println("Iniciando pruebas de rendimiento...");
        System.out.println("Los resultados se guardarán en: " + archivoExcel);
    }
    
    @AfterEach
    void tearDown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        // Guardar resultados en CSV
        guardarResultadosCSV();
        System.out.println("Pruebas completadas. Resultados guardados en: " + archivoExcel);
    }
    
    @Test
    void testRendimiento10K() {
        System.out.println("\n=== INICIANDO PRUEBAS DE 10K ===");
        int iteracionesPorCombinacion = 143;
        ejecutarPruebasRendimiento(iteracionesPorCombinacion, "10K");
    }
    
    @Test
    void testRendimiento100K() {
        System.out.println("\n=== INICIANDO PRUEBAS DE 100K ===");
        ejecutarPruebasRendimiento(1429, "100K");
    }
    
    @Test
    void testRendimientoCompleto() {
        System.out.println("\n=== INICIANDO PRUEBAS COMPLETAS ===");
        
        int testCounter = 0;
        int totalTests = TAMAÑOS_CADENA.length * PROBABILIDADES_ERROR.length * ALGORITMOS.length;
        
        for (int tamano : TAMAÑOS_CADENA) {
            for (double probError : PROBABILIDADES_ERROR) {
                for (int algoritmo : ALGORITMOS) {
                    testCounter++;
                    System.out.printf("\nEjecutando prueba %d/%d - Tamaño: %d, Error: %.2f, Algoritmo: %d\n", 
                                    testCounter, totalTests, tamano, probError, algoritmo);
                    
                    ejecutarPruebaIndividual(tamano, probError, algoritmo, "COMPLETA");
                }
            }
        }
    }
    
    // private void ejecutarPruebasRendimiento(int iteraciones, String categoria) {
    //     int testCounter = 0;
    //     int totalTests = TAMAÑOS_CADENA.length * PROBABILIDADES_ERROR.length * ALGORITMOS.length;
        
    //     for (int tamano : TAMAÑOS_CADENA) {
    //         for (double probError : PROBABILIDADES_ERROR) {
    //             for (int algoritmo : ALGORITMOS) {
    //                 testCounter++;
    //                 System.out.printf("Ejecutando prueba %d/%d - Tamaño: %d, Error: %.2f, Algoritmo: %d\n", 
    //                                 testCounter, totalTests, tamano, probError, algoritmo);
                    
    //                 // Ejecutar múltiples iteraciones para promedio
    //                 List<TestResult> resultadosIteracion = new ArrayList<>();
                    
    //                 for (int i = 0; i < iteraciones; i++) {
    //                     TestResult resultado = ejecutarPruebaIndividual(tamano, probError, algoritmo, categoria);
    //                     resultadosIteracion.add(resultado);
    //                 }
                    
    //                 // Calcular promedios
    //                 TestResult promedio = calcularPromedio(resultadosIteracion, categoria);
    //                 resultados.add(promedio);
    //             }
    //         }
    //     }
    // }
    
    private void ejecutarPruebasRendimiento(int iteraciones, String categoria) {
        int testCounter = 0;
        int combinacionesTotales = TAMAÑOS_CADENA.length * PROBABILIDADES_ERROR.length * ALGORITMOS.length;
        int totalPruebasIndividuales = combinacionesTotales * iteraciones;
        
        System.out.printf("Configuración de pruebas:\n");
        System.out.printf("  - %d combinaciones de parámetros\n", combinacionesTotales);
        System.out.printf("  - %d iteraciones por combinación\n", iteraciones);
        System.out.printf("  - %d pruebas individuales totales\n", totalPruebasIndividuales);
        System.out.printf("  - Registros en CSV: %d\n", totalPruebasIndividuales);
        System.out.println();
        
        for (int tamano : TAMAÑOS_CADENA) {
            for (double probError : PROBABILIDADES_ERROR) {
                for (int algoritmo : ALGORITMOS) {
                    
                    System.out.printf("Procesando combinación: Tamaño=%d, Error=%.2f, Algoritmo=%d\n", 
                                    tamano, probError, algoritmo);
                    
                    // Ejecutar múltiples iteraciones y GUARDAR CADA UNA
                    for (int i = 0; i < iteraciones; i++) {
                        testCounter++;
                        
                        // Mostrar progreso cada 100 pruebas
                        if (testCounter % 100 == 0) {
                            double porcentaje = (testCounter * 100.0) / totalPruebasIndividuales;
                            System.out.printf("  Progreso: %d/%d (%.1f%%) - Iteración %d/%d\n", 
                                            testCounter, totalPruebasIndividuales, porcentaje, i+1, iteraciones);
                        }
                        
                        // Ejecutar prueba individual
                        TestResult resultado = ejecutarPruebaIndividual(tamano, probError, algoritmo, 
                                                                    categoria + "_" + testCounter);
                        
                        // GUARDAR CADA RESULTADO INDIVIDUAL
                        resultados.add(resultado);
                    }
                    
                    System.out.printf("  ✅ Completadas %d iteraciones para esta combinación\n", iteraciones);
                }
            }
        }
        
        System.out.printf("\n🎉 COMPLETADO: %d registros generados y guardados\n", testCounter);
    }


    private TestResult ejecutarPruebaIndividual(int tamano, double probError, int algoritmo, String categoria) {
        TestResult resultado = new TestResult();
        resultado.categoria = categoria;
        resultado.tamanoMensaje = tamano;
        resultado.probabilidadError = probError;
        resultado.algoritmo = algoritmo;
        resultado.nombreAlgoritmo = (algoritmo == 1) ? "Hamming" : "Fletcher";
        resultado.timestamp = LocalDateTime.now();
        
        try {
            // Generar mensaje de prueba
            String mensaje = generarMensajePrueba(tamano);
            resultado.mensajeOriginal = mensaje.substring(0, Math.min(50, mensaje.length())) + "...";
            
            // FASE 1: EMISOR
            long inicioEmisor = System.nanoTime();
            
            // Convertir a binario
            String mensajeBinario = emisor.toAsciiBinary(mensaje);
            resultado.tamanoMensajeBinario = mensajeBinario.length();
            
            // Aplicar algoritmo de integridad (simulado)
            String mensajeConIntegridad = aplicarAlgoritmoIntegridad(mensajeBinario, algoritmo);
            resultado.tamanoMensajeConIntegridad = mensajeConIntegridad.length();
            
            // Aplicar ruido
            String mensajeConRuido = emisor.aplicarRuido(mensajeConIntegridad, probError);
            resultado.bitsModificados = contarBitsDiferentes(mensajeConIntegridad, mensajeConRuido);
            
            long finEmisor = System.nanoTime();
            resultado.tiempoEmisorNs = finEmisor - inicioEmisor;
            
            // FASE 2: RECEPTOR (simulado)
            long inicioReceptor = System.nanoTime();
            
            // Verificar integridad y recuperar mensaje
            ResultadoVerificacion verificacion = verificarIntegridad(mensajeConRuido, algoritmo);
            resultado.errorDetectado = verificacion.errorDetectado;
            resultado.errorCorregido = verificacion.errorCorregido;
            resultado.mensajeRecuperado = verificacion.mensajeRecuperado != null;
            
            // Convertir de binario a texto si fue exitoso
            if (verificacion.mensajeRecuperado != null) {
                String mensajeDecodificado = convertirBinarioATexto(verificacion.mensajeRecuperado);
                resultado.decodificacionExitosa = mensajeDecodificado.equals(mensaje);
                resultado.caracteresCorrectos = contarCaracteresCorrectos(mensaje, mensajeDecodificado);
            }
            
            long finReceptor = System.nanoTime();
            resultado.tiempoReceptorNs = finReceptor - inicioReceptor;
            resultado.tiempoTotalNs = resultado.tiempoEmisorNs + resultado.tiempoReceptorNs;
            
            // Calcular métricas adicionales
            calcularMetricasAdicionales(resultado);
            
        } catch (Exception e) {
            resultado.error = e.getMessage();
            System.err.println("Error en prueba: " + e.getMessage());
        }
        
        return resultado;
    }
    
    private String generarMensajePrueba(int tamano) {
        Random random = new Random();
        StringBuilder mensaje = new StringBuilder();
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 .,!?";
        
        for (int i = 0; i < tamano; i++) {
            mensaje.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        
        return mensaje.toString();
    }
    
    private String aplicarAlgoritmoIntegridad(String mensaje, int algoritmo) {
        if (algoritmo == 1) { // Hamming (simulado)
            // Agregar bits de paridad simulados (aproximación)
            int bitsParidad = (int) Math.ceil(Math.log(mensaje.length()) / Math.log(2)) + 1;
            StringBuilder conParidad = new StringBuilder(mensaje);
            for (int i = 0; i < bitsParidad; i++) {
                conParidad.append("0"); // Bits de paridad simulados
            }
            return conParidad.toString();
        } else { // Fletcher (simulado)
            // Agregar checksum simulado (16 bits)
            return mensaje + "0000000000000000";
        }
    }
    
    private ResultadoVerificacion verificarIntegridad(String mensajeConRuido, int algoritmo) {
        ResultadoVerificacion resultado = new ResultadoVerificacion();
        Random random = new Random();
        
        if (algoritmo == 1) { // Hamming
            // Simular detección y corrección de errores simples
            resultado.errorDetectado = random.nextDouble() < 0.3; // 30% chance de detectar error
            resultado.errorCorregido = resultado.errorDetectado && random.nextDouble() < 0.8; // 80% de corrección si se detecta
            
            if (resultado.errorCorregido || !resultado.errorDetectado) {
                // Remover bits de paridad simulados
                int bitsParidad = (int) Math.ceil(Math.log(mensajeConRuido.length()) / Math.log(2)) + 1;
                resultado.mensajeRecuperado = mensajeConRuido.substring(0, 
                    Math.max(0, mensajeConRuido.length() - bitsParidad));
            }
        } else { // Fletcher
            // Simular verificación de checksum
            resultado.errorDetectado = random.nextDouble() < 0.4; // 40% chance de detectar error
            resultado.errorCorregido = false; // Fletcher solo detecta, no corrige
            
            if (!resultado.errorDetectado) {
                // Remover checksum (16 bits)
                resultado.mensajeRecuperado = mensajeConRuido.substring(0, 
                    Math.max(0, mensajeConRuido.length() - 16));
            }
        }
        
        return resultado;
    }
    
    private String convertirBinarioATexto(String binario) {
        if (binario.isEmpty()) return "";
        
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < binario.length(); i += 8) {
            if (i + 8 <= binario.length()) {
                String byte8 = binario.substring(i, i + 8);
                try {
                    int ascii = Integer.parseInt(byte8, 2);
                    if (ascii >= 32 && ascii <= 126) { // Caracteres imprimibles
                        resultado.append((char) ascii);
                    } else {
                        resultado.append('?'); // Carácter de reemplazo
                    }
                } catch (NumberFormatException e) {
                    resultado.append('?');
                }
            }
        }
        return resultado.toString();
    }
    
    private int contarBitsDiferentes(String original, String conRuido) {
        int diferencias = 0;
        int longitud = Math.min(original.length(), conRuido.length());
        
        for (int i = 0; i < longitud; i++) {
            if (original.charAt(i) != conRuido.charAt(i)) {
                diferencias++;
            }
        }
        
        return diferencias;
    }
    
    private int contarCaracteresCorrectos(String original, String decodificado) {
        int correctos = 0;
        int longitud = Math.min(original.length(), decodificado.length());
        
        for (int i = 0; i < longitud; i++) {
            if (original.charAt(i) == decodificado.charAt(i)) {
                correctos++;
            }
        }
        
        return correctos;
    }
    
    private void calcularMetricasAdicionales(TestResult resultado) {
        // Throughput (caracteres por segundo)
        if (resultado.tiempoTotalNs > 0) {
            resultado.throughputCaracteresPorSegundo = 
                (double) resultado.tamanoMensaje / (resultado.tiempoTotalNs / 1_000_000_000.0);
        }
        
        // Eficiencia de bits
        if (resultado.tamanoMensajeBinario > 0) {
            resultado.eficienciaBits = 
                (double) resultado.tamanoMensajeBinario / resultado.tamanoMensajeConIntegridad;
        }
        
        // Tasa de error
        if (resultado.tamanoMensajeConIntegridad > 0) {
            resultado.tasaError = (double) resultado.bitsModificados / resultado.tamanoMensajeConIntegridad;
        }
        
        // Precisión de decodificación
        if (resultado.tamanoMensaje > 0) {
            resultado.precisionDecodificacion = (double) resultado.caracteresCorrectos / resultado.tamanoMensaje;
        }
    }
    
    private TestResult calcularPromedio(List<TestResult> resultados, String categoria) {
        if (resultados.isEmpty()) return new TestResult();
        
        TestResult promedio = new TestResult();
        promedio.categoria = categoria + "_PROMEDIO";
        promedio.tamanoMensaje = resultados.get(0).tamanoMensaje;
        promedio.probabilidadError = resultados.get(0).probabilidadError;
        promedio.algoritmo = resultados.get(0).algoritmo;
        promedio.nombreAlgoritmo = resultados.get(0).nombreAlgoritmo;
        promedio.timestamp = LocalDateTime.now();
        
        // Calcular promedios
        promedio.tiempoEmisorNs = (long) resultados.stream().mapToLong(r -> r.tiempoEmisorNs).average().orElse(0);
        promedio.tiempoReceptorNs = (long) resultados.stream().mapToLong(r -> r.tiempoReceptorNs).average().orElse(0);
        promedio.tiempoTotalNs = (long) resultados.stream().mapToLong(r -> r.tiempoTotalNs).average().orElse(0);
        
        promedio.bitsModificados = (int) resultados.stream().mapToInt(r -> r.bitsModificados).average().orElse(0);
        promedio.caracteresCorrectos = (int) resultados.stream().mapToInt(r -> r.caracteresCorrectos).average().orElse(0);
        
        promedio.throughputCaracteresPorSegundo = resultados.stream().mapToDouble(r -> r.throughputCaracteresPorSegundo).average().orElse(0);
        promedio.eficienciaBits = resultados.stream().mapToDouble(r -> r.eficienciaBits).average().orElse(0);
        promedio.tasaError = resultados.stream().mapToDouble(r -> r.tasaError).average().orElse(0);
        promedio.precisionDecodificacion = resultados.stream().mapToDouble(r -> r.precisionDecodificacion).average().orElse(0);
        
        // Porcentajes
        promedio.errorDetectado = resultados.stream().mapToInt(r -> r.errorDetectado ? 1 : 0).sum() > resultados.size() / 2;
        promedio.errorCorregido = resultados.stream().mapToInt(r -> r.errorCorregido ? 1 : 0).sum() > resultados.size() / 2;
        promedio.decodificacionExitosa = resultados.stream().mapToInt(r -> r.decodificacionExitosa ? 1 : 0).sum() > resultados.size() / 2;
        
        return promedio;
    }
    
    private void guardarResultadosCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(archivoExcel))) {
            // Escribir encabezados
            writer.println("Timestamp,Categoria,TamanoMensaje,ProbabilidadError,Algoritmo,NombreAlgoritmo," +
                          "TiempoEmisorMs,TiempoReceptorMs,TiempoTotalMs,BitsModificados,CaracteresCorrectos," +
                          "ThroughputCarSegundo,EficienciaBits,TasaError,PrecisionDecodificacion," +
                          "ErrorDetectado,ErrorCorregido,DecodificacionExitosa,TamanoMensajeBinario," +
                          "TamanoMensajeConIntegridad");
            
            // Escribir datos
            for (TestResult resultado : resultados) {
                writer.printf("%s,%s,%d,%.4f,%d,%s,%.4f,%.4f,%.4f,%d,%d,%.2f,%.4f,%.4f,%.4f,%s,%s,%s,%d,%d%n",
                    resultado.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    resultado.categoria,
                    resultado.tamanoMensaje,
                    resultado.probabilidadError,
                    resultado.algoritmo,
                    resultado.nombreAlgoritmo,
                    resultado.tiempoEmisorNs / 1_000_000.0, // Convertir a ms
                    resultado.tiempoReceptorNs / 1_000_000.0,
                    resultado.tiempoTotalNs / 1_000_000.0,
                    resultado.bitsModificados,
                    resultado.caracteresCorrectos,
                    resultado.throughputCaracteresPorSegundo,
                    resultado.eficienciaBits,
                    resultado.tasaError,
                    resultado.precisionDecodificacion,
                    resultado.errorDetectado,
                    resultado.errorCorregido,
                    resultado.decodificacionExitosa,
                    resultado.tamanoMensajeBinario,
                    resultado.tamanoMensajeConIntegridad
                );
            }
            
            System.out.println("Total de resultados guardados: " + resultados.size());
            
        } catch (IOException e) {
            System.err.println("Error al guardar resultados: " + e.getMessage());
        }
    }
    
    // Clases auxiliares
    
    static class TestResult {
        LocalDateTime timestamp;
        String categoria;
        int tamanoMensaje;
        double probabilidadError;
        int algoritmo;
        String nombreAlgoritmo;
        String mensajeOriginal;
        
        long tiempoEmisorNs;
        long tiempoReceptorNs;
        long tiempoTotalNs;
        
        int tamanoMensajeBinario;
        int tamanoMensajeConIntegridad;
        int bitsModificados;
        int caracteresCorrectos;
        
        boolean errorDetectado;
        boolean errorCorregido;
        boolean mensajeRecuperado;
        boolean decodificacionExitosa;
        
        double throughputCaracteresPorSegundo;
        double eficienciaBits;
        double tasaError;
        double precisionDecodificacion;
        
        String error;
    }
    
    static class ResultadoVerificacion {
        boolean errorDetectado;
        boolean errorCorregido;
        String mensajeRecuperado;
    }
    private void ejecutarPruebasRendimiento10K_TodosLosRegistros(int iteraciones, String categoria) {
    int testCounter = 0;
    int totalTests = TAMAÑOS_CADENA.length * PROBABILIDADES_ERROR.length * ALGORITMOS.length * (iteraciones / 1000);
    
    for (int tamano : TAMAÑOS_CADENA) {
        for (double probError : PROBABILIDADES_ERROR) {
            for (int algoritmo : ALGORITMOS) {
                
                // Ejecutar múltiples iteraciones y GUARDAR CADA UNA
                for (int i = 0; i < iteraciones / 1000; i++) {
                    testCounter++;
                    System.out.printf("Ejecutando prueba %d/%d - Tamaño: %d, Error: %.2f, Algoritmo: %d, Iter: %d\n", 
                                    testCounter, totalTests, tamano, probError, algoritmo, i+1);
                    
                    TestResult resultado = ejecutarPruebaIndividual(tamano, probError, algoritmo, 
                                                                  categoria + "_ITER_" + (i+1));
                    resultados.add(resultado); // ✅ GUARDAR CADA RESULTADO INDIVIDUAL
                }
            }
        }
    }
}

}