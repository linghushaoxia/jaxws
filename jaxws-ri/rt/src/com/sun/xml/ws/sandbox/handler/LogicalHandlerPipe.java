/*
 * LogicalHandlerPipe.java
 *
 * Created on February 9, 2006, 11:12 PM
 *
 *
 */

package com.sun.xml.ws.sandbox.handler;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.handler.HandlerPipe;
import com.sun.xml.ws.handler.MessageContextUtil;
import com.sun.xml.ws.sandbox.handler.HandlerProcessor.Direction;
import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;


/**
 * TODO: Kohsuke put a comment that packet.isOneWay may be null
 * @author WS Development Team
 */
public class LogicalHandlerPipe extends HandlerPipe {
    
    private WSBinding binding;
    private LogicalHandlerProcessor processor;
    private List<LogicalHandler> logicalHandlers;
    private boolean remedyActionTaken = false;
    private final boolean isClient;
    
    
    /** Creates a new instance of LogicalHandlerPipe */
    public LogicalHandlerPipe(WSBinding binding, Pipe next, boolean isClient) {
        super(next);
        this.binding = binding;
        this.isClient = isClient;
    }
    
    
    /**
     * This constructor is used on client-side where, SOAPHandlerPipe is created
     * first and then a LogicalHandlerPipe is created with a handler to that
     * SOAPHandlerPipe.
     * With this handle, LogicalHandlerPipe can call
     * SOAPHandlerPipe.closeHandlers()
     */
    public LogicalHandlerPipe(WSBinding binding, Pipe next, HandlerPipe cousinPipe, boolean isClient) {
        super(next,cousinPipe);
        this.binding = binding;
        this.isClient = isClient;
    }
    
    /**
     * Copy constructor for {@link com.sun.xml.ws.api.pipe.Pipe#copy(com.sun.xml.ws.api.pipe.PipeCloner)}.
     */
    
    protected LogicalHandlerPipe(LogicalHandlerPipe that, PipeCloner cloner) {
        super(that,cloner);
        this.isClient = that.isClient;               
    }
    
    @Override
    public Packet process( Packet packet) {
        // This is done here instead of the constructor, since User can change
        // the roles and handlerchain after a stub/proxy is created.
        setUpProcessor();
        
        if(logicalHandlers.isEmpty()) {
            return next.process(packet);
        }
        MessageContext msgContext = new MessageContextImpl(packet);
        try {
            boolean isOneWay = packet.isOneWay;            
            LogicalMessageContext context =  new LogicalMessageContextImpl(binding,packet,msgContext);
            boolean handlerResult = false;
            
            // Call handlers on Request
            try {
                if(isClient) {
                    //CLIENT-SIDE
                    handlerResult = processor.callHandlersRequest(Direction.OUTBOUND,context,!isOneWay);
                } else {
                    //SERVER-SIDE
                    handlerResult = processor.callHandlersRequest(Direction.INBOUND,context,!isOneWay);
                }    
                
            } catch (ProtocolException pe) {
                handlerResult = false;
                if (MessageContextUtil.ignoreFaultInMessage(msgContext)) {
                    // ignore fault in this case and use exception
                    throw pe;
                }
            }
            
            // the only case where no message is sent
            if (!isOneWay && !handlerResult) {
                remedyActionTaken = true;
                //TODO: return packet
                return packet;
            }
            
            // Call next Pipe.process() on msg
            Packet reply = next.process(packet);
            
            isOneWay = reply.isOneWay;
            //TODO: For now create again
            msgContext = new MessageContextImpl(packet);
            context =  new LogicalMessageContextImpl(binding,packet,msgContext);
            // Call handlers on Response
            if(isClient) {
                //CLIENT-SIDE
                processor.callHandlersResponse(Direction.INBOUND,context,!isOneWay);                
            } else {                
                //SERVER-SIDE                
                processor.callHandlersResponse(Direction.OUTBOUND,context,!isOneWay);
            }
            return reply;
        } finally {
            close(msgContext);
        }
        
        
    }
    
    /**
     * Close SOAPHandlers first and then LogicalHandlers
     */
    public void close(MessageContext msgContext){
        if(cousinPipe != null){
            // Close SOAPHandlers
            cousinPipe.close(msgContext);
        }
        closeLogicalHandlers(msgContext);
    }
    
    //TODO:
    private void closeLogicalHandlers(MessageContext msgContext){
      if(remedyActionTaken){
          //Close only invoked handlers in the chain
          if(isClient){
              //CLIENT-SIDE
              processor.closeHandlers(msgContext,0,processor.getIndex());
          } else {
              //SERVER-SIDE
              processor.closeHandlers(msgContext,logicalHandlers.size()-1,processor.getIndex());
          }
      } else {
          //Close all handlers in the chain
          processor.closeHandlers(msgContext,logicalHandlers.size()-1,0);
      }
    }
    
    /**
     * TODO:
     * @param cloner
     * @return
     */
    public Pipe copy(PipeCloner cloner) {
        return new LogicalHandlerPipe(this,cloner);
    }
    
    private void setUpProcessor() {
        // Take a snapshot, User may change chain after invocation, Same chain
        // should be used for the entire MEP
        logicalHandlers = new ArrayList<LogicalHandler>();
        logicalHandlers.addAll(((BindingImpl)binding).getLogicalHandlerChain());
        processor = new LogicalHandlerProcessor(binding,logicalHandlers);
    }
    
}
