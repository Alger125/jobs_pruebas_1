package com.apress.springbatch.chapter2.batch.util;

import java.util.Map;
import java.util.HashMap;
import com.apress.springbatch.chapter2.dto.ClienteDTO;
import com.apress.springbatch.chapter2.lib.GestionClientes;

/**
 * Utilidad tradicional para el formateo de archivos de ancho fijo.
 * Desarrollada bajo estándares de Java 1.8 sin el uso de expresiones Lambda.
 */
public class Utility {

    private static final String ID_EVENTO_DEFAULT = "BATCH-CLIENTES";
    private static final int MAX_LEN_TX_EVENTO = 2500;

    private GestionClientes gestionClientes; 
    private String odate; 

    // --- BLOQUE 1: ORQUESTADOR ---

    /**
     * Punto de entrada para el ItemWriter.
     */
    public String construirLineaTxt(ClienteDTO dto) {
        return generarLineaAnchoFijo(dto); 
    }

    // --- BLOQUE 2: GENERACIÓN DE TRAMA (ANCHO FIJO) ---

    /**
     * Genera la trama posicional.
     * Estructura: ID(10), Nombre(30), Saldo(15), Segmento(15), Cat(20), Ops(5)
     */
    public String generarLineaAnchoFijo(ClienteDTO dto) {
        StringBuilder sb = new StringBuilder();
        
        // 1. ID - 10 posiciones
        sb.append(this.aplicarPadding(dto.getId(), 10));
        
        // 2. Nombre - 30 posiciones
        sb.append(this.aplicarPadding(dto.getNombreCompleto(), 30));
        
        // 3. Saldo - 15 posiciones (Forzando 2 decimales para alineación)
        String saldoTxt = "0.00";
        if (dto.getSaldo() != null) {
            saldoTxt = String.format("%.2f", dto.getSaldo().doubleValue());
        }
        sb.append(this.aplicarPadding(saldoTxt, 15));
        
        // 4. Segmento - 15 posiciones
        sb.append(this.aplicarPadding(dto.getSegmento(), 15));
        
        // 5. Categoria - 20 posiciones
        sb.append(this.aplicarPadding(dto.getCategoriaNegocio(), 20));
        
        // 6. Operaciones - 5 posiciones
        sb.append(this.aplicarPadding(dto.getTotalOperaciones(), 5));
        
        return sb.toString();
    }

    // --- BLOQUE 3: UTILIDADES DE TEXTO (PADDDING TRADICIONAL) ---

    /**
     * Método de relleno manual para asegurar exactitud en los espacios.
     */
    private String aplicarPadding(Object valor, int longitud) {
        String texto = this.clean(valor);
        
        // Control de truncado: si el dato es mayor a la columna, se corta.
        if (texto.length() >= longitud) {
            return texto.substring(0, longitud);
        }
        
        // Relleno manual mediante StringBuilder (Forma tradicional Java 1.8)
        StringBuilder resultado = new StringBuilder(texto);
        int espaciosFaltantes = longitud - texto.length();
        
        for (int i = 0; i < espaciosFaltantes; i++) {
            resultado.append(" ");
        }
        
        return resultado.toString();
    }

    /**
     * Limpieza de nulos y espacios laterales.
     */
    public String clean(Object value) {
        if (value == null) {
            return "";
        }
        String str = String.valueOf(value).trim();
        if ("null".equalsIgnoreCase(str)) {
            return "";
        }
        return str;
    }

    // --- BLOQUE 4: GESTIÓN DE EVENTOS ---

    /**
     * Prepara el mapa para la bitácora de base de datos.
     */
    public Map<String, Object> prepararMapaEvento(String status, String mensaje) {
        Map<String, Object> mapEvent = new HashMap<String, Object>();
        
        mapEvent.put("CD_EVENTO", ID_EVENTO_DEFAULT); 
        mapEvent.put("ST_EVENTO", status);      
        mapEvent.put("TX_EVENTO", this.truncarMensaje(mensaje));
        mapEvent.put("ODATE", this.odate);
        
        return mapEvent;
    }

    private String truncarMensaje(String msg) {
        if (msg == null) {
            return "";
        }
        if (msg.length() > MAX_LEN_TX_EVENTO) {
            return msg.substring(0, MAX_LEN_TX_EVENTO - 3) + "...";
        }
        return msg;
    }

    // --- GETTERS Y SETTERS ---
    public String getOdate() { return odate; }
    public void setOdate(String odate) { this.odate = odate; }
    public GestionClientes getGestionClientes() { return gestionClientes; }
    public void setGestionClientes(GestionClientes gestionClientes) { this.gestionClientes = gestionClientes; }
}