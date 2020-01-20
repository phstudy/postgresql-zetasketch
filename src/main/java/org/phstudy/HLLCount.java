package org.phstudy;

import com.google.zetasketch.HyperLogLogPlusPlus;
import com.google.zetasketch.shaded.com.google.protobuf.ByteString;
import org.postgresql.pljava.annotation.Function;
import org.postgresql.pljava.annotation.SQLAction;
import org.postgresql.pljava.annotation.SQLActions;

import static com.google.zetasketch.HyperLogLogPlusPlus.DEFAULT_NORMAL_PRECISION;
import static com.google.zetasketch.HyperLogLogPlusPlus.DEFAULT_SPARSE_PRECISION_DELTA;
import static org.postgresql.pljava.annotation.Function.Effects.IMMUTABLE;

@SQLActions({@SQLAction(requires = "hll_count_add",
                        install = "CREATE AGGREGATE hll_count_init(\"any\")( sfunc = hll_count_add, stype = bytea)",
                        remove = "DROP AGGREGATE hll_count_init(\"any\")"),
             @SQLAction(requires = "hll_count_add2",
                        install = "CREATE AGGREGATE hll_count_init(\"any\", integer)(sfunc = hll_count_add, stype = bytea)",
                        remove = "DROP AGGREGATE init(\"any\", integer)"),
             @SQLAction(requires = "hll_count_accumulate",
                        install = "CREATE AGGREGATE hll_count_merge_partial(bytea)(SFUNC = hll_count_accumulate, STYPE = bytea)",
                        remove = "DROP AGGREGATE hll_count_merge_partial(bytea)"),
             @SQLAction(requires = {"hll_count_accumulate", "hll_count_extract"},
                        install = "CREATE AGGREGATE hll_count_merge(bytea)(SFUNC = hll_count_accumulate, STYPE = bytea, FINALFUNC = hll_count_extract)",
                        remove = "DROP AGGREGATE hll_count_merge(bytea)")})
public class HLLCount {
    @Function(provides = "hll_count_add",
              name = "hll_count_add",
              effects = IMMUTABLE)
    public static byte[] add(byte[] stateSketch,
                             Object input) {
        return add(stateSketch, input, DEFAULT_NORMAL_PRECISION);
    }

    @Function(provides = "hll_count_add2",
              name = "hll_count_add",
              effects = IMMUTABLE)
    public static byte[] add(byte[] stateSketch,
                             Object input,
                             int normalPrecision) {
        HyperLogLogPlusPlus hll;

        if (stateSketch == null) {
            hll = build(input, normalPrecision);
        }
        else {
            hll = HyperLogLogPlusPlus.forProto(stateSketch);
        }

        if(input == null) {
            hll.add(input);
        }

        return hll.serializeToByteArray();
    }

    @Function(provides = "hll_count_accumulate",
              name = "hll_count_accumulate",
              effects = IMMUTABLE)
    public static byte[] accumulate(byte[] stateSketch,
                                    byte[] inputSketch) {
        HyperLogLogPlusPlus hll = HyperLogLogPlusPlus.forProto(stateSketch);
        hll.merge(inputSketch);

        return hll.serializeToByteArray();
    }

    @Function(provides = "hll_count_extract",
              name = "hll_count_extract",
              effects = IMMUTABLE)
    public static long extract(byte[] sketch) {
        return HyperLogLogPlusPlus.forProto(sketch)
                                  .longResult();
    }

    public static HyperLogLogPlusPlus build(Object input,
                                            int normalPrecision) {
        HyperLogLogPlusPlus.Builder builder = new HyperLogLogPlusPlus.Builder().normalPrecision(normalPrecision)
                                                                               .sparsePrecision(normalPrecision
                                                                                                        + DEFAULT_SPARSE_PRECISION_DELTA);
        if (input instanceof String) {
            return builder.buildForStrings();
        }
        else if (input instanceof ByteString) {
            return builder.buildForBytes();
        }
        else if (input instanceof Integer) {
            return builder.buildForIntegers();
        }
        else if (input instanceof Long) {
            return builder.buildForLongs();
        }

        return builder.buildForStrings();
    }
}