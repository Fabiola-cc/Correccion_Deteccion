import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class EmisorTest {
     
    @Test
    void testToAsciiBinary() {
        // Prueba conversión de texto a binario ASCII
        String result = emisor.toAsciiBinary("A");
        assertEquals("01000001", result); // 'A' en ASCII es 65 = 01000001 en binario
        
        // Prueba con múltiples caracteres
        String result2 = emisor.toAsciiBinary("AB");
        assertEquals("0100000101000010", result2); // 'A' + 'B'
        
        // Prueba con string vacío
        String result3 = emisor.toAsciiBinary("");
        assertEquals("", result3);
        
        // Prueba con espacio
        String result4 = emisor.toAsciiBinary(" ");
        assertEquals("00100000", result4); // Espacio es ASCII 32 = 00100000
    }
    
    @Test
    void testAplicarRuido() {
        String inputBinario = "11111111";
        
        // Prueba con probabilidad 0 (no debería cambiar nada)
        String result1 = emisor.aplicarRuido(inputBinario, 0.0);
        assertEquals(inputBinario, result1);
        
        // Prueba que el resultado tenga la misma longitud
        String result2 = emisor.aplicarRuido(inputBinario, 0.5);
        assertEquals(inputBinario.length(), result2.length());
        
        // Prueba con string vacío
        String result3 = emisor.aplicarRuido("", 0.1);
        assertEquals("", result3);
        
        // Prueba que solo contenga 0s y 1s
        String result4 = emisor.aplicarRuido("101010", 0.3);
        assertTrue(result4.matches("[01]*"));
    }
    
    @Test
    void testAplicarRuidoConProbabilidad1() {
        // Con probabilidad 1.0, todos los bits deberían invertirse
        String input = "1010";
        String result = emisor.aplicarRuido(input, 1.0);
        assertEquals("0101", result);
        
        String input2 = "0000";
        String result2 = emisor.aplicarRuido(input2, 1.0);
        assertEquals("1111", result2);
    }
    
    @Test
    void testToAsciiBinaryConCaracteresEspeciales() {
        // Prueba con números
        String result1 = emisor.toAsciiBinary("1");
        assertEquals("00110001", result1); // '1' es ASCII 49
        
        // Prueba con símbolo
        String result2 = emisor.toAsciiBinary("@");
        assertEquals("01000000", result2); // '@' es ASCII 64
        
        // Prueba con letra minúscula
        String result3 = emisor.toAsciiBinary("a");
        assertEquals("01100001", result3); // 'a' es ASCII 97
    }
    
    @Test
    void testToAsciiBinaryPadding() {
        // Verificar que siempre se usen 8 bits (padding con ceros)
        String result = emisor.toAsciiBinary("?"); // ASCII 63 = 111111 binario
        assertEquals("00111111", result); // Debe tener padding de 2 ceros al inicio
        assertEquals(8, result.length());
    }
}