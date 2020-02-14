package org.phstudy;

import com.google.zetasketch.HyperLogLogPlusPlus;
import com.google.zetasketch.shaded.com.google.protobuf.ByteString;
import org.postgresql.pljava.annotation.Function;
import org.postgresql.pljava.annotation.SQLAction;
import org.postgresql.pljava.annotation.SQLActions;

import static com.google.zetasketch.HyperLogLogPlusPlus.DEFAULT_NORMAL_PRECISION;
import static com.google.zetasketch.HyperLogLogPlusPlus.DEFAULT_SPARSE_PRECISION_DELTA;
import static org.postgresql.pljava.annotation.Function.Effects.IMMUTABLE;

@SQLActions({@SQLAction(requires = "add",
                        install = "CREATE AGGREGATE hll_count.init(\"any\")( sfunc = hll_count.add, stype = bytea)",
                        remove = "DROP AGGREGATE hll_count.init(\"any\")"),
             @SQLAction(requires = "add2",
                        install = "CREATE AGGREGATE hll_count.init(\"any\", integer)(sfunc = hll_count.add, stype = bytea)",
                        remove = "DROP AGGREGATE hll_count.init(\"any\", integer)"),
             @SQLAction(requires = "accumulate",
                        install = "CREATE AGGREGATE hll_count.merge_partial(bytea)(SFUNC = hll_count.accumulate, STYPE = bytea)",
                        remove = "DROP AGGREGATE hll_count.merge_partial(bytea)"),
             @SQLAction(requires = {"accumulate", "extract"},
                        install = "CREATE AGGREGATE hll_count.merge(bytea)(SFUNC = hll_count.accumulate, STYPE = bytea, FINALFUNC = hll_count.extract)",
                        remove = "DROP AGGREGATE hll_count.merge(bytea)")})
public class HLLCount {
    @Function(provides = "add",
              schema = "hll_count",
              name = "add",
              effects = IMMUTABLE)
    public static byte[] add(byte[] stateSketch,
                             Object input) {
        return add(stateSketch, input, DEFAULT_NORMAL_PRECISION);
    }

    @Function(provides = "add2",
              schema = "hll_count",
              name = "add",
              effects = IMMUTABLE)
    public static byte[] add(byte[] stateSketch,
                             Object input,
                             int normalPrecision) {
        HyperLogLogPlusPlus hll;

        if (stateSketch == null) {
            hll = build(input.getClass(), normalPrecision);
        } else {
            hll = HyperLogLogPlusPlus.forProto(stateSketch);
        }

        if (input != null) {
            if(input instanceof byte[]) {
                hll.add((byte[]) input);
            } else {
                hll.add(input);
            }
        }

        return hll.serializeToByteArray();
    }

    @Function(provides = "accumulate",
              schema = "hll_count",
              name = "accumulate",
              effects = IMMUTABLE)
    public static byte[] accumulate(byte[] stateSketch,
                                    byte[] inputSketch) {
        if (stateSketch == null) {
            return HyperLogLogPlusPlus.forProto(inputSketch).serializeToByteArray();
        }

        HyperLogLogPlusPlus hll = HyperLogLogPlusPlus.forProto(stateSketch);
        hll.merge(inputSketch);

        return hll.serializeToByteArray();
    }

    @Function(provides = "extract",
              schema = "hll_count",
              name = "extract",
              effects = IMMUTABLE)
    public static long extract(byte[] sketch) {
        return HyperLogLogPlusPlus.forProto(sketch)
                                  .longResult();
    }

    public static HyperLogLogPlusPlus build(Class<?> input,
                                            int normalPrecision) {
        HyperLogLogPlusPlus.Builder builder = new HyperLogLogPlusPlus.Builder().normalPrecision(normalPrecision)
                                                                               .sparsePrecision(normalPrecision
                                                                                                        + DEFAULT_SPARSE_PRECISION_DELTA);
        if (input == String.class) {
            return builder.buildForStrings();
        }
        else if (input == byte[].class) {
            return builder.buildForBytes();
        }
        else if (input == Integer.class) {
            return builder.buildForIntegers();
        }
        else if (input == Long.class) {
            return builder.buildForLongs();
        }

        return builder.buildForStrings();
    }
}