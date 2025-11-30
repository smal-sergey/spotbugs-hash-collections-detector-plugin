package com.smalser.spotbugs;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.test.SpotBugsExtension;
import edu.umd.cs.findbugs.test.SpotBugsRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(SpotBugsExtension.class)
public class StreamCollectToSetWithMethodReturnTest extends BaseDetectorTest {
    private static final Logger log = LoggerFactory.getLogger(StreamCollectToSetWithMethodReturnTest.class);

    public interface Invoice {
    }

    public static class Positive {
        public Set<Invoice> run() {
            return Stream.of("abc")
                    .map(str -> new Invoice() {
                    })
                    .collect(Collectors.toSet());
        }
    }

    @Test
    public void test_variable_positive(SpotBugsRunner spotbugs) {
        BugCollection bugs = analyze(spotbugs, Positive.class);
        assertErrorExpected(bugs, true);
    }

    public static class Negative {
        public Set<String> run() {
            return Stream.of("abc").collect(Collectors.toSet());
        }
    }

    @Test
    public void test_variable_negative(SpotBugsRunner spotbugs) {
        BugCollection bugs = analyze(spotbugs, Negative.class);
        assertErrorExpected(bugs, false);
    }

}