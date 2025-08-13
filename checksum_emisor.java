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

        int missing = size_mensaje % deseado;
        missing = (missing != 0) ? deseado - missing : 0;

        if (missing != 0) {
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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 1. Solicitar un mensaje en binario. (i.e.: “110101”)
        System.out.println("Bienvenido. Esta es una simulación de Checksum Fletcher");
        System.out.println("Para empezar elige el tipo con el que quieres trabajar. (Escribe 1, 2 o 3)");
        System.out.println("1. Fletcher-8\n2. Fletcher-16\n3. Fletcher-32");
        int eleccion = sc.nextInt();
        sc.nextLine();

        // Inicializar recurso
        checksum_emisor emisor = new checksum_emisor(eleccion);

        System.out.println(String.format("\nGenial, estamos usando Fletcher-%d.", emisor.tipo));
        System.out.println("Ahora escribe el mensaje que quieres enviar (i.e.: “110101”):");
        String mensaje = sc.nextLine();

        // 2. Ejecutar el algoritmo
        String resultado = emisor.checksum(mensaje);

        // 3. Devolver el mensaje en binario concatenado dicha información
        System.out.println("\nEl resultado de checksum es:");
        System.out.println(resultado);

        sc.close();
    }

}
