/*
 * Algoritmo de Hamming - Emisor
 * 1. Solicitamos el mensaje binario al usuario (ejemplo: "01010101")
 * 2. Sacamos el número total de bits en el mensaje
 * 3. cantidad_paridad = obtenemos los bits de paridad usando ceil(ln(m)/ln(2))
 * 4. Creamos una matriz de m×2
 * 5. Colocamos los números de 1-m en la columna 1
 * 6. En la columna 0 apartamos los espacios 2^(for 0-cantidad_paridad) 
 *    en esos espacios vamos poniendo P1, P2, P3...
 * 7. En los espacios restantes de la columna 0 ponemos el mensaje original
 * 8. Creamos arrays para cada paridad P1, P2, P3, P4...
 * 9. Al Array de P1 ingresamos todos los números de la columna 1 que:
 *    - No sean de paridad
 *    - Su traducción a binario tenga un 1 en la primera casilla (equivale a 1)
 * 10. Al Array de P2 ingresamos todos los números que tengan un 1 en la segunda casilla (equivale a 2)
 * 11. Al Array de P3 ingresamos todos los números que tengan un 1 en la tercera casilla (equivale a 4)
 * 12. Traducimos estos números a su versión en la matriz columna 0 y los metemos en un stack
 * 13. Hacemos pop y pop del stack, aplicamos XOR y guardamos el resultado
 * 14. Continuamos con pop y XOR hasta terminar el stack y tener el resultado final
 * 15. Hacemos lo mismo para todas las paridades P1, P2, P3, P4...
 * 16. Reemplazamos cada P1, P2, P3... de la matriz columna 0 con los resultados finales
 */

import java.util.*;

public class Hamming_emisor {
    
    public static void main(String[] args) {
        // Paso 1: Solicitamos el mensaje binario al usuario
        Scanner scanner = new Scanner(System.in);
        String mensaje = solicitarMensaje(scanner);
        
        System.out.println("Mensaje original: " + mensaje);
        
        // Paso 2: Sacamos el número total de bits en el mensaje
        int longitudMensaje = mensaje.length();
        
        // Paso 3: Calculamos los bits de paridad usando ceil(ln(m)/ln(2))
        int cantidadParidad = calcularBitsParidad(longitudMensaje);
        int m = longitudMensaje + cantidadParidad; // Total de bits
        
        System.out.println("Bits en el mensaje: " + longitudMensaje);
        System.out.println("Bits de paridad necesarios: " + cantidadParidad);
        System.out.println("Total de bits (m): " + m);
        
        // Paso 4: Creamos una matriz de m×2
        String[][] matriz = new String[m][2];
        
        // Paso 5: Colocamos los números de 1-m en la columna 1
        for (int i = 0; i < m; i++) {
            matriz[i][1] = String.valueOf(i + 1);
        }
        
        // Paso 6: En la columna 0 apartamos los espacios 2^n para P1, P2, P3...
        Set<Integer> posicionesParidad = new HashSet<>();
        for (int i = 0; i < cantidadParidad; i++) {
            int posicion = (int) Math.pow(2, i); // 2^0=1, 2^1=2, 2^2=4, 2^3=8...
            posicionesParidad.add(posicion);
            matriz[posicion - 1][0] = "P" + (i + 1); // P1, P2, P3, etc.
        }
        
        // Paso 7: En los espacios restantes colocamos el mensaje original
        int indiceMensaje = 0;
        for (int i = 0; i < m; i++) {
            int posicion = i + 1;
            if (!posicionesParidad.contains(posicion)) {
                matriz[i][0] = String.valueOf(mensaje.charAt(indiceMensaje));
                indiceMensaje++;
            }
        }
        
        // Mostrar matriz inicial
        System.out.println("\nMatriz inicial:");
        System.out.print("Posición: ");
        for (int i = 0; i < m; i++) {
            System.out.printf("%4s ", matriz[i][1]);
        }
        System.out.println();
        System.out.print("Bit:      ");
        for (int i = 0; i < m; i++) {
            System.out.printf("%4s ", matriz[i][0]);
        }
        System.out.println();
        
        // Paso 8-16: Calculamos cada bit de paridad
        for (int p = 0; p < cantidadParidad; p++) {
            int posicionParidad = (int) Math.pow(2, p);
            int bitParidad = 1 << p; // 2^p para verificar el bit correspondiente
            
            System.out.println("\nCalculando P" + (p + 1) + " (posición " + posicionParidad + "):");
            
            // Paso 9-11: Creamos array/stack para esta paridad
            // Incluimos números cuya representación binaria tenga '1' en la posición correspondiente
            Stack<Character> stack = new Stack<>();
            System.out.print("Posiciones que tienen '1' en el bit " + (p + 1) + ": ");
            
            for (int i = 0; i < m; i++) {
                int posicion = i + 1;
                
                // No incluir posiciones de paridad y verificar si tiene '1' en la posición correcta
                if (!posicionesParidad.contains(posicion) && (posicion & bitParidad) != 0) {
                    System.out.print(posicion + " ");
                    stack.push(matriz[i][0].charAt(0)); // Paso 12: metemos el bit en el stack
                }
            }
            System.out.println();
            
            // Paso 13-14: Hacemos pop y XOR hasta terminar el stack
            char resultado = '0';
            System.out.print("Bits a evaluar: ");
            Stack<Character> tempStack = new Stack<>();
            tempStack.addAll(stack);
            
            // Mostramos los bits que vamos a evaluar
            while (!tempStack.isEmpty()) {
                System.out.print(tempStack.peek() + " ");
                tempStack.pop();
            }
            System.out.println();
            
            // Calculamos XOR de todos los elementos del stack
            while (!stack.isEmpty()) {
                char bit = stack.pop();
                if (resultado == '0') {
                    resultado = bit;
                } else {
                    // XOR: 0⊕0=0, 0⊕1=1, 1⊕0=1, 1⊕1=0
                    if (resultado == bit) {
                        resultado = '0';
                    } else {
                        resultado = '1';
                    }
                }
            }
            
            System.out.println("P" + (p + 1) + "_Final = " + resultado);
            
            // Paso 16: Reemplazamos Px en la matriz con el resultado final
            matriz[posicionParidad - 1][0] = String.valueOf(resultado);
        }
        
        // Mostrar matriz final
        System.out.println("\nMatriz final:");
        System.out.print("Posición: ");
        for (int i = 0; i < m; i++) {
            System.out.printf("%4s ", matriz[i][1]);
        }
        System.out.println();
        System.out.print("Bit:      ");
        for (int i = 0; i < m; i++) {
            System.out.printf("%4s ", matriz[i][0]);
        }
        System.out.println();
        
        // Mostrar palabra código completa
        System.out.println("\nPalabra código completa (mensaje + bits de paridad):");
        StringBuilder palabraCodigo = new StringBuilder();
        for (int i = 0; i < m; i++) {
            palabraCodigo.append(matriz[i][0]);
        }
        System.out.println(palabraCodigo.toString());
        
        scanner.close();
    }
    
    /**
     * Solicita un mensaje binario válido al usuario
     * Valida que solo contenga '0' y '1'
     */
    private static String solicitarMensaje(Scanner scanner) {
        String mensaje;
        do {
            System.out.print("Ingrese el mensaje binario: ");
            mensaje = scanner.nextLine().trim();
            
            if (!esMensajeBinarioValido(mensaje)) {
                System.out.println("Error: El mensaje debe contener solo '0' y '1'. Intente nuevamente.");
            }
        } while (!esMensajeBinarioValido(mensaje));
        
        return mensaje;
    }
    
    /**
     * Valida que el mensaje solo contenga caracteres binarios ('0' y '1')
     */
    private static boolean esMensajeBinarioValido(String mensaje) {
        if (mensaje.isEmpty()) {
            return false;
        }
        
        for (char c : mensaje.toCharArray()) {
            if (c != '0' && c != '1') {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calcula la cantidad de bits de paridad necesarios
     * Usando la fórmula: ceil(ln(longitudMensaje + bitsParidad)/ln(2))
     * Resolvemos: 2^p >= m + p + 1
     */
    private static int calcularBitsParidad(int longitudMensaje) {
    /**
     * Calcula la cantidad de bits de paridad necesarios
     * Usando la fórmula: ceil(ln(longitudMensaje + bitsParidad)/ln(2))
     * Resolvemos: 2^p >= m + p + 1
     */
        // ceil(log2(longitudMensaje + bitsParidad))
        // Necesitamos resolver: 2^p >= m + p + 1
        int p = 0;
        while (Math.pow(2, p) < longitudMensaje + p + 1) {
            p++;
        }
        return p;
    }
}