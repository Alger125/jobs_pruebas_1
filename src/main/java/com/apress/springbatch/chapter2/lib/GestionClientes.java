package com.apress.springbatch.chapter2.lib;

import java.util.List;
import java.util.Map;

public interface GestionClientes {

    /**
     * 1. LISTAR CLIENTES PARA PROCESO
     * ¿Qué show? Este es el motor del READER. 
     * Se encarga de traer a todos los clientes que cumplen con el filtro 
     * (ej. saldo > 10,000). Devuelve una lista de "filas" de la base de datos.
     */
    List<Map<String, Object>> listarClientesParaProceso(Map<String, Object> params);

    /**
     * 2. OBTENER ÚLTIMO MOVIMIENTO
     * ¿Qué show? Se usa para el control de auditoría simple. 
     * Trae la fecha o el tipo del último movimiento registrado para saber 
     * si la cuenta está activa antes de procesarla.
     */
    Map<String, Object> obtenerUltimoMovimiento(Map<String, Object> params);

    /**
     * 3. OBTENER DETALLE MOVIMIENTO
     * ¿Qué show? Este es para el ENRIQUECIMIENTO en el Processor.
     * Si el reporte necesita el "monto" o la "descripción" del movimiento, 
     * este método va a la tabla MOVIMIENTOS_BBVA y trae el detalle completo.
     */
    Map<String, Object> obtenerDetalleMovimiento(Map<String, Object> params);

    /**
     * 4. REGISTRAR EVENTO BATCH
     * ¿Qué show? Este es el informante del banco.
     * Escribe en la tabla de auditoría (TICR011) si el Job terminó bien 
     * o si hubo un error técnico, para que los monitores del banco lo vean.
     */
    int registrarEventoBatch(Map<String, Object> params);
    
    public Map<String, Object> obtenerResumen(Map<String, Object> params);
}