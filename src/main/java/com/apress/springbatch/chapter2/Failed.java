package com.apress.springbatch.chapter2;

import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import com.apress.springbatch.chapter2.batch.util.Utility;

public class Failed extends Utility implements Tasklet {
    @Override
    public RepeatStatus execute(org.springframework.batch.core.StepContribution step, 
                               org.springframework.batch.core.scope.context.ChunkContext chunk) throws Exception {
        
        this.getGestionClientes().registrarEventoBatch(
            this.prepararMapaEvento("KO", "Error critico en el proceso de Clientes")
        );
        
        System.err.println(">>> [JOB FAILED] Revisa el log de la tabla TICR011.");
        return RepeatStatus.FINISHED;
    }
}