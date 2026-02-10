package com.apress.springbatch.chapter2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.batch.item.ItemReader;
import com.apress.springbatch.chapter2.batch.util.Utility;

public class Reader extends Utility implements ItemReader<List<Map<String, Object>>> {
    
    private boolean endlist = false;

    @Override
    public List<Map<String, Object>> read() throws Exception {
        // 1. CONTROL DE FLUJO: Si ya leímos todo, devolvemos null para que el Batch sepa que ya acabó.
        if (endlist) return null;

        // 2. PARÁMETROS: Deben coincidir EXACTAMENTE con lo que pusiste en la Implementación
        Map<String, Object> params = new HashMap<String, Object>(); 
        params.put("MIN_SALDO", 10000); // Antes decía SALDO_MIN, lo ajustamos para que coincida con tu Impl
        params.put("ESTADO", "A");      

        // 3. LA LLAMADA: Invocamos a nuestra librería de Gestión de Clientes
        // Recuerda que 'this.getGestionClientes()' viene de la herencia de Utility
        List<Map<String, Object>> lista = this.getGestionClientes().listarClientesParaProceso(params);

        // 4. VALIDACIÓN: Si Oracle no regresó nada (ej. nadie tiene > 10,000 de saldo)
        if (lista == null || lista.isEmpty()) {
            System.out.println(">>> [READER] No se encontraron clientes para procesar.");
            endlist = true;
            return null;
        }
        
        System.out.println(">>> [READER] Se encontraron " + lista.size() + " clientes.");
        
        // 5. CIERRE: En procesos pequeños leemos todo de un jalón (una sola página)
        endlist = true; 
        return lista;
    }
}