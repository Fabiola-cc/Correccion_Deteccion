import FletcherChecksum.checksum_emisor;
import Hamming.Hamming_emisor; // Importar la clase de Hamming
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class emisor {
    public static void sendMessage(String verified_message, int scheme_type, int fletcher_type) {
        String host = "127.0.0.1";
        int port = 5555;

        try (Socket socket = new Socket(host, port)) {
            // Enviar mensaje
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            String json = String.format("{\"message\":\"%s\",\"scheme\":%d,\"fletcherT\":%d}",
                    verified_message, scheme_type, fletcher_type);
            writer.println(json);
            writer.println("Mensaje enviado");

            // Recibir respuesta
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String respuesta = reader.readLine();
            System.out.println("Respuesta del servidor: " + respuesta);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String toAsciiBinary(String text) {
        StringBuilder binary = new StringBuilder();
        for (char c : text.toCharArray()) {
            String bin = Integer.toBinaryString(c); // valor binario del char
            // Asegurar que sean 8 bits con ceros a la izquierda
            String padded = String.format("%8s", bin).replace(' ', '0');
            binary.append(padded);
        }
        return binary.toString();
    }

    public static String aplicarRuido(String binario, double probError) {
        StringBuilder conRuido = new StringBuilder(binario);
        Random rand = new Random();

        for (int i = 0; i < binario.length(); i++) {
            if (rand.nextDouble() < probError) {
                // Voltear el bit
                char bit = binario.charAt(i);
                conRuido.setCharAt(i, bit == '0' ? '1' : '0');
            }
        }
        return conRuido.toString();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Bienvenido al emisor");

        // APLICACIÓN
        // Elegir el algoritmo a trabajar
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

            System.out.println("\nElige el esquema con el que quieres trabajar. (Escribe 1 o 2)");
            System.out.println(
                    "1. Codigos de Hamming (Correccion de errores)\n2. Fletcher checksum (Deteccion de errores)");

            if (sc.hasNextInt()) {
                eleccion = sc.nextInt();
                if (eleccion < 1 || eleccion > 2) {
                    System.err.println("Error. Debes elegir un 1 o 2.");
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

        String eleccion_nombre = (eleccion == 1) ? "Codigos de Hamming" : "Fletcher checksum";
        System.out.println("Excelente, usamos el esquema de " + eleccion_nombre);

        // Solicitar mensaje a enviar
        System.out.println("Ahora escribe el mensaje que quieres enviar:");
        System.out.println("(Recuerda que un mensaje no binario se traduce como ASCII binario)");
        String mensaje = sc.nextLine();

        // PRESENTACIÓN
        String mensaje_binario = mensaje;
        // Encriptar mensaje si no es binario
        if (!mensaje.matches("[01]+")) {
            mensaje_binario = toAsciiBinary(mensaje);
        }

        // ENLACE
        String toSend = mensaje_binario;
        int fletcherT = -1;
        // Calcular integridad según el esquema elegido
        if (eleccion == 1) { // Hamming
            String[] received = Hamming_emisor.useHammingEmisor(mensaje_binario);
            if (received[0].equals("-1")) {
                System.err.println("Error al procesar con Hamming. Terminando programa.");
                sc.close();
                return;
            }
            fletcherT = Integer.parseInt(received[0]); // tipo de hamming (siempre 1)
            toSend = received[1]; // mensaje + bits de paridad
        } else if (eleccion == 2) { // Fletcher
            String[] received = checksum_emisor.useChecksumEmisor(mensaje_binario);
            fletcherT = Integer.parseInt(received[0]); // tipo de fletcher checksum a usar
            toSend = received[1]; // mensaje + checksum
        }

        // RUIDO
        toSend = aplicarRuido(toSend, 0.01); // 1% de probabilidad por bit

        // TRANSMISION
        sendMessage(toSend, eleccion, fletcherT);

        sc.close();
    }
}
