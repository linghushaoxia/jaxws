package com.sun.xml.ws.client.port;

//import com.sun.xml.ws.api.model.Parameter;

import com.sun.xml.ws.model.ParameterImpl;
import javax.xml.ws.Holder;

/**
 * Moves a Java value unmarshalled from a response message
 * to the right place.
 *
 * <p>
 * Sometimes values are returned as a return value, and
 * others are returned in the {@link Holder} value. Instances
 * of this interface abstracts this detail.
 *
 * <p>
 * {@link ValueSetter} is a stateless behavior encapsulation.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ValueSetter {
    private ValueSetter() {}

    /**
     * Moves the value to the expected place.
     *
     * @param obj
     *      The unmarshalled object.
     * @param args
     *      The arguments given to the Java method invocation. If <tt>obj</tt>
     *      is supposed to be returned as a {@link Holder} value, a suitable
     *      {@link Holder} is obtained from this argument list and <tt>obj</tt>
     *      is set.
     *
     * @return
     *      if <tt>obj</tt> is supposed to be returned as a return value
     *      from the method, this method returns <tt>obj</tt>. Otherwise null.
     */
    abstract Object put(Object obj, Object[] args);

    /**
     * Singleton instance.
     */
    private static final ValueSetter RETURN_VALUE = new ReturnValue();
    /**
     * {@link Param}s with small index numbers are used often,
     * so we pool them to reduce the footprint.
     */
    private static final ValueSetter[] POOL = new ValueSetter[16];

    static {
        for( int i=0; i<POOL.length; i++ )
            POOL[i] = new Param(i);
    }

    /**
     * Returns a {@link ValueSetter} suitable for the given {@link Parameter{.
     */
    public static ValueSetter get(ParameterImpl p) {
        int idx = p.getIndex();

        if(idx==-1)
            return RETURN_VALUE;
        if(idx<POOL.length)
            return POOL[idx];
        else
            return new Param(idx);
    }

    private static final class ReturnValue extends ValueSetter {
        Object put(Object obj, Object[] args) {
            return obj;
        }
    }

    static final class Param extends ValueSetter {
        /**
         * Index of the argument to put the value to.
         */
        private final int idx;

        public Param(int idx) {
            this.idx = idx;
        }

        Object put(Object obj, Object[] args) {
            Object arg = args[idx];
            if(arg!=null) {
                // we build model in such a way that this is guaranteed
                assert arg instanceof Holder;
                ((Holder)arg).value = obj;
            }
            // else {
            //   if null is given as a Holder, there's no place to return
            //   the value, so just ignore.
            // }

            // no value to return
            return null;
        }
    }
}
