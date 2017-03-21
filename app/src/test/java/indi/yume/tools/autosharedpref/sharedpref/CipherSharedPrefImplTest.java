package indi.yume.tools.autosharedpref.sharedpref;

import org.junit.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import fj.F;
import fj.P2;
import fj.data.List;
import fj.data.Option;
import fj.test.Arbitrary;
import fj.test.CheckResult;
import fj.test.Gen;
import fj.test.Property;
import fj.test.Shrink;
import indi.yume.tools.autosharedpref.util.ToStringUtil;

import static fj.P.p;
import static fj.data.List.list;
import static fj.test.Gen.frequency;
import static fj.test.Gen.sized;
import static fj.test.Gen.value;
import static indi.yume.tools.autosharedpref.sharedpref.CipherSharedPrefImpl.DataType.BOOL;
import static indi.yume.tools.autosharedpref.sharedpref.CipherSharedPrefImpl.DataType.DOUBLE;
import static indi.yume.tools.autosharedpref.sharedpref.CipherSharedPrefImpl.DataType.FLOAT;
import static indi.yume.tools.autosharedpref.sharedpref.CipherSharedPrefImpl.DataType.INT;
import static indi.yume.tools.autosharedpref.sharedpref.CipherSharedPrefImpl.DataType.LONG;
import static indi.yume.tools.autosharedpref.sharedpref.CipherSharedPrefImpl.DataType.NULL;
import static indi.yume.tools.autosharedpref.sharedpref.CipherSharedPrefImpl.DataType.STRING;
import static indi.yume.tools.autosharedpref.sharedpref.CipherSharedPrefImpl.DataType.STRING_SET;
import static org.junit.Assert.*;

/**
 * Created by yume on 17-3-21.
 */
public class CipherSharedPrefImplTest {
    private CipherAdapter TestCipherAdapter = new CipherAdapter() {
        @Override
        public String encrypt(String rawData) {
            return "1" + rawData;
        }

        @Override
        public String decrypt(String encryptedData) {
            return encryptedData.substring(1);
        }
    };

    private CipherSharedPrefImpl cipherSharedPref = new CipherSharedPrefImpl(TestCipherAdapter);

    public static final Gen<Set<String>> arbStringSet = Arbitrary.arbHashSet(Arbitrary.arbString).map(new F<HashSet<String>, Set<String>>() {
        @Override
        public Set<String> f(HashSet<String> strings) {
            return strings;
        }
    });

    public static final Gen<Object> arbNonNullObject = sized(new F<Integer, Gen<Object>>() {
        @SuppressWarnings("unchecked")
        public Gen<Object> f(final Integer i) {
            return frequency(list(
                    p(1, cast(Arbitrary.arbLongBoundaries)),
                    p(1, cast(Arbitrary.arbIntegerBoundaries)),
                    p(1, cast(Arbitrary.arbFloatBoundaries)),
                    p(1, cast(Arbitrary.arbBoolean)),
                    p(1, cast(Arbitrary.arbDoubleBoundaries)),
                    p(1, cast(Arbitrary.arbString)),
                    p(1, cast(arbStringSet))));
        }
    });

    public static final Gen<Object> arbNullableObject =
            Arbitrary.arbOption(arbNonNullObject).map(new F<Option<Object>, Object>() {
                @Override
                public Object f(Option<Object> objects) {
                    return objects.orSome((Object) null);
                }
            });

    public static <A> Gen<Object> cast(Gen<A> gen) {
        return gen.map(CipherSharedPrefImplTest.<A>cast());
    }

    public static <A> F<A, Object> cast() {
        return new F<A, Object>() {
            @Override
            public Object f(A a) {
                return a;
            }
        };
    }

    @Test
    public void encrypt() throws Exception {
        Property law = encodeLaw(arbNullableObject);
        check(law);
    }

    public static void check(Property law) {
        CheckResult.summary.println(law.check());
    }

    public static boolean objEquals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public Property encodeLaw(Gen<Object> gen) {
        return Property.property(gen,
                new F<Object, Property>() {
                    @Override
                    public Property f(Object raw) {
                        CipherSharedPrefImpl.Entry entry = cipherSharedPref.encrypt(raw);
                        Object decode = cipherSharedPref.getRawValue(entry.encode());

                        boolean isEqual = false;
                        if(raw instanceof Set)
                            isEqual = compareStringSet((Set<String>)raw, (Set<String>)decode);
                        else
                            isEqual = objEquals(decode, raw);

                        return Property.prop(
                                entry.getType() == getType(raw)
                                        && isEqual
                        );
                    }
                });
    }

    public static boolean compareStringSet(Set<String> set1, Set<String> set2) {
        boolean isEqual = set1.size() == set2.size();

        if(isEqual)
            for(String s : set1)
                if(!set2.contains(s)) {
                    isEqual = false;
                    break;
                }

        return isEqual;
    }

    CipherSharedPrefImpl.DataType getType(Object value) {
        if (value == null)
            return NULL;
        else if (value instanceof Set) {
            return STRING_SET;
        } else if (value instanceof String) {
            return STRING;
        }  else if (value instanceof Integer) {
            return INT;
        } else if(value instanceof Long) {
            return LONG;
        } else if(value instanceof Float) {
            return FLOAT;
        } else if(value instanceof Boolean) {
            return BOOL;
        } else if(value instanceof Double) {
            return DOUBLE;
        } else {
            throw new Error("SharedPref saving Error: Class not found: " + value.getClass());
        }
    }
}