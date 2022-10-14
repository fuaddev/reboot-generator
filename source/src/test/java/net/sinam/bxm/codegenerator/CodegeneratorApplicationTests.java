package net.sinam.bxm.codegenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.sinam.bxm.codegenerator.config.ArgumentConfig;
import net.sinam.bxm.codegenerator.config.ResourceReader;
import net.sinam.bxm.codegenerator.config.ResourceReaderImpl;
import net.sinam.bxm.codegenerator.generator.DirectoryManager;
import net.sinam.bxm.codegenerator.generator.EntityGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodegeneratorApplicationTests {

    @Mock
    ResourceReader resourceReader;
    @Mock
    ArgumentConfig argumentConfig;
    @Mock
    DirectoryManager directoryManager;

    @Test
    public void testFixJavaName() {

        EntityGenerator entityGenerator = new EntityGenerator(argumentConfig, resourceReader, directoryManager);
        assertEquals("AbResourceId", entityGenerator.fixJpaFieldName("ABResourceId"));
        assertEquals("DictAbPolicyType", entityGenerator.fixJpaFieldName("DictABPolicyType"));
        assertEquals("id", entityGenerator.fixJpaFieldName("id"));

    }

    @Test
    public void testPluralize() {

        EntityGenerator entityGenerator = new EntityGenerator(argumentConfig,resourceReader, directoryManager);
        assertEquals("AbResourceIds", entityGenerator.pluralizeWord("AbResourceId"));
        assertEquals("AbPolicies", entityGenerator.pluralizeWord("AbPolicy"));

    }
}
