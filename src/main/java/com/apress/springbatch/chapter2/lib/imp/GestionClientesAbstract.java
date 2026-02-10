package com.apress.springbatch.chapter2.lib.imp;

import java.util.Map;
import java.util.Properties;
import java.io.InputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import com.apress.springbatch.chapter2.lib.GestionClientes;

public abstract class GestionClientesAbstract implements GestionClientes {

    protected JdbcTemplate jdbcTemplate;
    // AQUÍ ESTÁ EL TRUCO: Debe ser protected para que el Impl lo vea
    protected Properties sqlQueries = new Properties();

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Constructor que carga los SQLs automáticamente
    public GestionClientesAbstract() {
        loadQueries();
    }

    private void loadQueries() {
        // Ruta al archivo que creamos antes
        String path = "com/apress/springbatch/chapter2/lib/imp/sql-GCLIENTES.properties";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input != null) {
                sqlQueries.load(input);
                System.out.println(">>> SQLs cargados correctamente.");
            } else {
                System.err.println(">>> Error: No se encontró el archivo .properties en: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public Map<String, Object> obtenerResumen(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}
}