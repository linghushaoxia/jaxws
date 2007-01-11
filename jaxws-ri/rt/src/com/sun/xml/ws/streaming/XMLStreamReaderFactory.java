package com.sun.xml.ws.streaming;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * Factory for {@link XMLStreamReader}.
 *
 * <p>
 * This wraps {@link XMLInputFactory} and allows us to reuse {@link XMLStreamReader} instances
 * when appropriate.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class XMLStreamReaderFactory {

    /**
     * Singleton instance.
     */
    private static volatile @NotNull XMLStreamReaderFactory theInstance;

    static {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

        XMLStreamReaderFactory f=null;

        // this system property can be used to disable the pooling altogether,
        // in case someone hits an issue with pooling in the production system.
        if(!Boolean.getBoolean(XMLStreamReaderFactory.class.getName()+".noPool"))
            f = Zephyr.newInstance(xif);
        if(f==null)
            f = new Default(xif);

        theInstance = f;
    }

    /**
     * Overrides the singleton {@link XMLStreamReaderFactory} instance that
     * the JAX-WS RI uses.
     */
    public static void set(XMLStreamReaderFactory f) {
        if(f==null) throw new IllegalArgumentException();
        theInstance = f;
    }

    public static XMLStreamReaderFactory get() {
        return theInstance;
    }

    public static XMLStreamReader create(InputSource source, boolean rejectDTDs) {
        try {
            // Char stream available?
            if (source.getCharacterStream() != null) {
                return get().doCreate(source.getSystemId(), source.getCharacterStream(), rejectDTDs);
            }

            // Byte stream available?
            if (source.getByteStream() != null) {
                return get().doCreate(source.getSystemId(), source.getByteStream(), rejectDTDs);
            }

            // Otherwise, open URI
            return get().doCreate(source.getSystemId(), new URL(source.getSystemId()).openStream(),rejectDTDs);
        } catch (IOException e) {
            throw new XMLReaderException("stax.cantCreate",e);
        }
    }

    public static XMLStreamReader create(@Nullable String systemId, InputStream in, boolean rejectDTDs) {
        return get().doCreate(systemId,in,rejectDTDs);
    }

    public static XMLStreamReader create(@Nullable String systemId, Reader reader, boolean rejectDTDs) {
        return get().doCreate(systemId,reader,rejectDTDs);
    }

    /**
     * Should be invoked when the code finished using an {@link XMLStreamReader}.
     *
     * <p>
     * If the recycled instance implements {@link RecycleAware},
     * {@link RecycleAware#onRecycled()} will be invoked to let the instance
     * know that it's being recycled.
     *
     * <p>
     * It is not a hard requirement to call this method on every {@link XMLStreamReader}
     * instance. Not doing so just reduces the performance by throwing away
     * possibly reusable instances. So the caller should always consider the effort
     * it takes to recycle vs the possible performance gain by doing so.
     *
     * <p>
     * This method may be invked by multiple threads concurrently.
     *
     * @param r
     *      The {@link XMLStreamReader} instance that the caller finished using.
     *      This could be any {@link XMLStreamReader} implementation, not just
     *      the ones that were created from this factory. So the implementation
     *      of this class needs to be aware of that.
     */
    public static void recycle(XMLStreamReader r) {
        get().doRecycle(r);
    }

    // implementations

    public abstract XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs);
    
    public abstract XMLStreamReader doCreate(String systemId, Reader reader, boolean rejectDTDs);

    public abstract void doRecycle(XMLStreamReader r);

    /**
     * Interface that can be implemented by {@link XMLStreamReader} to
     * be notified when it's recycled.
     *
     * <p>
     * This provides a filtering {@link XMLStreamReader} an opportunity to
     * recycle its inner {@link XMLStreamReader}.
     */
    public interface RecycleAware {
        void onRecycled();
    }

    /**
     * {@link XMLStreamReaderFactory} implementation for SJSXP/JAXP RI.
     */
    public static final class Zephyr extends XMLStreamReaderFactory {
        private final XMLInputFactory xif;

        private final ThreadLocal<XMLStreamReader> pool = new ThreadLocal<XMLStreamReader>();

        /**
         * Creates {@link Zephyr} instance if the given {@link XMLInputFactory} is the one
         * from Zephyr.
         */
        public static @Nullable
        XMLStreamReaderFactory newInstance(XMLInputFactory xif) {
            if(!allIsGood)  return null;
            if(false /*TODO: if xif belongs to Zephyr*/)
                return new Zephyr(xif);
            return null;
        }

        public Zephyr(XMLInputFactory xif) {
            try {
                // Turn OFF internal factory caching in Zephyr -- not thread safe
                xif.setProperty("reuse-instance", false);
            } catch (IllegalArgumentException e) {
                // falls through
            }
            this.xif = xif;
        }

        /**
         * Fetchs an instance from the pool if available, otherwise null.
         */
        private @Nullable XMLStreamReader fetch() {
            XMLStreamReader sr = pool.get();
            if(sr==null)    return null;
            pool.set(null);
            return sr;
        }

        public void doRecycle(XMLStreamReader r) {
            if(ZEPHYR_XMLREADER_CLASS.isInstance(r))
                pool.set(r);
            if(r instanceof RecycleAware)
                ((RecycleAware)r).onRecycled();
        }

        public XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            try {
                XMLStreamReader xsr = fetch();
                if(xsr==null)
                    return xif.createXMLStreamReader(systemId,in);

                // try re-using this instance.
                InputSource is = new InputSource(systemId);
                is.setByteStream(in);
                reuse(xsr,is);
                return xsr;
            } catch (IllegalAccessException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            } catch (InvocationTargetException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            }
        }

        public XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            try {
                XMLStreamReader xsr = fetch();
                if(xsr==null)
                    return xif.createXMLStreamReader(systemId,in);

                // try re-using this instance.
                InputSource is = new InputSource(systemId);
                is.setCharacterStream(in);
                reuse(xsr,is);
                return xsr;
            } catch (IllegalAccessException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            } catch (InvocationTargetException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            }
        }

        private void reuse(XMLStreamReader xsr, InputSource in) throws IllegalAccessException, InvocationTargetException {
            XMLReaderImpl_reset.invoke(xsr);
            XMLReaderImpl_setInputSource.invoke(xsr,in);
        }


        /**
         * Sun StAX impl <code>XMLReaderImpl.setInputSource()</code> method via reflection.
         */
        private static Method XMLReaderImpl_setInputSource;

        /**
         * Sun StAX impl <code>XMLReaderImpl.reset()</code> method via reflection.
         */
        private static Method XMLReaderImpl_reset;

        /**
         * The Sun StAX impl's {@link XMLStreamReader} implementation clas.
         */
        private static Class ZEPHYR_XMLREADER_CLASS;

        private static boolean allIsGood;

        static {
            try {
                try {
                    ZEPHYR_XMLREADER_CLASS = Class.forName("com.sun.xml.stream.XMLReaderImpl");
                } catch (ClassNotFoundException e) {
                    // Are we running on top of JAXP 1.4?
                    ZEPHYR_XMLREADER_CLASS = Class.forName("com.sun.xml.stream.XMLStreamReaderImpl");
                }
                if(ZEPHYR_XMLREADER_CLASS!=null) {
                    XMLReaderImpl_setInputSource =
                        ZEPHYR_XMLREADER_CLASS.getMethod("setInputSource", InputSource.class);
                    XMLReaderImpl_reset = ZEPHYR_XMLREADER_CLASS.getMethod("reset");
                }
                allIsGood = true;
            } catch (Exception e) {
                // falls through
            }
        }
    }

    /**
     * Default {@link XMLStreamReaderFactory} implementation
     * that can work with any {@link XMLInputFactory}.
     *
     * <p>
     * {@link XMLInputFactory} is not required to be thread-safe, so the
     * create method on this implementation is synchronized.
     */
    public static final class Default extends NoLock {
        public Default(XMLInputFactory xif) {
            super(xif);
        }

        public synchronized XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            return super.doCreate(systemId, in, rejectDTDs);
        }

        public synchronized XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            return super.doCreate(systemId, in, rejectDTDs);
        }
    }

    /**
     * Similar to {@link Default} but doesn't do any synchronization.
     *
     * <p>
     * This is useful when you know your {@link XMLInputFactory} is thread-safe by itself.
     */
    public static class NoLock extends XMLStreamReaderFactory {
        private final XMLInputFactory xif;

        public NoLock(XMLInputFactory xif) {
            this.xif = xif;
        }

        public XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            try {
                return xif.createXMLStreamReader(systemId,in);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            }
        }

        public XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            try {
                return xif.createXMLStreamReader(systemId,in);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate",e);
            }
        }

        public void doRecycle(XMLStreamReader r) {
            // there's no way to recycle with the default StAX API.
        }
    }
}
