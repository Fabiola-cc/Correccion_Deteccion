"""
Algoritmo de Hamming - Receptor

1. Solicitamos el mensaje binario concatenado con la información del emisor
2. Ejecutamos el algoritmo de verificación de integridad
3. Calculamos los bits de paridad para verificar errores
4. Determinamos si hay errores y su posición
5. Corregimos errores si es posible (1 bit) o descartamos si hay múltiples errores
6. Devolvemos el mensaje original sin los bits de paridad
"""

import math

def main():
    # Paso 1: Solicitar mensaje concatenado del emisor
    mensaje_recibido = solicitar_mensaje_recibido()
    
    print(f"Mensaje recibido: {mensaje_recibido}")
    
    # Paso 2: Ejecutar algoritmo de verificación
    procesar_mensaje_recibido(mensaje_recibido)

def solicitar_mensaje_recibido():
    """
    Solicita el mensaje binario recibido (mensaje + bits de paridad)
    """
    while True:
        mensaje = input("Ingrese el mensaje recibido (con bits de paridad): ").strip()
        
        if es_mensaje_binario_valido(mensaje):
            return mensaje
        else:
            print("Error: El mensaje debe contener solo '0' y '1'. Intente nuevamente.")

def es_mensaje_binario_valido(mensaje):
    """
    Valida que el mensaje solo contenga caracteres binarios
    """
    if not mensaje:
        return False
    
    for char in mensaje:
        if char not in ['0', '1']:
            return False
    
    return True

def procesar_mensaje_recibido(mensaje_recibido):
    """
    Procesa el mensaje recibido y ejecuta el algoritmo de verificación
    """
    longitud_total = len(mensaje_recibido)
    
    # Determinar cuántos bits de paridad hay en el mensaje
    cantidad_paridad = calcular_bits_paridad_en_mensaje(longitud_total)
    
    print(f"Longitud total del mensaje: {longitud_total}")
    print(f"Bits de paridad detectados: {cantidad_paridad}")
    
    # Crear matriz para el análisis
    matriz = []
    for i in range(longitud_total):
        posicion = str(i + 1)
        bit = mensaje_recibido[i]
        matriz.append([bit, posicion])
    
    # Identificar posiciones de paridad
    posiciones_paridad = set()
    for i in range(cantidad_paridad):
        posicion = 2 ** i  # 1, 2, 4, 8, 16...
        if posicion <= longitud_total:
            posiciones_paridad.add(posicion)
    
    # Mostrar matriz recibida
    print("\nMatriz del mensaje recibido:")
    print("Posición: ", end="")
    for i in range(longitud_total):
        print(f"{matriz[i][1]:>4}", end=" ")
    print()
    print("Bit:      ", end="")
    for i in range(longitud_total):
        print(f"{matriz[i][0]:>4}", end=" ")
    print()
    
    # Paso 3: Calcular bits de paridad para verificación
    print(f"\nVerificando integridad del mensaje...")
    print(f"Posiciones de paridad: {sorted(posiciones_paridad)}")
    
    # Calcular cada bit de paridad esperado
    bits_sindrome = []
    
    for p in range(cantidad_paridad):
        posicion_paridad = 2 ** p
        bit_paridad = 1 << p  # 2^p para verificar el bit correspondiente
        
        print(f"\nVerificando P{p + 1} (posición {posicion_paridad}):")
        
        # Crear lista de bits a evaluar (incluyendo el bit de paridad recibido)
        bits_a_evaluar = []
        posiciones_evaluadas = []
        
        for i in range(longitud_total):
            posicion = i + 1
            
            # Incluir todas las posiciones que tengan '1' en el bit correspondiente
            if (posicion & bit_paridad) != 0:
                bits_a_evaluar.append(mensaje_recibido[i])
                posiciones_evaluadas.append(posicion)
        
        print(f"Posiciones que tienen '1' en el bit {p + 1}: {posiciones_evaluadas}")
        print(f"Bits a evaluar: {' '.join(bits_a_evaluar)}")
        
        # Calcular XOR de todos los bits (incluyendo el de paridad)
        resultado_xor = '0'
        for bit in bits_a_evaluar:
            if resultado_xor == '0':
                resultado_xor = bit
            else:
                # XOR: 0⊕0=0, 0⊕1=1, 1⊕0=1, 1⊕1=0
                if resultado_xor == bit:
                    resultado_xor = '0'
                else:
                    resultado_xor = '1'
        
        print(f"Resultado XOR (síndrome S{p + 1}): {resultado_xor}")
        bits_sindrome.append(resultado_xor)
    
    # Paso 4: Determinar si hay errores y su posición
    sindrome_decimal = calcular_sindrome_decimal(bits_sindrome)
    
    print(f"\nSíndrome completo: {''.join(reversed(bits_sindrome))}")
    print(f"Síndrome en decimal: {sindrome_decimal}")
    
    # Paso 5: Corregir errores o descartar mensaje
    if sindrome_decimal == 0:
        # Paso 3a: No se detectaron errores
        print("\n✅ NO SE DETECTARON ERRORES")
        mensaje_original = extraer_mensaje_original(mensaje_recibido, posiciones_paridad)
        print(f"Mensaje original: {mensaje_original}")
        
    elif sindrome_decimal <= longitud_total:
        # Paso 3c: Se detectó y se puede corregir 1 error
        print(f"\n⚠️ SE DETECTÓ Y CORRIGIÓ 1 ERROR")
        print(f"Error en la posición: {sindrome_decimal}")
        
        # Corregir el error
        mensaje_corregido = list(mensaje_recibido)
        if mensaje_corregido[sindrome_decimal - 1] == '0':
            mensaje_corregido[sindrome_decimal - 1] = '1'
        else:
            mensaje_corregido[sindrome_decimal - 1] = '0'
        
        mensaje_corregido_str = ''.join(mensaje_corregido)
        print(f"Mensaje corregido: {mensaje_corregido_str}")
        
        # Extraer mensaje original sin bits de paridad
        mensaje_original = extraer_mensaje_original(mensaje_corregido_str, posiciones_paridad)
        print(f"Mensaje original: {mensaje_original}")
        
    else:
        # Paso 3b: Se detectaron múltiples errores (no se puede corregir)
        print(f"\n❌ SE DETECTARON MÚLTIPLES ERRORES")
        print(f"El mensaje se descarta por detectar errores no corregibles.")
        print(f"Síndrome fuera de rango: {sindrome_decimal} > {longitud_total}")

def calcular_bits_paridad_en_mensaje(longitud_total):
    """
    Calcula cuántos bits de paridad debe tener un mensaje de longitud dada
    """
    # Necesitamos encontrar p tal que 2^p >= longitud_total
    p = 0
    while 2 ** p < longitud_total:
        p += 1
    
    # Verificar que esta cantidad de bits de paridad es correcta
    # para la longitud del mensaje
    mensaje_datos = longitud_total - p
    while 2 ** p < mensaje_datos + p + 1:
        p += 1
        mensaje_datos = longitud_total - p
    
    return p

def calcular_sindrome_decimal(bits_sindrome):
    """
    Convierte el síndrome binario a decimal
    """
    decimal = 0
    for i, bit in enumerate(bits_sindrome):
        if bit == '1':
            decimal += 2 ** i
    
    return decimal

def extraer_mensaje_original(mensaje_completo, posiciones_paridad):
    """
    Extrae el mensaje original eliminando los bits de paridad
    """
    mensaje_original = ""
    
    for i in range(len(mensaje_completo)):
        posicion = i + 1
        if posicion not in posiciones_paridad:
            mensaje_original += mensaje_completo[i]
    
    return mensaje_original

def procesar_hamming_receptor(mensaje_recibido):
    """
    Usa las funciones existentes de Hamming_receptor.py
    Retorna (hay_error, mensaje_original, fue_corregido)
    """
    import io
    import sys
    from contextlib import redirect_stdout
    
    print(f"Procesando mensaje con Hamming: {mensaje_recibido}")
    
    longitud_total = len(mensaje_recibido)
    cantidad_paridad = calcular_bits_paridad_en_mensaje(longitud_total)
    
    # Identificar posiciones de paridad
    posiciones_paridad = set()
    for i in range(cantidad_paridad):
        posicion = 2 ** i  # 1, 2, 4, 8, 16...
        if posicion <= longitud_total:
            posiciones_paridad.add(posicion)
    
    # Calcular bits de síndrome
    bits_sindrome = []
    
    for p in range(cantidad_paridad):
        bit_paridad = 1 << p  # 2^p para verificar el bit correspondiente
        
        # Crear lista de bits a evaluar
        bits_a_evaluar = []
        
        for i in range(longitud_total):
            posicion = i + 1
            
            # Incluir todas las posiciones que tengan '1' en el bit correspondiente
            if (posicion & bit_paridad) != 0:
                bits_a_evaluar.append(mensaje_recibido[i])
        
        # Calcular XOR de todos los bits
        resultado_xor = '0'
        for bit in bits_a_evaluar:
            if resultado_xor == '0':
                resultado_xor = bit
            else:
                # XOR: 0⊕0=0, 0⊕1=1, 1⊕0=1, 1⊕1=0
                if resultado_xor == bit:
                    resultado_xor = '0'
                else:
                    resultado_xor = '1'
        
        bits_sindrome.append(resultado_xor)
    
    # Determinar si hay errores
    sindrome_decimal = calcular_sindrome_decimal(bits_sindrome)
    
    print(f"Síndrome en decimal: {sindrome_decimal}")
    
    # Procesar según el síndrome
    if sindrome_decimal == 0:
        # No se detectaron errores
        print("✅ NO SE DETECTARON ERRORES")
        mensaje_original = extraer_mensaje_original(mensaje_recibido, posiciones_paridad)
        return False, mensaje_original, False
        
    elif sindrome_decimal <= longitud_total:
        # Se detectó y se puede corregir 1 error
        print(f"⚠️ SE DETECTÓ Y CORRIGIÓ 1 ERROR en posición: {sindrome_decimal}")
        
        # Corregir el error
        mensaje_corregido = list(mensaje_recibido)
        if mensaje_corregido[sindrome_decimal - 1] == '0':
            mensaje_corregido[sindrome_decimal - 1] = '1'
        else:
            mensaje_corregido[sindrome_decimal - 1] = '0'
        
        mensaje_corregido_str = ''.join(mensaje_corregido)
        
        # Extraer mensaje original sin bits de paridad
        mensaje_original = extraer_mensaje_original(mensaje_corregido_str, posiciones_paridad)
        return False, mensaje_original, True  # No hay error después de corrección
        
    else:
        # Se detectaron múltiples errores (no se puede corregir)
        print(f"❌ SE DETECTARON MÚLTIPLES ERRORES")
        print(f"El mensaje se descarta por detectar errores no corregibles.")
        return True, "", False


if __name__ == "__main__":
    main()