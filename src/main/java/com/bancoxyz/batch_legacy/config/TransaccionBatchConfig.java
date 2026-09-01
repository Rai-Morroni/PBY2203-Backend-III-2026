package com.bancoxyz.batch_legacy.config;

import com.bancoxyz.batch_legacy.model.TransaccionCsv;
import com.bancoxyz.batch_legacy.model.TransaccionEntity;
import com.bancoxyz.batch_legacy.processor.TransaccionProcessor;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransaccionBatchConfig {

    @Bean
    public FlatFileItemReader<TransaccionCsv> transaccionReader() {
        return new FlatFileItemReaderBuilder<TransaccionCsv>()
                .name("transaccionReader")
                .resource(new ClassPathResource("transacciones.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .targetType(TransaccionCsv.class)
                .build();
    }

    @Bean
    public TransaccionProcessor transaccionProcessor() {
        return new TransaccionProcessor();
    }

    @Bean
    public JpaItemWriter<TransaccionEntity> transaccionWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<TransaccionEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // Escalamiento: 3 hilos paralelos
        executor.setMaxPoolSize(3);
        executor.setThreadNamePrefix("Batch-Thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Step transaccionesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                  FlatFileItemReader<TransaccionCsv> reader,
                                  TransaccionProcessor processor,
                                  JpaItemWriter<TransaccionEntity> writer,
                                  TaskExecutor taskExecutor) {
        return new StepBuilder("transaccionesStep", jobRepository)
                .<TransaccionCsv, TransaccionEntity>chunk(5, transactionManager) // Configuración: chunk de 5
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100) // Resiliencia: permite hasta 100 errores
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Job reporteDiarioJob(JobRepository jobRepository, Step transaccionesStep) {
        return new JobBuilder("reporteDiarioJob", jobRepository)
                .start(transaccionesStep)
                .build();
    }
}