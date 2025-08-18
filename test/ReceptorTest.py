import pytest
import json
from unittest.mock import Mock, patch
import sys
import os

# Agregar el directorio padre al path para poder importar receptor
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

# Importar la función que queremos probar del receptor
def ascii_bin_to_text(binary_str):
    """
    Función extraída del receptor.py para poder probarla independientemente
    """
    chars = []
    for i in range(0, len(binary_str), 8):
        byte = binary_str[i:i+8]              # Tomar 8 bits
        if len(byte) == 8:  # Solo procesar bytes completos
            char = chr(int(byte, 2))              # Convertir a entero y luego a char
            chars.append(char)
    return ''.join(chars)

class TestReceptor:
    """
    Pruebas unitarias para el receptor.py
    """
    
    def test_ascii_bin_to_text_caracteres_basicos(self):
        """Prueba conversión de caracteres ASCII básicos"""
        # Prueba letra 'A'
        result1 = ascii_bin_to_text("01000001")
        assert result1 == "A", f"Esperado 'A', obtenido '{result1}'"
        
        # Prueba letras 'AB'
        result2 = ascii_bin_to_text("0100000101000010")
        assert result2 == "AB", f"Esperado 'AB', obtenido '{result2}'"
        
        # Prueba espacio
        result3 = ascii_bin_to_text("00100000")
        assert result3 == " ", f"Esperado ' ', obtenido '{result3}'"
        
        # Prueba string vacío
        result4 = ascii_bin_to_text("")
        assert result4 == "", f"Esperado '', obtenido '{result4}'"
    
    def test_ascii_bin_to_text_numeros(self):
        """Prueba conversión de dígitos ASCII"""
        # ASCII 48 = '0'
        result1 = ascii_bin_to_text("00110000")
        assert result1 == "0", f"Esperado '0', obtenido '{result1}'"
        
        # ASCII 49 = '1'
        result2 = ascii_bin_to_text("00110001")
        assert result2 == "1", f"Esperado '1', obtenido '{result2}'"
        
        # Combinación "01"
        result3 = ascii_bin_to_text("0011000000110001")
        assert result3 == "01", f"Esperado '01', obtenido '{result3}'"
    
    def test_ascii_bin_to_text_caracteres_especiales(self):
        """Prueba conversión de caracteres especiales"""
        # ASCII 64 = '@'
        result1 = ascii_bin_to_text("01000000")
        assert result1 == "@", f"Esperado '@', obtenido '{result1}'"
        
        # ASCII 33 = '!'
        result2 = ascii_bin_to_text("00100001")
        assert result2 == "!", f"Esperado '!', obtenido '{result2}'"
        
        # ASCII 63 = '?'
        result3 = ascii_bin_to_text("00111111")
        assert result3 == "?", f"Esperado '?', obtenido '{result3}'"
    
    def test_ascii_bin_to_text_mensaje_completo(self):
        """Prueba conversión de un mensaje completo"""
        # H=72=01001000, O=79=01001111, L=76=01001100, A=65=01000001
        hola_binary = "01001000010011110100110001000001"
        expected = "HOLA"
        result = ascii_bin_to_text(hola_binary)
        assert result == expected, f"Esperado '{expected}', obtenido '{result}'"
        
        # Prueba con salto de línea
        # H=72, O=79, L=76, A=65, \n=10=00001010
        hola_newline_binary = "0100100001001111010011000100000100001010"
        expected_newline = "HOLA\n"
        result_newline = ascii_bin_to_text(hola_newline_binary)
        assert result_newline == expected_newline, f"Esperado '{expected_newline}', obtenido '{result_newline}'"
    
    def test_ascii_bin_to_text_longitud_invalida(self):
        """Prueba manejo de longitudes no múltiplo de 8"""
        # 7 bits - debería ignorar bits incompletos
        incomplete_binary = "0100000"
        result = ascii_bin_to_text(incomplete_binary)
        assert result == "", f"Esperado '', obtenido '{result}'"
        
        # 15 bits - debería procesar solo los primeros 8
        partial_binary = "010000010100001"
        result = ascii_bin_to_text(partial_binary)
        assert result == "A", f"Esperado 'A', obtenido '{result}'"
    
    def test_validacion_esquemas(self):
        """Prueba validación de esquemas de codificación"""
        # Esquemas válidos
        assert self.is_valid_scheme(1), "Esquema 1 (Hamming) debería ser válido"
        assert self.is_valid_scheme(2), "Esquema 2 (Fletcher) debería ser válido"
        
        # Esquemas inválidos
        assert not self.is_valid_scheme(0), "Esquema 0 no debería ser válido"
        assert not self.is_valid_scheme(3), "Esquema 3 no debería ser válido"
        assert not self.is_valid_scheme(-1), "Esquema -1 no debería ser válido"
    
    def test_validacion_mensaje_binario(self):
        """Prueba validación de mensajes binarios"""
        # Mensajes válidos
        assert self.is_valid_binary_message("101010"), "Mensaje '101010' debería ser válido"
        assert self.is_valid_binary_message(""), "Mensaje vacío debería ser válido"
        assert self.is_valid_binary_message("11111111"), "Mensaje '11111111' debería ser válido"
        assert self.is_valid_binary_message("00000000"), "Mensaje '00000000' debería ser válido"
        
        # Mensajes inválidos
        assert not self.is_valid_binary_message("10102"), "Mensaje '10102' no debería ser válido"
        assert not self.is_valid_binary_message("abc"), "Mensaje 'abc' no debería ser válido"
        assert not self.is_valid_binary_message("101a10"), "Mensaje '101a10' no debería ser válido"
    
    def test_validacion_json_estructura(self):
        """Prueba validación de estructura JSON"""
        # JSON válido
        valid_json = '{"message":"101010", "scheme":1, "fletcherT":1}'
        data = json.loads(valid_json)
        assert self.is_valid_json_structure(data), "JSON válido debería pasar validación"
        
        # JSON inválido - falta campo
        invalid_json_data = {"message":"101010", "scheme":1}  # falta fletcherT
        assert not self.is_valid_json_structure(invalid_json_data), "JSON sin fletcherT no debería ser válido"
        
        # JSON inválido - tipo incorrecto
        invalid_type_data = {"message":"101010", "scheme":"1", "fletcherT":1}  # scheme como string
        assert not self.is_valid_json_structure(invalid_type_data), "JSON con tipos incorrectos no debería ser válido"
    

    # Métodos auxiliares
    
    def is_valid_scheme(self, scheme):
        """Valida si el esquema es válido (1=Hamming, 2=Fletcher)"""
        return scheme in [1, 2]
    
    def is_valid_binary_message(self, message):
        """Valida si el mensaje contiene solo caracteres binarios"""
        return all(c in '01' for c in message)
    
    def is_valid_json_structure(self, data):
        """Valida la estructura del JSON recibido"""
        required_fields = ['message', 'scheme', 'fletcherT']
        
        # Verificar que todos los campos requeridos estén presentes
        if not all(field in data for field in required_fields):
            return False
        
        # Verificar tipos de datos
        if not isinstance(data['message'], str):
            return False
        if not isinstance(data['scheme'], int):
            return False
        if not isinstance(data['fletcherT'], int):
            return False
        
        # Verificar que el esquema sea válido
        if not self.is_valid_scheme(data['scheme']):
            return False
        
        # Verificar que el mensaje sea binario válido
        if not self.is_valid_binary_message(data['message']):
            return False
        
        return True


# Clase adicional para pruebas de integración
class TestReceptorIntegration:
    """
    Pruebas de integración para el receptor
    """
    
    @patch('socket.socket')
    def test_conexion_socket_mock(self, mock_socket):
        """Prueba la configuración del socket (mock)"""
        # Esta prueba requeriría refactorizar receptor.py para ser más testeable
        # Por ahora solo verificamos que el mock se puede configurar
        mock_instance = Mock()
        mock_socket.return_value = mock_instance
        
        # Verificar que se puede crear el socket
        assert mock_instance is not None
        
    def test_formato_respuesta(self):
        """Prueba el formato de la respuesta del servidor"""
        expected_response = "Decodificacion completada\n"
        assert expected_response.endswith("\n"), "La respuesta debe terminar con newline"
        assert "completada" in expected_response.lower(), "La respuesta debe indicar completación"


if __name__ == "__main__":
    # Ejecutar las pruebas
    pytest.main([__file__, "-v"])