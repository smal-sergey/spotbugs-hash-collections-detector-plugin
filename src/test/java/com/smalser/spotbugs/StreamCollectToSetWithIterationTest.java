package com.smalser.spotbugs;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.test.SpotBugsExtension;
import edu.umd.cs.findbugs.test.SpotBugsRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(SpotBugsExtension.class)
public class StreamCollectToSetWithIterationTest extends BaseDetectorTest {
    private static final Logger log = LoggerFactory.getLogger(StreamCollectToSetWithIterationTest.class);

    public interface Invoice {
    }

    public static class Positive {
        public void run() {
            Stream.of("abc")
                    .map(str -> new Invoice() {
                    })
                    .collect(Collectors.toSet())
                    .forEach(item -> {
                        log.info("item={}");
                    });
        }
    }

    @Test
    public void test_variable_positive(SpotBugsRunner spotbugs) {
        BugCollection bugs = analyze(spotbugs, Positive.class);
        assertErrorExpected(bugs, true);
    }

    public static class Negative {
        public void run() {
            Stream.of("abc")
                    .collect(Collectors.toSet())
                    .forEach(item -> {
                        log.info("item={}");
                    });
            ;
        }
    }

    @Test
    public void test_variable_negative(SpotBugsRunner spotbugs) {
        BugCollection bugs = analyze(spotbugs, Negative.class);
        assertErrorExpected(bugs, false);
    }

}