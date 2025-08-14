import sys
class checksum_receptor:  # Definición de la clase
    def __init__(self, eleccion):  # Método constructor (inicializador)
        if(eleccion == 1):
            self.tipo = 8
            self.n = 4
            self.sum1 = self.sum2 = 0b0001
        elif(eleccion == 2):
            self.tipo = 16
            self.n = 8
            self.sum1 = self.sum2 = 0b00000001
        elif(eleccion == 3):
            self.tipo = 32
            self.n = 16
            self.sum1 = self.sum2 = 0b0000000000000001

    def checksum(self, message):
        '''
        Cálculo de checksum para comparación
        '''
        # dividir el mensaje
        sections = [message[i:i + self.n] for i in range(0, len(message), self.n)]

        # calcular checksum
        toMod = 2**self.n -1
        for section in sections:
            self.sum1 = (self.sum1 + int(section, 2)) % toMod
            self.sum2 = (self.sum1 + self.sum2) % toMod
        
        checksum = (self.sum2 << self.n) | self.sum1
        return checksum
        

    def verify_checksum(self, given):
        '''
        Proceso completo de verificación; divide, calcula y compara
        '''
        # agregar ceros al inicio para asegurar la cantidad de bits
        size_mensaje = len(given)
        if size_mensaje % self.n != 0: 
            missing = self.n - (size_mensaje % self.n)
            given = '0' * missing + given

        # dividir mensaje y checksum
        message_size = len(given) - self.tipo # tamaño del mensaje original sin checksum
        message = given[:message_size] # mensaje original
        checksum_compare = int(given[-self.tipo:], 2) # checksum enviado

        # calcular el checksum
        calculated_checksum = self.checksum(message)

        # comparar y notificar
        if(checksum_compare != calculated_checksum):
            print("Hay un error en el mensaje recibido. El mensaje se descarta")
        else:
            print("\nNo se detectaron errores.")
            print(f"El mensaje es {message}")

if __name__ == "__main__":
    print("Bienvenido. Esta es una simulación del RECEPTOR de Checksum Fletcher")

    eleccion = 0
    tries = 0
    while True:
        if tries > 3:
            print("Lo lamento, has intentado más de tres veces.", file=sys.stderr)
            print("Vuelve a ejecutar el código para empezar de nuevo.", file=sys.stderr)
            sys.exit(1)  # Sale de la función principal

        print("\nElige el tipo con el que quieres trabajar. (Escribe 1, 2 o 3)")
        print("1. Fletcher-8\n2. Fletcher-16\n3. Fletcher-32")

        try:
            eleccion = int(input())
            if 1 <= eleccion <= 3:
                break  # Sale del bucle si la elección es válida
            else:
                print("Error. Debes elegir un número entre 1 y 3.", file=sys.stderr)
                tries += 1
        except ValueError:
            print("Entrada inválida. Debes escribir un número.", file=sys.stderr)
            tries += 1
    
    receptor =checksum_receptor(eleccion)
    
    print(f"\nGenial, estamos usando Fletcher-{receptor.tipo}.")
    print("Ahora la respuesta del emisor (i.e.: 110101):")
    response = input()
    
    if not response.isnumeric() or not all(c in '01' for c in response):
        print("Error: La respuesta debe ser binaria.", file=sys.stderr)
        sys.exit(1)

    # 2. Ejecutar el algoritmo y mostrar resultado
    receptor.verify_checksum(response)