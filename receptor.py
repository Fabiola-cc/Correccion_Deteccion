import socket
import json
from FletcherChecksum.checksum_receptor import checksum_receptor
# Importar funciones específicas del receptor de Hamming
from Hamming.Hamming_receptor import (procesar_mensaje_recibido, procesar_hamming_receptor)

def ascii_bin_to_text(binary_str):
    chars = []
    for i in range(0, len(binary_str), 8):
        byte = binary_str[i:i+8]              # Tomar 8 bits
        char = chr(int(byte, 2))              # Convertir a entero y luego a char
        chars.append(char)
    return ''.join(chars)

# TRANSMISIÓN
# Configuración del servidor
HOST = "127.0.0.1"  # Localhost
PORT = 5555

# Crear socket TCP
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind((HOST, PORT))
server_socket.listen(1)

print(f"Servidor escuchando en {HOST}:{PORT}...")

conn, addr = server_socket.accept()
print(f"Conectado por {addr}")

# Recibir datos
buffer = ""
while not buffer.endswith("\n"):
    buffer += conn.recv(4096).decode("utf-8")

buffer = buffer.strip()
data = json.loads(buffer)
msg = data["message"]
scheme = data["scheme"]
fletcherT = data["fletcherT"]

# ENLACE
# Revisar el mensaje según el esquema escogido
if (scheme == 1): # Hamming
    exists, bin_msg, fue_corregido = procesar_hamming_receptor(msg)
    
elif (scheme == 2): # Checksum
    flCR = checksum_receptor(fletcherT)
    exists, bin_msg = flCR.verify_checksum(msg)

# PRESENTACION y APLICACIÓN
if(exists == False): # NO existe un error (o fue corregido)
    og_msg = ascii_bin_to_text(bin_msg)

    if scheme == 1 and fue_corregido:
        print(f"\nSe corrigió un error. El mensaje decodificado es: {og_msg}")
    else:
        print(f"\nNo se detectaron errores. El mensaje es: {og_msg}")

elif (exists and scheme == 2): # No hay correción
    print("Hay un error en el mensaje recibido. El mensaje se descarta")

elif (exists and scheme == 1): # Hamming: Múltiples errores detectados
    print("Se detectaron múltiples errores. El mensaje se descarta")

# Responder
conn.sendall("Decodificacion completada\n".encode("utf-8"))

# Detener conexión
conn.shutdown(socket.SHUT_WR)  # Cierra solo escritura, no lectura
conn.close()
