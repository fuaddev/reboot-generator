package net.sinam.bxm.codegenerator;

import java.io.FileNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.sinam.bxm.codegenerator.config.ArgumentConfig;
import net.sinam.bxm.codegenerator.generator.Starter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Log4j2
@SpringBootApplication
@RequiredArgsConstructor
public class CodegeneratorApplication implements CommandLineRunner {

    final Starter starter;
    final ArgumentConfig argumentConfig;

    public static void main(String[] args) {
        SpringApplication.run(CodegeneratorApplication.class, args);
    }

    @Override
    public void run(String... args) throws FileNotFoundException {
        argumentConfig.validateArguments();
        starter.startGeneration();
    }
}
