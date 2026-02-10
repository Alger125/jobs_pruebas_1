package com.apress.springbatch.chapter2.dto;

import java.io.Serializable;

/**
 * Data Transfer Object (DTO) para la entidad Cliente.
 * Representa la estructura de datos que viajará por nuestro Batch.
 */
public class ClienteDTO implements Serializable {
    
    private static final long serialVersionUID = 1L; // Buena práctica para Serializable

    private Long id;
    private String nombreCompleto;
    private Double saldo;
    private String segmento; // PLATINO, ORO, ESTANDAR
    private String categoriaNegocio; // VIP_PREMIUM, CLIENTE_ACTIVO, etc.
    private Integer totalOperaciones; // <-- NUEVO CAMPO PARA EL RESUMEN

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nc) { this.nombreCompleto = nc; }

    public Double getSaldo() { return saldo; }
    public void setSaldo(Double s) { this.saldo = s; }

    public String getSegmento() { return segmento; }
    public void setSegmento(String seg) { this.segmento = seg; }

    public String getCategoriaNegocio() { return categoriaNegocio; }
    public void setCategoriaNegocio(String cat) { this.categoriaNegocio = cat; }

    // Nuevo Getter y Setter
    public Integer getTotalOperaciones() { return totalOperaciones; }
    public void setTotalOperaciones(Integer totalOperaciones) { this.totalOperaciones = totalOperaciones; }
}