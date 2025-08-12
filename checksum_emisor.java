import java.util.ArrayList;
import java.util.List;

public class checksum_emisor {
    int tipo; // fletcher - 8,16,32
    int n; // 4,8,16 bits
    int sum1;
    int sum2;

    public checksum_emisor(int eleccion) {
        get_tipo(eleccion);
    }

    /**
     * asignar el valor a tipo según la elección del usuario
     * y darle un valor a n según el tipo escogido
     * 
     * @param eleccion
     */
    private void get_tipo(int eleccion) {
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

        int missing = deseado - (size_mensaje % deseado);
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
        checksum_emisor emisor = new checksum_emisor(2);
        System.out.println(emisor.checksum("00000001000000100000001100000100"));
    }

}
