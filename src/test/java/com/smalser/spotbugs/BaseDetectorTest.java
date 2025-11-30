package com.smalser.spotbugs;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.test.SpotBugsRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class BaseDetectorTest {
    private static final Logger log = LoggerFactory.getLogger(BaseDetectorTest.class);

    protected static BugCollection analyze(SpotBugsRunner spotbugs, Class<?> clazz) {
        String path = "build/classes/java/test/" + clazz.getName().replace(".", "/") + ".class";
        return BaseDetectorTest.analyze(spotbugs, path);
    }

    private static BugCollection analyze(SpotBugsRunner spotbugs, String path) {
        BugCollection bugs = spotbugs.performAnalysis(Path.of(path));
        log.info("bug collection: {}", bugs.getCollection());
        return bugs;
    }

    protected static void assertErrorExpected(BugCollection bugs, boolean errorExpected) {
        assertEquals(errorExpected, bugs.getCollection().stream().anyMatch(bug -> "HC_MISSING_EQUALS_HASHCODE".equals(bug.getBugPattern().getType())));
    }
}
