package FletcherChecksum;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class checksum_emisor {
    int tipo; // fletcher - 8,16,32
    int n; // 4,8,16 bits
    int sum1;
    int sum2;

    public checksum_emisor(int eleccion) {
        // Asignar valores a tipo,n y sums según la elección del usuario
        if (eleccion == 1) {
            tipo = 8;
            n = 4;
            sum2 = sum1 = 0b0001;
        } else if (eleccion == 2) {
            tipo = 16;
            n = 8;
            sum2 = sum1 = 0b00000001;
        } else if (eleccion == 3) {
            tipo = 32;
            n = 16;
            sum2 = sum1 = 0b0000000000000001;
        }
    }

    private void apply(int section) {
        int toMod = (int) Math.pow(2, n) - 1;

        sum1 = (sum1 + section) % toMod;
        sum2 = (sum1 + sum2) % toMod;
    }

    private String paddString(String mensaje, int deseado) {
        int size_mensaje = mensaje.length();

        if (size_mensaje % deseado != 0) {
            int missing = deseado - size_mensaje % deseado;
            StringBuilder addition = new StringBuilder();
            for (int i = 0; i < missing; i++) {
                addition.append('0');
            }

            mensaje = addition + mensaje;
        }

        return mensaje;
    }

    public String checksum(String mensaje) {
        String checksum_result = "";

        // padding
        mensaje = paddString(mensaje, n);

        // dividir mensaje en secciones
        List<String> secciones = new ArrayList<>();
        for (int i = 0; i < mensaje.length(); i += n) {
            int fin = Math.min(i + n, mensaje.length());
            secciones.add(mensaje.substring(i, fin));
        }

        // ciclo de sums
        for (String seccion : secciones) {
            int binary_section = Integer.parseInt(seccion, 2);
            apply(binary_section);
        }

        // calcular checksum
        int checksum = (sum2 << n) | sum1;
        checksum_result = Integer.toBinaryString(checksum);

        // ajustar a n
        checksum_result = paddString(checksum_result, tipo);

        return checksum_result;
    }

    public static String[] useChecksumEmisor(String mensaje) {
        Scanner sc = new Scanner(System.in);

        int eleccion = 0;
        boolean invalid = true;
        int tries = 0;

        while (invalid) {
            if (tries > 3) {
                System.err.println("Lo lamento, has intentado más de tres veces.");
                System.err.println("Vuelve a ejecutar el código para empezar de nuevo.");
                sc.close();
                return new String[] { "-1" };
            }

            System.out.println("\nElige el tipo con el que quieres trabajar. (Escribe 1, 2 o 3)");
            System.out.println("1. Fletcher-8\n2. Fletcher-16\n3. Fletcher-32");

            if (sc.hasNextInt()) {
                eleccion = sc.nextInt();
                if (eleccion < 1 || eleccion > 3) {
                    System.err.println("Error. Debes elegir un número entre 1 y 3.");
                    tries++;
                } else {
                    invalid = false;
                }
            } else {
                System.err.println("Entrada inválida. Debes escribir un número.");
                sc.next(); // descartar entrada inválida
                tries++;
            }
        }
        sc.nextLine();

        // Inicializar recurso
        checksum_emisor emisor = new checksum_emisor(eleccion);

        System.out.println(String.format("\nGenial, estamos usando Fletcher-%d.", emisor.tipo));
        if (!mensaje.matches("[01]+")) {
            System.err.println("Error: El mensaje debe ser binario.");
            sc.close();
            return new String[] { "-1" };
        }

        // 2. Ejecutar el algoritmo
        String resultado = emisor.checksum(mensaje);

        // 3. Devolver el mensaje en binario concatenado dicha información
        System.out.println("\nEl resultado de checksum es:");
        System.out.println(resultado);

        System.out.println("\nEl mensaje para el receptor:");
        System.out.println(mensaje + resultado);

        sc.close();
        return new String[] { String.valueOf(eleccion), (mensaje + resultado) };
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 1. Solicitar un mensaje en binario. (i.e.: 110101)
        System.out.println("Bienvenido. Esta es una simulación de Checksum Fletcher");

        int eleccion = 0;
        boolean invalid = true;
        int tries = 0;

        while (invalid) {
            if (tries > 3) {
                System.err.println("Lo lamento, has intentado más de tres veces.");
                System.err.println("Vuelve a ejecutar el código para empezar de nuevo.");
                sc.close();
                return; // rompe el main
            }

            System.out.println("\nElige el tipo con el que quieres trabajar. (Escribe 1, 2 o 3)");
            System.out.println("1. Fletcher-8\n2. Fletcher-16\n3. Fletcher-32");

            if (sc.hasNextInt()) {
                eleccion = sc.nextInt();
                if (eleccion < 1 || eleccion > 3) {
                    System.err.println("Error. Debes elegir un número entre 1 y 3.");
                    tries++;
                } else {
                    invalid = false;
                }
            } else {
                System.err.println("Entrada inválida. Debes escribir un número.");
                sc.next(); // descartar entrada inválida
                tries++;
            }
        }
        sc.nextLine();

        // Inicializar recurso
        checksum_emisor emisor = new checksum_emisor(eleccion);

        System.out.println(String.format("\nGenial, estamos usando Fletcher-%d.", emisor.tipo));
        System.out.println("Ahora escribe el mensaje que quieres enviar (i.e.: 110101):");
        String mensaje = sc.nextLine();

        if (!mensaje.matches("[01]+")) {
            System.err.println("Error: El mensaje debe ser binario.");
            sc.close();
            return; // sale del main
        }

        // 2. Ejecutar el algoritmo
        String resultado = emisor.checksum(mensaje);

        // 3. Devolver el mensaje en binario concatenado dicha información
        System.out.println("\nEl resultado de checksum es:");
        System.out.println(resultado);

        System.out.println("\nEl mensaje para el receptor:");
        System.out.println(mensaje + resultado);

        sc.close();
    }

}
