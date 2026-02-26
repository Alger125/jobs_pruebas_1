# jobs_pruebas_1 — Proyecto LOX Batch (México)

Repositorio multi-módulo que implementa un proceso batch bancario para el procesamiento
diario de contratos de crédito de clientes VIP.

---

## 🏗️ Arquitectura General

El proyecto está dividido en tres módulos con responsabilidades bien separadas:
```
loxc001          (Capa de Modelo)
    └── MovimientoDTO.java       → DTO principal de transferencia de datos
    └── PimientosMapper.java     → RowMapper para lectura de BD Oracle

loxbd001 / LOXR174              (Capa de Acceso a Datos y Lógica)
    └── LOXR174.java             → Interfaz del servicio
    └── LOXR174Abstract.java     → Inyección de dependencias (Spring setter injection)
    └── LOXR174Impl.java         → Implementación: valida eventos y consulta saldos
    └── VariablesSQL.java        → Catálogo de claves SQL (enum)

loxj001-01-mx / LOXJ162-01-MX   (Capa de Orquestación — Spring Batch Job)
    └── Reader.java              → Stub local (el reader real es JdbcCursorItemReader)
    └── Process.java             → ItemProcessor: valida, notifica a LOXR174, formatea salida
    └── Complete.java            → Tasklet: notifica OK al finalizar
    └── Failed.java              → Tasklet: captura errores y notifica KO
    └── Utility.java             → Clase base con herramientas comunes
```

---

## ⚙️ Flujo de Ejecución

El job recibe como parámetro de entrada la fecha operativa (`odate` en formato `YYYYMMDD`).

1. **Reader** — Consulta Oracle con `odate` y segmento `VIP`. Une las tablas
   `MOVIMIENTOS`, `CUENTAS` y `CLIENTES`. Produce objetos `MovimientoDTO`.

2. **Processor** — Por cada `MovimientoDTO` recibido:
   - Construye un evento con `STATUS=PROCESS` y lo envía a `LOXR174.executeCreateCreditContract()`
   - Formatea una línea de salida con nombre, cuenta, monto y fecha (`YYYY/MM/DD`)
   - Imprime progreso en consola cada 1,000 registros

3. **Writer** — Escribe las líneas formateadas en el archivo de salida:
   `LOX_D02_{odate}.txt`

4. **Complete / Failed** — Al terminar el step:
   - Si fue exitoso → notifica `STATUS=OK` a LOXR174
   - Si falló → extrae la excepción del contexto de Spring Batch y notifica `STATUS=KO`

---

## 🗄️ Base de Datos

**Motor:** Oracle XE  
**Tablas principales:**

| Tabla | Rol |
|---|---|
| `MOVIMIENTOS` | Movimientos diarios por cliente |
| `CUENTAS` | Relación cliente-cuenta |
| `CLIENTES` | Datos del cliente (nombre) |
| `SALDOS` | Saldo disponible y límite de crédito por cuenta |

Las consultas SQL se externalizan en:
`src/main/resources/sql-LOXBR001IMP.properties`

---

## ▶️ Cómo ejecutar el Job

### Parámetros requeridos

| Parámetro | Formato | Ejemplo |
|---|---|---|
| `odate` | `YYYYMMDD` | `20260212` |
| `time` | timestamp (evita duplicados) | `System.currentTimeMillis()` |

### Ejecución manual (desarrollo local)

Correr la clase `LoxJobTestManual.java` como JUnit test desde el IDE.
Requiere conexión activa a Oracle y las tablas de Spring Batch creadas en el esquema.

---

## 🧪 Cobertura de Tests

Todos los módulos cuentan con tests unitarios con JUnit 4 + Mockito.

| Módulo | Tests | Cobertura |
|---|---|---|
| `loxc001` | 7 | 100% instrucciones |
| `loxbd001` | 9 | 88% instrucciones, 78% branches |
| `loxj001-01-mx` | 6 | Unitarios por clase |

---

## 🛠️ Stack Tecnológico

- Java 8
- Spring Batch / Spring JDBC 3.0.x
- Oracle JDBC (ojdbc)
- JUnit 4.13.2 + Mockito 3.12.4
- JaCoCo 0.8.8
- Maven (estructura multi-módulo)

---

## 📁 Archivo de Salida

El job genera un archivo de texto plano por ejecución:
```
LOX_D02_20260212.txt
```

Formato de cada línea:
```
NOMBRE: JUAN PEREZ         | CUENTA: CTA-001        | MONTO:   15000.00 | FECHA: 2026/02/12
```

> **Nota:** La fecha en el archivo usa formato `YYYY/MM/DD`.  
> La fecha enviada a la librería y almacenada en BD usa formato plano `YYYYMMDD`.
