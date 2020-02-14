package org.phstudy;

import com.google.zetasketch.HyperLogLogPlusPlus;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;

class HLLCountTest {

    @org.junit.jupiter.api.Test
    void testAddInteger() {
        HyperLogLogPlusPlus hll = HyperLogLogPlusPlus.forProto(HLLCount.add(null,123));
        assertEquals(1, hll.longResult());
    }

    @org.junit.jupiter.api.Test
    void testAddLong() {
        HyperLogLogPlusPlus hll = HyperLogLogPlusPlus.forProto(HLLCount.add(null,123L));
        assertEquals(1, hll.longResult());
    }

    @org.junit.jupiter.api.Test
    void testAddString() {
        HyperLogLogPlusPlus hll = HyperLogLogPlusPlus.forProto(HLLCount.add(null,"Apple"));
        assertEquals(1, hll.longResult());
    }

    @org.junit.jupiter.api.Test
    void testAddBytes() {
        HyperLogLogPlusPlus hll = HyperLogLogPlusPlus.forProto(HLLCount.add(null, "Bytes".getBytes()));
        assertEquals(1, hll.longResult());
    }

    @org.junit.jupiter.api.Test
    void testAddWithState() {
        HyperLogLogPlusPlus state = new HyperLogLogPlusPlus.Builder().buildForStrings();
        state.add("Orange");

        HyperLogLogPlusPlus hll = HyperLogLogPlusPlus.forProto(HLLCount.add(state.serializeToByteArray(), "Apple"));
        assertEquals(2, hll.longResult());
    }

    @org.junit.jupiter.api.Test
    void testAddWrongType() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            HyperLogLogPlusPlus state = new HyperLogLogPlusPlus.Builder().buildForIntegers();
            state.add(1);

            HyperLogLogPlusPlus.forProto(HLLCount.add(state.serializeToByteArray(), "Apple"));
        });
    }

    @org.junit.jupiter.api.Test
    void testAddWithPrecision() {
        HyperLogLogPlusPlus state = new HyperLogLogPlusPlus.Builder().normalPrecision(10).buildForStrings();
        state.add("Orange");

        HyperLogLogPlusPlus hll = HyperLogLogPlusPlus.forProto(HLLCount.add(state.serializeToByteArray(), "Apple"));
        assertEquals(2, hll.longResult());
    }

    @org.junit.jupiter.api.Test
    void testAccumulate() {
        HyperLogLogPlusPlus state = new HyperLogLogPlusPlus.Builder().buildForStrings();
        state.add("Orange");
        state.add("Apple");

        HyperLogLogPlusPlus input = new HyperLogLogPlusPlus.Builder().buildForStrings();
        input.add("Orange");
        input.add("Banana");

        HyperLogLogPlusPlus hll = HyperLogLogPlusPlus.forProto(HLLCount.accumulate(state.serializeToByteArray(), input.serializeToByteArray()));
        assertEquals(3, hll.longResult());
    }

    @org.junit.jupiter.api.Test
    void testExtract() {
        HyperLogLogPlusPlus sketch = new HyperLogLogPlusPlus.Builder().buildForStrings();
        sketch.add("Orange");
        sketch.add("Banana");

        assertEquals(2, HLLCount.extract(sketch.serializeToByteArray()));
    }

    @org.junit.jupiter.api.Test
    void testBuild() {
        HyperLogLogPlusPlus expectedSketch = new HyperLogLogPlusPlus.Builder().normalPrecision(15).buildForStrings();
        expectedSketch.add("Orange");

        HyperLogLogPlusPlus actualBuild = HLLCount.build(String.class, 15);
        assertEquals(expectedSketch.getNormalPrecision(), actualBuild.getNormalPrecision());

        actualBuild.merge(expectedSketch);
        assertEquals(1, actualBuild.longResult());
    }
}