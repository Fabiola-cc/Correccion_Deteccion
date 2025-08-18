import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from datetime import datetime

# Configurar estilo simple
plt.style.use('default')
sns.set_palette("Set2")

# Leer los datos
df = pd.read_csv('C:/Users/villa/Desktop/Clases_S8/1.Redes/1.Lab_2/Correccion_Deteccion/test/10K.csv')  # Cambia por el nombre de tu archivo CSV

# Crear timestamp para guardar
timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

# ===============================
# UNA SOLA IMAGEN CON 4 GRÁFICAS SIMPLES
# ===============================
fig, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, figsize=(16, 12))
fig.suptitle('Análisis Simple: Hamming vs Fletcher', fontsize=16, fontweight='bold')

# 1. Tiempo Total por Tamaño (Promedio)
hamming = df[df['Algoritmo'] == 1].groupby('TamanoMensaje')['TiempoTotalMs'].mean()
fletcher = df[df['Algoritmo'] == 2].groupby('TamanoMensaje')['TiempoTotalMs'].mean()

ax1.plot(hamming.index, hamming.values, 'o-', label='Hamming', linewidth=2, markersize=8)
ax1.plot(fletcher.index, fletcher.values, 's-', label='Fletcher', linewidth=2, markersize=8)
ax1.set_xlabel('Tamaño del Mensaje')
ax1.set_ylabel('Tiempo Total (ms)')
ax1.set_title('Tiempo vs Tamaño')
ax1.legend()
ax1.grid(True, alpha=0.3)

# 2. Throughput por Tamaño (Promedio)
hamming_th = df[df['Algoritmo'] == 1].groupby('TamanoMensaje')['ThroughputCarSegundo'].mean()
fletcher_th = df[df['Algoritmo'] == 2].groupby('TamanoMensaje')['ThroughputCarSegundo'].mean()

ax2.plot(hamming_th.index, hamming_th.values, 'o-', label='Hamming', linewidth=2, markersize=8)
ax2.plot(fletcher_th.index, fletcher_th.values, 's-', label='Fletcher', linewidth=2, markersize=8)
ax2.set_xlabel('Tamaño del Mensaje')
ax2.set_ylabel('Throughput (car/seg)')
ax2.set_title('Velocidad vs Tamaño')
ax2.legend()
ax2.grid(True, alpha=0.3)

# 3. Precisión por Probabilidad de Error (Promedio)
hamming_prec = df[df['Algoritmo'] == 1].groupby('ProbabilidadError')['PrecisionDecodificacion'].mean()
fletcher_prec = df[df['Algoritmo'] == 2].groupby('ProbabilidadError')['PrecisionDecodificacion'].mean()

ax3.plot(hamming_prec.index, hamming_prec.values, 'o-', label='Hamming', linewidth=2, markersize=8)
ax3.plot(fletcher_prec.index, fletcher_prec.values, 's-', label='Fletcher', linewidth=2, markersize=8)
ax3.set_xlabel('Probabilidad de Error')
ax3.set_ylabel('Precisión')
ax3.set_title('Precisión vs Error')
ax3.legend()
ax3.grid(True, alpha=0.3)

# 4. Comparación Simple de Promedios
algoritmos = ['Hamming', 'Fletcher']
tiempos = [df[df['Algoritmo'] == 1]['TiempoTotalMs'].mean(), 
           df[df['Algoritmo'] == 2]['TiempoTotalMs'].mean()]
throughputs = [df[df['Algoritmo'] == 1]['ThroughputCarSegundo'].mean(), 
               df[df['Algoritmo'] == 2]['ThroughputCarSegundo'].mean()]

# Normalizar para mostrar en el mismo gráfico
tiempos_norm = [t/max(tiempos) for t in tiempos]
throughputs_norm = [t/max(throughputs) for t in throughputs]

x = range(len(algoritmos))
width = 0.35

ax4.bar([i - width/2 for i in x], tiempos_norm, width, label='Tiempo (norm)', alpha=0.8)
ax4.bar([i + width/2 for i in x], throughputs_norm, width, label='Throughput (norm)', alpha=0.8)
ax4.set_xlabel('Algoritmo')
ax4.set_ylabel('Valor Normalizado')
ax4.set_title('Comparación General')
ax4.set_xticks(x)
ax4.set_xticklabels(algoritmos)
ax4.legend()
ax4.grid(True, alpha=0.3)

plt.tight_layout()

# ===============================
# ESTADÍSTICAS SÚPER SIMPLES
# ===============================
print("\n" + "="*50)
print("📊 RESUMEN SÚPER SIMPLE")
print("="*50)

# Datos generales
total_registros = len(df)
hamming_data = df[df['Algoritmo'] == 1]
fletcher_data = df[df['Algoritmo'] == 2]

print(f"\n📈 Total de pruebas: {total_registros}")
print(f"🔷 Pruebas Hamming: {len(hamming_data)}")
print(f"🔶 Pruebas Fletcher: {len(fletcher_data)}")

# Promedios simples
print(f"\n⏱️  TIEMPO PROMEDIO:")
print(f"   Hamming:  {hamming_data['TiempoTotalMs'].mean():.1f} ms")
print(f"   Fletcher: {fletcher_data['TiempoTotalMs'].mean():.1f} ms")

print(f"\n🚀 VELOCIDAD PROMEDIO:")
print(f"   Hamming:  {hamming_data['ThroughputCarSegundo'].mean():.0f} car/seg")
print(f"   Fletcher: {fletcher_data['ThroughputCarSegundo'].mean():.0f} car/seg")

print(f"\n🎯 PRECISIÓN PROMEDIO:")
print(f"   Hamming:  {hamming_data['PrecisionDecodificacion'].mean():.3f}")
print(f"   Fletcher: {fletcher_data['PrecisionDecodificacion'].mean():.3f}")

# Ganadores
faster = "Hamming" if hamming_data['TiempoTotalMs'].mean() < fletcher_data['TiempoTotalMs'].mean() else "Fletcher"
speedier = "Hamming" if hamming_data['ThroughputCarSegundo'].mean() > fletcher_data['ThroughputCarSegundo'].mean() else "Fletcher"
precise = "Hamming" if hamming_data['PrecisionDecodificacion'].mean() > fletcher_data['PrecisionDecodificacion'].mean() else "Fletcher"

print(f"\n🏆 GANADORES:")
print(f"   ⚡ Más rápido: {faster}")
print(f"   🚀 Más veloz: {speedier}")
print(f"   🎯 Más preciso: {precise}")

print("\n" + "="*50)
print("🎉 ¡Listo!")

# ===============================
# TABLA RESUMEN SIMPLE
# ===============================
print("\n📋 TABLA RESUMEN:")
print("-" * 60)
print(f"{'Métrica':<20} {'Hamming':<15} {'Fletcher':<15} {'Ganador':<10}")
print("-" * 60)

metrics = [
    ('Tiempo Promedio', f"{hamming_data['TiempoTotalMs'].mean():.1f} ms", 
     f"{fletcher_data['TiempoTotalMs'].mean():.1f} ms", faster),
    ('Throughput', f"{hamming_data['ThroughputCarSegundo'].mean():.0f}", 
     f"{fletcher_data['ThroughputCarSegundo'].mean():.0f}", speedier),
    ('Precisión', f"{hamming_data['PrecisionDecodificacion'].mean():.3f}", 
     f"{fletcher_data['PrecisionDecodificacion'].mean():.3f}", precise),
    ('Eficiencia', f"{hamming_data['EficienciaBits'].mean():.4f}", 
     f"{fletcher_data['EficienciaBits'].mean():.4f}", 
     "Hamming" if hamming_data['EficienciaBits'].mean() > fletcher_data['EficienciaBits'].mean() else "Fletcher")
]

for metric, ham_val, flet_val, winner in metrics:
    print(f"{metric:<20} {ham_val:<15} {flet_val:<15} {winner:<10}")

print("-" * 60)