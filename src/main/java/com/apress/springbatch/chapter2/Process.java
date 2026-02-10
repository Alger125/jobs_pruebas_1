package com.apress.springbatch.chapter2;

import org.apache.log4j.Logger;
import org.springframework.batch.item.ItemProcessor;
import com.apress.springbatch.chapter2.batch.util.Utility;
import com.apress.springbatch.chapter2.dto.ClienteDTO;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

public class Process extends Utility implements ItemProcessor<List<Map<String, Object>>, String> {

    private static final Logger log = Logger.getLogger(Process.class);

    @Override
    public String process(List<Map<String, Object>> items) throws Exception {
        log.info("Iniciando el procesamiento de " + items.size() + " clientes.");
        StringBuilder sb = new StringBuilder();
        
        for (Map<String, Object> fila : items) {
            try {
                // 1. Mapeo inicial (Datos de CLIENTES_BBVA)
                ClienteDTO dto = mapearFilaADto(fila);
                
                // 2. Enriquecimiento (Datos de MOVIMIENTOS_BBVA)
                enriquecerConResumen(dto);
                
                // 3. Reglas de Negocio
                determinarCategoriaNegocio(dto);
                
                // 4. Construcción de línea para el TXT
                sb.append(this.construirLineaTxt(dto)).append(System.lineSeparator());
                
            } catch (Exception e) {
                log.error("Error crítico procesando la fila con ID: " + fila.get("CLIENTE_ID"), e);
                // En un Batch, si una fila truena, logueamos y seguimos con la siguiente
            }
        }
        
        return sb.toString();
    }

    private ClienteDTO mapearFilaADto(Map<String, Object> fila) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(Long.parseLong(String.valueOf(fila.get("CLIENTE_ID"))));
        
        String nombre = this.clean(fila.get("NOMBRE"));
        String apellido = this.clean(fila.get("APELLIDO"));
        dto.setNombreCompleto(nombre + " " + apellido);
        
        dto.setSaldo(Double.parseDouble(String.valueOf(fila.get("SALDO"))));
        dto.setSegmento(this.clean(fila.get("SEGMENTO")));
        
        return dto;
    }

    private void enriquecerConResumen(ClienteDTO dto) {
        Map<String, Object> params = new HashMap<>();
        params.put("CLIENTE_ID", dto.getId());

        // Llamada a la DB mediante el método que ya explicamos paso a paso
        Map<String, Object> resumen = this.getGestionClientes().obtenerResumen(params);

        if (resumen != null && !resumen.isEmpty()) {
            // Extraemos el conteo del query (TOTAL_OPERACIONES)
            Object totalOps = resumen.get("TOTAL_OPERACIONES");
            dto.setTotalOperaciones(Integer.parseInt(String.valueOf(totalOps)));
            
            if (log.isDebugEnabled()) {
                log.debug("Enriquecido Cliente ID: " + dto.getId() + " con " + totalOps + " operaciones.");
            }
        } else {
            // Si no tiene movimientos, seteamos 0 para que no salga 'null' en el archivo
            dto.setTotalOperaciones(0);
            log.warn("Sin movimientos para el cliente: " + dto.getId());
        }
    }

    private void determinarCategoriaNegocio(ClienteDTO dto) {
        // Lógica de negocio combinada (Saldo + Segmento)
        if ("PLATINO".equals(dto.getSegmento()) && dto.getSaldo() > 50000) {
            dto.setCategoriaNegocio("VIP_PREMIUM");
        } else if (dto.getSaldo() > 10000) {
            dto.setCategoriaNegocio("CLIENTE_ACTIVO");
        } else {
            dto.setCategoriaNegocio("POTENCIAL");
        }
    }
}