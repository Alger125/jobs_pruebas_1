jobs_pruebas_1
Batch para exportar clientes a un archivo de ancho fijo con Spring Batch (configuración XML), consultas JdbcTemplate y SQL en .properties.
Incluye paginación real (evita cargar todo en memoria), chunking, y auditoría vía listener al final del Job.
Tabla de contenidos
#arquitectura
#qué-cambió-antes-vs-después
#componentes-y-clases

#1-pagedclienteitemreader-lector-paginado
#2-clienteitemprocessor-reglas-de-negocio
#3-clientelineaggregator-formato-ancho-fijo
#4-auditoriajoblistener-auditoría-final
#5-gestionclientes--gestionclientesimpl
#6-utility

#configuración-de-sql-properties
#configuración-xml-del-job
#cómo-ejecutar
#parámetros-del-job
#tuning-y-recomendaciones
#manejo-de-errores-y-reinicios
#pruebas-locales
#checklist-de-producción
#faq

Arquitectura
Spring Batch (XML): definición del Job y Step en XML clásico (sin Boot obligatorio).
JdbcTemplate + .properties: SQL parametrizado en sql-GCLIENTES.properties.
Reader paginado: pide páginas con OFFSET … FETCH NEXT … (Oracle 12c+), evitando OOM.
Chunking: confirmaciones por lotes (commit-interval), métricas y tolerancia a fallos por ítem.
Processor: aplica reglas de negocio sin I/O.
Writer: genera archivo fixed-width usando Utility.
Auditoría: al finalizar el Job, un JobExecutionListener registra OK/KO (sin pasos dummy).


Qué cambió (antes vs. después)
Antes

Reader devolvía una sola lista gigante (List<Map<...>>) → sin chunking real y riesgo de memoria.
Process hacía N+1 a DB por cliente y concatenaba un String gigante.
Auditoría en pasos Complete/Failed dentro del flujo.

Después
PagedClienteItemReader: entrega 1 ClienteDTO por read() con paginación real.
Nada de N+1: el SQL enriquecido trae TOTAL_OPERACIONES en la misma consulta.
ClienteItemProcessor: sólo reglas de negocio (sin I/O), deja el formateo al Writer.
ClienteLineAggregator: reusa Utility para formateo de ancho fijo.
AuditoriaJobListener: registra OK/KO al terminar el Job, basado en su ExitStatus.

Componentes y clases
1) PagedClienteItemReader (lector paginado)
Paquete: com.apress.springbatch.chapter2.reader
Responsabilidad: Lector ItemStreamReader<ClienteDTO> que:

Pide páginas a GestionClientes.listarClientesParaProcesoPaginado(params, offset, pageSize).
Entrega un ClienteDTO por read() (chunking real).
Evita cargar todo en memoria; escala a 100k+ filas.
Soporta minSaldo, estado (opcional), pageSize y maxRows por setters (inyectados desde XML).

Por qué es mejor: Eficiencia (RAM), control de throughput, y compatibilidad con tolerancia a fallos por ítem.

2) ClienteItemProcessor (reglas de negocio)
Paquete: com.apress.springbatch.chapter2.processor
Responsabilidad:

Normalización menor (trim del nombre),
Clasificación en VIP_PREMIUM, CLIENTE_ACTIVO o POTENCIAL (misma lógica que tenías).
Sin I/O (las agregaciones vienen del Reader enriquecido).

Por qué es mejor: Procesamiento puro, fácil de testear, sin N+1.

3) ClienteLineAggregator (formato ancho fijo)
Paquete: com.apress.springbatch.chapter2.file
Responsabilidad:

Implementa LineAggregator<ClienteDTO> y delega el formateo a Utility.construirLineaTxt(dto).
FlatFileItemWriter lo usa para escribir una línea por ítem.

Por qué es mejor: Separa el formateo del procesamiento y reusa tu Utility.

4) AuditoriaJobListener (auditoría final)
Paquete: com.apress.springbatch.chapter2.listener
Responsabilidad:

En afterJob, mapea COMPLETED → OK y otros estados → KO.
Llama a utility.prepararMapaEvento(...) y a gestionClientes.registrarEventoBatch(...) para insertar en la tabla de auditoría.

Por qué es mejor: Auditoría desacoplada del flujo de pasos (robusto y claro).

5) GestionClientes & GestionClientesImpl
Paquetes:

Interfaz: com.apress.springbatch.chapter2.lib
Impl: com.apress.springbatch.chapter2.lib.imp

Cambios clave:

Nuevo método listarClientesParaProcesoPaginado(Map params, int offset, int pageSize)
→ usa SQL paginado (y enriquecido si está en el .properties).
registrarEventoBatch(...) ahora envía 4 parámetros (incluye ODATE) y el INSERT está alineado con las columnas reales.
La versión no paginada se conserva para compatibilidad, pero el Job usa la paginada.


6) Utility
Paquete: com.apress.springbatch.chapter2.batch.util
Rol:

Ancho fijo: construirLineaTxt(...) / generarLineaAnchoFijo(...) (ID, Nombre, Saldo, Segmento, Categoría, Operaciones).
Auditoría: prepararMapaEvento(status, mensaje) con (CD_EVENTO, ST_EVENTO, TX_EVENTO, ODATE).

Nota: odate se inyecta desde jobParameters en XML (scope step).

Configuración de SQL (.properties)
Archivo: com/apress/springbatch/chapter2/lib/imp/sql-GCLIENTES.properties
Consultas clave
Listado enriquecido paginado (recomendado)
Plain Textproperties no es totalmente compatible. El resaltado de sintaxis se basa en Plain Text.SQL_LISTAR_CLIENTES_ENRIQUECIDO_PAGINADO=\SELECT \    c.cliente_id, \    c.nombre, \    c.apellido, \    c.segmento, \    cu.saldo, \    NVL(m.TOTAL_OPERACIONES, 0) AS TOTAL_OPERACIONES \FROM CLIENTES c \INNER JOIN CUENTAS cu ON c.cliente_id = cu.cliente_id \LEFT JOIN ( \    SELECT cliente_id, COUNT(*) AS TOTAL_OPERACIONES \    FROM MOVIMIENTOS \    GROUP BY cliente_id \) m ON m.cliente_id = c.cliente_id \WHERE cu.saldo > ? \OFFSET ? ROWS FETCH NEXT ? ROWS ONLYMostrar más líneas
Resumen por cliente (si se usa de forma independiente)
Plain Textproperties no es totalmente compatible. El resaltado de sintaxis se basa en Plain Text.SQL_GET_RESUMEN_MOVIMIENTOS=\SELECT \    COUNT(*) AS TOTAL_OPERACIONES, \    SUM(monto) AS SUMA_TOTAL, \    AVG(monto) AS PROMEDIO_MONTO \FROM MOVIMIENTOS \WHERE cliente_id = ? \GROUP BY cliente_idMostrar más líneas
Auditoría (4 columnas: incluye ODATE)
Plain Textproperties no es totalmente compatible. El resaltado de sintaxis se basa en Plain Text.SQL_INSERT_EVENTO=\INSERT INTO BATCH_LOG (CD_EVENTO, ST_EVENTO, TX_EVENTO, ODATE) \VALUES (?, ?, ?, ?)Mostrar más líneas

Importante
No mezclar XML dentro del .properties.
Si usas PostgreSQL/MySQL, reemplaza NVL por COALESCE.
Si aplicas filtro por ESTADO, añade AND c.estado = ? y pasa ese parámetro.

Configuración XML del Job
Archivo XML (ejemplo): classpath:jobs/job-clientes.xml
Puntos clave:

Inyección de parámetros del Job (MIN_SALDO, ODATE, OUTPUT, ESTADO opcional).
Reader = PagedClienteItemReader (paginado).
Processor = ClienteItemProcessor.
Writer = FlatFileItemWriter con ClienteLineAggregator.
Listener de auditoría (AuditoriaJobListener).
Chunking y tolerancia a fallos.

XML<beans xmlns="http://www.springframework.org/schema/beans"       xmlns:batch="http://www.springframework.org/schema/batch"       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"       xsi:schemaLocation="          http://www.springframework.org/schema/beans  http://www.springframework.org/schema/beans/spring-beans.xsd          http://www.springframework.org/schema/batch  http://www.springframework.org/schema/batch/spring-batch.xsd">    <!-- Utility con ODATE desde parámetros -->    <bean id="utility" class="com.apress.springbatch.chapter2.batch.util.Utility" scope="step">        <property name="odate" value="#{jobParameters['ODATE']}"/>        <property name="gestionClientes" ref="gestionClientes"/>    </bean>    <!-- DAO -->    <bean id="gestionClientes" class="com.apress.springbatch.chapter2.lib.imp.GestionClientesImpl"/>    <!-- Reader paginado -->    <bean id="clienteReader" class="com.apress.springbatch.chapter2.reader.PagedClienteItemReader" scope="step">        <property name="gestionClientes" ref="gestionClientes"/>        <property name="minSaldo" value="#{jobParameters['MIN_SALDO']}"/>        <!-- opcional -->        <property name="estado" value="#{jobParameters['ESTADO']}"/>        <!-- tuning -->        <property name="pageSize" value="2000"/>        <!-- <property name="maxRows" value="100000"/> -->    </bean>    <!-- Processor -->    <bean id="clienteProcessor" class="com.apress.springbatch.chapter2.processor.ClienteItemProcessor"/>    <!-- LineAggregator que se apoya en Utility -->    <bean id="clienteLineAggregator" class="com.apress.springbatch.chapter2.file.ClienteLineAggregator">        <property name="utility" ref="utility"/>    </bean>    <!-- Writer a archivo -->    <bean id="clienteWriter" class="org.springframework.batch.item.file.FlatFileItemWriter" scope="step">        <property name="resource" value="#{ 'file:' + jobParameters['OUTPUT'] }"/>        <property name="shouldDeleteIfExists" value="true"/>        <property name="lineAggregator" ref="clienteLineAggregator"/>        <property name="encoding" value="UTF-8"/>        <property name="saveState" value="true"/>    </bean>    <!-- Listener de auditoría -->    <bean id="auditoriaListener" class="com.apress.springbatch.chapter2.listener.AuditoriaJobListener">        <property name="utility" ref="utility"/>        <property name="gestionClientes" ref="gestionClientes"/>    </bean>    <!-- Job y Step -->    <batch:job id="jobClientes">        <batch:listener ref="auditoriaListener"/>        <batch:step id="stepExport">            <batch:tasklet transaction-manager="transactionManager">                <batch:chunk reader="clienteReader" processor="clienteProcessor" writer="clienteWriter" commit-interval="1000"/>                <batch:skippable-exception-classes>                    <batch:include class="java.lang.Exception"/>                </batch:skippable-exception-classes>            </batch:tasklet>        </batch:step>    </batch:job></beans>Mostrar más líneas

Cómo ejecutar
Opción A — CommandLineJobRunner (XML puro)

Empaqueta tu proyecto (mvn clean package o gradle build).
Ejecuta:
Shelljava -cp target/tu-jar-con-deps.jar \  org.springframework.batch.core.launch.support.CommandLineJobRunner \  jobs/job-clientes.xml jobClientes \  MIN_SALDO=10000 ODATE=20260211 OUTPUT=/ruta/salida/clientes.txt ESTADO=AMostrar más líneas

MIN_SALDO: mínimo saldo para filtrar.
ODATE: fecha de operación (ej. yyyyMMdd).
OUTPUT: ruta del archivo de salida.
ESTADO: opcional.

Opción B — Si usas Spring Boot (no obligatorio)

Define spring.batch.job.enabled=true y pasa los parámetros como JobParameters:
--MIN_SALDO=10000 --ODATE=20260211 --OUTPUT=/ruta/salida/clientes.txt --ESTADO=A

Parámetros del Job

ParámetroObligatorioDescripciónEjemploMIN_SALDOSíFiltro mínimo de saldo en la consulta10000ODATESíFecha de operación para auditoría20260211OUTPUTSíRuta del archivo de salida/tmp/out.txtESTADONoFiltro adicional de estado de clienteA

Tuning y recomendaciones

pageSize del Reader: empieza con 2000–5000.

Si la DB/Red está lenta: sube el commit-interval o baja pageSize.
Si el disco es el cuello de botella: baja el commit-interval para flush más frecuente.


commit-interval: para 100k–1M registros, 1000 es buen inicio.
Índices DB:

CUENTAS_BBVA(cliente_id, saldo)
MOVIMIENTOS_BBVA(cliente_id, fecha) (para “último movimiento” y agregados).


Encoding: UTF-8 en el Writer (ajusta según requerimiento del banco).
Layout numérico: por defecto el padding es a la derecha con espacios; si el banco pide ceros a la izquierda, ajusta Utility para esos campos numéricos.


Manejo de errores y reinicios

Tolerancia a fallas: configurada a nivel Step (skippable-exception-classes).
Skips y métricas: ahora son por ítem (cliente), no por lista completa.
Restartability: Reader/Writer con saveState=true y ItemStream guardan progreso.
Auditoría: el listener registra OK o KO con ODATE y mensaje de cierre.


Pruebas locales

DB: asegúrate de tener tablas de ejemplo (CLIENTES_BBVA, CUENTAS_BBVA, MOVIMIENTOS_BBVA) y la de auditoría (BATCH_LOG o TICR011) con su secuencia si aplica.
SQL: verifica que NVL/FETCH correspondan a tu motor (Oracle 12c+).
Datos: inserta 100k filas para medir pageSize y commit-interval.
Ejecución: corre el job con OUTPUT apuntando a una carpeta con permisos de escritura.
Validación: abre el archivo, confirma longitudes fijas por columna y número total de líneas ≈ número de clientes procesados.


Checklist de producción

 SQL_LISTAR_CLIENTES_ENRIQUECIDO_PAGINADO activo y probado.
 Índices en tablas de lectura (ver sección Tuning).
 Tabla de auditoría disponible y SQL_INSERT_EVENTO con 4 placeholders (incluye ODATE).
 Parámetros del job definidos en el scheduler (Control‑M/Airflow/cron).
 Logs con rotación (INFO por defecto, DEBUG sólo para troubleshooting).
 Ruta OUTPUT con espacio suficiente y limpieza post‑proceso (retención).
 Validación de layout (QA): tamaños de campo y encoding final.
 Pruebas de restart (matar el proceso y relanzar) para validar ItemStream.


FAQ
1) ¿Qué pasa si mi motor no soporta OFFSET … FETCH NEXT …?
Usa paginado por ROWNUM (Oracle <12c) o ventanas. Podemos agregar un SQL alterno con esos mecanismos y cambiar el método paginado a esa consulta.
2) ¿Puedo paralelizar el Step?
Sí. Este Reader paginado es compatible con Partitioner por rangos de cliente_id o multi‑threaded step, siempre que cada partición lea rangos disjuntos.
Recomendado al superar ~5–10 millones de filas.
4) ¿Qué ganó el equipo con este refactor?
   
Estabilidad de memoria (paginado real),
rendimientos lineales (sin N+1),
operabilidad (skips/retry por ítem) y
auditoría confiable (listener al cierre).
