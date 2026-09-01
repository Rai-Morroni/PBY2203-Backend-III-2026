package com.bancoxyz.batch_legacy.config;

import com.bancoxyz.batch_legacy.model.InteresCsv;
import com.bancoxyz.batch_legacy.model.InteresEntity;
import com.bancoxyz.batch_legacy.processor.InteresProcessor;
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
public class InteresesBatchConfig {

    @Bean
    public FlatFileItemReader<InteresCsv> interesReader() {
        return new FlatFileItemReaderBuilder<InteresCsv>()
                .name("interesReader")
                .resource(new ClassPathResource("intereses.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuenta_id", "nombre", "saldo", "edad", "tipo")
                .targetType(InteresCsv.class)
                .build();
    }

    @Bean
    public InteresProcessor interesProcessor() {
        return new InteresProcessor();
    }

    @Bean
    public JpaItemWriter<InteresEntity> interesWriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<InteresEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    // Definimos un TaskExecutor específico para aislar los recursos de este Job
    @Bean(name = "interesTaskExecutor")
    public TaskExecutor interesTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); 
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("Interes-Thread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Step calculoInteresesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                     FlatFileItemReader<InteresCsv> interesReader,
                                     InteresProcessor interesProcessor,
                                     JpaItemWriter<InteresEntity> interesWriter,
                                     @Qualifier("interesTaskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("calculoInteresesStep", jobRepository)
                .<InteresCsv, InteresEntity>chunk(5, transactionManager)
                .reader(interesReader)
                .processor(interesProcessor)
                .writer(interesWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50) // Ajuste de política: toleramos hasta 50 errores en este archivo
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Job calculoInteresesJob(JobRepository jobRepository, Step calculoInteresesStep) {
        return new JobBuilder("calculoInteresesJob", jobRepository)
                .start(calculoInteresesStep)
                .build();
    }
}