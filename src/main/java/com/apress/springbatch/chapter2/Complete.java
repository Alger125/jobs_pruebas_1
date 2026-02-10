package com.apress.springbatch.chapter2;

import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import com.apress.springbatch.chapter2.batch.util.Utility;

public class Complete extends Utility implements Tasklet {
    @Override
    public RepeatStatus execute(org.springframework.batch.core.StepContribution step, 
                               org.springframework.batch.core.scope.context.ChunkContext chunk) throws Exception {
        
        // Registramos el éxito usando nuestra nueva interfaz
        this.getGestionClientes().registrarEventoBatch(
            this.prepararMapaEvento("OK", "Proceso de Clientes terminado con EXITO")
        );
        
        System.out.println(">>> [JOB FINISHED] Archivo generado en el escritorio.");
        return RepeatStatus.FINISHED;
    }
}