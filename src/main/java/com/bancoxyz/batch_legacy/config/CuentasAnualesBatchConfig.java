package com.bancoxyz.batch_legacy.config;

import com.bancoxyz.batch_legacy.model.CuentaAnualCsv;
import com.bancoxyz.batch_legacy.model.CuentaAnualEntity;
import com.bancoxyz.batch_legacy.processor.CuentaAnualProcessor;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CuentasAnualesBatchConfig {

    @Bean
    public FlatFileItemReader<CuentaAnualCsv> cuentaAnualReader() {
        return new FlatFileItemReaderBuilder<CuentaAnualCsv>()
                .name("cuentaAnualReader")
                .resource(new ClassPathResource("cuentas_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuenta_id", "fecha", "transaccion", "monto", "descripcion")
                .targetType(CuentaAnualCsv.class)
                .build();
    }

    @Bean
    public CuentaAnualProcessor cuentaAnualProcessor() {
        return new CuentaAnualProcessor();
    }

    @Bean
    public JpaItemWriter<CuentaAnualEntity> cuentaAnualWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<CuentaAnualEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    // TaskExecutor aislado para evitar cruce de hilos con otros Jobs
    @Bean(name = "cuentaAnualTaskExecutor")
    public TaskExecutor cuentaAnualTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // Escalamiento a 3 hilos
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("Anual-Thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Step estadoCuentaAnualStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                      FlatFileItemReader<CuentaAnualCsv> cuentaAnualReader,
                                      CuentaAnualProcessor cuentaAnualProcessor,
                                      JpaItemWriter<CuentaAnualEntity> cuentaAnualWriter,
                                      @Qualifier("cuentaAnualTaskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("estadoCuentaAnualStep", jobRepository)
                .<CuentaAnualCsv, CuentaAnualEntity>chunk(5, transactionManager) // Chunks de tamaño 5
                .reader(cuentaAnualReader)
                .processor(cuentaAnualProcessor)
                .writer(cuentaAnualWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100) // Tolerancia a fallos: omite hasta 100 registros anómalos
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Job estadoCuentaAnualJob(JobRepository jobRepository, Step estadoCuentaAnualStep) {
        return new JobBuilder("estadoCuentaAnualJob", jobRepository)
                .start(estadoCuentaAnualStep)
                .build();
    }
}