package com.apress.springbatch.chapter2.lib.imp;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.apress.springbatch.chapter2.lib.GestionClientes;

public class GestionClientesImpl extends GestionClientesAbstract implements GestionClientes {

    /**
     * 1. LISTAR CLIENTES
     * Show: El Reader lo llama para traer la masa de datos inicial.
     */
    @Override
    public List<Map<String, Object>> listarClientesParaProceso(Map<String, Object> params) {
        String sql = sqlQueries.getProperty("SQL_LISTAR_CLIENTES_CON_SALDO");
        // Filtramos por el saldo mínimo que pasamos desde el Job
        return this.jdbcTemplate.queryForList(sql, params.get("MIN_SALDO"));
    }

    /**
     * 2. OBTENER DETALLE MOVIMIENTO
     * Show: Se usa en el Processor para traer montos y fechas específicas.
     */
    @Override
    public Map<String, Object> obtenerDetalleMovimiento(Map<String, Object> params) {
        String sql = sqlQueries.getProperty("SQL_GET_DETALLE_MOVIMIENTO");
        List<Map<String, Object>> lista = this.jdbcTemplate.queryForList(sql, params.get("CLIENTE_ID"));
        return (lista.isEmpty()) ? new HashMap<String, Object>() : lista.get(0);
    }

    /**
     * 3. OBTENER ÚLTIMO MOVIMIENTO
     * Show: Consulta rápida para saber la última actividad del cliente.
     */
    @Override
    public Map<String, Object> obtenerUltimoMovimiento(Map<String, Object> params) {
        String sql = sqlQueries.getProperty("SQL_GET_ULTIMO_MOVIMIENTO");
        List<Map<String, Object>> lista = this.jdbcTemplate.queryForList(sql, params.get("CLIENTE_ID"));
        return (lista.isEmpty()) ? new HashMap<String, Object>() : lista.get(0);
    }

    /**
     * 4. REGISTRAR EVENTO BATCH
     * Show: Inserta en la tabla TICR011 para que el banco sepa si el proceso falló o triunfó.
     * Usamos .update() porque es un INSERT.
     */
    @Override
    public int registrarEventoBatch(Map<String, Object> params) {
        String sql = sqlQueries.getProperty("SQL_INSERT_EVENTO");
        return this.jdbcTemplate.update(sql, 
            params.get("CD_EVENTO"), 
            params.get("ST_EVENTO"), 
            params.get("TX_EVENTO")
        );
    }
    
    @Override
    public Map<String, Object> obtenerResumen(Map<String, Object> params) {
        String sql = sqlQueries.getProperty("SQL_GET_RESUMEN_MOVIMIENTOS");
        List<Map<String, Object>> resultado = this.jdbcTemplate.queryForList(sql, params.get("CLIENTE_ID"));
        if (resultado.isEmpty()) {
            return new HashMap<String, Object>(); 
        } else {
            return resultado.get(0);
        }
    }
}