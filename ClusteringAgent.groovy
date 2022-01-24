import org.arl.fjage.*
import org.arl.fjage.param.Parameter
import org.arl.unet.*


  class ClusteringAgent extends UnetAgent {

  enum Params implements Parameter {        
    delay , info
  }

  final String title = 'ClusteringAgent'        
  final String description = 'Receives attribute requests from other nodes and responds with its attributes, Also updates neighbour attribute values' 

  int delay = 0;   
  LinkedHashMap info = []
  
  @Override
  void startup() {
    
    subscribeForService(Services.DATAGRAM);
    def phy = agentForService(Services.PHYSICAL);

    int d = 30000 + (Math.random()*20000) //30 seconds + (0-20 seconds) // 
    System.out.println("Broadcast attribute requests");
      
      
      //periodically broadcast attribute requests with a random delay
      add new TickerBehavior(d, {  
          
         phy << new ClearReq()
         phy << new DatagramReq(to: 0, protocol: Protocol.USER, data: []);
      })
   
    
  
  }

  @Override
  void processMessage(Message msg) {
    if (msg instanceof DatagramNtf && msg.protocol == Protocol.USER) {
      // respond to protocol USER datagram (Attribute requests)
     
     float lq = Math.random(); //link quality
     float ea = Math.random(); // energy attribute
     int d = Math.random()*5000; //delay
     
     def node = agentForService(Services.NODE_INFO);
     int depth=node.location[2]; //depth
     
      add new WakerBehavior(d, {
        send new DatagramReq(
          recipient: msg.sender,
          to: msg.from,
          protocol: Protocol.DATA,
          data: [lq * 100 , ea * 100,depth] //the three attributes
        )
      })
    }
    
    if (msg instanceof DatagramNtf && msg.protocol == Protocol.DATA) //Received attributes from other nodes ?
    {
                  info[msg.from]=msg.data; //update!
    
    }
    
  }

  List<Parameter> getParameterList() {      
    allOf(Params)
  }

}